package jean.wencelius.ventepoissons.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jean.wencelius.ventepoissons.recopemValues;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    /**
     * SQL for creating table TRACKPOINT
     */
    private static final String SQL_CREATE_TABLE_TRACKPOINT = ""
            + "create table " + TrackContentProvider.Schema.TBL_TRACKPOINT + " ("
            + TrackContentProvider.Schema.COL_ID	+ " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_UUID + " text,"
            + TrackContentProvider.Schema.COL_TRACK_ID + " integer not null,"
            + TrackContentProvider.Schema.COL_LATITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_LONGITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_SPEED + " double null,"
            + TrackContentProvider.Schema.COL_ACCURACY + " double null,"
            + TrackContentProvider.Schema.COL_TIMESTAMP + " long not null"
            + ")";

    /**
     * SQL for creating index TRACKPOINT_idx (track id)
     * @since 12
     */
    private static final String SQL_CREATE_IDX_TRACKPOINT_TRACK
            = "create index if not exists "
            + TrackContentProvider.Schema.TBL_TRACKPOINT
            + "_idx ON " + TrackContentProvider.Schema.TBL_TRACKPOINT + "(" + TrackContentProvider.Schema.COL_TRACK_ID + ")";


    /**
     * SQL for creating table PICTURE
     */
    private static final String SQL_CREATE_TABLE_PICTURE = ""
            + "create table " + TrackContentProvider.Schema.TBL_PICTURE + " ("
            + TrackContentProvider.Schema.COL_ID	+ " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_TRACK_ID + " integer not null,"
            + TrackContentProvider.Schema.COL_UUID + " text,"
            + TrackContentProvider.Schema.COL_PIC_PATH + " text"
            + ")";

    /**
     * SQL for creating index PICTURE_idx (track id)
     * @since 12
     */
    private static final String SQL_CREATE_IDX_PICTURE_TRACK
            = "create index if not exists "
            + TrackContentProvider.Schema.TBL_PICTURE
            + "_idx ON " + TrackContentProvider.Schema.TBL_PICTURE + "(" + TrackContentProvider.Schema.COL_TRACK_ID + ")";


    /**
     * SQL for creating table FISH_CAUGHT
     */
    private static final String SQL_CREATE_TABLE_POISSON = ""
            + "create table " + TrackContentProvider.Schema.TBL_POISSON + " ("
            + TrackContentProvider.Schema.COL_ID + " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_TRACK_ID + " integer not null,"
            + TrackContentProvider.Schema.COL_CATCH_DESTINATION + " text,"
            + TrackContentProvider.Schema.COL_FISH_FAMILY + " text,"
            + TrackContentProvider.Schema.COL_FISH_TAHITIAN + " text,"
            + TrackContentProvider.Schema.COL_CATCH_N + " integer not null,"
            + TrackContentProvider.Schema.COL_CATCH_N_TYPE+ " text"
            + ")";

    /**
     * SQL for creating index FISH_CAUGHT_idx (track id)
     * @since 12
     */
    private static final String SQL_CREATE_IDX_POISSON_TRACK
            = "create index if not exists "
            + TrackContentProvider.Schema.TBL_POISSON
            + "_idx ON " + TrackContentProvider.Schema.TBL_POISSON + "(" + TrackContentProvider.Schema.COL_TRACK_ID + ")";

    /**
     * SQL for creating table WAYPOINT
     */
    private static final String SQL_CREATE_TABLE_WAYPOINT = ""
            + "create table " + TrackContentProvider.Schema.TBL_WAYPOINT + " ("
            + TrackContentProvider.Schema.COL_ID + " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_TRACK_ID + " integer not null,"
            + TrackContentProvider.Schema.COL_UUID + " text,"
            + TrackContentProvider.Schema.COL_LATITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_LONGITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_ACCURACY + " double null,"
            + TrackContentProvider.Schema.COL_TIMESTAMP + " long not null,"
            + TrackContentProvider.Schema.COL_NAME + " text"
            + ")";

    /**
     * SQL for creating index WAYPOINT_idx (track id)
     * @since 12
     */
    private static final String SQL_CREATE_IDX_WAYPOINT_TRACK
            = "create index if not exists "
            + TrackContentProvider.Schema.TBL_WAYPOINT
            + "_idx ON " + TrackContentProvider.Schema.TBL_WAYPOINT + "(" + TrackContentProvider.Schema.COL_TRACK_ID + ")";

    private static final String SQL_CREATE_TABLE_FISHER = ""
            + "create table " + TrackContentProvider.Schema.TBL_FISHER + " ("
            + TrackContentProvider.Schema.COL_ID + " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_TRACK_ID + " integer not null,"
            + TrackContentProvider.Schema.COL_FISHER_NAME + " text,"
            + TrackContentProvider.Schema.COL_FISHER_RESIDENCE + " text,"
            + TrackContentProvider.Schema.COL_FISHER_PHONE + " text"
            +")";

    /**
     * SQL for creating index FISH_CAUGHT_idx (track id)
     * @since 12
     */
    private static final String SQL_CREATE_IDX_FISHER_TRACK
            = "create index if not exists "
            + TrackContentProvider.Schema.TBL_FISHER
            + "_idx ON " + TrackContentProvider.Schema.TBL_FISHER + "(" + TrackContentProvider.Schema.COL_TRACK_ID + ")";

    /**
     * SQL for creating table TRACK
     * @since 5
     */
    private static final String SQL_CREATE_TABLE_TRACK = ""
            + "create table " + TrackContentProvider.Schema.TBL_TRACK + " ("
            + TrackContentProvider.Schema.COL_ID + " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_NAME + " text,"
            + TrackContentProvider.Schema.COL_DIR + " text,"
            + TrackContentProvider.Schema.COL_INF_ID + " text,"
            + TrackContentProvider.Schema.COL_RECOPEM_TRACK_ID + " text,"
            + TrackContentProvider.Schema.COL_GPS_METHOD + " text,"
            + TrackContentProvider.Schema.COL_START_DATE + " long not null,"
            + TrackContentProvider.Schema.COL_WEEKDAY + " text,"
            + TrackContentProvider.Schema.COL_NEW_FISHER + " text,"
            + TrackContentProvider.Schema.COL_FISHER_NAME + " text,"
            + TrackContentProvider.Schema.COL_FISHER_RESIDENCE + " text,"
            + TrackContentProvider.Schema.COL_FISHER_PHONE + " text,"
            + TrackContentProvider.Schema.COL_ROADSIDE_WHEN + " text,"
            + TrackContentProvider.Schema.COL_ROADSIDE_WHEN_WHERE + " text,"
            + TrackContentProvider.Schema.COL_ROADSIDE_HABITATS + " text,"
            + TrackContentProvider.Schema.COL_GEAR + " text,"
            + TrackContentProvider.Schema.COL_GEAR_OTHER_DETAILS + " text,"
            + TrackContentProvider.Schema.COL_BOAT + " text,"
            + TrackContentProvider.Schema.COL_CREW_ALONE + " text,"
            + TrackContentProvider.Schema.COL_CREW_N + " integer,"
            + TrackContentProvider.Schema.COL_WIND_FISHER + " text,"
            + TrackContentProvider.Schema.COL_CURRENT_FISHER + " text,"
            + TrackContentProvider.Schema.COL_CATCH_SALE_N + " integer,"
            + TrackContentProvider.Schema.COL_CATCH_SALE_TYPE + " text,"
            + TrackContentProvider.Schema.COL_CATCH_SALE_PRICE + " text,"
            + TrackContentProvider.Schema.COL_CATCH_SALE_DETAILS + " text,"
            + TrackContentProvider.Schema.COL_TUI_RACK_SEVERAL + " text,"
            + TrackContentProvider.Schema.COL_TUI_RACK_DETAILS + " text,"
            + TrackContentProvider.Schema.COL_PIC_PATH + " text,"
            + TrackContentProvider.Schema.COL_TRACK_DATA_ADDED + " text,"
            + TrackContentProvider.Schema.COL_PIC_ADDED + " text,"
            + TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS + " text,"
            + TrackContentProvider.Schema.COL_EXPORTED + " text,"
            + TrackContentProvider.Schema.COL_SENT_EMAIL + " text,"
            + TrackContentProvider.Schema.COL_DEVICE + " text,"
            + TrackContentProvider.Schema.COL_ACTIVE + " integer not null default 0"
            + ")";

    private static final String DB_NAME = recopemValues.class.getSimpleName();
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_TRACKPOINT);
        db.execSQL(SQL_CREATE_TABLE_TRACKPOINT);
        db.execSQL(SQL_CREATE_IDX_TRACKPOINT_TRACK);
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_PICTURE);
        db.execSQL(SQL_CREATE_TABLE_PICTURE);
        db.execSQL(SQL_CREATE_IDX_PICTURE_TRACK);
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_POISSON);
        db.execSQL(SQL_CREATE_TABLE_POISSON);
        db.execSQL(SQL_CREATE_IDX_POISSON_TRACK);
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_WAYPOINT);
        db.execSQL(SQL_CREATE_TABLE_WAYPOINT);
        db.execSQL(SQL_CREATE_IDX_WAYPOINT_TRACK);
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_FISHER);
        db.execSQL(SQL_CREATE_TABLE_FISHER);
        db.execSQL(SQL_CREATE_IDX_FISHER_TRACK);
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_TRACK);
        db.execSQL(SQL_CREATE_TABLE_TRACK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
