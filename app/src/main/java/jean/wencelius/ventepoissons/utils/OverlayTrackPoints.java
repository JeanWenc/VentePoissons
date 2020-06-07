package jean.wencelius.ventepoissons.utils;

import android.database.Cursor;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.util.ArrayList;
import java.util.List;

import jean.wencelius.ventepoissons.db.TrackContentProvider;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class OverlayTrackPoints {
    public static SimpleFastPointOverlay createPointOverlay(Cursor c){

        List<IGeoPoint> points = new ArrayList<>();

        c.moveToFirst();
        double lastLat = 0;
        double lastLon = 0;

        double minLat = 91.0, minLon = 181.0;
        double maxLat = -91.0, maxLon = -181.0;

        //int primaryKeyColumnIndex = c.getColumnIndex(TrackContentProvider.Schema.COL_ID);
        int latitudeColumnIndex = c.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE);
        int longitudeColumnIndex = c.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE);

        // Add each new point to the track
        while(!c.isAfterLast()) {
            lastLat = c.getDouble(latitudeColumnIndex);
            lastLon = c.getDouble(longitudeColumnIndex);
            //points.add(new LabelledGeoPoint((int)(lastLat * 1e6), (int)(lastLon * 1e6)));
            points.add(new LabelledGeoPoint(lastLat, lastLon));

            if (lastLat < minLat)  minLat = lastLat;
            if (lastLon < minLon)  minLon = lastLon;
            if (lastLat > maxLat)  maxLat = lastLat;
            if (lastLon > maxLon)  maxLon = lastLon;

            c.moveToNext();
        }

        SimplePointTheme pt = new SimplePointTheme(points, false);

        // create label style
        /*Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setColor(Color.parseColor("#0000ff"));
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(24);*/

        // set some visual options for the overlay
        // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(false).setCellSize(15);

        // create the overlay with the theme
        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);

        // onClick callback
        /*sfpo.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                Toast.makeText(mMapView.getContext()
                        , "You clicked " + ((LabelledGeoPoint) points.get(point)).getLabel()
                        , Toast.LENGTH_SHORT).show();
            }
        });*/

        return sfpo;
    }
}
