package so.sauru;

import java.io.File;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class UriUtils {
	private static final String LOGTAG = "SR.UriUtils";
	public static File getFileFromUri(Uri uri, Activity activity) {
		String path = uri.getPath();
		String scheme = uri.getScheme();

		if (path != null && scheme != null
				&& scheme.equals("file")) {
			return new File(path);
		}

		String[] projection = {
				MediaStore.Images.Media.DATA
		};
		Cursor c = activity.getContentResolver().query(uri,
				projection, null, null, null);
		if (c != null && c.moveToFirst()) {
			path = c.getString(0);
			if (path != null) {
				return new File(path);
			}
			c.close();
		} else {
			Log.e(LOGTAG, "uri query failed for: " + uri.toString());
		}
		return null;
	}
}
