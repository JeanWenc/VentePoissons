package jean.wencelius.ventepoissons.db;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.model.Track;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class TrackListAdapter extends CursorAdapter {
    public TrackListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        bind(cursor, view, context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup vg) {
        View view = LayoutInflater.from(vg.getContext()).inflate(R.layout.tracklist_item,
                vg, false);
        return view;
    }

    /**
     * Do the binding between data and item view.
     *
     * @param cursor
     *				Cursor to pull data
     * @param v
     *				RelativeView representing one item
     * @param context
     *				Context, to get resources
     * @return The relative view with data bound.
     */
    private View bind(Cursor cursor, View v, Context context) {
        TextView vId = (TextView) v.findViewById(R.id.tracklist_item_id);
        TextView vWeekday = (TextView) v.findViewById(R.id.tracklist_item_weekday);
        TextView vNameOrStartDate = (TextView) v.findViewById(R.id.tracklist_item_nameordate);
        TextView vGpsMethod = (TextView) v.findViewById(R.id.tracklist_item_gpsmethod_which);
        TextView vRecopemId = (TextView) v.findViewById(R.id.tracklist_item_recopem_id);
        TextView vDataAdded = (TextView) v.findViewById(R.id.tracklist_item_data_value);
        TextView vPicAdded = (TextView) v.findViewById(R.id.tracklist_item_pictures_value);
        TextView vExported = (TextView) v.findViewById(R.id.tracklist_item_exported_value);
        TextView vSentEmail = (TextView) v.findViewById(R.id.tracklist_item_sent_email_value);
        LinearLayout vMainLayout = (LinearLayout) v.findViewById(R.id.tracklist_item_mainlayout);

        TextView vTps = (TextView) v.findViewById(R.id.tracklist_item_tps);

        // Bind id
        long trackId = cursor.getLong(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ID));
        String strTrackId = Long.toString(trackId);
        vId.setText("#" + strTrackId);

        // Bind WP count, TP count, name
        Track t = Track.build(trackId, cursor, context.getContentResolver(), false);
        vWeekday.setText(t.getWeekday());
        vGpsMethod.setText(t.getGpsMethod());
        vTps.setText(Integer.toString(t.getTpCount()));
        vNameOrStartDate.setText(t.getName());

        vRecopemId.setText(t.getRecopemId());

        String mDataAdded = t.getDataAdded();
        String mPicAdded = t.getPicAdded();
        String mCaughtFishDetails = t.getCaughtFishDetails();
        String mExported = t.getExported();
        String mSentEmail = t.getSentEmail();

        if(mDataAdded.equals("true")){
            vDataAdded.setText(R.string.answer_yes);
        }else{
            vDataAdded.setText(R.string.answer_no);
        }
        if(mPicAdded.equals("true")){
            vPicAdded.setText(R.string.answer_yes);
        }else{
            if(mCaughtFishDetails.equals("true")){
                vPicAdded.setText(R.string.track_item_caught_fish_details);
            }else{
                vPicAdded.setText(R.string.answer_no);
            }
        }
        if(mExported.equals("true")){
            vExported.setText(R.string.answer_yes);
        }else{
            vExported.setText(R.string.answer_no);
        }
        if(mSentEmail.equals("true")){
            vSentEmail.setText(R.string.answer_yes);
        }else{
            vSentEmail.setText(R.string.answer_no);
        }

        if(mDataAdded.equals("false") && mPicAdded.equals("false")){
            vMainLayout.setBackgroundColor(Color.parseColor("#F21A00"));
        }else if(mDataAdded.equals("true") && mPicAdded.equals("false")){
            vMainLayout.setBackgroundColor(Color.parseColor("#E1AF00"));
        }else if(mDataAdded.equals("false") && !mPicAdded.equals("false")){
            vMainLayout.setBackgroundColor(Color.parseColor("#E1AF00"));
        }else{
            if(mExported.equals("false") && mSentEmail.equals("false")){
                vMainLayout.setBackgroundColor(Color.parseColor("#78B7C5"));
            }else if(mSentEmail.equals("false")){
                vMainLayout.setBackgroundColor(Color.parseColor("#3B9AB2"));
            }else if(mSentEmail.equals("true")){
                vMainLayout.setBackgroundColor(Color.parseColor("#A0A0A0"));
            }
        }

        return v;
    }
}
