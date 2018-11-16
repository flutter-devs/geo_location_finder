package com.successive.adhoc.flutterlocationplugin;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.successive.adhoc.flutterlocationplugin.util.LocationFusedAPIUtility;

import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

import static android.app.Activity.RESULT_CANCELED;

public class FlutterLocationPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {
    private static final String TAG = FlutterLocationPlugin.class.getSimpleName();
    private static final int REQUEST_ENABLE_GPS_CODE = 101;
    private static final String METHOD_CHANNEL = "github.com/geo_location_finder";
    private Activity _activity;
    private Result _result;

    public FlutterLocationPlugin(Activity _activity) {
        this._activity = _activity;
    }

    public static void registerWith(PluginRegistry.Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), METHOD_CHANNEL);
        final FlutterLocationPlugin instance = new FlutterLocationPlugin(registrar.activity());
        registrar.addActivityResultListener(instance);
        channel.setMethodCallHandler(instance);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onMethodCall(MethodCall call, Result result) {
        this._result = result;
        if (call.method.equals("getLocation")) {

            Dexter.withActivity(_activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            if (isGPSEnabled(_activity)) getLocation();
                            else {
                                buildEnableGPSDialog();
                            }
                        }
                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Log.v(TAG, "PERMISSION_DENIED");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("message", "Location permission is required");
                            _result.success(map);
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

        } else {
            result.notImplemented();
        }
    }

    public boolean isGPSEnabled(Context mContext) {
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void buildEnableGPSDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        _activity.startActivityForResult(gpsIntent, REQUEST_ENABLE_GPS_CODE);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        returnErrorStatus("Please enable GPS");
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /*private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(_activity, REQUEST_ENABLE_GPS_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }*/


    private void getLocation() {
        final LocationFusedAPIUtility fusedAPI = new LocationFusedAPIUtility(_activity);
        fusedAPI.requestSingleUpdate(new LocationFusedAPIUtility.LocationCallback() {
            @Override
            public void onNewLocationAvailable(LocationFusedAPIUtility.GPSCoordinates location) {
                Log.e("LAT_LNG", "LAT : " + location.latitude + "\n" + "LNG : " + location.longitude);
                HashMap<String, Object> map = new HashMap<>();
                map.put("status", true);
                map.put("latitude", location.latitude);
                map.put("longitude", location.longitude);
                _result.success(map);
            }
        });
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == REQUEST_ENABLE_GPS_CODE && resultCode == RESULT_CANCELED) {
            String provider = Settings.Secure.getString(_activity.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (provider != null && provider.trim().length() > 0) {
                Log.v(TAG, " Location providers: " + provider);
                getLocation();
                return true;
            } else {
                returnErrorStatus("Please enable GPS");
            }
        } else {
            returnErrorStatus("Please enable GPS");
        }
        return false;
    }

    private void returnErrorStatus(String message) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", false);
        map.put("message", message);
        _result.success(map);
    }

}
