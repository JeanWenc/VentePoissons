package jean.wencelius.ventepoissons.gpx;

import android.content.Context;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class ExportToStorageTask extends ExportTrackTask {
    private static final String TAG = ExportToStorageTask.class.getSimpleName();

    public ExportToStorageTask(Context context, String saveDir, long... trackId) {
        super(context, saveDir,trackId);
    }

    @Override
    protected boolean exportMediaFiles() {
        return false;
    }

    @Override
    protected boolean updateExportDate() {
        return false;
    }
}
