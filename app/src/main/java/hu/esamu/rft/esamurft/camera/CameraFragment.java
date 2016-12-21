package hu.esamu.rft.esamurft.camera;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import hu.esamu.rft.esamurft.FirebaseSender;
import hu.esamu.rft.esamurft.R;
import hu.esamu.rft.esamurft.gps.GPSService;

/**
 * Created by kadargergely on 2016-12-15.
 */

public class CameraFragment extends Fragment {

    private static final String TAG="CAMERA FRAGMENT";
    public static final int REQUEST_CAMERA_PERMISSION_RESULT = 1;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 3;
    private Size previewSize;
    private Size jpegSizes[] = null;

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private String cameraId;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    Button buttonGetPicture;
    private ProgressDialog progressDialog;

    private String nearbyMaterialName = GPSService.NO_MATERIAL;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            startCamera();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            transformImage(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            nearbyMaterialName = intent.getStringExtra("name");
        }
    };

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        progressDialog=new ProgressDialog(getActivity());
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        textureView = (TextureView) getView().findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        buttonGetPicture = (Button) getView().findViewById(R.id.buttonGetPicture);
        buttonGetPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();

                File picture = getOutputMediaFile();
                if (picture.exists()){
                    sendPicture(picture);
                    Log.d(TAG,"File exist!");
                } else {
                    Log.d(TAG,"File doesn't exist!");
                }

//                if (!nearbyMaterialName.equals(GPSService.NO_MATERIAL)) {
//                    Toast.makeText(getActivity(), "Itt egy " + nearbyMaterialName + ".", Toast.LENGTH_SHORT).show();
//                    getPicture();
//                } else {
//                    Toast.makeText(getActivity(), "Nincs a közelben nyersanyag.", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                messageReceiver, new IntentFilter(GPSService.NEARBY_RAW_MATERIAL_CHANGED));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            setupCamera(textureView.getWidth(), textureView.getHeight());
            transformImage(textureView.getWidth(), textureView.getHeight());
            connectCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        super.onPause();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Log.d(this.getClass().getName(), "permission request caught.");
//        switch (requestCode) {
//            case REQUEST_CAMERA_PERMISSION_RESULT:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d(this.getClass().getName(), "Camera permission granted by user.");
//                    // openCamera(textureView.getWidth(), textureView.getHeight());
//                } else {
//                    Log.d(this.getClass().getName(), "Camera permission denied by user.");
//                }
//                return;
//        }
//    }

    void getPicture() {
        if (cameraDevice == null) {
            Log.e(this.getClass().getName(), "cameraDevice is null.");
            return;
        }

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            }

            int width = 640;
            int height = 480;

            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    save(bytes);
                    if (image != null) {
                        image.close();
                    }
                }

                void save(byte[] bytes) {
                    File outFile = getOutputMediaFile();
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(outFile);
                        outputStream.write(bytes);
                        Log.i(this.getClass().getName(), "Photo saved to: " + outFile.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
                        }
                        Log.e(this.getClass().getName(), "Failed to create output media file.", e);
                    } catch (IOException e) {
                        Log.e(this.getClass().getName(), "Failed to write to the output media file.", e);
                    } finally {
                        try {
                            if (outputStream != null)
                                outputStream.close();
                        } catch (IOException e) {
                            Log.w(this.getClass().getName(), "Failed to close outputStream.", e);
                        }
                    }
                }
            };
            HandlerThread handlerThread = new HandlerThread("takepicture");
            handlerThread.start();
            final Handler handler = new Handler(handlerThread.getLooper());
            reader.setOnImageAvailableListener(imageAvailableListener, handler);
            final CameraCaptureSession.CaptureCallback previewSession = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    startCamera();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), previewSession, handler);
                    } catch (CameraAccessException e) {
                        Log.e(this.getClass().getName(), "Failed to access camera.", e);
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, handler);
        } catch (CameraAccessException e) {
            Log.e(this.getClass().getName(), "Failed to access camera.", e);
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ESamuPictures");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("ESamu_CameraFrag", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        Random rand = new Random();
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + timeStamp + "_" + nearbyMaterialName + "_" + String.valueOf(rand.nextInt(9000) + 1000) + ".jpg");
        return mediaFile;
    }

    public void connectCamera() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(cameraId, stateCallback, null);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(getActivity(),
                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            } else {
                cameraManager.openCamera(cameraId, stateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setupCamera(int width, int height) {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(id);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    int deviceOrientation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                    int totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                    boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                    int rotatedWidth = width;
                    int rotatedHeight = height;
                    if (swapRotation) {
                        rotatedWidth = height;
                        rotatedHeight = width;
                    }
                    previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(this.getClass().getName(), "Camera permission denied!", e);
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrienatation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrienatation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    void startCamera() {
        if (cameraDevice == null || !textureView.isAvailable() || previewSize == null) {
            Log.w(this.getClass().getName(), "Failed to start camera.");
            return;
        }
        SurfaceTexture texture = textureView.getSurfaceTexture();
        if (texture == null) {
            Log.w(this.getClass().getName(), "Failed to start camera.");
            return;
        }
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    previewSession = session;
                    getChangedPreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(this.getClass().getName(), "Capture session configuration failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(this.getClass().getName(), "Couldn't access camera.", e);
        }
    }

    void getChangedPreview() {
        if (cameraDevice == null) {
            Log.e(this.getClass().getName(), "Unable to update preview.");
            return;
        }
        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("changed Preview");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        try {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, handler);
        } catch (CameraAccessException e) {
            Log.e(this.getClass().getName(), "Couldn't access camera.", e);
        }
    }

    private void transformImage(int width, int height) {
        if (previewSize == null || textureView == null) {
            return;
        }
        Matrix matrix = new Matrix();
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(),
                    centerY - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / previewSize.getWidth(),
                    (float) height / previewSize.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private void sendPicture(File picture){
        Uri photoURI = FileProvider.getUriForFile(getContext(),
                "hu.esamu.rft.esamurft.camera",
                picture);
        FirebaseSender firebaseSender = new FirebaseSender();
        if (firebaseSender.isConnected()){
            if (firebaseSender.uploadPicture(photoURI)){
                progressDialog.setMessage("Uploaded.");
            } else {
                progressDialog.setMessage("Not uploaded.");
            }
            progressDialog.show();
        } else {
            Log.d(TAG,"Not connected to firebase!");
        }
    }

}
