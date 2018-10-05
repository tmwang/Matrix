package com.laioffer.accidentreporter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huang on 4/7/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private Context mContext;

    /**
     * Called when message is received.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (mContext == null) {
            mContext = getBaseContext();
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        sendNotification(remoteMessage);

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Define pending intent to trigger activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String type = remoteMessage.getData().get("type");


        Bitmap icon = null;

        switch(type) {
            case Config.POLICE:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.policeman);
                break;
            case Config.TRAFFIC:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.traffic);
                break;
            case Config.NO_ENTRY:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.no_entry);
                break;
            case Config.NO_PARKING:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.no_parking);
                break;
            case Config.SECURITY_CAMERA:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.security_camera);
                break;
            case Config.HEADLIGHT:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.lights);
            case Config.SPEEDING:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.no_parking);
                break;
            case Config.CONSTRUCTION:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.security_camera);
                break;
            case Config.SLIPPERY:
                icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.lights);
                break;
        }

        //Create Notification according to builder pattern
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "firebase");
        notificationBuilder
                .setSmallIcon(R.drawable.baseline_notifications_none_black_18dp)
                .setLargeIcon(icon)
                .setContentTitle(type)
                .setContentText("Please take care.")
                .setSound(defaultSoundUri)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(pendingIntent);

        // Get Notification Manager
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Send notification
        notificationManager.notify(1, notificationBuilder.build());
    }

}

