package jean.wencelius.ventepoissons.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import java.text.SimpleDateFormat;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class DataHelper {
    private static final String TAG = DataHelper.class.getSimpleName();

    /*** ContentResolver to interact with content provider*/
    private ContentResolver contentResolver;

    /**
     * GPX file extension.
     */
    public static final String EXTENSION_GPX = ".gpx";

    public static final String EXTENSION_CSV = ".csv";
    public static final String EXTENSION_ZIP = ".zip";
    /**
     * JPG file extension
     */
    public static final String EXTENSION_JPG = ".jpg";

    /**
     * Formatter for various files (GPX, media)
     */
    public static final SimpleDateFormat FILENAME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /**Constructor.
     @param c Application context.
     */
    public DataHelper(Context c) {
        contentResolver = c.getContentResolver();
    }

    public void track(long trackId, Location location) {
        ContentValues values = new ContentValues();
        values.put(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
        values.put(TrackContentProvider.Schema.COL_LATITUDE, location.getLatitude());
        values.put(TrackContentProvider.Schema.COL_LONGITUDE, location.getLongitude());

        if (location.hasAccuracy()) {
            values.put(TrackContentProvider.Schema.COL_ACCURACY, location.getAccuracy());
        }
        if (location.hasSpeed()) {
            values.put(TrackContentProvider.Schema.COL_SPEED, location.getSpeed());
        }

        values.put(TrackContentProvider.Schema.COL_TIMESTAMP, location.getTime());

        Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
        contentResolver.insert(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_TRACKPOINT + "s"), values);
    }

    /**
     * Tracks a way point with link
     *
     * @param trackId
     *				Id of the track
     * @param location
     *				Location of waypoint

     * @param name
     *				Name of waypoint

     * @param uuid
     * 			    Unique id of the waypoint

     */
    public void wayPoint(long trackId, Location location, String name, String uuid) {

        // location should not be null, but sometime is.
        // TODO investigate this issue.
        if (location != null) {
            ContentValues values = new ContentValues();
            values.put(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
            values.put(TrackContentProvider.Schema.COL_LATITUDE, location.getLatitude());
            values.put(TrackContentProvider.Schema.COL_LONGITUDE, location.getLongitude());
            values.put(TrackContentProvider.Schema.COL_NAME, name);

            if (uuid != null) {
                values.put(TrackContentProvider.Schema.COL_UUID, uuid);
            }

            if (location.hasAccuracy()) {
                values.put(TrackContentProvider.Schema.COL_ACCURACY, location.getAccuracy());
            }

            values.put(TrackContentProvider.Schema.COL_TIMESTAMP, location.getTime());

            Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
            contentResolver.insert(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_WAYPOINT + "s"), values);
        }
    }

    public void deletePicture(String uuid) {
        if (uuid != null) {
            contentResolver.delete(Uri.withAppendedPath(TrackContentProvider.CONTENT_URI_PICTURE_UUID, uuid), null, null);
        }
    }

    public void deleteWaypoint(String uuid) {
        if (uuid != null) {
            contentResolver.delete(Uri.withAppendedPath(TrackContentProvider.CONTENT_URI_WAYPOINT_UUID, uuid), null, null);
        }
    }

    public void deleteTrackpoint(String uuid) {
        if (uuid != null) {
            contentResolver.delete(Uri.withAppendedPath(TrackContentProvider.CONTENT_URI_TRACKPOINT_UUID, uuid), null, null);
        }
    }

    /** Stop tracking by making the track inactive
     * @param trackId Id of the track
     */
    public void stopTracking(long trackId) {
        Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
        ContentValues values = new ContentValues();
        values.put(TrackContentProvider.Schema.COL_ACTIVE, TrackContentProvider.Schema.VAL_TRACK_INACTIVE);
        contentResolver.update(trackUri, values, null, null);
    }
}
