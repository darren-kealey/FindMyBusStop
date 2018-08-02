package com.findstop.darren.findmystop;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import java.util.ArrayList;
import java.util.List;



public class GeofenceIntentClass extends IntentService {

    private static final String TAG = GeofenceIntentClass.class.getSimpleName();

    public static final int notification_id = 0;

    public GeofenceIntentClass() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences );

            // Send notification details as a String
            sendNotification( geofenceTransitionDetails );
        }
    }


    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    private void sendNotification(String msg) {

        Intent notificationIntent = FindStops.makeNotificationIntent(getApplicationContext(), msg);
        Intent notificationIntent1 = sevenAInward.makeNotificationIntent(getApplicationContext(), msg);
        Intent notificationIntent2 = sevenAOutbound.makeNotificationIntent(getApplicationContext(), msg);
        Intent notificationIntent3 = jtownOutbound.makeNotificationIntent(getApplicationContext(), msg);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        TaskStackBuilder stackBuilder1 = TaskStackBuilder.create(this);
        TaskStackBuilder stackBuilder2 = TaskStackBuilder.create(this);
        TaskStackBuilder stackBuilder3 = TaskStackBuilder.create(this);


        stackBuilder.addParentStack(FindStops.class);
        stackBuilder1.addParentStack(sevenAInward.class);
        stackBuilder2.addParentStack(sevenAOutbound.class);
        stackBuilder3.addParentStack(jtownOutbound.class);

        stackBuilder.addNextIntent(notificationIntent);
        stackBuilder1.addNextIntent(notificationIntent1);
        stackBuilder2.addNextIntent(notificationIntent2);
        stackBuilder3.addNextIntent(notificationIntent3);



        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificationMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationMng.notify(
                notification_id,
                createNotification( notificationPendingIntent));
    }
    // Create notification
    private Notification createNotification(PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.logo)
                .setColor(Color.WHITE)
                .setContentTitle("Find My Stop")
                .setContentText("Less than 1 kilometre from your Stop!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}

