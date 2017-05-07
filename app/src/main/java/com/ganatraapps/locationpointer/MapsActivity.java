package com.ganatraapps.locationpointer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    Context context;
    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 10;
    private boolean hasLocationPermission = true;
    TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = new Intent(this, MyService.class);
        //Start Service
        startService(intent);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Location Pointer");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        }

        context = this;
        errorText = (TextView) findViewById(R.id.error_text);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();


    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();

        if (mMap != null)
            // Request for current location once map is ready
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)         // 10 seconds
                    .setFastestInterval(1000);      // 1 second
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(8.0f);
        mMap.setMaxZoomPreference(20.0f);

        if (mMap != null)
            // Request for current location once map is ready
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)         // 10 seconds
                    .setFastestInterval(1000);      // 1 second


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (hasLocationPermission)
            getLocation();

    }

    private void getLocation() {
        Log.d("GPS", "getLocation called");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = false;

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) { //in case user clicked 'never ask again' and denied permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Request Location Permission");
                builder.setMessage("The application needs permission to access your current location, " +
                        "please grant location permission for the application from Settings>Permissions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MapsActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), null);
                builder.show();
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }

        } else {
            errorText.setVisibility(View.GONE);
            //first checking if location is enabled
            Log.d("GPS", "Checking for gps");
            if (isLocationEnabled(this)) {
                Log.d("GPS", "Checking for gps- true");

                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location == null) {
                    Log.d("GPS", "location null");

                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    if (hasLocationPermission)
                        getLocation();

                } else {
                    Log.d("GPS", "location not null");

                    //If everything went fine lets get latitude and longitude
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    if (mMap != null) {
                        // Add a marker in Sydney and move the camera
                        LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title(getString(R.string.location_tag)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f)); //zooming in on the location, 8.0-20.0 only

                    }

                    Log.d("location", currentLatitude + " and " + currentLongitude + "");
                }
            } else {
                // notify user that location needs to be turned on
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("To continue, turn on location to track GPS location on your phone. \n");
                dialog.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.cancel), null);
                dialog.show();
            }
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    hasLocationPermission = true;
                    if (mGoogleApiClient.isConnected()) {
                        getLocation();
                    } else {
                        showError();

                    }

                    Log.d("Permission", "Location permission got");
                } else { // permission denied
                    Log.d("Permission", "Location permission denied");


                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) { //in case user clicked 'never ask again' and denied permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Location Permission Denied");
                        builder.setMessage("The application needs permission to access your current location, " +
                                "please grant location permission for the application from Settings>Permissions");
                        builder.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 20);
                            }
                        });
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showError();
                            }
                        });
                        builder.show();
                    } else {
                        showError();
                    }

                }
                break;
        }

    }

    private void showError() {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(getString(R.string.no_permission));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();


    }
}
