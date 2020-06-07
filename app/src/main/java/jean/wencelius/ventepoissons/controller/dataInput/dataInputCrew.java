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
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputCrew extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    static dataInputCrew crewAct;

    private static final String QUESTION_NUMBER = "Question 5/";

    private String mCrewAlone;
    private int mCrewN;

    private long trackId;

    private static final String BUNDLE_STATE_CREW_ANS = "crewAns";
    private static final String BUNDLE_STATE_CREW_N = "crewN";

    //Views
    private TextView mCrewQuestionN;
    private NumberPicker mCrewInputN;

    private boolean showNext;

    private static final String EMPTY = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_crew);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        crewAct = this;

        mCrewQuestionN = (TextView) findViewById(R.id.activity_data_input_crew_question_N);

        mCrewInputN = (NumberPicker) findViewById(R.id.activity_data_input_crew_input_N);
        mCrewInputN.setMinValue(0);
        mCrewInputN.setMaxValue(10);
        mCrewInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCrewInputN.setOnValueChangedListener(this);

        if (savedInstanceState != null) {
            mCrewAlone = savedInstanceState.getString(BUNDLE_STATE_CREW_ANS);
            mCrewN = savedInstanceState.getInt(BUNDLE_STATE_CREW_N);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
        } else {
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String crewAlone = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CREW_ALONE));
            int crewN = mTrackCursor.getInt(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CREW_N));
            mTrackCursor.close();

            if (crewAlone != null) {
                mCrewAlone = crewAlone;
                mCrewN = crewN;
            } else {
                mCrewAlone = EMPTY;
                mCrewN = 0;
            }
        }

        RadioButton crewInputAloneY = (RadioButton) findViewById(R.id.activity_data_input_crew_question_yes);
        RadioButton crewInputAloneN = (RadioButton) findViewById(R.id.activity_data_input_crew_question_no);

        crewInputAloneN.setChecked(mCrewAlone.equals("false"));
        crewInputAloneY.setChecked(mCrewAlone.equals("true"));

        if (mCrewAlone.equals("false")) {


            mCrewQuestionN.setVisibility(View.VISIBLE);
            mCrewInputN.setVisibility(View.VISIBLE);

            if (mCrewN != 0) {
                mCrewInputN.setValue(mCrewN);
            }
        } else {
            mCrewQuestionN.setVisibility(View.INVISIBLE);
            mCrewInputN.setVisibility(View.INVISIBLE);
        }

        showNext = false;
        if (mCrewAlone.equals("true")) {
            showNext = true;
        } else if (mCrewAlone.equals("false")) {
            if (mCrewN != 0) showNext = true;
        }
        invalidateOptionsMenu();

        setTitle(QUESTION_NUMBER + recopemValues.TOT_NB_QUESTIONS);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_CREW_ANS, mCrewAlone);
        outState.putInt(BUNDLE_STATE_CREW_N, mCrewN);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID, trackId);
        super.onSaveInstanceState(outState);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_crew_question_yes:
                if (checked) {
                    mCrewAlone = "true";
                    showNext = true;
                    mCrewQuestionN.setVisibility(View.INVISIBLE);
                    mCrewInputN.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.activity_data_input_crew_question_no:
                if (checked) {
                    mCrewAlone = "false";
                    showNext = mCrewN != 0;

                    mCrewQuestionN.setVisibility(View.VISIBLE);
                    mCrewInputN.setVisibility(View.VISIBLE);
                }
                break;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        mCrewN = newVal;
        showNext = newVal != 0;
        invalidateOptionsMenu();
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

                ContentValues crewValues = new ContentValues();
                crewValues.put(TrackContentProvider.Schema.COL_CREW_ALONE, mCrewAlone);
                crewValues.put(TrackContentProvider.Schema.COL_CREW_N, mCrewN);

                getContentResolver().update(trackUri, crewValues, null, null);

                Intent NextIntent = new Intent(dataInputCrew.this, dataInputCatchSale.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputCrew getInstance() {
        return crewAct;
    }
}