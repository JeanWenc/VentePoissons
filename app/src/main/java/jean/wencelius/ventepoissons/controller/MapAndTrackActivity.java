package jean.wencelius.ventepoissons.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;
import jean.wencelius.ventepoissons.service.gpsLogger;
import jean.wencelius.ventepoissons.service.gpsLoggerServiceConnection;
import jean.wencelius.ventepoissons.utils.MapTileProvider;
import jean.wencelius.ventepoissons.utils.OverlayTrackPoints;

public class MapAndTrackActivity extends AppCompatActivity {

    private static final String STATE_IS_TRACKING = "isTracking";
    /**
     * GPS Logger service, to receive events and be able to update UI.
     */
    private gpsLogger mGpsLogger;
    /**
     * GPS Logger service intent, to be used in start/stopService();
     */
    private Intent mGpsLoggerServiceIntent;
    /**
     * Flag to check GPS status at startup.
     */
    private boolean checkGPSFlag = true;
    /**
     * Handles the bind to the GPS Logger service
     */
    private ServiceConnection gpsLoggerConnection = new gpsLoggerServiceConnection(this);

    public long currentTrackId;

    public TextView mShowPointCount;

    private ImageButton btCenterMap;
    private ImageButton btShowCurrentTrack;

    private Boolean IS_CURRENT_TRACK_SHOWING;
    private String selTileProvider;

    private double mZoomLevel, mCurrentLon, mCurrentLat;
    private final static double mMooreaCenterLon = -149.831712;
    private final static double mMooreaCenterLat = -17.543859;

    MapView mMap = null;
    private IMapController mapController;

    private Bitmap mPersonIcon;

    private MyLocationNewOverlay mLocationOverlay;

    private ContentObserver trackpointContentObserver;

    private int mPointCount;
    private String pointCountText = "Nb de points = ";
    private String fullPointCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_track);

        mShowPointCount = (TextView) findViewById(R.id.activity_display_map_show_point_count);
        /*mPointCount = 0;
        fullPointCountText = pointCountText + mPointCount;
        mShowPointCount.setText(fullPointCountText);*/

        mMap = (MapView) findViewById(R.id.activity_display_map_map);

        btCenterMap = (ImageButton) findViewById(R.id.activity_display_map_ic_center_map);
        btShowCurrentTrack = (ImageButton) findViewById(R.id.activity_display_map_show_current_track);

        mPersonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);

        selTileProvider = recopemValues.MAP_TILE_PROVIDER_MOOREA_SAT;

        if (null != savedInstanceState) {
            currentTrackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            IS_CURRENT_TRACK_SHOWING = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_SHOW_CURRENT_TRACK);
            mZoomLevel = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_ZOOM);
            mCurrentLon = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_LONGITUDE);
            mCurrentLat = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_LATITUDE);
        } else {
            currentTrackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            IS_CURRENT_TRACK_SHOWING = false;
            mZoomLevel = 13.0;
            mCurrentLat = mMooreaCenterLat;
            mCurrentLon = mMooreaCenterLon;
        }

        mGpsLoggerServiceIntent = new Intent(this, gpsLogger.class);

        // Create content observer for trackpoints
        trackpointContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                UpdatePointCount();
            }
        };
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID, currentTrackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_SHOW_CURRENT_TRACK, IS_CURRENT_TRACK_SHOWING);
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_ZOOM, mMap.getZoomLevelDouble());
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_LATITUDE, mMap.getMapCenter().getLatitude());
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_LONGITUDE, mMap.getMapCenter().getLongitude());
        if (mGpsLogger != null) {
            outState.putBoolean(STATE_IS_TRACKING, mGpsLogger.isTracking());
        }
        //outState.putInt(BUNDLE_STATE_MLINE_INDEX,mLineIndex);
        super.onSaveInstanceState(outState);
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //Checking if proper permissions, and if not requesting them
        showMap();

        startTrackLoggerForNewTrack();

        String fulltext = "Enregistrement tracé # " + Long.toString(currentTrackId);
        setTitle(fulltext);

        resumeActivity();

        mMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    private void resumeActivity(){
        // Register content observer for any trackpoint changes
        getContentResolver().registerContentObserver(
                TrackContentProvider.trackPointsUri(currentTrackId),
                true, trackpointContentObserver);
        UpdatePointCount();
    }

    public void onPause() {
        getContentResolver().unregisterContentObserver(trackpointContentObserver);
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (mGpsLogger != null) {
            if (!mGpsLogger.isTracking()) {
                unbindService(gpsLoggerConnection);
                stopService(mGpsLoggerServiceIntent);
            } else {
                unbindService(gpsLoggerConnection);
            }
        }
        mMap.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void showMap() {
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mMap.setMultiTouchControls(true);
        mMap.setUseDataConnection(false);
        mMap.setTileProvider(MapTileProvider.setMapTileProvider(ctx, selTileProvider));

        mapController = mMap.getController();
        mapController.setZoom(mZoomLevel);

        GeoPoint startPoint = new GeoPoint(mCurrentLat, mCurrentLon);
        mapController.setCenter(startPoint);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMap);
        mLocationOverlay.setPersonIcon(mPersonIcon);
        mLocationOverlay.enableMyLocation();
        //mLocationOverlay.enableFollowLocation();
        mMap.getOverlays().add(mLocationOverlay);


        if (IS_CURRENT_TRACK_SHOWING) {
            showCurrentTrack();
        }

        btCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint myPosition = mLocationOverlay.getMyLocation();
                mMap.getController().animateTo(myPosition);
            }
        });

        btShowCurrentTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentTrack();
                IS_CURRENT_TRACK_SHOWING = true;
            }
        });
    }


    public void showCurrentTrack() {
        Cursor c = getContentResolver().query(TrackContentProvider.trackPointsUri(currentTrackId), null, null, null, null);
        if (c.getCount() > 0) {
            final SimpleFastPointOverlay sfpo = OverlayTrackPoints.createPointOverlay(c);

            mMap.getOverlays().add(sfpo);

            final double nor = sfpo.getBoundingBox().getLatNorth();
            final double sou = sfpo.getBoundingBox().getLatSouth();
            final double eas = sfpo.getBoundingBox().getLonEast();
            final double wes = sfpo.getBoundingBox().getLonWest();
            c.moveToLast();
            final GeoPoint lastGeoPoint = new GeoPoint(c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE)), c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE)));
            mMap.post(new Runnable() {
                @Override
                public void run() {
                    mapController.zoomToSpan((int) (nor - sou), (int) (eas - wes));
                    mapController.setCenter(lastGeoPoint);
                    //mapController.setCenter(new GeoPoint((nor + sou) / 2, (eas + wes) / 2));
                }
            });
        }
        c.close();
    }

    private void checkGPSProvider() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS isn't enabled. Offer user to go enable it
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_title))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_content))
                    .setCancelable(true).setPositiveButton(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create().show();
            checkGPSFlag = false;
        }
    }

    private void startTrackLoggerForNewTrack() {
        if (checkGPSFlag) {
            checkGPSProvider();
        }

        mGpsLoggerServiceIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, currentTrackId);

        // Start GPS Logger service
        startService(mGpsLoggerServiceIntent);

        // Bind to GPS service.
        // We can't use BIND_AUTO_CREATE here, because when we'll ubound
        // later, we want to keep the service alive in background
        bindService(mGpsLoggerServiceIntent, gpsLoggerConnection, 0);
    }

    private void stopTrackLoggerForNewTrack() {
        System.out.println("TrackRecordingStopped");
        if (mGpsLogger.isTracking()) {
            Intent intent = new Intent(recopemValues.INTENT_STOP_TRACKING);
            sendBroadcast(intent);
        }
    }

    public void UpdatePointCount(){
        if(mGpsLogger!=null) {
            mPointCount = mGpsLogger.getPointCount();
        }
        fullPointCountText = pointCountText + mPointCount;
        mShowPointCount.setText(fullPointCountText);

        if(mPointCount>5){
            stopTrackLoggerForNewTrack();
            Toast.makeText(MapAndTrackActivity.this, R.string.thank_you_message,Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run(){
                    Intent menuActivityIntent = new Intent(MapAndTrackActivity.this, TrackListActivity.class);
                    startActivity(menuActivityIntent);
                }
            },2000); //LENGTH_SHORT is usually 2 second long
        }
    }

    public long getCurrentTrackId() {
        return this.currentTrackId;
    }

    /**
     * Getter for gpsLogger @return Activity
     */
    public gpsLogger getGpsLogger() {
        return mGpsLogger;
    }

    /**
     * Setter for gpsLogger
     *
     * @param l
     */
    public void setGpsLogger(gpsLogger l) {
        this.mGpsLogger = l;
    }

    @Override
    public void onBackPressed() {
    }
}