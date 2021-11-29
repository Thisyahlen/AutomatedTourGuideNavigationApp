package com.example.automatedtourguidesystem;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsIS";

    private GeoDataClient mGeoDataClient;
    String geofenceTransitionString;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);



    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            for(String key : bundle.keySet()) {
                Log.i(TAG, "onHandleIntent 1 " + key +  " - " + bundle.getString(key));
            }
        }
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.i(TAG, "onHandleIntent getTriggeringLocation " + geofencingEvent.getTriggeringLocation());
        Log.i(TAG, "onHandleIntent getGeofenceTransition " + geofencingEvent.getGeofenceTransition());
        Log.i(TAG, "onHandleIntent getErrorCode " + geofencingEvent.getErrorCode());
        if(geofencingEvent.getTriggeringGeofences() != null) {
            for(Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                Log.i(TAG, "onHandleIntent 2 " + geofence);
                Log.i(TAG, "onHandleIntent geofence " + geofence.toString());
                //Geofence[CIRCLE id:placeUID transitions:7 3.081684, 101.612679 16060m, resp=0s, dwell=100ms, @268400505]
            }
        }
        if (geofencingEvent.hasError()) {
            //String errorMessage = GeofenceErrorMessages.getErrorString(this,
            // geofencingEvent.getErrorCode());
            Log.e("1", "onHandleIntent geofencingEvent error");
            return;
        }

        Log.i(TAG, "onHandleIntent 3");
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i(TAG, "onHandleIntent GEOFENCE_TRANSITION_ENTER");

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);

            mGeoDataClient = Places.getGeoDataClient(this);

            Log.i(TAG, geofenceTransitionDetails +" check here");

            Log.i(TAG, geofenceTransitionDetails +" dwdqwdqwdqwdqwdqwcheck here");

            if(LocationGeofence.locationGeofence != null) {
                LocationGeofence.locationGeofence.locationCallback(geofenceTransitionDetails);
            }

//            mGeoDataClient.getPlaceById(geofenceTransitionDetails).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
//                @Override
//                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
//                    if (task.isSuccessful()) {
//                        PlaceBufferResponse places = task.getResult();
//                        Place myPlace = places.get(0);
//                        Log.i(TAG, "Place found: " + myPlace.getName());
//                        CharSequence name = myPlace.getName();
//                        String placeName = name.toString();
//                        places.release();
//
//                        Calendar currentTime = Calendar.getInstance();
//                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
//                        String formattedDate = df.format(currentTime.getTime());
//
//                        sendNotification(geofenceTransitionString + ": " + placeName + " " + formattedDate);
//                    } else {
//                        Log.e(TAG, "Place not found.");
//                        Calendar currentTime = Calendar.getInstance();
//                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
//                        String formattedDate = df.format(currentTime.getTime());
//
//                        sendNotification(geofenceTransitionString + ": Unknown Place " + formattedDate);
//                    }
//                }
//            });
//            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        geofenceTransitionString = getTransitionString(geofenceTransition);
        Log.e("1", "gdsffdfsharrrrr  febbbbbbbls");

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());


        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);



        return triggeringGeofencesIdsString;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Log.e("1", "started haha geofencing111 here here ");
        Intent notificationIntent = new Intent(getApplicationContext(), AddLocationActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(AddLocationActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        Log.e("1", "tyyyyy febbbbbbbls");
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}
