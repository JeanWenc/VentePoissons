package jean.wencelius.ventepoissons;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class recopemValues {
    public final static String PACKAGE_NAME = recopemValues.class.getPackage().getName();

    /**
     * Intent for tracking a waypoint
     */
    public final static String INTENT_TRACK_WP = recopemValues.PACKAGE_NAME + ".intent.TRACK_WP";

    /**
     * Intent to start tracking
     */
    public final static String INTENT_START_TRACKING = recopemValues.PACKAGE_NAME + ".intent.START_TRACKING";

    /**
     * Intent to stop tracking
     */
    public final static String INTENT_STOP_TRACKING = recopemValues.PACKAGE_NAME + ".intent.STOP_TRACKING";

    /**JW: All the below is when creating a waypoint*/
    /**
     * Key for extra data "waypoint name" in Intent
     */
    public final static String INTENT_KEY_NAME = "name";

    /**
     * Key for extra data "uuid" in Intent
     */
    public final static String INTENT_KEY_UUID = "uuid";

    //TODO:
    public static final String TOT_NB_QUESTIONS = "9";

    public static final String PREF_KEY_FISHER_NAME = "PREF_KEY_FISHER_NAME";
    public static final String PREF_KEY_FISHER_ID = "PREF_KEY_FISHER_ID";

    public static final int MY_DANGEROUS_PERMISSIONS_REQUESTS=42;

    public static final int MAX_TRACK_ID = 99999;

    public static final String BUNDLE_STATE_TRACK_ID = "trackId";
    public static final String BUNDLE_STATE_BUTTON = "nxtButton";
    public static final String BUNDLE_STATE_ANS = "mainAnswer";
    public static final String BUNDLE_STATE_WHERE_INT = "whereInt";
    public static final String BUNDLE_STATE_CATCH_N = "catchN";
    public static final String BUNDLE_STATE_TYPE_INT = "typeInt";
    public static final String BUNDLE_STATE_PRICE_INT = "priceInt";
    public static final String BUNDLE_STATE_DETAILS = "details";
    public static final String BUNDLE_STATE_PIC_ANS = "picAnswer";
    public static final String BUNDLE_EXTRA_CATCH_DESTINATION = "catchDestination";
    public static final String BUNDLE_STATE_ORDER_PIC_ANS = "orderPicAnswer";
    public static final String BUNDLE_STATE_GIVE_PIC_ANS = "givePicAnswer";

    public static final String BUNDLE_STATE_SHOW_CURRENT_TRACK = "showCurrentTrack";


    public static final String BUNDLE_STATE_CURRENT_ZOOM = "currentZoom";
    public static final String BUNDLE_STATE_CURRENT_LATITUDE = "currentLat";
    public static final String BUNDLE_STATE_CURRENT_LONGITUDE  = "currentLong";

    public static final String BUNDLE_STATE_NEW_PIC_ADDED = "newPicAdded";

    public static final String MAP_TILE_PROVIDER_MOOREA_SAT ="moorea.mbtiles";

    public static final String EXPORT_TRACK_DATA = "exportTrackData";
    public static final String EXPORT_CAUGHT_FISH = "exportCaughtFish";

    public static final String EMAIL_RECIPIENT = "recopem.reports@gmail.com";

    /** Device string identifiers */
    public static final class Devices {
        public static final String NEXUS_S = "Nexus S";
    }

    public static String getWeekdayString (int intWeekday){
        String mWeekdayString = new String();
        switch (intWeekday){
            case 1:
                mWeekdayString = "Dimanche";
                break;
            case 2 :
                mWeekdayString = "Lundi";
                break;
            case 3:
                mWeekdayString = "Mardi";
                break;
            case 4:
                mWeekdayString = "Mercredi";
                break;
            case 5:
                mWeekdayString = "Jeudi";
                break;
            case 6:
                mWeekdayString = "Vendredi";
                break;
            case 7:
                mWeekdayString = "Samedi";
                break;
        }
        return mWeekdayString;
    }
}
