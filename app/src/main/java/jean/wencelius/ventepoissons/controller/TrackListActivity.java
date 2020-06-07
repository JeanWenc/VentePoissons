package jean.wencelius.ventepoissons.controller;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.db.TrackContentProvider;
import jean.wencelius.ventepoissons.db.TrackListAdapter;

public class TrackListActivity extends ListActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        getListView().setEmptyView(findViewById(R.id.activity_tracklist_empty));

        ImageButton mBtnBack = (ImageButton)  findViewById(R.id.activity_tracklist_home);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MenuActivityIntent = new Intent(TrackListActivity.this, MenuActivity.class);
                startActivity(MenuActivityIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        Cursor cursor = getContentResolver().query(
                TrackContentProvider.CONTENT_URI_TRACK, null, null, null,
                TrackContentProvider.Schema.COL_START_DATE + " desc");

        startManagingCursor(cursor);
        setListAdapter(new TrackListAdapter(TrackListActivity.this, cursor));
        getListView().setEmptyView(findViewById(R.id.activity_tracklist_empty));  // undo change from onPause

        super.onResume();
    }

    @Override
    protected void onPause() {
        CursorAdapter adapter = (CursorAdapter) getListAdapter();
        if (adapter != null) {
            // Prevents on-screen 'no tracks' message
            getListView().setEmptyView(findViewById(android.R.id.empty));
            // Properly close the adapter cursor
            Cursor cursor = adapter.getCursor();
            stopManagingCursor(cursor);
            cursor.close();
            setListAdapter(null);
        }
        super.onPause();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(
                ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, id),
                null, null, null, null);

        cursor.moveToPosition(0);

        String picAdded = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_PIC_ADDED));
        String dataAdded = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED));
        String exported = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_EXPORTED));
        String sentEmail = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_SENT_EMAIL));
        String saveDir = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DIR));

        cursor.close();

        Intent TrackDetailIntent = new Intent(TrackListActivity.this,TrackDetailActivity.class);

        TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, id);
        TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED,picAdded);
        TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED, dataAdded);
        TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_EXPORTED, exported);
        TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_DIR,saveDir);
        TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_SENT_EMAIL,sentEmail);

        startActivity(TrackDetailIntent);

        Toast.makeText(this, "Vendeur # "+Long.toString(id), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Intent MenuActivityIntent = new Intent(TrackListActivity.this,MenuActivity.class);
        startActivity(MenuActivityIntent);
        super.onBackPressed();
    }
}
