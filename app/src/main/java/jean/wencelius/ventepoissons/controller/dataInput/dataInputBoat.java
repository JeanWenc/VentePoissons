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
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.recopemValues;

public class dataInputBoat extends AppCompatActivity {

    static dataInputBoat boatAct;

    private static final String QUESTION_NUMBER = "Question 4/";

    public String mBoat;

    private long trackId;

    private static final String EMPTY = "empty";
    private static final String MOTORBOAT = "motorboat";
    private static final String OUTRIGGER = "outrigger";
    private static final String SWIM = "swim";
    private static final String FROM_SHORE = "from_shore";

    private static final String BUNDLE_STATE_BOAT = "boatType";

    private boolean showNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_boat);

        boatAct = this;

        RadioButton radioMotorboat = (RadioButton) findViewById(R.id.activity_data_input_boat_motor);
        RadioButton radioOutrigger = (RadioButton) findViewById(R.id.activity_data_input_boat_pirogue);
        RadioButton radioSwim = (RadioButton) findViewById(R.id.activity_data_input_boat_nage);
        RadioButton radioShore = (RadioButton) findViewById(R.id.activity_data_input_boat_shore);

        if(savedInstanceState!=null){
            showNext = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON);
            mBoat = savedInstanceState.getString(BUNDLE_STATE_BOAT);
            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK,trackId),null,null,null,null);
            mTrackCursor.moveToPosition(0);
            String boat = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_BOAT));
            mTrackCursor.close();

            if(null != boat){
                mBoat = boat;
            }else{
                mBoat = EMPTY;
            }
        }

        if(mBoat.equals(EMPTY)){
            showNext = false;
        }else{
            radioMotorboat.setChecked(mBoat.equals(MOTORBOAT));
            radioOutrigger.setChecked(mBoat.equals(OUTRIGGER));
            radioShore.setChecked(mBoat.equals(FROM_SHORE));
            radioSwim.setChecked(mBoat.equals(SWIM));
            showNext = true;
        }

        invalidateOptionsMenu();

        setTitle(QUESTION_NUMBER+recopemValues.TOT_NB_QUESTIONS);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_BOAT,mBoat);
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,showNext);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);

        super.onSaveInstanceState(outState);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_boat_motor:
                if (checked){
                    mBoat = MOTORBOAT;
                }
                break;
            case R.id.activity_data_input_boat_pirogue:
                if (checked){
                    mBoat = OUTRIGGER;
                }
                break;
            case R.id.activity_data_input_boat_nage:
                if (checked){
                    mBoat = SWIM;
                }
                break;
            case R.id.activity_data_input_boat_shore:
                if (checked){
                    mBoat = FROM_SHORE;
                }
                break;
        }
        showNext = true;
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

                ContentValues boatValues = new ContentValues();
                boatValues.put(TrackContentProvider.Schema.COL_BOAT,mBoat);

                getContentResolver().update(trackUri, boatValues, null, null);

                Intent NextIntent = new Intent(dataInputBoat.this, dataInputCrew.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputBoat getInstance(){
        return   boatAct;
    }
}
