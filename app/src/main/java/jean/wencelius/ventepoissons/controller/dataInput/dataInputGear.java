package jean.wencelius.ventepoissons.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.controller.TrackListActivity;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputGear extends AppCompatActivity {

    static dataInputGear gearAct;

    private static final String QUESTION_NUMBER = "Question 3/";

    private EditText mInputOtherDetail;
    private String mGear;
    private String mOtherDetail;

    private long trackId;
    private boolean mNewPicAdded;
    private boolean showNext;

    private static final String BUNDLE_STATE_GEAR = "gear";
    private static final String BUNDLE_STATE_OTHER_DETAIL = "otherDetail";

    private static final String EMPTY = "empty";
    private static final String AND = " & ";
    private static final String OTHER = "other";
    private static final String SPEAR = "spear";
    private static final String NET = "net";
    private static final String LINE = "line";
    private static final String INVERTEBRATE = "invertebrate";
    private static final String CAGE = "cage";
    private static final String HARPOON = "harpoon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_gear);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        gearAct = this;

        mInputOtherDetail = (EditText) findViewById(R.id.activity_data_input_gear_autre_detail);

        if(savedInstanceState != null){
            mGear = savedInstanceState.getString(BUNDLE_STATE_GEAR);
            mOtherDetail = savedInstanceState.getString(BUNDLE_STATE_OTHER_DETAIL);

            showNext = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);
        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK,trackId),null,null,null,null);
            mTrackCursor.moveToPosition(0);
            String gear = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_GEAR));
            String otherDetail = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_GEAR_OTHER_DETAILS));
            mTrackCursor.close();

            if(gear!=null){
                mGear = gear;
                checkResponses(mGear);
                mOtherDetail = otherDetail;
                if(mGear.contains(OTHER) && mOtherDetail.equals(""))
                    showNext = false;
                else
                    showNext = true;
            }else{
                mGear=EMPTY;
                mOtherDetail = "";
                showNext = false;
            }
        }

        if(mGear.contains(OTHER)){
            mInputOtherDetail.setVisibility(View.VISIBLE);
            mInputOtherDetail.setText(mOtherDetail);
            mInputOtherDetail.setSelection(mOtherDetail.length());
        }else{
            mInputOtherDetail.setVisibility(View.INVISIBLE);
        }

        mInputOtherDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showNext = s.toString().length()!=0;
                invalidateOptionsMenu();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        invalidateOptionsMenu();

        setTitle(QUESTION_NUMBER+recopemValues.TOT_NB_QUESTIONS);
    }

    private void checkResponses(String gear) {
        if(gear.contains(SPEAR)){
            CheckBox mSpearCb = (CheckBox) findViewById(R.id.activity_data_input_gear_fusil);
            mSpearCb.setChecked(true);
        }
        if(gear.contains(NET)){
            CheckBox mNetCb = (CheckBox) findViewById(R.id.activity_data_input_gear_filet);
            mNetCb.setChecked(true);
        }
        if(gear.contains(LINE)){
            CheckBox mLineCb = (CheckBox) findViewById(R.id.activity_data_input_gear_ligne);
            mLineCb.setChecked(true);
        }
        if(gear.contains(INVERTEBRATE)){
            CheckBox mInvCb = (CheckBox) findViewById(R.id.activity_data_input_gear_rama);
            mInvCb.setChecked(true);
        }
        if(gear.contains(CAGE)){
            CheckBox mCageCb = (CheckBox) findViewById(R.id.activity_data_input_gear_cage);
            mCageCb.setChecked(true);
        }
        if(gear.contains(HARPOON)){
            CheckBox mHarpCb = (CheckBox) findViewById(R.id.activity_data_input_gear_harpon);
            mHarpCb.setChecked(true);
        }
        if(gear.contains(OTHER)){
            CheckBox mOtherCb = (CheckBox) findViewById(R.id.activity_data_input_gear_autre);
            mOtherCb.setChecked(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_GEAR,mGear);
        outState.putString(BUNDLE_STATE_OTHER_DETAIL,mInputOtherDetail.getText().toString());
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,showNext);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);

        super.onSaveInstanceState(outState);
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.activity_data_input_gear_fusil:
                if (checked){
                    if(!mGear.equals(EMPTY))
                        mGear+=AND + SPEAR;
                    else
                        mGear=SPEAR;
                }else{
                    if(mGear.contains(SPEAR)) removeString(SPEAR);
                }
                break;
            case R.id.activity_data_input_gear_filet:
                if (checked){
                    if(!mGear.equals(EMPTY))
                        mGear+=AND + NET;
                    else
                        mGear=NET;
                }else{
                    if(mGear.contains(NET)) removeString(NET);
                }

                break;
            case R.id.activity_data_input_gear_ligne:
                if (checked){
                    if(!mGear.equals(EMPTY))
                        mGear+=AND + LINE;
                    else
                        mGear=LINE;
                }else{
                    if(mGear.contains(LINE)) removeString(LINE);
                }
                break;
            case R.id.activity_data_input_gear_rama:
                if (checked){
                    if(!mGear.equals(EMPTY))
                        mGear+=AND + INVERTEBRATE;
                    else
                        mGear=INVERTEBRATE;
                }else{
                    if(mGear.contains(INVERTEBRATE)) removeString(INVERTEBRATE);
                }
                break;
            case R.id.activity_data_input_gear_cage:
                if (checked){
                    if(!mGear.equals(EMPTY))
                        mGear+=AND + CAGE;
                    else
                        mGear=CAGE;
                }else{
                    if(mGear.contains(CAGE)) removeString(CAGE);
                }
                break;

            case R.id.activity_data_input_gear_harpon:
                if (checked){
                    if(!mGear.equals(EMPTY))
                        mGear+=AND + HARPOON;
                    else
                        mGear=HARPOON;
                }else{
                    if(mGear.contains(HARPOON)) removeString(HARPOON);
                }
                break;

            case R.id.activity_data_input_gear_autre:
                if (checked){
                    mInputOtherDetail.setVisibility(View.VISIBLE);
                    if(!mGear.equals(EMPTY))
                        mGear+=AND + OTHER;
                    else
                        mGear=OTHER;
                }else{
                    mInputOtherDetail.getText().clear();
                    mInputOtherDetail.setVisibility(View.INVISIBLE);
                    if(mGear.contains(OTHER)) removeString(OTHER);
                }
                break;
        }

        if(mGear.contains(OTHER)){
            showNext = !mInputOtherDetail.getText().toString().equals("");
        }else{
            showNext = !mGear.equals(EMPTY);
        }
        invalidateOptionsMenu();
    }

    public void removeString(String string){
        if(mGear.contains(AND+string)){
            mGear=mGear.replace(AND+string,"");
        }else if(mGear.contains(string+AND)){
            mGear=mGear.replace(string + AND,"");
        }else{
            mGear=EMPTY;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.datainput_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_data_input_menu_next).setVisible(showNext);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.activity_data_input_menu_next:
                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues gearValues = new ContentValues();
                gearValues.put(TrackContentProvider.Schema.COL_GEAR,mGear);

                if(mGear.contains(OTHER)){
                    mOtherDetail = mInputOtherDetail.getText().toString();
                    gearValues.put(TrackContentProvider.Schema.COL_GEAR_OTHER_DETAILS,mOtherDetail);
                }

                getContentResolver().update(trackUri, gearValues, null, null);

                Intent NextIntent = new Intent(dataInputGear.this, dataInputBoat.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public static dataInputGear getInstance(){
        return   gearAct;
    }
}