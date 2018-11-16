package com.successive.adhoc.flutterlocationplugin.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

@SuppressWarnings("deprecation")
public class LocationFusedAPIUtility implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String TAG = LocationFusedAPIUtility.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final long INTERVAL = 1000 * 30 * 60;
    private static final long FASTEST_INTERVAL = 1000 * 25 * 60;
    private static final long MEDIUM_INTERVAL = 1000 * 30 * 60;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    private Location location;
    private Context context;
    private LocationCallback callback;

    public LocationFusedAPIUtility(Context context) {
        this.context = context;
        initLocation();
    }

    public void initLocation() {
        createLocationRequest();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.v(TAG, "Location update started ..............: ");
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && LocationServices.FusedLocationApi != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.v(TAG, "Location update stopped .......................");
            mGoogleApiClient.disconnect();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(FASTEST_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    public void requestSingleUpdate(final LocationCallback callback) {
        this.callback = callback;
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            startLocationUpdates();
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        //Toast.makeText(this, "LAT : " + location.getLatitude() + ", " + "LNG : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        Log.e("LAT_LNG", "LAT : " + location.getLatitude() + "\n" + "LNG : " + location.getLongitude());
        callback.onNewLocationAvailable(new LocationFusedAPIUtility.GPSCoordinates(location.getLatitude(), location.getLongitude()));

        stopLocationUpdates();
    }

    public Location getLastLocation() {
        return location;
    }

    public void dispose() {
        stopLocationUpdates();
        Log.v(TAG, "Service Stopped!");
    }
    public static interface LocationCallback {
        public void onNewLocationAvailable(LocationFusedAPIUtility.GPSCoordinates location);
    }

    public static class GPSCoordinates {
        public double longitude = -1;
        public double latitude = -1;

        public GPSCoordinates(float theLatitude, float theLongitude) {
            longitude = theLongitude;
            latitude = theLatitude;
        }

        public GPSCoordinates(double theLatitude, double theLongitude) {
            longitude = theLongitude;
            latitude = theLatitude;
        }
    }
}
