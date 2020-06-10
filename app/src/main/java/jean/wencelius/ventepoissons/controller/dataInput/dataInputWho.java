package jean.wencelius.ventepoissons.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputWho extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    static dataInputWho whoAct;

    private static final String QUESTION_NUMBER = "Question 1/";

    private long trackId;

    private String mNewFisherAns;

    private String mOldFisher;
    private int mOldFisherInt;

    //Views
    private LinearLayout mPickOldFisher;
    private LinearLayout mInputNewFisher;

    private String mNewFisherName;
    private String mNewFisherResidence;
    private int mNewFisherResidenceInt;
    private String mNewFisherPhone;

    private EditText mNewFisherNameInput;
    private EditText mNewFisherPhoneInput;

    private ArrayList<String> fisherNames = new ArrayList<>();
    private String [] places;

    private boolean ansValid;
    private boolean oldFisherValid;
    private boolean newFisherValid;
    private boolean newFisherResidenceValid;

    private boolean showNext;

    private static final String NO_OLD_FISHER = "Choisi le pêcheur";

    private static final String BUNDLE_STATE_OLD_FISHER_INT = "oldFisherNameInt";
    private static final String BUNDLE_STATE_FISHER_NAME = "newFisherName";
    private static final String BUNDLE_STATE_FISHER_PHONE = "newFisherPhone";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_who);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        whoAct = this;

        mPickOldFisher = (LinearLayout) findViewById(R.id.activity_who_old_fisher_frame);
        mInputNewFisher = (LinearLayout) findViewById(R.id.activity_who_new_fisher_frame);

        mNewFisherNameInput = (EditText) findViewById(R.id.activity_data_input_who_input_new_fisher_name);
        mNewFisherPhoneInput = (EditText) findViewById(R.id.activity_data_input_who_input_new_fisher_phone);

        RadioButton mRadioNewFisher = (RadioButton) findViewById(R.id.activity_data_input_new_fisher_yes);
        RadioButton mRadioOldFisher = (RadioButton) findViewById(R.id.activity_data_input_new_fisher_no);

        Spinner mSelectOldFisher = (Spinner) findViewById(R.id.activity_data_who_select_old_fisher);
        Spinner mNewFisherResidenceInput = (Spinner) findViewById(R.id.activity_data_who_input_new_fisher_residence);

        Cursor fisherCursor = getContentResolver().query(TrackContentProvider.CONTENT_URI_FISHER,
                null,null,null,TrackContentProvider.Schema.COL_FISHER_NAME + " asc");

        fisherNames.clear();
        if(fisherCursor.getCount()>0){
            while(fisherCursor.moveToNext()){
                String tempNewFisher = fisherCursor.getString(fisherCursor.getColumnIndex(TrackContentProvider.Schema.COL_FISHER_NAME));
                if(!fisherNames.contains(tempNewFisher)){
                    fisherNames.add(tempNewFisher);
                }
            }
            Collections.sort(fisherNames, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });
        }
        fisherNames.add(0,NO_OLD_FISHER);

        fisherCursor.close();

        ArrayAdapter<String> fisherAdapter = new ArrayAdapter<String>(this,R.layout.spinner_custom_layout,fisherNames);
        // Specify the layout to use when the list of choices appears
        //fisherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSelectOldFisher.setAdapter(fisherAdapter);
        mSelectOldFisher.setOnItemSelectedListener(this);

        places = this.getResources().getStringArray(R.array.data_input_catch_sale_where);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.data_input_catch_sale_where, R.layout.spinner_custom_layout);
        // Specify the layout to use when the list of choices appears
        //whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mNewFisherResidenceInput.setAdapter(whereAdapter);
        mNewFisherResidenceInput.setOnItemSelectedListener(this);

        if(savedInstanceState != null){
            mNewFisherAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ANS);

            mOldFisherInt = savedInstanceState.getInt(BUNDLE_STATE_OLD_FISHER_INT);
            mOldFisher = fisherNames.get(mOldFisherInt);

            mNewFisherName = savedInstanceState.getString(BUNDLE_STATE_FISHER_NAME);
            mNewFisherResidenceInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_WHERE_INT);
            mNewFisherResidence = places[mNewFisherResidenceInt];
            mNewFisherPhone = savedInstanceState.getString(BUNDLE_STATE_FISHER_PHONE);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String newFisherAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_NEW_FISHER));
            String fisherName  = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_FISHER_NAME)); //TODO: Make sure in TBL_TRACK
            String fisherResidence = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_FISHER_RESIDENCE));
            String fisherPhone = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_FISHER_PHONE));

            mTrackCursor.close();

            if(newFisherAns!=null){
                mNewFisherAns = newFisherAns;
                if(mNewFisherAns.equals("true")){
                    mOldFisher = "NA";
                    mOldFisherInt=0;
                    mNewFisherName = fisherName;
                    mNewFisherResidence = fisherResidence;
                    mNewFisherResidenceInt = Arrays.asList(places).indexOf(mNewFisherResidence);
                    mNewFisherPhone = fisherPhone;
                }else{
                    mOldFisher = fisherName;
                    mOldFisherInt = fisherNames.indexOf(mOldFisher);
                    mNewFisherName = "";
                    mNewFisherResidence = places[0];
                    mNewFisherResidenceInt = 0;
                    mNewFisherPhone = "";
                }
            }else{
                mNewFisherAns = "NA";
                mOldFisher = NO_OLD_FISHER;
                mOldFisherInt = 0;
                mNewFisherName = "";
                mNewFisherResidence = places[0];
                mNewFisherResidenceInt = 0;
                mNewFisherPhone = "";
            }
        }

        mRadioNewFisher.setChecked(mNewFisherAns.equals("true"));
        mRadioOldFisher.setChecked(mNewFisherAns.equals("false"));

        mSelectOldFisher.setSelection(mOldFisherInt);
        mNewFisherResidenceInput.setSelection(mNewFisherResidenceInt);

        if(!mNewFisherName.equals("NA")){
            mNewFisherNameInput.setText(mNewFisherName);
            mNewFisherNameInput.setSelection(mNewFisherName.length());
        }
        if(!mNewFisherPhone.equals("NA")){
            mNewFisherPhoneInput.setText(mNewFisherPhone);
            mNewFisherPhoneInput.setSelection(mNewFisherPhone.length());
        }

        ansValid = !mNewFisherAns.equals("NA");
        oldFisherValid = mNewFisherAns.equals("true") || !mOldFisher.equals(NO_OLD_FISHER);
        newFisherValid = mNewFisherAns.equals("false") || !mNewFisherName.equals("");
        newFisherResidenceValid = mNewFisherAns.equals("false") || mNewFisherResidenceInt!=0;

        mPickOldFisher.setVisibility(View.INVISIBLE);
        mInputNewFisher.setVisibility(View.INVISIBLE);

        if(mNewFisherAns.equals("true")){
            mInputNewFisher.setVisibility(View.VISIBLE);
        }else if(mNewFisherAns.equals("false")){
            mPickOldFisher.setVisibility(View.VISIBLE);
        }

        mNewFisherNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newFisherValid = mNewFisherAns.equals("false") || s.toString().length()!=0;
                oldFisherValid = mNewFisherAns.equals("true");
                showNext = ansValid && oldFisherValid && newFisherValid && newFisherResidenceValid;
                invalidateOptionsMenu();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setTitle(QUESTION_NUMBER+recopemValues.TOT_NB_QUESTIONS);

        showNext = ansValid && oldFisherValid && newFisherValid && newFisherResidenceValid;
        invalidateOptionsMenu();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_new_fisher_yes:
                if (checked) {
                    mNewFisherAns="true";
                    mInputNewFisher.setVisibility(View.VISIBLE);
                    mPickOldFisher.setVisibility(View.INVISIBLE);
                    ansValid=true;
                }
                break;
            case R.id.activity_data_input_new_fisher_no:
                if (checked) {
                    mNewFisherAns="false";
                    mInputNewFisher.setVisibility(View.INVISIBLE);
                    mPickOldFisher.setVisibility(View.VISIBLE);
                    ansValid=true;
                }
                break;
        }
        newFisherValid = mNewFisherAns.equals("false") || !mNewFisherNameInput.getText().toString().equals("");
        newFisherResidenceValid = mNewFisherAns.equals("false") || !mNewFisherResidence.equals(places[0]);
        oldFisherValid = mNewFisherAns.equals("true") || !mOldFisher.equals(NO_OLD_FISHER);

        showNext = ansValid && oldFisherValid && newFisherValid && newFisherResidenceValid;
        invalidateOptionsMenu();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;

        if(spin.getId() == R.id.activity_data_who_select_old_fisher)
        {
            mOldFisher = fisherNames.get(position);
            mOldFisherInt=position;
            newFisherValid = true;
            newFisherResidenceValid=true;
            oldFisherValid = mNewFisherAns.equals("true") || !mOldFisher.equals(NO_OLD_FISHER);
        }else{
            mNewFisherResidence = places[position];
            mNewFisherResidenceInt=position;
            newFisherValid = mNewFisherAns.equals("false") || !mNewFisherNameInput.getText().toString().equals("");
            newFisherResidenceValid = mNewFisherAns.equals("false") || !mNewFisherResidence.equals(places[0]);
            oldFisherValid=true;
        }

        showNext = ansValid && oldFisherValid && newFisherValid && newFisherResidenceValid;
        invalidateOptionsMenu();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(recopemValues.BUNDLE_STATE_ANS,mNewFisherAns);
        outState.putInt(BUNDLE_STATE_OLD_FISHER_INT,mOldFisherInt);
        outState.putString(BUNDLE_STATE_FISHER_NAME,mNewFisherNameInput.getText().toString());
        outState.putInt(recopemValues.BUNDLE_STATE_WHERE_INT,mNewFisherResidenceInt);
        outState.putString(BUNDLE_STATE_FISHER_PHONE,mNewFisherPhoneInput.getText().toString());

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        super.onSaveInstanceState(outState);
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

                if(mNewFisherAns.equals("true")){
                    mNewFisherName = mNewFisherNameInput.getText().toString();
                    mNewFisherPhone = mNewFisherPhoneInput.getText().toString();

                    ContentValues newFisherValues = new ContentValues();
                    newFisherValues.put(TrackContentProvider.Schema.COL_FISHER_NAME,mNewFisherName);
                    newFisherValues.put(TrackContentProvider.Schema.COL_FISHER_RESIDENCE,mNewFisherResidence);
                    newFisherValues.put(TrackContentProvider.Schema.COL_FISHER_PHONE,mNewFisherPhone);
                    newFisherValues.put(TrackContentProvider.Schema.COL_TRACK_ID,trackId);
                    getContentResolver().insert(TrackContentProvider.CONTENT_URI_FISHER, newFisherValues);
                }

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues whoValues = new ContentValues();
                whoValues.put(TrackContentProvider.Schema.COL_NEW_FISHER,mNewFisherAns);
                String fisherName = "";
                if(mNewFisherAns.equals("false")){
                    fisherName = mOldFisher;
                }else{
                    fisherName = mNewFisherName;
                }
                whoValues.put(TrackContentProvider.Schema.COL_FISHER_NAME,fisherName);
                whoValues.put(TrackContentProvider.Schema.COL_FISHER_RESIDENCE,mNewFisherResidence);
                whoValues.put(TrackContentProvider.Schema.COL_FISHER_PHONE,mNewFisherPhone);

                getContentResolver().update(trackUri, whoValues, null, null);

                Intent NextIntent = new Intent(dataInputWho.this, dataInputWhen.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputWho getInstance(){
        return   whoAct;
    }
}
