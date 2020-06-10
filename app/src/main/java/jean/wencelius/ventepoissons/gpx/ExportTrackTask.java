package jean.wencelius.ventepoissons.gpx;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.DataHelper;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.exception.ExportTrackException;
import jean.wencelius.ventepoissons.recopemValues;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public abstract class ExportTrackTask extends AsyncTask<Void, Long, Boolean> {
    /**
     * XML header.
     */
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    /**
     * GPX opening tag
     */
    private static final String TAG_GPX = "<gpx"
            + " xmlns=\"http://www.topografix.com/GPX/1/1\""
            + " version=\"1.1\""
            + " creator=\"Recopem Traceur\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd \">";

    /**
     * Date format for a point timestamp.
     */
    private SimpleDateFormat pointDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Very simple data format (year - month - day) for filename of .gpx (folder name kept with hour and minutes in case several tracks same day)
     */

    private SimpleDateFormat yearMonthDayDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * {@link Context} to get resources
     */
    protected Context context;

    /**
     * Track IDs to export
     */
    protected long[] trackIds;

    protected String saveDir;

    /**
     * Dialog to display while exporting
     */
    protected ProgressDialog dialog;

    /**
     * Message in case of an error
     */
    private String errorMsg = null;

    /**
     * @param startDate
     * @return The directory in which the track file should be created
     * @throws ExportTrackException
     */
    //protected abstract File getExportDirectory(String startDate) throws ExportTrackException;

    /**
     * Whereas to export the media files or not
     * @return
     */
    protected abstract boolean exportMediaFiles();

    /**
     * Whereas to update the track export date in the database at the end or not
     * @return
     */
    protected abstract boolean updateExportDate();

    public ExportTrackTask(Context context, String saveDir,long... trackIds) {
        this.context = context;
        this.trackIds = trackIds;
        this.saveDir=saveDir;

        pointDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        yearMonthDayDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    protected void onPreExecute() {
        // Display dialog
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage(context.getResources().getString(R.string.trackmgr_exporting_prepare));
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            for (int i=0; i<trackIds.length; i++) {
                exportTrackAsGpx(trackIds[i]);
            }
        } catch (ExportTrackException ete) {
            errorMsg = ete.getMessage();
            return false;
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        if (values.length == 1) {
            // Standard progress update
            dialog.incrementProgressBy(values[0].intValue());
        } else if (values.length == 2) {
            // To initialise the dialog, 2 values are passed to onProgressUpdate()
            // trackId, number of track points
            dialog.dismiss();

            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.setProgress(0);
            dialog.setMax(values[1].intValue());
            dialog.setTitle(
                    context.getResources().getString(R.string.trackmgr_exporting)
                            .replace("{0}", Long.toString(values[0])));
            dialog.show();
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        dialog.dismiss();
        if (!success) {
            new AlertDialog.Builder(context)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(context.getResources()
                            .getString(R.string.trackmgr_export_error)
                            .replace("{0}", errorMsg))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void exportTrackAsGpx(long trackId) throws ExportTrackException {

        File sdRoot = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            sdRoot = context.getExternalFilesDir(null);
            assert sdRoot!= null;
            if(!sdRoot.exists()){
                if(sdRoot.mkdirs()){
                }
            }
        }else{
            sdRoot = Environment.getExternalStorageDirectory();
        }

        ContentResolver cr = context.getContentResolver();
        Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            if (sdRoot.canWrite()) {

                File perTrackExportDirectory = new File(saveDir);

                String startDateYearMonthDay = saveDir.substring(saveDir.length()-19);
                startDateYearMonthDay = startDateYearMonthDay.substring(0,10);

                String filenameBaseGpx = startDateYearMonthDay + DataHelper.EXTENSION_GPX;
                String filenameBaseTrackCsv= startDateYearMonthDay + DataHelper.EXTENSION_CSV;
                String filenameBaseFishCsv= startDateYearMonthDay +"_fish_caught"+ DataHelper.EXTENSION_CSV;

                File trackFile = new File(perTrackExportDirectory, filenameBaseGpx);
                File trackCsvFile = new File(perTrackExportDirectory,filenameBaseTrackCsv);
                File fishCsvFile = new File(perTrackExportDirectory,filenameBaseFishCsv);

                Cursor cTrackPoints = cr.query(TrackContentProvider.trackPointsUri(trackId), null,
                        null, null, TrackContentProvider.Schema.COL_TIMESTAMP + " asc");
                Cursor cTrack = cr.query(trackUri, null, null, null, null);
                Cursor cFishCaught = cr.query(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_POISSON + "s"), null, null, null, null);

                boolean goExportTrackPoints = false;
                if(null!=cTrackPoints) goExportTrackPoints = cTrackPoints.getCount()>0;
                boolean goExportTrackCsv = false;
                if(null!=cTrack) goExportTrackCsv = cTrack.getCount()>0;
                boolean goExportFishCsv = false;
                if(null!=cFishCaught) goExportFishCsv = cFishCaught.getCount()>0;

                if(goExportTrackCsv){
                    try{
                        writeCsvFile(trackCsvFile,cTrack,startDateYearMonthDay, recopemValues.EXPORT_TRACK_DATA);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally{
                        cTrack.close();
                    }
                }
                if(goExportFishCsv){
                    try{
                        writeCsvFile(fishCsvFile,cFishCaught,startDateYearMonthDay,recopemValues.EXPORT_CAUGHT_FISH);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally{
                        cTrack.close();
                    }
                }

                if (goExportTrackPoints) {
                    publishProgress(new Long[]{trackId, (long) cTrackPoints.getCount()});

                    try {
                        writeGpxFile(cTrackPoints, trackFile);
                    } catch (IOException ioe) {
                        throw new ExportTrackException(ioe.getMessage());
                    } finally {
                        cTrackPoints.close();
                    }

                    // Force rescan of directory
                    ArrayList<String> files = new ArrayList<String>();
                    for (File file : perTrackExportDirectory.listFiles()) {
                        files.add(file.getAbsolutePath());
                    }
                    MediaScannerConnection.scanFile(context, files.toArray(new String[0]), null, null);
                }
            } else {
                throw new ExportTrackException(context.getResources().getString(R.string.error_externalstorage_not_writable));
            }
        }
    }

    /**
     * Writes the GPX file
     * @param cTrackPoints Cursor to track points.
     * @param target Target GPX file
     * @throws IOException
     */
    private void writeGpxFile(Cursor cTrackPoints, File target) throws IOException {

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(target));

            writer.write(XML_HEADER + "\n");
            writer.write(TAG_GPX + "\n");

            writeTrackPoints(context.getResources().getString(R.string.gpx_track_name), writer, cTrackPoints);

            writer.write("</gpx>");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Iterates on track points and write them.
     * @param trackName Name of the track (metadata).
     * @param fw Writer to the target file.
     * @param c Cursor to track points.
     * @throws IOException
     */
    private void writeTrackPoints(String trackName, Writer fw, Cursor c) throws IOException {
        // Update dialog every 1%
        int dialogUpdateThreshold = c.getCount() / 100;
        if (dialogUpdateThreshold == 0) {
            dialogUpdateThreshold++;
        }

        fw.write("\t" + "<trk>" + "\n");
        fw.write("\t\t" + "<name>" + CDATA_START + trackName + CDATA_END + "</name>" + "\n");

        fw.write("\t\t" + "<trkseg>" + "\n");

        int i=0;
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext(),i++) {
            StringBuffer out = new StringBuffer();
            out.append("\t\t\t" + "<trkpt lat=\""
                    + c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE)) + "\" "
                    + "lon=\"" + c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE)) + "\">" + "\n");

            out.append("\t\t\t\t" + "<time>" + pointDateFormatter.format(new Date(c.getLong(c.getColumnIndex(TrackContentProvider.Schema.COL_TIMESTAMP)))) + "</time>" + "\n");

            if(! c.isNull(c.getColumnIndex(TrackContentProvider.Schema.COL_ACCURACY))) {
                out.append("\t\t\t\t" + "<hdop>" + (c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_ACCURACY))) + "</hdop>" + "\n");
            }

            String buff = "";
            if(! c.isNull(c.getColumnIndex(TrackContentProvider.Schema.COL_SPEED))) {
                buff += "\t\t\t\t\t" + "<speed>" + c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_SPEED)) + "</speed>" + "\n";
            }

            if(! buff.equals("")) {
                out.append("\t\t\t\t" + "<extensions>\n");
                out.append(buff);
                out.append("\t\t\t\t" + "</extensions>\n");
            }

            out.append("\t\t\t" + "</trkpt>" + "\n");
            fw.write(out.toString());

            if (i % dialogUpdateThreshold == 0) {
                publishProgress((long) dialogUpdateThreshold);
            }
        }

        fw.write("\t\t" + "</trkseg>" + "\n");
        fw.write("\t" + "</trk>" + "\n");
    }

    private void writeCsvFile(File fileToExport,Cursor cursor,String startDate,String csvType) throws IOException{
        fileToExport.createNewFile();
        CSVWriter csvWrite = new CSVWriter(new FileWriter(fileToExport));
        csvWrite.writeNext(cursor.getColumnNames());
        while (cursor.moveToNext()) {
            String arrStr[] = null;
            if(csvType.equals(recopemValues.EXPORT_TRACK_DATA)){
                arrStr = returnTrackValues(cursor,startDate);
            }else if(csvType.equals(recopemValues.EXPORT_CAUGHT_FISH)){
                arrStr = returnFishCaughtValues(cursor);
            }
            csvWrite.writeNext(arrStr);
        }
        csvWrite.close();
    }


    private String [] returnTrackValues(Cursor cursor, String startDate){
        String arrStr [] = {
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ID)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_NAME)),
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ACTIVE))),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DIR)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_INF_ID)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_RECOPEM_TRACK_ID)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_EXPORTED)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_SENT_EMAIL)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_PIC_ADDED)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS)),
                startDate,
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_GPS_METHOD)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_WEEKDAY)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DEVICE)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_NEW_FISHER)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_FISHER_NAME)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_FISHER_RESIDENCE)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_FISHER_PHONE)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ROADSIDE_WHEN)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ROADSIDE_WHEN_WHERE)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ROADSIDE_HABITATS)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_GEAR)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_GEAR_OTHER_DETAILS)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_BOAT)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CREW_ALONE)),
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CREW_N))),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_WIND_FISHER)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CURRENT_FISHER)),
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_N))),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_TYPE)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_PRICE)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_DETAILS)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TUI_RACK_SEVERAL)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TUI_RACK_DETAILS)),
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACKPOINT_COUNT))),
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_WAYPOINT_COUNT)))
        };
        return arrStr;
    }

    private String [] returnFishCaughtValues(Cursor cursor){
        String arrStr [] = {
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ID))),
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_ID))),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_DESTINATION)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_FISH_FAMILY)),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_FISH_TAHITIAN)),
                Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N))),
                cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N_TYPE))
        };
        return arrStr;
    }


    /**
     * Copy all files from the OSMTracker external storage location to gpxOutputDirectory
     * @param gpxOutputDirectory The directory to which the track is being exported
     */
    /**
     private void copyWaypointFiles(long trackId, File gpxOutputDirectory) {
     // Get the new location where files related to these waypoints are/should be stored
     File trackDir = DataHelper.getTrackDirectory(trackId);

     if(trackDir != null){

     FileSystemUtils.copyDirectoryContents(gpxOutputDirectory, trackDir);
     }
     }
     */
}
