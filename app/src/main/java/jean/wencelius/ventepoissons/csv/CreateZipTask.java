package jean.wencelius.ventepoissons.csv;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.DataHelper;
import jean.wencelius.ventepoissons.model.AppPreferences;
import jean.wencelius.ventepoissons.recopemValues;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public abstract class CreateZipTask extends AsyncTask<Void, Long, Boolean> {
    private ProgressDialog dialog;

    protected Context context;

    private File zipExportDirectory;

    private File[] fileList;

    private String startDateYearMonthDay;


    //protected abstract File getExportDirectory(String startDate) throws ExportTrackException;

    static final int BUFFER = 2048;

    public CreateZipTask(Context context, String saveDir) {
        this.context = context;

        this.zipExportDirectory = new File(saveDir);
        this.fileList = zipExportDirectory.listFiles();

        this.startDateYearMonthDay = saveDir.substring(saveDir.length()-19);
        //String startDateYearMonthDay = saveDir.substring(saveDir.length()-19);
        //this.startDateYearMonthDay = startDateYearMonthDay.substring(0,10);
    }

    @Override
    protected void onPreExecute() {
        // Display dialog
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setProgress(0);
        dialog.setMax(fileList.length);
        dialog.setMessage(context.getResources().getString(R.string.activity_track_detail_zip_email));
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        zip();
        sendEmail();
        return true;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        dialog.incrementProgressBy(values[0].intValue());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        dialog.dismiss();
        if (!success) {
            new AlertDialog.Builder(context)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(context.getResources()
                            .getString(R.string.activity_track_detail_zip_task_msg_error))
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

    private void zip(){
        File [] zipArchiveList = zipExportDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });
        if(zipArchiveList.length>0){
            for(int i=0;i<zipArchiveList.length;i++){
                boolean deleted = zipArchiveList[i].delete();
            }
        }

        //Calculate total file size and determine number of needed zip archive to stay under 10000
        File _zipFile = new File(zipExportDirectory,startDateYearMonthDay + DataHelper.EXTENSION_ZIP);

        int fileCount = 0;

        try  {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(_zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] data = new byte[BUFFER];

            while(fileCount < fileList.length) {
                publishProgress((long) fileCount);
                String iFile = fileList[fileCount].getAbsolutePath();

                FileInputStream fi = new FileInputStream(iFile);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(iFile.substring(iFile.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                fileCount++;
                origin.close();
            }
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmail (){

        File [] zipArchiveList = zipExportDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });

        if(zipArchiveList.length>0){
            String email = recopemValues.EMAIL_RECIPIENT;
            String subject = "";
            String message = "";
            String fisher = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_ID,context);

            for(int i=0;i<zipArchiveList.length;i++){
                String archiveN = Integer.toString(i+1)+ "/"+Integer.toString(zipArchiveList.length);
                File tempZip = zipArchiveList[i];

                message = "Fisher = " + fisher  +
                        "\nDate = " + startDateYearMonthDay +
                        "\nArchive # " + archiveN;
                subject = fisher + " - " + startDateYearMonthDay + " - "+archiveN;

                try{
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { email });
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,subject);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempZip));
                    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
                    context.startActivity(Intent.createChooser(emailIntent,"Sending email..."));
                }catch(Throwable t){

                }
            }
        }
    }
}
