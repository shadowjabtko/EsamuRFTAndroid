package hu.esamu.rft.esamurft.gps;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hu.esamu.rft.esamurft.R;

/**
 * Created by Sanya on 2016. 11. 10.
 * https://developer.android.com/guide/topics/location/strategies.html
 * https://developer.android.com/guide/components/services.html
 */
public class GPSService extends Service {

    // Acquire a reference to the system Location Manager
    private LocationManager locationManager;
    private LocationListener locationListener;

    private NotificationManager notificationManager;

    // ezen a távolságon belül ad notificationt (méterben)
    private double maxDistance = 15;
    private JSONObject obj;
    private JSONArray jsonArray;
    private List<RawMaterial> locations;

    final static String MY_ACTION = "MY_GPS_ACTION";

    private void loadJSON(){
        locations = new ArrayList<>();
        try {
            obj = new JSONObject(loadJSONFromAsset());
            jsonArray = obj.getJSONArray("locations");
            for (int i = 0; i < jsonArray.length(); i++) {

                String name = (String) jsonArray.getJSONObject(i).get("name");
                double latitde = Double.valueOf((String) jsonArray.getJSONObject(i).get("latitude"));
                double longitude = Double.valueOf((String) jsonArray.getJSONObject(i).get("longitude"));
                locations.add(new RawMaterial(name, latitde, longitude));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = GPSService.this.getAssets().open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Nem bindelünk.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, R.string.service_starting, Toast.LENGTH_SHORT).show();
        if (isLocationAvailableAndConnected()) {
            createGPSListener();
        } else {
            Toast.makeText(this, R.string.service_start_error_no_gps, Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        loadJSON();
        return START_STICKY;
    }

    private boolean isLocationAvailableAndConnected() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isGPSActive = /*manager.getLastKnownLocation() != null*/true;
        return isGPSEnabled && isGPSActive;
    }

    private void createGPSListener() {
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                notificationManager.cancel(1);
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(GPSService.this, R.string.service_running_error_gps_disabled_onthefly, Toast.LENGTH_SHORT).show();
                startMyNoti(1, "GPS le lett tiltva!", "A GPS le lett tiltva az alkalmazás működése közben. Kellene.");
                //stopSelf();
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            Toast.makeText(this, R.string.service_start_error_no_gps_permission, Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    private void makeUseOfNewLocation(Location location) {
        // a MapsActivitynek küldjük át a hely értékeket
        Bundle b = new Bundle();
        b.putParcelable("location", location);
        Intent i = new Intent();
        i.setAction(MY_ACTION);
        i.putExtra("bundle", b);
        sendBroadcast(i);

        // ha egy tárgytól adtott távolságon belül vagyunk, akkor notificationt rakunk ki
        for(RawMaterial r : locations) {
            double d = distance(location, r.getLocation());

            if (d < maxDistance) {
                startMyNoti(0, "Nyersanyag!!!", String.format("%.2f", d) + " méterre van egy " + r.getName() + "!");
            }
        }
    }

    private double distance(Location location1, LatLng location2) {
        float[] result = new float[1];
        Location.distanceBetween(location1.getLatitude(), location1.getLongitude(),
                location2.latitude, location2.longitude, result);
        return result[0];
    }

    @Override
    public void onDestroy() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            if (locationListener!=null){
                locationManager.removeUpdates(locationListener);
            }
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();

        notificationManager.cancelAll();
    }

    private void startMyNoti(int id, String title, String message){
        Intent intent = new Intent();
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification noti = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLights(0xff000000, 500, 500)
                .setContentIntent(pIntent).build();
        noti.flags = Notification.FLAG_AUTO_CANCEL;
        //noti.defaults = Notification.DEFAULT_VIBRATE;
        notificationManager.notify(id, noti);
    }
}