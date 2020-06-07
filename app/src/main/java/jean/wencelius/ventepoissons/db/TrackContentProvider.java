package jean.wencelius.ventepoissons.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import jean.wencelius.ventepoissons.recopemValues;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class TrackContentProvider extends ContentProvider {
    private static final String TAG = TrackContentProvider.class.getSimpleName();

    /**Authority for Uris*/
    public static final String AUTHORITY = recopemValues.class.getPackage().getName() + ".provider";

    /** Uri for track*/
    public static final Uri CONTENT_URI_TRACK = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_TRACK);

    public static final Uri CONTENT_URI_FISHER = Uri.parse("content://"+ AUTHORITY + "/" + Schema.TBL_FISHER);

    /**Uri for the active track*/
    public static final Uri CONTENT_URI_TRACK_ACTIVE = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_TRACK + "/active");

    /**Uri for a specific picture*/
    public static final Uri CONTENT_URI_PICTURE_UUID = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_PICTURE + "/uuid");

    /**Uri for a specific waypoint*/
    public static final Uri CONTENT_URI_WAYPOINT_UUID = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_WAYPOINT + "/uuid");

    /**Uri for a specific waypoint*/
    public static final Uri CONTENT_URI_TRACKPOINT_UUID = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_TRACKPOINT + "/uuid");

    /**tables and joins to be used within a query to get the important informations of a track*/
    private static final String TRACK_TABLES = Schema.TBL_TRACK + " left join " + Schema.TBL_TRACKPOINT + " on " + Schema.TBL_TRACK + "." + Schema.COL_ID + " = " + Schema.TBL_TRACKPOINT + "." + Schema.COL_TRACK_ID;

    /**the projection to be used to get the important informations of a track*/
    private static final String[] TRACK_TABLES_PROJECTION = {
            Schema.TBL_TRACK + "." + Schema.COL_ID + " as " + Schema.COL_ID,
            Schema.COL_NAME,
            Schema.COL_ACTIVE,
            Schema.COL_DIR,
            Schema.COL_INF_ID,
            Schema.COL_RECOPEM_TRACK_ID,
            Schema.COL_TRACK_DATA_ADDED,
            Schema.COL_EXPORTED,
            Schema.COL_SENT_EMAIL,
            Schema.COL_PIC_ADDED,
            Schema.COL_CAUGHT_FISH_DETAILS,
            Schema.COL_START_DATE,
            Schema.COL_GPS_METHOD,
            Schema.COL_WEEKDAY,
            Schema.COL_DEVICE,
            Schema.COL_NEW_FISHER,
            Schema.COL_FISHER_NAME,
            Schema.COL_FISHER_RESIDENCE,
            Schema.COL_FISHER_PHONE,
            Schema.COL_ROADSIDE_WHEN,
            Schema.COL_ROADSIDE_WHEN_WHERE,
            Schema.COL_ROADSIDE_HABITATS,
            Schema.COL_GEAR,
            Schema.COL_GEAR_OTHER_DETAILS,
            Schema.COL_BOAT,
            Schema.COL_CREW_ALONE,
            Schema.COL_CREW_N,
            Schema.COL_WIND_FISHER,
            Schema.COL_CURRENT_FISHER,
            Schema.COL_CATCH_SALE,
            Schema.COL_CATCH_SALE_N,
            Schema.COL_CATCH_SALE_TYPE,
            Schema.COL_CATCH_SALE_PRICE,
            Schema.COL_CATCH_SALE_DETAILS,
            Schema.COL_CATCH_SALE_SEVERAL_FISHERS,
            Schema.COL_CATCH_ORDER,
            Schema.COL_CATCH_ORDER_N,
            Schema.COL_CATCH_ORDER_TYPE,
            Schema.COL_CATCH_ORDER_PRICE,
            Schema.COL_CATCH_ORDER_WHERE,
            Schema.COL_CATCH_ORDER_DETAILS,
            Schema.COL_CATCH_ORDER_PIC,
            Schema.COL_CATCH_GIVE,
            Schema.COL_CATCH_GIVE_N,
            Schema.COL_CATCH_GIVE_TYPE,
            Schema.COL_CATCH_GIVE_WHERE,
            Schema.COL_CATCH_GIVE_DETAILS,
            Schema.COL_CATCH_GIVE_PIC,
            Schema.COL_CATCH_CONS,
            Schema.COL_CATCH_CONS_N,
            Schema.COL_CATCH_CONS_TYPE,
            Schema.COL_CATCH_CONS_DETAILS,
            Schema.COL_CATCH_CONS_PIC,
            "count(" + Schema.TBL_TRACKPOINT + "." + Schema.COL_ID + ") as " + Schema.COL_TRACKPOINT_COUNT,
            "(SELECT count("+Schema.TBL_WAYPOINT+"."+Schema.COL_TRACK_ID+") FROM "+Schema.TBL_WAYPOINT+" WHERE "+Schema.TBL_WAYPOINT+"."+Schema.COL_TRACK_ID+" = " + Schema.TBL_TRACK + "." + Schema.COL_ID + ") as " + Schema.COL_WAYPOINT_COUNT
    };

    /**the group by statement that is used for the track statements*/
    private static final String TRACK_TABLES_GROUP_BY = Schema.TBL_TRACK + "." + Schema.COL_ID;

    /**Uri Matcher*/
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK, Schema.URI_CODE_TRACK);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/active", Schema.URI_CODE_TRACK_ACTIVE);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#", Schema.URI_CODE_TRACK_ID);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/start", Schema.URI_CODE_TRACK_START);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/end", Schema.URI_CODE_TRACK_END);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/" + Schema.TBL_WAYPOINT + "s", Schema.URI_CODE_TRACK_WAYPOINTS);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/" + Schema.TBL_TRACKPOINT + "s", Schema.URI_CODE_TRACK_TRACKPOINTS);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/" + Schema.TBL_PICTURE + "s", Schema.URI_CODE_TRACK_PICTURES);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/" + Schema.TBL_POISSON + "s", Schema.URI_CODE_TRACK_POISSONS);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_WAYPOINT + "/uuid/*", Schema.URI_CODE_WAYPOINT_UUID);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_PICTURE + "/uuid/*", Schema.URI_CODE_PICTURE_UUID);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACKPOINT + "/uuid/*", Schema.URI_CODE_TRACKPOINT_UUID);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_FISHER, Schema.URI_CODE_FISHER);
    }

    /**
     * @param trackId target track id
     * @return Uri for the waypoints of the track
     */
    public static final Uri waypointsUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                Schema.TBL_WAYPOINT + "s" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the trackpoints of the track
     */
    public static final Uri trackPointsUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                Schema.TBL_TRACKPOINT + "s" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the pictures of the track
     */
    public static final Uri picturesUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                Schema.TBL_PICTURE + "s" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the pictures of the track
     */
    public static final Uri poissonsUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                Schema.TBL_POISSON + "s" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the startpoint of the track
     */
    public static final Uri trackStartUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                "start" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the endpoint of the track
     */
    public static final Uri trackEndUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                "end" );
    }

    /**
     * Database Helper
     */
    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        String uuid;
        // Select which data type to delete
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK:
                count = dbHelper.getWritableDatabase().delete(Schema.TBL_TRACK, selection, selectionArgs);
                break;
            case Schema.URI_CODE_TRACK_ID:
                // the URI matches a specific track, delete all related entities
                String trackId = Long.toString(ContentUris.parseId(uri));
                dbHelper.getWritableDatabase().delete(Schema.TBL_WAYPOINT, Schema.COL_TRACK_ID + " = ?", new String[] {trackId});
                dbHelper.getWritableDatabase().delete(Schema.TBL_TRACKPOINT, Schema.COL_TRACK_ID + " = ?", new String[] {trackId});
                count = dbHelper.getWritableDatabase().delete(Schema.TBL_TRACK, Schema.COL_ID + " = ?", new String[] {trackId});
                break;
            case Schema.URI_CODE_PICTURE_UUID:
                uuid = uri.getLastPathSegment();
                if(uuid != null){
                    count = dbHelper.getWritableDatabase().delete(Schema.TBL_PICTURE, Schema.COL_UUID + " = ?", new String[]{uuid});
                }else{
                    count = 0;
                }
                break;
            case Schema.URI_CODE_WAYPOINT_UUID:
                uuid = uri.getLastPathSegment();
                if(uuid != null){
                    count = dbHelper.getWritableDatabase().delete(Schema.TBL_WAYPOINT, Schema.COL_UUID + " = ?", new String[]{uuid});
                }else{
                    count = 0;
                }
                break;
            case Schema.URI_CODE_TRACKPOINT_UUID:
                uuid = uri.getLastPathSegment();
                if(uuid != null){
                    count = dbHelper.getWritableDatabase().delete(Schema.TBL_TRACKPOINT, Schema.COL_UUID + " = ?", new String[]{uuid});
                }else{
                    count = 0;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Match and get the URI type, if recognized:
     * Matches {@link Schema#URI_CODE_TRACK_TRACKPOINTS}, {@link Schema#URI_CODE_TRACK_WAYPOINTS},
     * or {@link Schema#URI_CODE_TRACK}.
     * @throws IllegalArgumentException if not matched
     */
    @Override
    public String getType(Uri uri) throws IllegalArgumentException {
        // Select which type to return
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_TRACKPOINTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_TRACKPOINT;
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_WAYPOINT;
            case Schema.URI_CODE_TRACK:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_TRACK;
            case Schema.URI_CODE_FISHER:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_FISHER;
            case Schema.URI_CODE_TRACK_PICTURES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_PICTURE;
            case Schema.URI_CODE_TRACK_POISSONS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_POISSON;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // Select which data type to insert
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_TRACKPOINTS:
                // Check that mandatory columns are present.
                if (values.containsKey(Schema.COL_TRACK_ID) && values.containsKey(Schema.COL_LONGITUDE)
                        && values.containsKey(Schema.COL_LATITUDE) && values.containsKey(Schema.COL_TIMESTAMP)) {

                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_TRACKPOINT, null, values);
                    if (rowId > 0) {
                        Uri trackpointUri = ContentUris.withAppendedId(uri, rowId);
                        getContext().getContentResolver().notifyChange(trackpointUri, null);
                        return trackpointUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_LONGITUDE + ", "
                            + Schema.COL_LATITUDE + ", " + Schema.COL_TIMESTAMP);
                }
                break;
            case Schema.URI_CODE_TRACK_PICTURES:
                // Check that mandatory columns are present.
                if (values.containsKey(Schema.COL_TRACK_ID) && values.containsKey(Schema.COL_PIC_PATH)) {

                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_PICTURE, null, values);
                    if (rowId > 0) {
                        Uri pictureUri = ContentUris.withAppendedId(uri, rowId);
                        getContext().getContentResolver().notifyChange(pictureUri, null);
                        return pictureUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_PIC_PATH);
                }
                break;
            case Schema.URI_CODE_TRACK_POISSONS:
                // Check that mandatory columns are present.
                if (values.containsKey(Schema.COL_TRACK_ID) && values.containsKey(Schema.COL_CATCH_DESTINATION)) {

                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_POISSON, null, values);
                    if (rowId > 0) {
                        Uri poissonUri = ContentUris.withAppendedId(uri, rowId);
                        getContext().getContentResolver().notifyChange(poissonUri, null);
                        return poissonUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_CATCH_DESTINATION);
                }
                break;
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                // Check that mandatory columns are present.
                if (values.containsKey(Schema.COL_TRACK_ID) && values.containsKey(Schema.COL_LONGITUDE)
                        && values.containsKey(Schema.COL_LATITUDE) && values.containsKey(Schema.COL_TIMESTAMP) ) {

                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_WAYPOINT, null, values);
                    if (rowId > 0) {
                        Uri waypointUri = ContentUris.withAppendedId(uri, rowId);
                        getContext().getContentResolver().notifyChange(waypointUri, null);
                        return waypointUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_LONGITUDE + ", "
                            + Schema.COL_LATITUDE + ", " + Schema.COL_TIMESTAMP);
                }
                break;
            case Schema.URI_CODE_FISHER:
                if (values.containsKey(Schema.COL_FISHER_NAME) && values.containsKey(Schema.COL_FISHER_RESIDENCE)) {
                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_FISHER, null, values);
                    if (rowId > 0) {
                        Uri fisherUri = ContentUris.withAppendedId(CONTENT_URI_FISHER, rowId);
                        getContext().getContentResolver().notifyChange(fisherUri, null);
                        return fisherUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_FISHER_NAME + " & " + Schema.COL_FISHER_RESIDENCE);
                }
                break;
            case Schema.URI_CODE_TRACK:
                if (values.containsKey(Schema.COL_START_DATE)) {
                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_TRACK, null, values);
                    if (rowId > 0) {
                        Uri trackUri = ContentUris.withAppendedId(CONTENT_URI_TRACK, rowId);
                        getContext().getContentResolver().notifyChange(trackUri, null);
                        return trackUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_START_DATE);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return null;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selectionIn, String[] selectionArgsIn, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String selection = selectionIn;
        String[] selectionArgs = selectionArgsIn;

        String groupBy = null;
        String limit = null;

        // Select which datatype was requested
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_TRACKPOINTS:
                String trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_TRACKPOINT);
                selection = Schema.COL_TRACK_ID + " = ?";
                // Deal with any additional selection info provided by the caller
                if (null != selectionIn) {
                    selection += " AND " + selectionIn;
                }

                List<String> selctionArgsList = new ArrayList<String>();
                selctionArgsList.add(trackId);
                // Add the callers selection arguments, if any
                if (null != selectionArgsIn) {
                    for (String arg : selectionArgsIn) {
                        selctionArgsList.add(arg);
                    }
                }
                selectionArgs = selctionArgsList.toArray(new String[0]);
                // Finished with the temporary selection arguments list. release it for GC
                selctionArgsList.clear();
                selctionArgsList = null;
                break;
            case Schema.URI_CODE_TRACK_PICTURES:
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_PICTURE);
                selection = Schema.COL_TRACK_ID + " = ?";
                // Deal with any additional selection info provided by the caller
                if (null != selectionIn) {
                    selection += " AND " + selectionIn;
                }

                selctionArgsList = new ArrayList<String>();
                selctionArgsList.add(trackId);
                // Add the callers selection arguments, if any
                if (null != selectionArgsIn) {
                    for (String arg : selectionArgsIn) {
                        selctionArgsList.add(arg);
                    }
                }
                selectionArgs = selctionArgsList.toArray(new String[0]);
                // Finished with the temporary selection arguments list. release it for GC
                selctionArgsList.clear();
                selctionArgsList = null;
                break;
            case Schema.URI_CODE_TRACK_POISSONS:
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_POISSON);
                selection = Schema.COL_TRACK_ID + " = ?";
                // Deal with any additional selection info provided by the caller
                if (null != selectionIn) {
                    selection += " AND " + selectionIn;
                }

                selctionArgsList = new ArrayList<String>();
                selctionArgsList.add(trackId);
                // Add the callers selection arguments, if any
                if (null != selectionArgsIn) {
                    for (String arg : selectionArgsIn) {
                        selctionArgsList.add(arg);
                    }
                }
                selectionArgs = selctionArgsList.toArray(new String[0]);
                // Finished with the temporary selection arguments list. release it for GC
                selctionArgsList.clear();
                selctionArgsList = null;
                break;
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_WAYPOINT);
                if(!trackId.equals(Integer.toString(recopemValues.MAX_TRACK_ID))){
                    selection = Schema.COL_TRACK_ID + " = ?";
                    selectionArgs = new String[] {trackId};
                }else{
                    selection = null;
                    selectionArgs=null;
                }

                break;
            case Schema.URI_CODE_TRACK_START:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_TRACKPOINT);
                selection = Schema.COL_TRACK_ID + " = ?";
                selectionArgs = new String[] {trackId};
                sortOrder = Schema.COL_ID + " asc";
                limit = "1";
                break;
            case Schema.URI_CODE_TRACK_END:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_TRACKPOINT);
                selection = Schema.COL_TRACK_ID + " = ?";
                selectionArgs = new String[] {trackId};
                sortOrder = Schema.COL_ID + " desc";
                limit = "1";
                break;
            case Schema.URI_CODE_FISHER:
                qb.setTables(Schema.TBL_FISHER);
                break;
            case Schema.URI_CODE_TRACK:
                qb.setTables(TRACK_TABLES);
                if (projection == null)
                    projection = TRACK_TABLES_PROJECTION;
                groupBy = TRACK_TABLES_GROUP_BY;
                break;
            case Schema.URI_CODE_TRACK_ID:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getLastPathSegment();
                qb.setTables(TRACK_TABLES);
                if (projection == null)
                    projection = TRACK_TABLES_PROJECTION;
                groupBy = TRACK_TABLES_GROUP_BY;
                selection = Schema.TBL_TRACK + "." + Schema.COL_ID + " = ?";
                selectionArgs = new String[] {trackId};
                break;
            case Schema.URI_CODE_TRACK_ACTIVE:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                qb.setTables(Schema.TBL_TRACK);
                selection = Schema.COL_ACTIVE + " = ?";
                selectionArgs = new String[] {Integer.toString(Schema.VAL_TRACK_ACTIVE)};
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor c = qb.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, groupBy, null, sortOrder, limit);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selectionIn, String[] selectionArgsIn) {

        String table;
        String selection = selectionIn;
        String[] selectionArgs = selectionArgsIn;

        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                if (selectionIn == null || selectionArgsIn == null) {
                    // Caller must narrow to a specific waypoint
                    throw new IllegalArgumentException();
                }
                table = Schema.TBL_WAYPOINT;
                break;
            case Schema.URI_CODE_TRACK_POISSONS:
                if (selectionIn == null || selectionArgsIn == null) {
                    // Caller must narrow to a specific waypoint
                    throw new IllegalArgumentException();
                }
                table = Schema.TBL_POISSON;
                break;
            case Schema.URI_CODE_TRACK_ID:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                table = Schema.TBL_TRACK;
                String trackId = uri.getLastPathSegment();
                selection = Schema.COL_ID + " = ?";
                selectionArgs = new String[] {trackId};
                break;
            case Schema.URI_CODE_TRACK_ACTIVE:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                table = Schema.TBL_TRACK;
                selection = Schema.COL_ACTIVE + " = ?";
                selectionArgs = new String[] {Integer.toString(Schema.VAL_TRACK_ACTIVE)};
                break;
            case Schema.URI_CODE_FISHER:
                // Dangerous: Will update all the tracks, but necessary for instance
                // to switch all the tracks to inactive
                table = Schema.TBL_FISHER;
                break;
            case Schema.URI_CODE_TRACK:
                // Dangerous: Will update all the tracks, but necessary for instance
                // to switch all the tracks to inactive
                table = Schema.TBL_TRACK;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        int rows = dbHelper.getWritableDatabase().update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;

    }

    /**
     * Represents Data Schema.
     */
    public static final class Schema {
        public static final String TBL_TRACKPOINT = "trackpoint";
        public static final String TBL_WAYPOINT = "waypoint";
        public static final String TBL_TRACK = "track";
        public static final String TBL_PICTURE = "picture";
        public static final String TBL_POISSON = "poisson";
        public static final String TBL_FISHER = "fisher";

        public static final String COL_UUID = "uuid"; // In DataHelper (called by gpsLogger) TBL_WAYPOINTS

        public static final String COL_ID = "_id"; // EVERYWHERE
        public static final String COL_TRACK_ID = "track_id"; // EVERYWHERE
        public static final String COL_LONGITUDE = "longitude"; // In DataHelper (called by gpsLogger) TBL_TRACKPOINTS
        public static final String COL_LATITUDE = "latitude"; // In DataHelper (called by gpsLogger) TBL_TRACKPOINTS
        public static final String COL_SPEED = "speed"; // In DataHelper (called by gpsLogger) TBL_TRACKPOINTS
        public static final String COL_ACCURACY = "accuracy"; // In DataHelper (called by gpsLogger) TBL_TRACKPOINTS
        public static final String COL_TIMESTAMP = "point_timestamp"; // In DataHelper (called by gpsLogger) TBL_TRACKPOINTS
        public static final String COL_NAME = "name";// In MenuActiviy TBL_TRACK but not used ; // In DataHelper (called by gpsLogger) TBL_WAYPOINTS
        public static final String COL_START_DATE = "Day"; // MenuActivity TBL_TRACK

        //TBL_PICTURES
        public static final String COL_PIC_PATH ="path_to_pictures"; // MenuActivity TBL_PICTURES

        //TBL_POISSONS
        public static final String COL_FISH_FAMILY = "fishFamily"; //FishPickerDialog
        public static final String COL_FISH_TAHITIAN = "fishTahitian";//FishPickerDialog
        public static final String COL_CATCH_DESTINATION = "catchDestination";//FishPickerDialog
        public static final String COL_CATCH_N = "catchN";//FishPickerDialog
        public static final String COL_CATCH_N_TYPE = "catchNType";//FishPickerDialog

        //TBL FISHERS
        public static final String COL_FISHER_NAME = "fisherName";
        public static final String COL_FISHER_PHONE = "fisherPhone";
        public static final String COL_FISHER_RESIDENCE = "fisherResidence";

        public static final String COL_INF_ID = "Inf_ID"; // MenuActivity TBL_TRACK
        public static final String COL_RECOPEM_TRACK_ID = "My_Track_ID"; // MenuActivity TBL_TRACK
        public static final String COL_GPS_METHOD = "GPS_data_coll_method"; // MenuActivity TBL_TRACK
        public static final String COL_WEEKDAY = "Weekday"; // MenuActivity TBL_TRACK
        public static final String COL_NEW_FISHER = "newFisher"; //DataInputWho TBL_TRACK
        public static final String COL_ROADSIDE_WHEN = "roadsideWhen"; //DataInputWhen TBL_TRACK
        public static final String COL_ROADSIDE_WHEN_WHERE = "roadsideWhere"; //DataInputWhen TBL_TRACK
        public static final String COL_ROADSIDE_HABITATS  = "roadsideHabitats"; //DataInputWhen TBL_TRACK
        public static final String COL_GEAR = "Gear"; // DataInputGear TBL_TRACK
        public static final String COL_GEAR_OTHER_DETAILS = "Gear_details";// DataInputGear TBL_TRACK
        public static final String COL_BOAT = "Boat"; //DataInputBoat TBL_TRACK
        public static final String COL_CREW_ALONE ="crew_alone";//DataInputCrew TBL_TRACK
        public static final String COL_CREW_N ="crew_N";//DataInputCrew TBL_TRACK
        public static final String COL_WIND_FISHER = "Wind_est_fisher";//DataInputWind TBL_TRACK
        public static final String COL_CURRENT_FISHER = "Current_est_fisher";//DataInputWind TBL_TRACK
        public static final String COL_CATCH_SALE = "Catch_sale";//DataInputCatchSale TBL_TRACK
        public static final String COL_CATCH_SALE_N = "Catch_sale_N";//DataInputCatchSale TBL_TRACK
        public static final String COL_CATCH_SALE_TYPE = "Catch_sale_type";//DataInputCatchSale TBL_TRACK
        public static final String COL_CATCH_SALE_PRICE = "Catch_sale_price";//DataInputCatchSale TBL_TRACK
        public static final String COL_CATCH_SALE_DETAILS = "Catch_sale_details";//DataInputCatchSale TBL_TRACK
        public static final String COL_CATCH_SALE_SEVERAL_FISHERS = "Catch_sale_several_fishers";//DataInputCatchSale TBL_TRACK
        public static final String COL_CATCH_ORDER ="Catch_order";//DataInputCatchOrder TBL_TRACK
        public static final String COL_CATCH_ORDER_N = "Catch_order_N";//DataInputCatchOrder TBL_TRACK
        public static final String COL_CATCH_ORDER_TYPE = "Catch_order_type";//DataInputCatchOrder TBL_TRACK
        public static final String COL_CATCH_ORDER_PRICE = "Catch_order_price";//DataInputCatchOrder TBL_TRACK
        public static final String COL_CATCH_ORDER_WHERE= "Catch_order_where";//DataInputCatchOrder TBL_TRACK
        public static final String COL_CATCH_ORDER_DETAILS = "Catch_order_details";//DataInputCatchOrder TBL_TRACK
        public static final String COL_CATCH_ORDER_PIC = "Catch_order_pic";//DataInputCatchOrder TBL_TRACK
        public static final String COL_CATCH_CONS = "Catch_cons";//DataInputCatchCons TBL_TRACK
        public static final String COL_CATCH_CONS_N = "Catch_cons_N";//DataInputCatchCons TBL_TRACK
        public static final String COL_CATCH_CONS_TYPE = "Catch_cons_type";//DataInputCatchCons TBL_TRACK
        public static final String COL_CATCH_CONS_DETAILS = "Catch_cons_details";//DataInputCatchCons TBL_TRACK
        public static final String COL_CATCH_CONS_PIC = "Catch_cons_pic";//DataInputCatchCons TBL_TRACK
        public static final String COL_CATCH_GIVE = "Catch_give";//DataInputCatchGive TBL_TRACK
        public static final String COL_CATCH_GIVE_N = "Catch_give_N";//DataInputCatchGive TBL_TRACK
        public static final String COL_CATCH_GIVE_TYPE = "Catch_give_type";//DataInputCatchGive TBL_TRACK
        public static final String COL_CATCH_GIVE_WHERE= "Catch_give_where";//DataInputCatchGive TBL_TRACK
        public static final String COL_CATCH_GIVE_DETAILS = "Catch_give_details";//DataInputCatchGive TBL_TRACK
        public static final String COL_CATCH_GIVE_PIC = "Catch_give_pic";//DataInputCatchGive TBL_TRACK
        public static final String COL_PIC_ADDED = "Pic_added"; // MenuActivity TBL_TRACK
        public static final String COL_CAUGHT_FISH_DETAILS = "Caught_fish_details";// MenuActivity TBL_TRA
        public static final String COL_TRACK_DATA_ADDED = "Track_data_added"; // MenuActivity TBL_TRACK
        public static final String COL_EXPORTED = "Exported"; // MenuActivity TBL_TRACK
        public static final String COL_SENT_EMAIL = "emailSent"; //MenuActivity TBL_TRACK
        public static final String COL_DIR = "directory"; // MenuActivity TBL_TRACK
        public static final String COL_DEVICE = "device";// MenuActivity TBL_TRACK
        public static final String COL_ACTIVE = "active";// MenuActivity TBL_TRACK

        // virtual colums that are used in some sqls but dont exist in database
        public static final String COL_TRACKPOINT_COUNT = "tp_count";
        public static final String COL_WAYPOINT_COUNT = "wp_count";

        // Codes for UriMatcher
        public static final int URI_CODE_TRACK = 3;
        public static final int URI_CODE_TRACK_ID = 4;
        public static final int URI_CODE_TRACK_WAYPOINTS = 5;
        public static final int URI_CODE_TRACK_TRACKPOINTS = 6;
        public static final int URI_CODE_TRACK_ACTIVE = 7;
        public static final int URI_CODE_PICTURE_UUID = 8;
        public static final int URI_CODE_TRACK_START = 9;
        public static final int URI_CODE_TRACK_END = 10;
        public static final int URI_CODE_TRACK_PICTURES = 11;
        public static final int URI_CODE_TRACK_POISSONS = 12;
        public static final int URI_CODE_WAYPOINT_UUID = 13;
        public static final int URI_CODE_TRACKPOINT_UUID = 14;
        public static final int URI_CODE_FISHER = 15;

        public static final int VAL_TRACK_ACTIVE = 1;
        public static final int VAL_TRACK_INACTIVE = 0;
    }
}
