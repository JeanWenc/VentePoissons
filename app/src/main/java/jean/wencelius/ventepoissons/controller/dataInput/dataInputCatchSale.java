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
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.controller.TrackDetailActivity;
import jean.wencelius.ventepoissons.controller.TrackListActivity;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputCatchSale extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String QUESTION_NUMBER = "Question 7/";

    private int mCatchSaleN;
    private String mCatchSaleType;
    private int mCatchSaleTypeInt;
    private String mCatchSalePrice;
    private int mCatchSalePriceInt;
    private String mCatchSaleDetails;

    private static final String mCatchDestination = "sale";

    private long trackId;

    private EditText mCatchSaleInputDetails;

    private String[] prices;
    private String [] type;

    private boolean nValid;
    private boolean typeValid;
    private boolean priceValid;
    private boolean caughtFishValid;

    private static final String BUNDLE_STATE_CAUGHT_FISH_VALID = "caughtFishValid";

    private boolean showNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_catch_sale);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mCatchSaleInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_sale_input_details);

        Button mLaunchFishCaught = (Button) findViewById(R.id.activity_data_input_catch_sale_launch_fish_caught);

        mLaunchFishCaught.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                caughtFishValid = true;
                showNext = nValid && typeValid && priceValid && caughtFishValid;
                invalidateOptionsMenu();
                LaunchFishCaughtIntent();
            }
        });

        NumberPicker mCatchSaleInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_N);
        NumberPicker mCatchSaleInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_type);
        Spinner mCatchSaleInputPrice = (Spinner) findViewById(R.id.activity_data_input_catch_sale_input_price);

        mCatchSaleInputN.setMinValue(0);
        mCatchSaleInputN.setMaxValue(100);
        mCatchSaleInputN.setOnValueChangedListener(new nPicker());
        mCatchSaleInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchSaleInputType.setMinValue(0);
        mCatchSaleInputType.setMaxValue(type.length-1);
        mCatchSaleInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mCatchSaleInputType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCatchSaleInputType.setDisplayedValues(type);
        mCatchSaleInputType.setOnValueChangedListener(new typePicker());

        prices = this.getResources().getStringArray(R.array.data_input_catch_sale_price);
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this, R.array.data_input_catch_sale_price,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCatchSaleInputPrice.setAdapter(priceAdapter);
        mCatchSaleInputPrice.setOnItemSelectedListener(this);

        caughtFishValid = false;
        if(savedInstanceState != null){
            mCatchSaleN = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_CATCH_N);
            mCatchSaleTypeInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_TYPE_INT);
            mCatchSaleType = type[mCatchSaleTypeInt];
            mCatchSalePriceInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_PRICE_INT);
            mCatchSalePrice = prices[mCatchSalePriceInt];
            mCatchSaleDetails = savedInstanceState.getString(recopemValues.BUNDLE_STATE_DETAILS);
            caughtFishValid = savedInstanceState.getBoolean(BUNDLE_STATE_CAUGHT_FISH_VALID);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            int catchSaleN = mTrackCursor.getInt(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_N));
            String catchSaleType = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_TYPE));
            String catchSalePrice = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_PRICE));
            String catchSaleDetails = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_DETAILS));

            mTrackCursor.close();

            if(catchSaleType!=null){
                mCatchSaleN = catchSaleN;
                mCatchSaleType = catchSaleType;
                if (catchSaleType!=null){
                    mCatchSaleTypeInt = Arrays.asList(type).indexOf(catchSaleType);
                }else{
                    mCatchSaleTypeInt = 0;
                }
                mCatchSalePrice = catchSalePrice;
                if (catchSalePrice!=null){
                    mCatchSalePriceInt = Arrays.asList(prices).indexOf(catchSalePrice);
                }else{
                    mCatchSalePriceInt = 0;
                }
                mCatchSaleDetails = catchSaleDetails;
            }else{
                mCatchSaleN = 0;
                mCatchSaleType = type[0];
                mCatchSaleTypeInt = 0;
                mCatchSalePrice = prices[0];
                mCatchSalePriceInt = 0;
                mCatchSaleDetails = "NA";
            }
        }

        mCatchSaleInputN.setValue(mCatchSaleN);
        mCatchSaleInputType.setValue(mCatchSaleTypeInt);
        mCatchSaleInputPrice.setSelection(mCatchSalePriceInt);

        if(!mCatchSaleDetails.equals("NA")){
            mCatchSaleInputDetails.setText(mCatchSaleDetails);
            mCatchSaleInputDetails.setSelection(mCatchSaleDetails.length());
        }

        nValid = mCatchSaleN!=0;
        typeValid = mCatchSaleTypeInt!=0;
        priceValid = mCatchSalePriceInt!=0;

        showNext = nValid && typeValid && priceValid && caughtFishValid;
        invalidateOptionsMenu();

        setTitle(QUESTION_NUMBER + recopemValues.TOT_NB_QUESTIONS);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        mCatchSalePrice = prices[position];
        mCatchSalePriceInt=position;

        priceValid = !mCatchSalePrice.equals(prices[0]);

        showNext = nValid && typeValid && priceValid && caughtFishValid;

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
            mCatchSaleN = newVal;
            nValid = mCatchSaleN!=0;
            showNext = nValid && typeValid && priceValid && caughtFishValid;
            invalidateOptionsMenu();
        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchSaleType = type[newVal];
            mCatchSaleTypeInt = newVal;
            typeValid = !mCatchSaleType.equals(type[0]);
            showNext = nValid && typeValid && priceValid && caughtFishValid;
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(recopemValues.BUNDLE_STATE_CATCH_N,mCatchSaleN);
        outState.putInt(recopemValues.BUNDLE_STATE_TYPE_INT,mCatchSaleTypeInt);
        outState.putInt(recopemValues.BUNDLE_STATE_PRICE_INT,mCatchSalePriceInt);
        outState.putString(recopemValues.BUNDLE_STATE_DETAILS,mCatchSaleInputDetails.getText().toString());
        outState.putBoolean(BUNDLE_STATE_CAUGHT_FISH_VALID,caughtFishValid);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        super.onSaveInstanceState(outState);
    }

    private void LaunchFishCaughtIntent() {
        Intent fishCaughtIntent = new Intent(dataInputCatchSale.this, dataInputFishCaught.class);
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
                mCatchSaleDetails = mCatchSaleInputDetails.getText().toString();

                Cursor mCursorFishCaught = getContentResolver().query(TrackContentProvider.poissonsUri(trackId), null,
                        null, null, null);
                boolean addedFishCaughtInfo = false;
                if(mCursorFishCaught != null){
                    addedFishCaughtInfo = mCursorFishCaught.getCount()>0;
                }

                mCursorFishCaught.close();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchSaleValues = new ContentValues();
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_N,mCatchSaleN);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_TYPE,mCatchSaleType);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_PRICE,mCatchSalePrice);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_DETAILS,mCatchSaleDetails);
                catchSaleValues.put(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED,"true");
                catchSaleValues.put(TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS,Boolean.toString(addedFishCaughtInfo));

                getContentResolver().update(trackUri, catchSaleValues, null, null);

                Intent NextIntent = new Intent(dataInputCatchSale.this, TrackListActivity.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                startActivity(NextIntent);

                TrackDetailActivity.getInstance().finish();
                dataInputWho.getInstance().finish();
                dataInputWhen.getInstance().finish();
                dataInputGear.getInstance().finish();
                dataInputBoat.getInstance().finish();
                dataInputCrew.getInstance().finish();
                dataInputTuiRack.getInstance().finish();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
