package app.flaneurs.com.flaneurs.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by kpu on 3/5/16.
 */
public class LocationProvider implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public abstract interface ILocationListener {
        public void onLocationChanged(Location location);
    }

    public static final String TAG = LocationProvider.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ArrayList<ILocationListener> mLocationListeners;
    private Context mContext;
    private Location mCurrentLocation;

    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public void addListener(ILocationListener locationListener) {
        mLocationListeners.add(locationListener);
    }

    public LocationProvider(Context context) {
        mLocationListeners = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        mContext = context;
    }

    public void connect() {
        // Connect the client if possible.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(TAG, "Google Play services is available.");
            return true;
        } else {
            // In debug mode, log the status
            Toast.makeText(mContext, "Google Play services is currently unavailable.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the mCurrentLocation
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location services connected.");

        // Try to get last known location
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            mCurrentLocation = location;

            Toast.makeText(mContext, "GPS location was found!", Toast.LENGTH_SHORT).show();
            this.fireOnLocationChanged(location);
        } else {
            Toast.makeText(mContext, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Current location was null.");
        }

        // Start listening for location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(mContext, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Location services connection suspended - Service Disconnected.");
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(mContext, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Location services connection suspended - Network Lost.");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, String.format("Location changed: %s", location.toString()));
        this.fireOnLocationChanged(location);
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Location services connection failed.");

		/*
         * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity) mContext;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
            Toast.makeText(mContext, "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Google play services unable to resolve location services connection failure.");
        }
    }

    private void fireOnLocationChanged(Location location) {
        if (location != null) {
            mCurrentLocation = location;
            for (ILocationListener listener : mLocationListeners) {
                listener.onLocationChanged(location);
            }
        }
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }
}
