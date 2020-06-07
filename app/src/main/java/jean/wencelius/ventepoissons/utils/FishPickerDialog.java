package jean.wencelius.ventepoissons.utils;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.controller.dataInput.dataInputFishCaught;
import jean.wencelius.ventepoissons.db.TrackContentProvider;

public class FishPickerDialog extends DialogFragment implements NumberPicker.OnValueChangeListener {

    private static final String ARG_FISH_FAMILY = "fishFamily";
    private static final String ARG_FISH_TAHITIAN = "fishTahitian";
    private static final String ARG_CATCH_DESTINATION = "catchDestination";
    private static final String ARG_TRACK_ID = "trackId";
    private static final String ARG_SEL_PIC = "selPic";

    private String [] type;
    private ContentResolver mCr;
    private Cursor mCursorFishCaught;

    private String mFishFamily;
    private String mFishTahitian;
    private long mTrackId;
    private String mCatchDestination;
    private int mSelPic;
    private boolean mNeedCheckUpdate;

    private String selectionIn;
    private String [] selectionArgsList;

    private Button mOkButton;
    private EditText mInputOtherFish;

    private int mCatchN;

    private String mCatchType;

    private boolean mInputOtherFishValid;
    private boolean mCatchTypeValid;
    private boolean mCatchNValid;


    public FishPickerDialog() {
        // Required empty public constructor
    }

    public static FishPickerDialog newInstance(int selPic,String fishFamily, String fishTahitian, long trackId, String catchDestination) {
        FishPickerDialog fragment = new FishPickerDialog();
        Bundle args = new Bundle();
        args.putString(ARG_FISH_FAMILY, fishFamily);
        args.putString(ARG_FISH_TAHITIAN, fishTahitian);
        args.putLong(ARG_TRACK_ID, trackId);
        args.putString(ARG_CATCH_DESTINATION, catchDestination);
        args.putInt(ARG_SEL_PIC,selPic);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFishFamily = getArguments().getString(ARG_FISH_FAMILY);
            mFishTahitian = getArguments().getString(ARG_FISH_TAHITIAN);
            mTrackId = getArguments().getLong(ARG_TRACK_ID);
            mCatchDestination = getArguments().getString(ARG_CATCH_DESTINATION);
            mSelPic = getArguments().getInt(ARG_SEL_PIC);
        }
        mCatchN = 0;
        mCatchNValid = false;
        mCatchType = "Choisi";
        mCatchTypeValid = false;
        if(mSelPic==0){
            mInputOtherFishValid = false;
        }else{
            mInputOtherFishValid = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fish_picker_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView mDialogFishName = (TextView) view.findViewById(R.id.dialog_fish_picker_tahitian_name);
        mDialogFishName.setText(mFishTahitian);

        mInputOtherFish = (EditText) view.findViewById(R.id.dialog_fish_picker_input_other_fish);

        if(mSelPic==0){
            mInputOtherFish.setVisibility(View.VISIBLE);

            mInputOtherFish.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mInputOtherFishValid = (s.toString().length()!=0);
                    mOkButton.setEnabled(mCatchNValid && mCatchTypeValid && mInputOtherFishValid);
                }
                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }else{
            mInputOtherFish.setVisibility(View.INVISIBLE);
        }

        NumberPicker mPickerCatchN = (NumberPicker) view.findViewById(R.id.dialog_fish_picker_N);
        mPickerCatchN.setMinValue(0);
        mPickerCatchN.setMaxValue(100);
        mPickerCatchN.setOnValueChangedListener(this);
        mPickerCatchN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        NumberPicker mPickerCatchType = (NumberPicker) view.findViewById(R.id.dialog_fish_picker_type);
        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mPickerCatchType.setMinValue(0);
        mPickerCatchType.setMaxValue(type.length-1);
        mPickerCatchType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mPickerCatchType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mPickerCatchType.setDisplayedValues(type);
        mPickerCatchType.setOnValueChangedListener(this);

        mOkButton = (Button) view.findViewById(R.id.dialog_fish_picker_button_ok);
        mOkButton.setEnabled(mCatchNValid && mCatchTypeValid && mInputOtherFishValid);
        Button mCancelButton = (Button) view.findViewById(R.id.dialog_fish_picker_button_cancel);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCr = getActivity().getContentResolver();

                if(mSelPic == 0){
                    mFishTahitian = mInputOtherFish.getText().toString();
                }

                selectionIn = TrackContentProvider.Schema.COL_CATCH_DESTINATION + " = ? AND " + TrackContentProvider.Schema.COL_FISH_TAHITIAN + " = ?";
                selectionArgsList = new String [] {mCatchDestination, mFishTahitian};
                mCursorFishCaught = mCr.query(TrackContentProvider.poissonsUri(mTrackId), null,
                        selectionIn, selectionArgsList, TrackContentProvider.Schema.COL_ID + " asc");

                int catchN = 0;
                String catchType = "";

                boolean isCursorNotEmpty = mCursorFishCaught.moveToFirst();

                if(isCursorNotEmpty){
                    catchN = mCursorFishCaught.getInt(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N));
                    catchType = mCursorFishCaught.getString(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N_TYPE));
                    mNeedCheckUpdate = true;
                }else{
                    mNeedCheckUpdate = false;
                }
                mCursorFishCaught.close();

                if(mNeedCheckUpdate){
                    if(catchN!=mCatchN || !catchType.equals(mCatchType)){

                        String message = "Tu vas changer les informations pour " + mFishTahitian + " !\n\n"
                                + "Anciennes informations : " + mFishTahitian+" : "+ catchN + " "+ catchType+ "." +"\n\n"
                                + "Nouvelles informations : " + mFishTahitian+" : "+ mCatchN + " " + mCatchType +".";

                        new AlertDialog.Builder(getActivity())
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(message)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(
                                        android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ContentValues valuesToUpdate = new ContentValues();
                                                valuesToUpdate.put(TrackContentProvider.Schema.COL_CATCH_N,mCatchN);
                                                valuesToUpdate.put(TrackContentProvider.Schema.COL_CATCH_N_TYPE,mCatchType);
                                                mCr.update(TrackContentProvider.poissonsUri(mTrackId),valuesToUpdate,selectionIn,selectionArgsList);
                                                dialog.dismiss();
                                            }
                                        }
                                ).setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }
                        )
                                .show();
                    }
                }else{
                    ContentValues values = new ContentValues();
                    values.put(TrackContentProvider.Schema.COL_TRACK_ID,mTrackId);
                    values.put(TrackContentProvider.Schema.COL_CATCH_DESTINATION,mCatchDestination);
                    values.put(TrackContentProvider.Schema.COL_FISH_FAMILY,mFishFamily);
                    values.put(TrackContentProvider.Schema.COL_FISH_TAHITIAN,mFishTahitian);
                    values.put(TrackContentProvider.Schema.COL_CATCH_N,mCatchN);
                    values.put(TrackContentProvider.Schema.COL_CATCH_N_TYPE,mCatchType);

                    mCr.insert(TrackContentProvider.poissonsUri(mTrackId),values);
                }

                dataInputFishCaught prevActivity = (dataInputFishCaught) getActivity();
                prevActivity.setMyCaughtFish(mFishTahitian,mCatchN,mCatchType);
                getDialog().dismiss();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        if(picker.getId() == R.id.dialog_fish_picker_N){
            mCatchNValid = newVal!=0;
            mCatchN = newVal;
            mOkButton.setEnabled(mCatchNValid && mCatchTypeValid && mInputOtherFishValid);
        }
        if(picker.getId() == R.id.dialog_fish_picker_type){
            mCatchTypeValid = newVal!=0;
            mCatchType = type[newVal];
            mOkButton.setEnabled(mCatchNValid && mCatchTypeValid && mInputOtherFishValid);
        }
    }
}
