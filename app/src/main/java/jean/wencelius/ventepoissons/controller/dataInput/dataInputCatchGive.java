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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.model.AppPreferences;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputCatchGive extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    static dataInputCatchGive giveAct;

    private static final String QUESTION_NUMBER = "Question 8/";

    private String mCatchGiveAns;
    private int mCatchGiveN;
    private String mCatchGiveType;
    private int mCatchGiveTypeInt;
    private String mCatchGiveWhere;
    private int mCatchGiveWhereInt;
    private String mCatchGiveDetails;
    private String mCatchGivePicAns;

    private String mCatchOrderPicAns;

    private String mCatchDestination;
    private long trackId;

    //Views
    private RelativeLayout mCatchGiveQuantityFrame;
    private LinearLayout mCatchGivePicFrame;

    private EditText mCatchGiveInputDetails;

    private String [] places;
    private String [] type;

    private boolean nValid;
    private boolean typeValid;
    private boolean whereValid;
    private boolean picValid;
    private boolean showNext;

    private static final String EMPTY = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_catch_give);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        giveAct = this;

        mCatchGiveQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_give_quantity_frame);
        mCatchGivePicFrame = (LinearLayout) findViewById(R.id.activity_catch_give_pic_frame);
        mCatchGiveInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_give_input_details);

        mCatchDestination = "give";

        TextView mPicGiveQuestion = (TextView) findViewById(R.id.activity_data_input_catch_give_question_pic);

        RadioButton mCatchGiveInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_yes);
        RadioButton mCatchGiveInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_no);
        RadioButton mCatchGiveInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_pic_yes);
        RadioButton mCatchGiveInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_pic_no);

        NumberPicker mCatchGiveInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_give_input_N);
        NumberPicker mCatchGiveInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_give_input_type);
        Spinner mCatchGiveInputWhere = (Spinner) findViewById(R.id.activity_data_input_catch_give_input_where);

        mCatchGiveInputN.setMinValue(0);
        mCatchGiveInputN.setMaxValue(100);
        mCatchGiveInputN.setOnValueChangedListener(new dataInputCatchGive.nPicker());
        mCatchGiveInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchGiveInputType.setMinValue(0);
        mCatchGiveInputType.setMaxValue(type.length-1);
        mCatchGiveInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mCatchGiveInputType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCatchGiveInputType.setDisplayedValues(type);
        mCatchGiveInputType.setOnValueChangedListener(new dataInputCatchGive.typePicker());

        places = this.getResources().getStringArray(R.array.data_input_catch_sale_where);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.data_input_catch_sale_where, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCatchGiveInputWhere.setAdapter(whereAdapter);
        mCatchGiveInputWhere.setOnItemSelectedListener(this);

        if(savedInstanceState != null){
            mCatchGiveAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ANS);
            mCatchGiveN = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_CATCH_N);
            mCatchGiveTypeInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_TYPE_INT);
            mCatchGiveType = type[mCatchGiveTypeInt];
            mCatchGiveWhereInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_WHERE_INT);
            mCatchGiveWhere = places[mCatchGiveWhereInt];
            mCatchGivePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_PIC_ANS);
            mCatchGiveDetails = savedInstanceState.getString(recopemValues.BUNDLE_STATE_DETAILS);

            mCatchOrderPicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            mCatchOrderPicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String catchGiveAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE));
            int catchGiveN = mTrackCursor.getInt(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_N));
            String catchGiveType = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_TYPE));
            String catchGiveWhere = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_WHERE));
            String catchGiveDetails = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_DETAILS));
            String catchGivePicAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_PIC));

            mTrackCursor.close();
            if(catchGiveAns!=null){
                mCatchGiveAns = catchGiveAns;
                mCatchGiveN = catchGiveN;
                mCatchGiveType = catchGiveType;
                if (catchGiveType!=null){
                    mCatchGiveTypeInt = Arrays.asList(type).indexOf(catchGiveType);
                }else{
                    mCatchGiveTypeInt = 0;
                }
                mCatchGiveWhere = catchGiveWhere;
                if (catchGiveWhere!=null){
                    mCatchGiveWhereInt = Arrays.asList(places).indexOf(catchGiveWhere);
                }else{
                    mCatchGiveWhereInt = 0;
                }
                mCatchGivePicAns = catchGivePicAns;
                mCatchGiveDetails = catchGiveDetails;
            }else {
                mCatchGiveAns = EMPTY;
                mCatchGiveN = 0;
                mCatchGiveType = type[0];
                mCatchGiveTypeInt = 0;
                mCatchGivePicAns = "NA";
                mCatchGiveDetails = "NA";
                mCatchGiveWhere = places[0];
                mCatchGiveWhereInt = 0;
            }
        }

        mCatchGiveInputAnsY.setChecked(mCatchGiveAns.equals("true"));
        mCatchGiveInputAnsN.setChecked(mCatchGiveAns.equals("false"));
        mCatchGiveInputPicAnsY.setChecked(mCatchGivePicAns.equals("true"));
        mCatchGiveInputPicAnsN.setChecked(mCatchGivePicAns.equals("false"));

        mCatchGiveInputN.setValue(mCatchGiveN);
        mCatchGiveInputType.setValue(mCatchGiveTypeInt);
        mCatchGiveInputWhere.setSelection(mCatchGiveWhereInt);

        if(!mCatchGiveDetails.equals("NA")){
            mCatchGiveInputDetails.setText(mCatchGiveDetails);
            mCatchGiveInputDetails.setSelection(mCatchGiveDetails.length());
        }

        if(mCatchOrderPicAns.equals("true")) mPicGiveQuestion.setText(R.string.data_input_catch_give_question_pic_if_sale_pic);

        nValid = mCatchGiveN!=0;
        typeValid = mCatchGiveTypeInt!=0;
        whereValid = mCatchGiveWhereInt!=0;
        picValid = mCatchGivePicAns.equals("true") || mCatchGivePicAns.equals("false");

        showNext = mCatchGiveAns.equals("false") || (nValid && typeValid && whereValid && picValid);
        invalidateOptionsMenu();

        if(mCatchGiveAns.equals("true")){
            mCatchGiveQuantityFrame.setVisibility(View.VISIBLE);
            mCatchGivePicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchGiveQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchGivePicFrame.setVisibility(View.INVISIBLE);
        }

        setTitle(QUESTION_NUMBER + recopemValues.TOT_NB_QUESTIONS);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_give_question_no:
                if (checked) {
                    mCatchGiveAns="false";
                    showNext = true;
                    mCatchGiveQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchGivePicFrame.setVisibility(View.INVISIBLE);
                    mCatchGiveN = 0;
                    mCatchGiveType = type[0];
                    mCatchGiveTypeInt = 0;
                    mCatchGiveWhere = places[0];
                    mCatchGiveWhereInt = 0;
                    mCatchGivePicAns = "false";
                }
                break;
            case R.id.activity_data_input_catch_give_question_yes:
                if (checked) {
                    mCatchGiveAns="true";
                    showNext = false;
                    mCatchGiveQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchGivePicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_give_question_pic_no:
                if (checked) {
                    mCatchGivePicAns = "false";
                    picValid=true;
                    showNext = mCatchGiveAns.equals("false") || (nValid && typeValid && whereValid && picValid);
                    LaunchFishCaughtIntent();
                }
                break;
            case R.id.activity_data_input_catch_give_question_pic_yes:
                if (checked) {
                    mCatchGivePicAns = "true";
                    picValid=true;
                    showNext = mCatchGiveAns.equals("false") || (nValid && typeValid && whereValid && picValid);
                    LaunchFishCaughtIntent();
                }
                break;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;
        mCatchGiveWhere = places[position];
        mCatchGiveWhereInt=position;
        whereValid = !mCatchGiveWhere.equals(places[0]);
        showNext = mCatchGiveAns.equals("false") || (nValid && typeValid && whereValid && picValid);
        invalidateOptionsMenu();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        showNext = false;
        invalidateOptionsMenu();
    }

    class nPicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchGiveN = newVal;
            nValid = mCatchGiveN!=0;
            showNext = mCatchGiveAns.equals("false") || (nValid && typeValid && whereValid && picValid);
            invalidateOptionsMenu();
        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchGiveType = type[newVal];
            mCatchGiveTypeInt = newVal;
            typeValid = !mCatchGiveType.equals(type[0]);
            showNext = mCatchGiveAns.equals("false") || (nValid && typeValid && whereValid && picValid);
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(recopemValues.BUNDLE_STATE_ANS,mCatchGiveAns);
        outState.putInt(recopemValues.BUNDLE_STATE_CATCH_N,mCatchGiveN);
        outState.putInt(recopemValues.BUNDLE_STATE_TYPE_INT,mCatchGiveTypeInt);
        outState.putInt(recopemValues.BUNDLE_STATE_WHERE_INT,mCatchGiveWhereInt);
        outState.putString(recopemValues.BUNDLE_STATE_DETAILS,mCatchGiveInputDetails.getText().toString());
        outState.putString(recopemValues.BUNDLE_STATE_PIC_ANS,mCatchGivePicAns);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);

        outState.putString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
        super.onSaveInstanceState(outState);
    }

    private void LaunchFishCaughtIntent() {
        Intent fishCaughtIntent = new Intent(dataInputCatchGive.this, dataInputFishCaught.class);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
        fishCaughtIntent.putExtra(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        startActivity(fishCaughtIntent);
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
                mCatchGiveDetails = mCatchGiveInputDetails.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchGiveValues = new ContentValues();
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE,mCatchGiveAns);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_N,mCatchGiveN);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_TYPE,mCatchGiveType);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_WHERE,mCatchGiveWhere);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_DETAILS,mCatchGiveDetails);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_PIC,mCatchGivePicAns);

                getContentResolver().update(trackUri, catchGiveValues, null, null);

                Intent NextIntent = new Intent(dataInputCatchGive.this, dataInputCatchCons.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);

                NextIntent.putExtra(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS,mCatchGivePicAns);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputCatchGive getInstance(){
        return   giveAct;
    }
}
