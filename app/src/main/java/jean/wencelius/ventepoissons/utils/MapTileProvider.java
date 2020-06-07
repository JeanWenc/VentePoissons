package jean.wencelius.ventepoissons.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jean Wencélius on 04/06/2020.
 */
public class MapTileProvider {
    public static OfflineTileProvider setMapTileProvider(Context ctx, String tileType) {

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        OfflineTileProvider tileProvider =null;

        File file = null;
        try {
            file = getFileFromAssets(tileType, ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            File[] fileTab = new File[1];
            fileTab[0] = file;
            try {
                tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(ctx), fileTab);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tileProvider;
    }

    public static File getFileFromAssets(String aFileName, Context ctx) throws IOException {
        File cacheFile = new File(ctx.getCacheDir(), aFileName);
        try {
            InputStream inputStream = ctx.getAssets().open(aFileName);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new IOException("Could not open "+aFileName, e);
        }
        return cacheFile;
    }
}
