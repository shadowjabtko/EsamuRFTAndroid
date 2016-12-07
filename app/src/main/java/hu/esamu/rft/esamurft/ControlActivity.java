package hu.esamu.rft.esamurft;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import hu.esamu.rft.esamurft.gps.GPSService;
import hu.esamu.rft.esamurft.map.MapsActivity;

/**
 * Created by ShadowJabtko on 2016.12.04..
 */

public class ControlActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{


    private static GamePageAdapter gamePageAdapter;
    private MainButtonsManipulator mainButtonsManipulator;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_game);

        enableMyLocation();

        if(AccessToken.getCurrentAccessToken() == null && !LoginActivity.signedIn){
            goLoginScreen();
        }

        final ViewPager viewPagerGame = (ViewPager) findViewById(R.id.viewPagerGame);
        TextView textViewMenu=(TextView)findViewById(R.id.textViewMenu);

        int pixelHeight=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MainButtonsManipulator.MODIFIED_HEIGHT_IN_DP, getResources().getDisplayMetrics());
        mainButtonsManipulator=new MainButtonsManipulator(pixelHeight,textViewMenu);

        try {
            ImageButton buttonBase=(ImageButton)findViewById(R.id.buttonBase);
            ImageButton buttonInventory=(ImageButton)findViewById(R.id.buttonInventory);
            ImageButton buttonMap=(ImageButton)findViewById(R.id.buttonMap);
            ImageButton buttonCamera=(ImageButton)findViewById(R.id.buttonCamera);
            ImageButton buttonQuest=(ImageButton)findViewById(R.id.buttonQuest);
            ImageButton buttonRecipes=(ImageButton)findViewById(R.id.buttonRecipes);

            mainButtonsManipulator.addImageButton(buttonBase,getString(R.string.title_base),MainButtonsManipulator.BASE_POSITION);
            mainButtonsManipulator.addImageButton(buttonInventory,getString(R.string.title_inventory),MainButtonsManipulator.INVENTORY_POSITION);
            mainButtonsManipulator.addImageButton(buttonMap,getString(R.string.title_map),MainButtonsManipulator.MAP_POSITION);
            mainButtonsManipulator.addImageButton(buttonCamera,getString(R.string.title_camera),MainButtonsManipulator.CAMERA_POSITION);
            mainButtonsManipulator.addImageButton(buttonQuest,getString(R.string.title_quests),MainButtonsManipulator.QUEST_POSITION);
            mainButtonsManipulator.addImageButton(buttonRecipes,getString(R.string.title_recipies),MainButtonsManipulator.RECIPES_POSITION);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gamePageAdapter = new GamePageAdapter(getSupportFragmentManager());
        viewPagerGame.setAdapter(gamePageAdapter);
        viewPagerGame.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mainButtonsManipulator.setButtonsLayoutToDefault();
                mainButtonsManipulator.setButtonLayoutToHigher(position);
                mainButtonsManipulator.setTextViewTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPagerGame.setCurrentItem(MainButtonsManipulator.BASE_POSITION);

        if (mPermissionDenied == false && !isMyServiceRunning(GPSService.class)) {
            Intent intent = new Intent(this, GPSService.class);
            startService(intent);
        }

        /*
        * Starting the network service
        * */
        if (!isMyServiceRunning(EsamuRTFService.class)){
            Intent intent = new Intent(this, EsamuRTFService.class);
            startService(intent);
        }

    }


    public void toBase(View v){
        ViewPager viewPagerGame = (ViewPager) findViewById(R.id.viewPagerGame);
        viewPagerGame.setCurrentItem(MainButtonsManipulator.BASE_POSITION);
    }

    public void toMap(View v){
        ViewPager viewPagerGame = (ViewPager) findViewById(R.id.viewPagerGame);
        viewPagerGame.setCurrentItem(MainButtonsManipulator.MAP_POSITION);
    }

    public void toInventory(View v){
        ViewPager viewPagerGame = (ViewPager) findViewById(R.id.viewPagerGame);
        viewPagerGame.setCurrentItem(MainButtonsManipulator.INVENTORY_POSITION);
    }

    public void toCamera(View v){
        ViewPager viewPagerGame = (ViewPager) findViewById(R.id.viewPagerGame);
        viewPagerGame.setCurrentItem(MainButtonsManipulator.CAMERA_POSITION);
    }

    public void toRecipes(View v){
        ViewPager viewPagerGame = (ViewPager) findViewById(R.id.viewPagerGame);
        viewPagerGame.setCurrentItem(MainButtonsManipulator.RECIPES_POSITION);
    }

    public void toQuest(View v){
        ViewPager viewPagerGame = (ViewPager) findViewById(R.id.viewPagerGame);
        viewPagerGame.setCurrentItem(MainButtonsManipulator.QUEST_POSITION);
    }

    public void toLogOut(View v){
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        else if (LoginActivity.signedIn) {
            LoginActivity.signedIn=false;
        }
        goLoginScreen();
    }

    public void toOptions(View v){
        Toast.makeText(
                this,
                "Settings button pressed.", Toast.LENGTH_SHORT).show();
    }

    public void openMap(View v){
        this.startActivity(new Intent(this, MapsActivity.class));
    }

    private void goLoginScreen() {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags( intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        mPermissionDenied = !PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        locationManager  = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Manager");
            builder.setMessage("Would you like to enable GPS?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(i, 666);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //No location service, no Activity
                    finish();
                }
            });
            builder.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 666) {
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "You just escaped the settings, GTFO", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
