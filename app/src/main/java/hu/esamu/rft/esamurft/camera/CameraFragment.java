package hu.esamu.rft.esamurft.camera;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.protobuf.ByteString;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


import hu.esamu.rft.esamurft.EsamuRTFService;
import hu.esamu.rft.esamurft.ProtoPicture;
import hu.esamu.rft.esamurft.R;

/**
 * Created by ShadowJabtko on 2016.12.04..
 */

public class CameraFragment extends Fragment {

    /**
     * Some constraints for the debugging and intent filtering.
     */
    private static final String TAG = "ServiceTesterActivity";
    private static final String EXTRA_SERVER_MESSAGE = "hu.esamu.rft.esamurft.server_message";
    private static final String EXTRA_NETWORK_NOT_AVAILABLE = "hu.esamu.rft.esamurft.network_not_available";
    private static final String EXTRA_SERVER_NOT_RUNNING = "hu.esamu.rft.esamurft.server_not_running";
    private static final String EXTRA_CANT_SEND_MESSAGE = "hu.esamu.rft.esamurft.cant_send_message";

    private Camera camera;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        camera = new Camera(getActivity().getApplicationContext(), getActivity());

        registerBroadcastReceiver();

        Button buttonTakePhoto = (Button) view.findViewById(R.id.buttonTakePhoto);
        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.dispatchTakePictureIntent();
            }
        });

        Button buttonUpload = (Button) view.findViewById(R.id.buttonUpload);
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                byte[] readFileToByteArray;

                InputStream inputStream;
                try {
                    inputStream = getActivity().getAssets().open("mypic.jpg");
                    readFileToByteArray = IOUtils.toByteArray(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                ProtoPicture.Picture protoPicture = ProtoPicture.Picture
                        .newBuilder()
                        .setLatitude(0.0)
                        .setLongitude(0.0)
                        .setImage(ByteString.copyFrom(readFileToByteArray))
                        .build();


                mService.sendMessage(protoPicture);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), EsamuRTFService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    EsamuRTFService mService;
    boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            EsamuRTFService.EsamuRTFServiceBinder binder = (EsamuRTFService.EsamuRTFServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    /**
     * Receives the service's broadcast messages and handles them.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (EXTRA_SERVER_MESSAGE.equals(intent.getAction())) {
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_SERVER_MESSAGE));
            }
            if (EXTRA_NETWORK_NOT_AVAILABLE.equals(intent.getAction())) {
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_NETWORK_NOT_AVAILABLE));
            }
            if (EXTRA_SERVER_NOT_RUNNING.equals(intent.getAction())) {
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_SERVER_NOT_RUNNING));
            }
            if (EXTRA_CANT_SEND_MESSAGE.equals(intent.getAction())) {
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_CANT_SEND_MESSAGE));
            }
        }
    };

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(EXTRA_SERVER_MESSAGE);
        filter.addAction(EXTRA_NETWORK_NOT_AVAILABLE);
        filter.addAction(EXTRA_SERVER_NOT_RUNNING);
        filter.addAction(EXTRA_CANT_SEND_MESSAGE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }


}
