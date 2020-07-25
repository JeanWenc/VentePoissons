package jean.wencelius.ventepoissons.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.controller.dataInput.dataInputWhen;
import jean.wencelius.ventepoissons.controller.dataInput.dataInputWho;
import jean.wencelius.ventepoissons.csv.ExportZip;
import jean.wencelius.ventepoissons.db.DataHelper;
import jean.wencelius.ventepoissons.db.ImageAdapter;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.gpx.ExportToStorageTask;
import jean.wencelius.ventepoissons.model.AppPreferences;
import jean.wencelius.ventepoissons.model.ImageUrl;
import jean.wencelius.ventepoissons.recopemValues;
import jean.wencelius.ventepoissons.utils.MapTileProvider;
import jean.wencelius.ventepoissons.utils.OverlayTrackPoints;

public class TrackDetailActivity extends AppCompatActivity implements ImageAdapter.OnImageListener{

    static TrackDetailActivity trackDetailActivity;

    public MapView mMapView;
    public IMapController mMapViewController;

    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;

    public ContentResolver mCr;
    public Cursor mCursorPictures;
    public Cursor mCursorTrackpoints;

    public long trackId;

    public String mFisherID;

    public Boolean mPicEmpty;
    private Boolean mNewPicAdded;
    public Boolean mDataAdded;
    public Boolean mExported;
    public Boolean mSentEmail;

    public String mSaveDir;

    private File currentImageFile;

    private static final String BUNDLE_STATE_SAVE_DIR = "stateSaveDir";
    private static final String BUNDLE_STATE_EXPORTED = "stateExported";
    private static final String BUNDLE_STATE_DATA = "stateData";
    private static final String BUNDLE_STATE_PIC = "statePic";
    private static final String BUNDLE_STATE_SENT_EMAIL = "stateSentEmail";
    private static final String BUNDLE_STATE_CURRENT_IMAGE_FILE ="stateCurrentImageFile";

    private Uri trackUri;

    private static final int REQUEST_TAKE_PHOTO = 0;
    public static final int REQUEST_BROWSE_PHOTO = 1;

    public static final String PREF_KEY_FISHER_ID = recopemValues.PREF_KEY_FISHER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);

        trackDetailActivity = this;

        mMapView = (MapView) findViewById(R.id.activity_track_detail_display_track);
        mMapView.setMultiTouchControls(true);
        mMapView.setUseDataConnection(false);
        mMapView.setTileProvider(MapTileProvider.setMapTileProvider(getApplicationContext(),recopemValues.MAP_TILE_PROVIDER_MOOREA_SAT));
        mMapViewController = mMapView.getController();
        mMapViewController.setZoom(13);

        if(savedInstanceState!=null){
            mSaveDir = savedInstanceState.getString(BUNDLE_STATE_SAVE_DIR);
            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);

            mNewPicAdded=savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);
            mPicEmpty=savedInstanceState.getBoolean(BUNDLE_STATE_PIC);
            mDataAdded = savedInstanceState.getBoolean(BUNDLE_STATE_DATA);
            mExported=savedInstanceState.getBoolean(BUNDLE_STATE_EXPORTED);
            mSentEmail=savedInstanceState.getBoolean(BUNDLE_STATE_SENT_EMAIL);

            String tempCurrentImageFile = savedInstanceState.getString(BUNDLE_STATE_CURRENT_IMAGE_FILE);
            if(!tempCurrentImageFile.equals("none")) currentImageFile = new File(tempCurrentImageFile);

        }else{
            mSaveDir = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_DIR);
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded= getIntent().getExtras().getString(TrackContentProvider.Schema.COL_PIC_ADDED).equals("true");
            mPicEmpty = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_PIC_ADDED).equals("false");

            mDataAdded = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED).equals("true");
            mExported = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_EXPORTED).equals("true");
            mSentEmail = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_SENT_EMAIL).equals("true");
        }
        setTitle("Tracé #" + trackId);

        trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

        mFisherID = AppPreferences.getDefaultsString(PREF_KEY_FISHER_ID,getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Image Gallery
        recyclerView = (RecyclerView) findViewById(R.id.activity_track_detail_recyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        mCr = getContentResolver();

        mCursorPictures = mCr.query(TrackContentProvider.picturesUri(trackId), null,
                null, null, TrackContentProvider.Schema.COL_ID + " asc");

        ArrayList imageUrlList = prepareData(mCursorPictures);
        ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext(), imageUrlList, this);

        recyclerView.setAdapter(imageAdapter);

        //Map
        GeoPoint startPoint = new GeoPoint(-17.543859, -149.831712);
        mMapViewController.setCenter(startPoint);

        mCursorTrackpoints = mCr.query(TrackContentProvider.trackPointsUri(trackId), null,
                null, null, TrackContentProvider.Schema.COL_TIMESTAMP + " asc");

        if(mCursorTrackpoints.getCount()>0){
            final SimpleFastPointOverlay sfpo = OverlayTrackPoints.createPointOverlay(mCursorTrackpoints);

            mMapView.getOverlays().add(sfpo);

            final double nor = sfpo.getBoundingBox().getLatNorth();
            final double sou = sfpo.getBoundingBox().getLatSouth();
            final double eas = sfpo.getBoundingBox().getLonEast();
            final double wes = sfpo.getBoundingBox().getLonWest();
            mMapView.post(new Runnable() {
                @Override
                public void run() {
                    mMapViewController.zoomToSpan((int) (nor-sou), (int) (eas-wes));
                    mMapViewController.setCenter(new GeoPoint((nor + sou) / 2, (eas + wes) / 2));
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putString(BUNDLE_STATE_SAVE_DIR,mSaveDir);

        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putBoolean(BUNDLE_STATE_PIC,mPicEmpty);
        outState.putBoolean(BUNDLE_STATE_DATA,mDataAdded);
        outState.putBoolean(BUNDLE_STATE_EXPORTED,mExported);
        outState.putBoolean(BUNDLE_STATE_SENT_EMAIL,mSentEmail);


        String tempCurrentImageFile = "none";
        if(currentImageFile!=null){
            tempCurrentImageFile = currentImageFile.toString();
        }
        outState.putString(BUNDLE_STATE_CURRENT_IMAGE_FILE,tempCurrentImageFile.toString());

        super.onSaveInstanceState(outState);
    }

    private ArrayList prepareData(Cursor cursor) {
        int i=0;
        ArrayList imageUrlList = new ArrayList<>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext(),i++) {
            String imagePath = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_PIC_PATH));

            ImageUrl imageUrl = new ImageUrl();
            imageUrl.setImageUrl(imagePath);
            imageUrlList.add(imageUrl);
        }
        cursor.close();
        return imageUrlList;
    }

    @Override
    public void onImageClick(int position) {
        if(position==0) {
            getNewPictures(REQUEST_BROWSE_PHOTO);
        }else{
            Cursor c = getContentResolver().query(TrackContentProvider.picturesUri(trackId), null,
                    null, null, TrackContentProvider.Schema.COL_ID + " asc");
            c.moveToPosition(position);

            String imagePath = c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_PIC_PATH));
            String imageUuid = c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_UUID));

            c.close();

            Intent ShowPictureIntent = new Intent(TrackDetailActivity.this,ShowPictureActivity.class);
            ShowPictureIntent.putExtra(TrackContentProvider.Schema.COL_PIC_PATH, imagePath);
            ShowPictureIntent.putExtra(TrackContentProvider.Schema.COL_UUID,imageUuid);

            startActivity(ShowPictureIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trackdetail_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.trackdetail_menu_export).setVisible(mDataAdded && !mPicEmpty);
        menu.findItem(R.id.trackdetail_menu_email).setVisible(mExported);
        menu.findItem(R.id.trackdetail_menu_delete).setVisible(mSentEmail);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.trackdetail_menu_add_data:
                Intent AddDataIntent = new Intent(TrackDetailActivity.this, dataInputWho.class);
                AddDataIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                startActivity(AddDataIntent);
                break;

            case R.id.trackdetail_menu_camera:
                getNewPictures(REQUEST_TAKE_PHOTO);
                break;

            case R.id.trackdetail_menu_export:
                new ExportToStorageTask(this, mSaveDir, trackId).execute();
                mExported = true;
                invalidateOptionsMenu();

                ContentValues valuesExp = new ContentValues();
                valuesExp.put(TrackContentProvider.Schema.COL_EXPORTED,"true");
                getContentResolver().update(trackUri, valuesExp, null, null);

                Toast.makeText(this, R.string.activity_track_detail_export_message_success, Toast.LENGTH_SHORT).show();
                break;

            case R.id.trackdetail_menu_email:
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected()) {
                    zipAndEmail(this,mSaveDir);
                }else{
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.activity_track_detail_wifi_dialog_title))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage(getResources().getString(R.string.activity_track_detail_wifi_dialog_message))
                            .setPositiveButton(getResources().getString(R.string.activity_track_detail_wifi_dialog_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    zipAndEmail(TrackDetailActivity.this,mSaveDir);
                                }
                            }).setNegativeButton(getResources().getString(R.string.activity_track_detail_wifi_dialog_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
                }
                break;

            case R.id.trackdetail_menu_delete:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.activity_track_detail_delete_dialog_title)
                        .setMessage(getResources().getString(R.string.activity_track_detail_delete_dialog_message))
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteTrack(trackId);
                                dialog.dismiss();
                                Intent TrackListActivityIntent = new Intent(TrackDetailActivity.this,TrackListActivity.class);
                                startActivity(TrackListActivityIntent);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteTrack(long trackId) {
        getContentResolver().delete(
                ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId),
                null, null);

        // Delete any data stored for the track we're deleting
        File trackStorageDirectory = new File(mSaveDir);
        if (trackStorageDirectory.exists()) {
            boolean deleted = false;

            //If it's a directory and we should delete it recursively, try to delete all childs
            if(trackStorageDirectory.isDirectory()){
                for(File child:trackStorageDirectory.listFiles()){
                    deleted = child.delete();
                }
            }
            deleted = trackStorageDirectory.delete();
        }

        Intent TrackListActivityIntent = new Intent(TrackDetailActivity.this,TrackListActivity.class);
        startActivity(TrackListActivityIntent);
        finish();
    }

    private void zipAndEmail(Context ctx, String saveDir){
        new ExportZip(ctx,saveDir).execute();
        mSentEmail = true;
        invalidateOptionsMenu();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackContentProvider.Schema.COL_SENT_EMAIL,"true");
        getContentResolver().update(trackUri, contentValues, null, null);

    }

    private void getNewPictures(int requestType) {
        //From OsmTracker
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent getNewPictureIntent = null;

        if(requestType == REQUEST_TAKE_PHOTO){
            getNewPictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }else if(requestType == REQUEST_BROWSE_PHOTO){
            getNewPictureIntent = new Intent(Intent.ACTION_PICK);
        }


        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            getNewPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            if(requestType== REQUEST_BROWSE_PHOTO){
                getNewPictureIntent.setType("image/*");
            }
            startActivityForResult(getNewPictureIntent, requestType);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ContentValues picVal = new ContentValues();
        ContentValues trackDataPic = new ContentValues();
        File imageFile =null;

        String imageUuid = null;
        switch(requestCode) {
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {

                    imageFile = popImageFile();
                    imageUuid = UUID.randomUUID().toString();
                    picVal.put(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                    picVal.put(TrackContentProvider.Schema.COL_UUID, imageUuid);
                    picVal.put(TrackContentProvider.Schema.COL_PIC_PATH, imageFile.toString());
                    getContentResolver().insert(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_PICTURE + "s"), picVal);
                    mNewPicAdded=true;
                    mPicEmpty = false;
                    trackDataPic.put(TrackContentProvider.Schema.COL_PIC_ADDED,"true");
                    getContentResolver().update(trackUri, trackDataPic, null, null);
                }
                break;
            case REQUEST_BROWSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    String[] fileIdColumn = {MediaStore.Images.Media._ID};
                    if (data.getData() != null) {

                        //Source file
                        Uri mImageUri = data.getData();

                        Cursor c = getContentResolver().query(mImageUri,
                                fileIdColumn, null, null, null);

                        c.moveToFirst();
                        int columnIndex = c.getColumnIndexOrThrow(fileIdColumn[0]);
                        Long imageId = c.getLong(columnIndex);
                        c.close();

                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,imageId);

                        //Destination File
                        imageFile = popImageFile();

                        try {
                            copyFile(contentUri, imageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        imageUuid = UUID.randomUUID().toString();

                        picVal.put(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                        picVal.put(TrackContentProvider.Schema.COL_UUID, imageUuid);
                        picVal.put(TrackContentProvider.Schema.COL_PIC_PATH, imageFile.toString());
                        getContentResolver().insert(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_PICTURE + "s"), picVal);
                        mNewPicAdded=true;
                        mPicEmpty = false;
                        trackDataPic.put(TrackContentProvider.Schema.COL_PIC_ADDED,"true");
                        getContentResolver().update(trackUri, trackDataPic, null, null);
                    }
                }
                break;
        }
        invalidateOptionsMenu();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File createImageFile() throws IOException {
        currentImageFile = null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = mFisherID + "_" + Long.toString(trackId) + "_" + timeStamp;

        File storageDir = new File (mSaveDir);
        // Create the track storage directory if it does not yet exist
        if (!storageDir.exists()) {
            if ( !storageDir.mkdirs() ) {
                Toast.makeText(this, "Directory [" + storageDir.getAbsolutePath() + "] does not exist and cannot be created", Toast.LENGTH_LONG).show();
            }
        }
        if (storageDir.exists() && storageDir.canWrite()) {
            currentImageFile = new File(storageDir,
                    imageFileName + DataHelper.EXTENSION_JPG);
        } else {
            Toast.makeText(this, "The directory [" + storageDir.getAbsolutePath() + "] will not allow files to be created", Toast.LENGTH_SHORT).show();
        }

        return currentImageFile;
    }

    private File popImageFile() {
        File imageFile = currentImageFile;
        currentImageFile = null;
        return imageFile;
    }

    private void copyFile(Uri sourceFile, File destFile) throws IOException {
        if (null != destFile && null != sourceFile) {
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            byte[] dataBuffer = new byte[1024];
            try {
                inputStream = getContentResolver().openInputStream(sourceFile);
                outputStream = new FileOutputStream(destFile);

                try {
                    int bytesRead = inputStream.read(dataBuffer);
                    while (-1 != bytesRead) {
                        outputStream.write(dataBuffer, 0, bytesRead);
                        bytesRead = inputStream.read(dataBuffer);
                    }

                    // No errors copying the file, look like we're good
                } catch (IOException e) {
                    String txtToDisplay = "IOException trying to write copy file ["
                            + sourceFile.getPath() + "] to ["
                            + destFile.getAbsolutePath() +"]: ["
                            + e.getMessage() + "]";
                    Toast.makeText(this, txtToDisplay, Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Source = "+destFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                String txtToDisplay2 = "File not found exception trying to write copy file ["
                        + sourceFile.getPath() + "] to ["
                        + destFile.getAbsolutePath() +"]: ["
                        + e.getMessage() + "]";
                Toast.makeText(this, txtToDisplay2, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static TrackDetailActivity getInstance(){
        return trackDetailActivity;
    }
}
