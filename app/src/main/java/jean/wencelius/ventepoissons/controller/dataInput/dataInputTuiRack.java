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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputTuiRack extends AppCompatActivity{

    static dataInputTuiRack tuiRackAct;

    private static final String QUESTION_NUMBER = "Question 6/";

    private String mTuiRackAns;
    private String mTuiRackDetails;

    private long trackId;

    private static final String BUNDLE_STATE_TUI_RACK_ANS = "tuiRackAns";
    private static final String BUNDLE_STATE_TUI_RACK_DETAILS = "tuiRackDetails";

    //Views
    private EditText mTuiRackInputDetails;
    private LinearLayout mTuiRackDetailFrame;

    private boolean ansValid;
    private boolean ansDetailValid;
    private boolean showNext;

    private static final String EMPTY = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_tui_rack);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        tuiRackAct = this;

        mTuiRackDetailFrame = (LinearLayout) findViewById(R.id.activity_data_input_tui_rack_detail_frame);
        mTuiRackInputDetails = (EditText) findViewById(R.id.activity_data_input_tui_rack_detail);

        if (savedInstanceState != null) {
            mTuiRackAns = savedInstanceState.getString(BUNDLE_STATE_TUI_RACK_ANS);
            mTuiRackDetails = savedInstanceState.getString(BUNDLE_STATE_TUI_RACK_DETAILS);
            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
        } else {
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);
            String tuiRackAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_TUI_RACK_SEVERAL));
            String tuiRackDetails = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_TUI_RACK_DETAILS));
            mTrackCursor.close();

            if (tuiRackAns != null) {
                mTuiRackAns = tuiRackAns;
                mTuiRackDetails = tuiRackDetails;
            }else{
                mTuiRackAns = EMPTY;
                mTuiRackDetails = "";
            }
        }

        RadioButton tuiRackMultipleY = (RadioButton) findViewById(R.id.activity_data_input_tui_rack_question_yes);
        RadioButton tuiRackMultipleN = (RadioButton) findViewById(R.id.activity_data_input_tui_rack_question_no);

        mTuiRackInputDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ansDetailValid = s.toString().length()!=0;
                showNext = ansValid && ansDetailValid;
                invalidateOptionsMenu();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if(mTuiRackAns.equals(EMPTY)){
            ansValid = false;
            ansDetailValid = false;
            mTuiRackDetailFrame.setVisibility(View.INVISIBLE);
        }else{
            tuiRackMultipleY.setChecked(mTuiRackAns.equals("true"));
            tuiRackMultipleN.setChecked(mTuiRackAns.equals("false"));
            ansValid = true;

            if(mTuiRackAns.equals("true")){
                mTuiRackInputDetails.setText(mTuiRackDetails);
                mTuiRackInputDetails.setSelection(mTuiRackDetails.length());
                mTuiRackDetailFrame.setVisibility(View.VISIBLE);
                ansDetailValid = !mTuiRackDetails.equals("");
            }else{
                mTuiRackDetailFrame.setVisibility(View.INVISIBLE);
                ansDetailValid = true;
            }
        }

        showNext = ansValid && ansDetailValid;
        invalidateOptionsMenu();

        setTitle(QUESTION_NUMBER + recopemValues.TOT_NB_QUESTIONS);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_TUI_RACK_ANS, mTuiRackAns);
        outState.putString(BUNDLE_STATE_TUI_RACK_DETAILS, mTuiRackDetails);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID, trackId);
        super.onSaveInstanceState(outState);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_tui_rack_question_yes:
                if (checked) {
                    mTuiRackAns = "true";
                    mTuiRackDetailFrame.setVisibility(View.VISIBLE);
                    ansDetailValid = !mTuiRackInputDetails.getText().toString().equals("");
                }
                break;
            case R.id.activity_data_input_tui_rack_question_no:
                if (checked) {
                    mTuiRackAns = "false";
                    mTuiRackDetailFrame.setVisibility(View.INVISIBLE);
                    mTuiRackDetails = "";
                    ansDetailValid = true;
                }
                break;
        }
        ansValid=true;
        showNext=ansValid && ansDetailValid;
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

                if(mTuiRackAns.equals("true")) mTuiRackDetails = mTuiRackInputDetails.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues tuiRackValues = new ContentValues();
                tuiRackValues.put(TrackContentProvider.Schema.COL_TUI_RACK_SEVERAL, mTuiRackAns);
                tuiRackValues.put(TrackContentProvider.Schema.COL_TUI_RACK_DETAILS, mTuiRackDetails);

                getContentResolver().update(trackUri, tuiRackValues, null, null);

                Intent NextIntent = new Intent(dataInputTuiRack.this, dataInputCatchSale.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputTuiRack getInstance() {
        return tuiRackAct;
    }
}
