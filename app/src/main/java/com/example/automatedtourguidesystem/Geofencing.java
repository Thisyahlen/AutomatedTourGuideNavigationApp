package com.example.automatedtourguidesystem;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {

    // Constants
    public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 250; // 50 meters
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public Geofencing(Context context, GoogleApiClient client) {
        Log.e("1", "Geofencing");
        mContext = context;
        mGoogleApiClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    /***
     * Registers the list of Geofences specified in mGeofenceList with Google Place Services
     * Uses {@code #mGoogleApiClient} to connect to Google Place Services
     * Uses {@link #getGeofencingRequest} to get the list of Geofences to be registered
     * Uses {@link #getGeofencePendingIntent} to get the pending intent to launch the IntentService
     * when the Geofence is triggered
     * Triggers {@link #onResult} when the geofences have been registered successfully
     */
    public void registerAllGeofences() {
        Log.e("1", "Geofencing registerAllGeofences");
        // Check that the API client is connected and that the list has Geofences in it
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            Log.e("1", "Geofencing registerAllGeofences 3");
            return;
        }
        try {
            Log.e("1", "Geofencing registerAllGeofences 4");
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);

            Log.e("1", "Geofencing registerAllGeofences 2");
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }

    /***
     * Unregisters all the Geofences created by this app from Google Place Services
     * Uses {@code #mGoogleApiClient} to connect to Google Place Services
     * Uses {@link #getGeofencePendingIntent} to get the pending intent passed when
     * registering the Geofences in the first place
     * Triggers {@link #onResult} when the geofences have been unregistered successfully
     */
    public void unRegisterAllGeofences() {
        Log.e("1", "Geofencing unRegisterAllGeofences");
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in registerGeofences
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }


    /***
     * Updates the local ArrayList of Geofences using data from the passed in list
     * Uses the Place ID defined by the API as the Geofence object Id
     *
//     * @param places the PlaceBuffer result of the getPlaceById call
//     */
    public void updateGeofencesList(double latitude,double longitude) {
        Log.e("1", "Geofencing updateGeofencesList");
        mGeofenceList = new ArrayList<>();
//         Read the place information from the DB cursor
//        String placeUID = places.getId();
//        double placeLat = places.getLatLng().latitude;
//        double placeLng = places.getLatLng().longitude;
        // Build a Geofence object
        Geofence geofence = new Geofence.Builder()
                .setRequestId("placeUID")
                .setExpirationDuration(GEOFENCE_TIMEOUT)
                .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS)
                .setLoiteringDelay(100)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
        // Add it to the list
        mGeofenceList.add(geofence);

    }

    /***
     * Creates a GeofencingRequest object using the mGeofenceList ArrayList of Geofences
     * Used by {@code #registerGeofences}
     *
     * @return the GeofencingRequest object
     */
    private GeofencingRequest getGeofencingRequest() {
        Log.e("1", "Geofencing getGeofencingRequest");
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    /***
     * Creates a PendingIntent object using the GeofenceTransitionsIntentService class
     * Used by {@code #registerGeofences}
     *
     * @return the PendingIntent object
     */
    private PendingIntent getGeofencePendingIntent() {
        Log.e("1", "Geofencing getGeofencePendingIntent");
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        mContext.startService(intent);
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Geofencing onResult Error adding/removing geofence : %s",
                result.getStatus().toString()));
    }

}
