package jean.wencelius.ventepoissons.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.controller.TrackDetailActivity;
import jean.wencelius.ventepoissons.controller.TrackListActivity;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.model.AppPreferences;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputCatchCons extends AppCompatActivity{

    private static final String QUESTION_NUMBER = "Question 9/";

    private String mCatchConsAns;
    private int mCatchConsN;
    private String mCatchConsType;
    private int mCatchConsTypeInt;
    private String mCatchConsDetails;
    private String mCatchConsPicAns;

    private String mCatchOrderPicAns;
    private String mCatchGivePicAns;

    private String mCatchDestination;
    private long trackId;

    //Views
    private RelativeLayout mCatchConsQuantityFrame;
    private LinearLayout mCatchConsPicFrame;

    private EditText mCatchConsInputDetails;

    private String [] type;

    private boolean nValid;
    private boolean typeValid;
    private boolean picValid;
    private boolean showNext;

    private static final String EMPTY = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_catch_cons);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView mPicConsQuestion = (TextView) findViewById(R.id.activity_data_input_catch_cons_question_pic);

        mCatchConsQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_cons_quantity_frame);
        mCatchConsPicFrame = (LinearLayout) findViewById(R.id.activity_catch_cons_pic_frame);

        RadioButton mCatchConsInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_yes);
        RadioButton mCatchConsInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_no);
        RadioButton mCatchConsInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_pic_yes);
        RadioButton mCatchConsInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_pic_no);

        NumberPicker mCatchConsInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_cons_input_N);
        NumberPicker mCatchConsInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_cons_input_type);
        mCatchConsInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_cons_input_details);

        mCatchDestination = "cons";

        mCatchConsInputN.setMinValue(0);
        mCatchConsInputN.setMaxValue(100);
        mCatchConsInputN.setOnValueChangedListener(new dataInputCatchCons.nPicker());
        mCatchConsInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchConsInputType.setMinValue(0);
        mCatchConsInputType.setMaxValue(type.length-1);
        mCatchConsInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mCatchConsInputType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCatchConsInputType.setDisplayedValues(type);
        mCatchConsInputType.setOnValueChangedListener(new dataInputCatchCons.typePicker());

        if(savedInstanceState != null){
            mCatchConsAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ANS);
            mCatchConsN = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_CATCH_N);
            mCatchConsTypeInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_TYPE_INT);
            mCatchConsType = type[mCatchConsTypeInt];
            mCatchConsPicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_PIC_ANS);
            mCatchConsDetails = savedInstanceState.getString(recopemValues.BUNDLE_STATE_DETAILS);

            mCatchOrderPicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);
            mCatchGivePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            mCatchOrderPicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);
            mCatchGivePicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String catchConsAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS));
            int catchConsN = mTrackCursor.getInt(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_N));
            String catchConsType = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_TYPE));
            String catchConsDetails = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_DETAILS));
            String catchConsPicAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_PIC));

            mTrackCursor.close();
            if(catchConsAns!=null){
                mCatchConsAns = catchConsAns;
                mCatchConsN = catchConsN;
                mCatchConsType = catchConsType;
                if (catchConsType!=null){
                    mCatchConsTypeInt = Arrays.asList(type).indexOf(catchConsType);
                }else{
                    mCatchConsTypeInt = 0;
                }
                mCatchConsPicAns = catchConsPicAns;
                mCatchConsDetails = catchConsDetails;
            }else {
                mCatchConsAns = EMPTY;
                mCatchConsN = 0;
                mCatchConsType = type[0];
                mCatchConsTypeInt = 0;
                mCatchConsPicAns = "NA";
                mCatchConsDetails = "NA";
            }
        }

        mCatchConsInputAnsY.setChecked(mCatchConsAns.equals("true"));
        mCatchConsInputAnsN.setChecked(mCatchConsAns.equals("false"));
        mCatchConsInputPicAnsY.setChecked(mCatchConsPicAns.equals("true"));
        mCatchConsInputPicAnsN.setChecked(mCatchConsPicAns.equals("false"));

        mCatchConsInputN.setValue(mCatchConsN);
        mCatchConsInputType.setValue(mCatchConsTypeInt);

        if(!mCatchConsDetails.equals("NA")){
            mCatchConsInputDetails.setText(mCatchConsDetails);
            mCatchConsInputDetails.setSelection(mCatchConsDetails.length());
        }

        nValid = mCatchConsN!=0;
        typeValid = mCatchConsTypeInt!=0;
        picValid = mCatchConsPicAns.equals("true") || mCatchConsPicAns.equals("false");

        showNext = mCatchConsAns.equals("false") ||(nValid && typeValid && picValid);
        invalidateOptionsMenu();

        if(mCatchConsAns.equals("true")){
            mCatchConsQuantityFrame.setVisibility(View.VISIBLE);
            mCatchConsPicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchConsQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchConsPicFrame.setVisibility(View.INVISIBLE);
        }

        if(mCatchOrderPicAns.equals("true") || mCatchGivePicAns.equals("true")) mPicConsQuestion.setText(R.string.data_input_catch_cons_question_pic_if_sale_pic);

        setTitle(QUESTION_NUMBER + recopemValues.TOT_NB_QUESTIONS);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_cons_question_no:
                if (checked) {
                    mCatchConsAns="false";
                    showNext = true;
                    mCatchConsQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchConsPicFrame.setVisibility(View.INVISIBLE);
                    mCatchConsN = 0;
                    mCatchConsType = type[0];
                    mCatchConsTypeInt = 0;
                    mCatchConsPicAns = "false";
                }
                break;
            case R.id.activity_data_input_catch_cons_question_yes:
                if (checked) {
                    mCatchConsAns="true";
                    showNext = false;
                    mCatchConsQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchConsPicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_cons_question_pic_no:
                if (checked) {
                    mCatchConsPicAns = "false";
                    picValid=true;
                    showNext = mCatchConsAns.equals("false") ||(nValid && typeValid && picValid);
                    LaunchFishCaughtIntent();
                }
                break;
            case R.id.activity_data_input_catch_cons_question_pic_yes:
                if (checked) {
                    mCatchConsPicAns = "true";
                    picValid=true;
                    showNext = mCatchConsAns.equals("false") ||(nValid && typeValid && picValid);

                    LaunchFishCaughtIntent();
                }
                break;
        }
        invalidateOptionsMenu();
    }

    class nPicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchConsN = newVal;
            nValid = mCatchConsN!=0;

            showNext = mCatchConsAns.equals("false") ||(nValid && typeValid && picValid);
            invalidateOptionsMenu();
        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchConsType = type[newVal];
            mCatchConsTypeInt = newVal;
            typeValid = !mCatchConsType.equals("Choisi");

            showNext = mCatchConsAns.equals("false") ||(nValid && typeValid && picValid);
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(recopemValues.BUNDLE_STATE_ANS,mCatchConsAns);
        outState.putInt(recopemValues.BUNDLE_STATE_CATCH_N,mCatchConsN);
        outState.putInt(recopemValues.BUNDLE_STATE_TYPE_INT,mCatchConsTypeInt);
        outState.putString(recopemValues.BUNDLE_STATE_DETAILS,mCatchConsInputDetails.getText().toString());
        outState.putString(recopemValues.BUNDLE_STATE_PIC_ANS,mCatchConsPicAns);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);

        outState.putString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
        outState.putString(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS,mCatchGivePicAns);
        super.onSaveInstanceState(outState);
    }

    private void LaunchFishCaughtIntent() {
        Intent fishCaughtIntent = new Intent(dataInputCatchCons.this, dataInputFishCaught.class);
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
                mCatchConsDetails = mCatchConsInputDetails.getText().toString();

                Cursor mCursorFishCaught = getContentResolver().query(TrackContentProvider.poissonsUri(trackId), null,
                        null, null, null);
                boolean addedFishCaughtInfo= mCursorFishCaught.getCount()>0;
                mCursorFishCaught.close();


                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchConsValues = new ContentValues();
                catchConsValues.put(TrackContentProvider.Schema.COL_CATCH_CONS,mCatchConsAns);
                catchConsValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_N,mCatchConsN);
                catchConsValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_TYPE,mCatchConsType);
                catchConsValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_DETAILS,mCatchConsDetails);
                catchConsValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_PIC,mCatchConsPicAns);
                catchConsValues.put(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED,"true");
                catchConsValues.put(TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS,Boolean.toString(addedFishCaughtInfo));

                getContentResolver().update(trackUri, catchConsValues, null, null);

                Intent NextIntent = new Intent(dataInputCatchCons.this, TrackListActivity.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);

                NextIntent.putExtra(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS,mCatchGivePicAns);
                startActivity(NextIntent);
                TrackDetailActivity.getInstance().finish();
                dataInputWho.getInstance().finish();
                dataInputWhen.getInstance().finish();
                dataInputGear.getInstance().finish();
                dataInputBoat.getInstance().finish();
                dataInputCrew.getInstance().finish();
                dataInputCatchSale.getInstance().finish();
                dataInputCatchOrder.getInstance().finish();
                dataInputCatchGive.getInstance().finish();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}