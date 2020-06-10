package jean.wencelius.ventepoissons.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputWhen extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    static dataInputWhen whenAct;

    private static final String QUESTION_NUMBER = "Question 2/";

    private String mWhen;
    private String mWhere;
    private int mWhereInt;
    private String mHabitats;

    private long trackId;

    private String [] places;

    private static final String BUNDLE_STATE_WHEN_ANS = "whenAns";
    private static final String BUNDLE_STATE_WHEN_WHERE_INT_ANS = "whereAns";
    private static final String BUNDLE_STATE_WHEN_HABITATS_ANS = "habitatsAns";

    private boolean whenValid;
    private boolean whereValid;
    private boolean habitatsValid;

    private boolean showNext;

    private static final String YESTERDAY_ANSWER = "yesterdayDay";
    private static final String LASTNIGHT_ANSWER ="lastNight";

    private static final String EMPTY = "emtpy";
    private static final String AND = " & ";
    private static final String FORE_REEF = "fore_reef";
    private static final String PASS = "pass";
    private static final String LAGOON = "lagoon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_when);

        whenAct = this;

        Spinner mWhereInput = (Spinner) findViewById(R.id.activity_data_when_where_input);

        places = this.getResources().getStringArray(R.array.data_input_catch_sale_where);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.data_input_catch_sale_where, R.layout.spinner_custom_layout);

        mWhereInput.setAdapter(whereAdapter);
        mWhereInput.setOnItemSelectedListener(this);


        if(savedInstanceState!=null){
            mWhen = savedInstanceState.getString(BUNDLE_STATE_WHEN_ANS);
            mWhereInt = savedInstanceState.getInt(BUNDLE_STATE_WHEN_WHERE_INT_ANS);
            mWhere = places[mWhereInt];
            mHabitats = savedInstanceState.getString(BUNDLE_STATE_WHEN_HABITATS_ANS);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String when = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_ROADSIDE_WHEN));
            String where = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_ROADSIDE_WHEN_WHERE));
            String habitats = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_ROADSIDE_HABITATS));
            mTrackCursor.close();

            if (when != null) {
                mWhen = when;
                mWhere = where;
                mWhereInt = Arrays.asList(places).indexOf(mWhere);
                mHabitats = habitats;
            } else {
                mWhen = EMPTY;
                mWhereInt = 0;
                mWhere = places[mWhereInt];
                mHabitats = EMPTY;
            }
        }

        RadioButton whenInputYesterday = (RadioButton) findViewById(R.id.activity_data_input_when_question_yesterday_day);
        RadioButton whenInputLastNight = (RadioButton) findViewById(R.id.activity_data_input_when_question_last_night);

        whenValid = false;
        if(!mWhen.equals(EMPTY)){
            whenInputYesterday.setChecked(mWhen.equals(YESTERDAY_ANSWER));
            whenInputLastNight.setChecked(mWhen.equals(LASTNIGHT_ANSWER));
            whenValid = true;
        }

        mWhereInput.setSelection(mWhereInt);

        habitatsValid=false;
        if(!mHabitats.equals(EMPTY)){
            checkResponses(mHabitats);
            habitatsValid = true;
        }
        whereValid = mWhereInt!=0;

        showNext = whenValid && whereValid && habitatsValid;
        invalidateOptionsMenu();

        setTitle(QUESTION_NUMBER+recopemValues.TOT_NB_QUESTIONS);
    }

    private void checkResponses(String habitats) {
        if(habitats.contains(FORE_REEF)){
            CheckBox mForeReef = (CheckBox) findViewById(R.id.activity_data_input_when_habitat_fore_reef);
            mForeReef.setChecked(true);
        }
        if(habitats.contains(PASS)){
            CheckBox mPass = (CheckBox) findViewById(R.id.activity_data_input_when_habitat_pass);
            mPass.setChecked(true);
        }
        if(habitats.contains(LAGOON)){
            CheckBox mBackReef = (CheckBox) findViewById(R.id.activity_data_input_when_habitat_lagoon);
            mBackReef.setChecked(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_WHEN_ANS,mWhen);
        outState.putInt(BUNDLE_STATE_WHEN_WHERE_INT_ANS,mWhereInt);
        outState.putString(BUNDLE_STATE_WHEN_HABITATS_ANS,mHabitats);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        super.onSaveInstanceState(outState);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_when_question_last_night:
                if (checked) {
                    mWhen = LASTNIGHT_ANSWER;
                }
                break;
            case R.id.activity_data_input_when_question_yesterday_day:
                if (checked) {
                    mWhen = YESTERDAY_ANSWER;
                }
                break;
        }
        whenValid = true;
        showNext = whenValid && whereValid && habitatsValid;
        invalidateOptionsMenu();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mWhereInt = position;
        mWhere = places[position];

        whereValid = position!=0;
        showNext = whenValid && whereValid & habitatsValid;
        invalidateOptionsMenu();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
       showNext = false;
        invalidateOptionsMenu();
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.activity_data_input_when_habitat_fore_reef:
                if (checked){
                    if(!mHabitats.equals(EMPTY))
                        mHabitats+= AND + FORE_REEF;
                    else
                        mHabitats = FORE_REEF;
                }else{
                    if(mHabitats.contains(FORE_REEF)) removeString(FORE_REEF);
                }
                break;
            case R.id.activity_data_input_when_habitat_pass:
                if (checked){
                    if(!mHabitats.equals(EMPTY))
                        mHabitats+=AND + PASS;
                    else
                        mHabitats=PASS;
                }else{
                    if(mHabitats.contains(PASS)) removeString(PASS);
                }

                break;
            case R.id.activity_data_input_when_habitat_lagoon:
                if (checked){
                    if(!mHabitats.equals(EMPTY))
                        mHabitats+=AND + LAGOON;
                    else
                        mHabitats=LAGOON;
                }else{
                    if(mHabitats.contains(LAGOON)) removeString(LAGOON);
                }
                break;
        }

        habitatsValid = !mHabitats.equals(EMPTY);
        showNext = whenValid && whereValid && habitatsValid;
        invalidateOptionsMenu();
    }

    public void removeString(String string){
        if(mHabitats.contains(AND + string)){
            mHabitats=mHabitats.replace(AND + string,"");
        }else if(mHabitats.contains(string + AND)){
            mHabitats=mHabitats.replace(string + AND,"");
        }else{
            mHabitats=EMPTY;
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

                ContentValues whenValues = new ContentValues();
                whenValues.put(TrackContentProvider.Schema.COL_ROADSIDE_WHEN,mWhen);
                whenValues.put(TrackContentProvider.Schema.COL_ROADSIDE_WHEN_WHERE,mWhere);
                whenValues.put(TrackContentProvider.Schema.COL_ROADSIDE_HABITATS,mHabitats);

                getContentResolver().update(trackUri, whenValues, null, null);

                Intent NextIntent = new Intent(dataInputWhen.this, dataInputGear.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputWhen getInstance(){
        return   whenAct;
    }
}
