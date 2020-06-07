package jean.wencelius.ventepoissons.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import jean.wencelius.ventepoissons.controller.MapAndTrackActivity;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class gpsLoggerServiceConnection implements ServiceConnection {
    private MapAndTrackActivity activity;

    public gpsLoggerServiceConnection(MapAndTrackActivity dma){
        activity = dma;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        activity.setGpsLogger(((gpsLogger.gpsLoggerBinder) service).getService());
        Intent intent = new Intent(recopemValues.INTENT_START_TRACKING);
        intent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, activity.getCurrentTrackId());
        activity.sendBroadcast(intent);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        activity.setGpsLogger(null);
    }
}
