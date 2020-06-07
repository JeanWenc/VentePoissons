package jean.wencelius.ventepoissons.controller.dataInput;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.ImageFishAdapter;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.model.ImageUrl;
import jean.wencelius.ventepoissons.recopemValues;
import jean.wencelius.ventepoissons.utils.FishPickerDialog;
import jean.wencelius.ventepoissons.utils.MapTileProvider;

public class dataInputFishCaught extends AppCompatActivity implements ImageFishAdapter.OnImageListener{

    private TextView mOtherFishIntro;
    private TextView mOtherFishDetail;

    private String [] mFishFileList;
    private ArrayList<String> mFishTahitianList;
    private List<String> mFishFamilyList;
    private ArrayList<String> mFishCountList;

    public ContentResolver mCr;
    public Cursor mCursorFishCaught;

    private int mSelImage;

    private String mOtherCaughtFish;

    private long trackId;
    private String mCatchDestination;

    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;

    private static final String BUNDLE_STATE_LAST_SELECTED_IMAGE = "lastSelectedImage";
    private static final String BUNDLE_STATE_FISH_COUNT_LIST = "fishCountList";
    private static final String BUNDLE_STATE_OTHER_CAUGHT_FISH = "otherCaughtFish";
    private static final String BUNDLE_STATE_TAHITIAN_FISH_LIST = "tahitianFishList";

    private ArrayList imageUrlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_fish_caught);

        mOtherFishIntro = (TextView) findViewById(R.id.activity_data_input_fish_caught_other_fish);
        mOtherFishDetail = (TextView) findViewById(R.id.activity_data_input_fish_caught_other_fish_detail);

        mFishFileList = getResources().getStringArray(R.array.data_input_fish_caught_fish_file_list);
        mFishFamilyList = Arrays.asList(getResources().getStringArray(R.array.data_input_fish_caught_fish_family_list));

        if(savedInstanceState!=null){
            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mCatchDestination = savedInstanceState.getString(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION);
            mSelImage = savedInstanceState.getInt(BUNDLE_STATE_LAST_SELECTED_IMAGE);
            mFishCountList = new ArrayList<String>(Arrays.asList(savedInstanceState.getStringArray(BUNDLE_STATE_FISH_COUNT_LIST)));
            mFishTahitianList = new ArrayList<String>(Arrays.asList(savedInstanceState.getStringArray(BUNDLE_STATE_TAHITIAN_FISH_LIST)));
            mOtherCaughtFish = savedInstanceState.getString(BUNDLE_STATE_OTHER_CAUGHT_FISH);
        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mCatchDestination = getIntent().getExtras().getString(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION);
            mFishTahitianList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.data_input_fish_caught_fish_tahitian_list)));
            mSelImage = -1;

            mFishCountList = new ArrayList<String>(Arrays.asList(new String[mFishFileList.length]));
            populateFishCountList();
        }

        if(mOtherCaughtFish.equals("")){
            mOtherFishIntro.setVisibility(View.INVISIBLE);
        }else{
            mOtherFishIntro.setVisibility(View.VISIBLE);
            mOtherFishDetail.setText(mOtherCaughtFish);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView = (RecyclerView) findViewById(R.id.activity_data_input_fish_caught_recyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        imageUrlList = prepareData(mFishFileList);

        setFishAdapter();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putString(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        outState.putInt(BUNDLE_STATE_LAST_SELECTED_IMAGE,mSelImage);
        outState.putString(BUNDLE_STATE_OTHER_CAUGHT_FISH,mOtherCaughtFish);

        String [] fishCountListArray = new String[mFishCountList.size()];
        fishCountListArray = mFishCountList.toArray(fishCountListArray);
        outState.putStringArray(BUNDLE_STATE_FISH_COUNT_LIST,fishCountListArray);

        String [] fishTahitianListArray = new String[mFishTahitianList.size()];
        fishTahitianListArray = mFishTahitianList.toArray(fishTahitianListArray);
        outState.putStringArray(BUNDLE_STATE_TAHITIAN_FISH_LIST,fishTahitianListArray);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fishcaught_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_data_input_fish_caught_back:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageClick(int position) {

        mSelImage = position;

        if(mSelImage == 0){
            FragmentManager fm = getSupportFragmentManager();
            FishPickerDialog alertDialog = FishPickerDialog.newInstance(mSelImage,mFishFamilyList.get(position),mFishTahitianList.get(position),(long) trackId,mCatchDestination);
            alertDialog.show(fm, "fragment_alert");
        }else{
            String catchN = mFishCountList.get(position);
            int tempCatchN = 1;
            try {
                tempCatchN = Integer.parseInt(catchN);
                tempCatchN++;
            }catch(final NumberFormatException e){
                e.getStackTrace();
            }

            if(tempCatchN>3){
                mFishCountList.set(mSelImage,"0");
            }else{
                mFishCountList.set(mSelImage,Integer.toString(tempCatchN));
            }

            setFishAdapter();
        }
    }

    private void populateFishCountList() {
        mOtherCaughtFish =  "";

        mCr = getContentResolver();
        String selectionIn = TrackContentProvider.Schema.COL_CATCH_DESTINATION + " = ?";
        String [] selectionArgsList = {mCatchDestination};
        mCursorFishCaught = mCr.query(TrackContentProvider.poissonsUri(trackId), null,
                selectionIn, selectionArgsList, TrackContentProvider.Schema.COL_ID + " asc");

        if(mCursorFishCaught.moveToFirst()){
            for(mCursorFishCaught.moveToFirst(); !mCursorFishCaught.isAfterLast(); mCursorFishCaught.moveToNext()) {

                String fishFamily = mCursorFishCaught.getString(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_FISH_FAMILY));
                String fishTahitian = mCursorFishCaught.getString(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_FISH_TAHITIAN));
                int catchN = mCursorFishCaught.getInt(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N));

                int fishCountListIndex = mFishTahitianList.indexOf(fishTahitian);
                if(fishCountListIndex==-1) {
                    mFishTahitianList.add(fishTahitian);
                    mFishCountList.add(Integer.toString(catchN));
                    String otherCaughtFish = fishTahitian + " = " + Integer.toString(catchN);
                    if (mOtherCaughtFish.equals("")) {
                        mOtherCaughtFish = otherCaughtFish;
                    } else {
                        mOtherCaughtFish += "\n" + otherCaughtFish;
                    }
                }else{
                    mFishCountList.set(fishCountListIndex, Integer.toString(catchN));
                }
            }
        }

        mCursorFishCaught.close();
    }

    private ArrayList prepareData(String [] fishFileList) {

        ArrayList imageUrlList = new ArrayList<>();

        for(int i = 0; i < fishFileList.length; i++) {
            File thisImage =null;
            try {
                thisImage = MapTileProvider.getFileFromAssets(fishFileList[i], getApplicationContext());
                ImageUrl imageUrl = new ImageUrl();
                imageUrl.setImageUrl(thisImage.getAbsolutePath());
                imageUrlList.add(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageUrlList;
    }

    private void setFishAdapter() {
        List<String> subArr = mFishCountList.subList(0,mFishFileList.length);
        String [] fishCountList = new String[subArr.size()];
        fishCountList = subArr.toArray(fishCountList);

        ImageFishAdapter imageFishAdapter = new ImageFishAdapter(getApplicationContext(), imageUrlList, fishCountList, this);
        recyclerView.setAdapter(imageFishAdapter);
    }

    public void setMyCaughtFish(String fishTahitian, int catchN, String catchType) {
        int checkIndex = mFishTahitianList.indexOf(fishTahitian);
        if(checkIndex ==-1){
            mFishCountList.add(Integer.toString(catchN)+" "+catchType);
            mFishTahitianList.add(fishTahitian);
        }else{
            mFishCountList.set(checkIndex,Integer.toString(catchN)+" "+catchType);
            setFishAdapter();
        }

        mOtherCaughtFish="";
        String otherCaughtFish="";
        for(int i = mFishFileList.length;i < mFishCountList.size();i++){
            otherCaughtFish = mFishTahitianList.get(i) + " = " + mFishCountList.get(i);
            if(i>mFishFileList.length) otherCaughtFish = "\n" + otherCaughtFish;
            mOtherCaughtFish+=otherCaughtFish;
        }

        mOtherFishIntro.setVisibility(View.VISIBLE);
        mOtherFishDetail.setText(mOtherCaughtFish);
    }
}
