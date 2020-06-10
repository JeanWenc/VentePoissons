package jean.wencelius.ventepoissons.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.controller.MapAndTrackActivity;
import jean.wencelius.ventepoissons.db.DataHelper;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class gpsLogger extends Service {
    public int pointCount;

    private DataHelper dataHelper;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallBack;
    private Location mLastLocation;

    private boolean isTracking = false;

    private long currentTrackId = -1;

    private static long gpsLoggingInterval = 2000;
    private static long gpsLoggingIntervalFastest = 1000;

    private static final int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "recopemTraceur_Channel";


    public gpsLogger() {
    }

    /**Binder for service interaction*/
    private final IBinder binder = new gpsLoggerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // If we aren't currently tracking we can stop ourselves
        if (! isTracking ) {
            stopSelf();
        }
        // We don't want onRebind() to be called, so return false.
        return false;
    }

    /**Bind interface for service interaction*/
    public class gpsLoggerBinder extends Binder {
        /**
         * Called by the activity when binding.Returns itself.
         * @return the gpsLogger service
         */
        public gpsLogger getService() {
            return gpsLogger.this;
        }
    }

    /**Getter for isTracking
     * @return true if we're currently tracking, otherwise false.*/
    public boolean isTracking() {
        return isTracking;
    }

    /**Receives Intent for way point tracking, and stop/start logging.*/
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (recopemValues.INTENT_TRACK_WP.equals(intent.getAction())) {
                // Track a way point
                Bundle extras = intent.getExtras();
                if (extras != null) {

                    if(mLastLocation!=null){
                        Long trackId = extras.getLong(TrackContentProvider.Schema.COL_TRACK_ID);
                        String uuid = extras.getString(recopemValues.INTENT_KEY_UUID);
                        String name = extras.getString(recopemValues.INTENT_KEY_NAME);

                        dataHelper.wayPoint(trackId, mLastLocation, name, uuid);
                    }
                }
            }else if (recopemValues.INTENT_START_TRACKING.equals(intent.getAction()) ) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    long trackId = extras.getLong(TrackContentProvider.Schema.COL_TRACK_ID);
                    startTracking(trackId);
                }
            } else if (recopemValues.INTENT_STOP_TRACKING.equals(intent.getAction()) ) {
                stopTrackingAndSave();
            }
        }
    };

    @Override
    public void onCreate() {
        dataHelper = new DataHelper(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        pointCount=0;

        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(recopemValues.INTENT_TRACK_WP);
        filter.addAction(recopemValues.INTENT_START_TRACKING);
        filter.addAction(recopemValues.INTENT_STOP_TRACKING);
        registerReceiver(receiver, filter);

        super.onCreate();
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(gpsLoggingInterval);
        mLocationRequest.setFastestInterval(gpsLoggingIntervalFastest);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void getLastLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mLastLocation = location;
                }
            });
        }
    }

    private void startLocationUpdates(){
        fusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallBack, Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        if (isTracking) {
            // If we're currently tracking, save user data.
            stopTrackingAndSave();
        }
        // Unregister listener
        fusedLocationClient.removeLocationUpdates(mLocationCallBack);
        // Unregister broadcast receiver
        unregisterReceiver(receiver);

        stopNotifyBackgroundService();
        super.onDestroy();
    }

    private void startTracking(long trackId) {
        currentTrackId = trackId;

        isTracking = true;

        createLocationRequest();
        getLastLocation();

        mLocationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if (locationResult == null){
                    return;
                }
                for(Location location:locationResult.getLocations()){
                    mLastLocation = location;
                    if (isTracking) {
                        dataHelper.track(currentTrackId, location);
                        pointCount++;
                    }
                }
            }
        };
        startLocationUpdates();

        createNotificationChannel();
        NotificationManagerCompat nmgr = NotificationManagerCompat.from(this);
        nmgr.notify(NOTIFICATION_ID, getNotification());
    }

    private void stopTrackingAndSave() {
        isTracking = false;
        dataHelper.stopTracking(currentTrackId);
        this.stopSelf();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.app_name);
            String description = "Display when tracking in Background";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification getNotification() {

        Intent startDisplayMapActivity = new Intent(this, MapAndTrackActivity.class);
        startDisplayMapActivity.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, currentTrackId);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startDisplayMapActivity, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_recopem)
                .setContentTitle(getResources().getString(R.string.tracking_notification_title) +currentTrackId)
                .setContentText(getResources().getString(R.string.tracking_notification_content))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        return mBuilder.build();
    }

    private void stopNotifyBackgroundService() {
        NotificationManagerCompat nmgr = NotificationManagerCompat.from(this);
        nmgr.cancel(NOTIFICATION_ID);
    }

    public int getPointCount() {
        return this.pointCount;
    }
}
