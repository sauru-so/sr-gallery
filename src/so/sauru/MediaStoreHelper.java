package so.sauru;

import java.io.File;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

public class MediaStoreHelper {
	private static final String LOGTAG = "SR.MediaStoreHelper";
	Context context = null;
	private ContentResolver contentResolver;

	public MediaStoreHelper(Context context) {
		this.context = context;
		this.contentResolver = context.getContentResolver();
	}

	public Uri getContentUriFromFile(Uri provider, String filepath) {
		Uri uri = null;
		String[] projection = { MediaColumns._ID, MediaColumns.DATA };

		Cursor c = contentResolver.query(provider, projection,
				MediaColumns.DATA + "=?", new String[] { filepath }, null);

		if (c != null && c.getCount() > 0) {
			long id = -1;
			c.moveToFirst();
			id = c.getLong(c.getColumnIndexOrThrow(MediaColumns._ID));
			c.close();
			Log.d(LOGTAG, "ok, retreived content id: " + id);
			uri = Uri.withAppendedPath(provider, Long.toString(id));
		} else {
			Log.w(LOGTAG, "oops! cursor is null or return nothing!");
		}
		return uri;
	}

	public int delete(Uri uri) {
		String id = uri.getLastPathSegment().toString();
		String provider = uri.toString().replace("/" + id, "");
		Log.d(LOGTAG, "delete uri " + uri + " from provider " + provider);

		int rows = contentResolver.delete(uri, null, null);
		Log.i(LOGTAG, "total " + rows + " row(s) deleted.");
		return rows;
	}

	public void scanDir(String dir) {
		Log.d(LOGTAG, "about to scan dir... " + dir);
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, dir);
		values.put("format", 0x3001);
		values.put(MediaStore.MediaColumns.DATE_MODIFIED,
				System.currentTimeMillis() / 1000);
		contentResolver.insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}

	public Uri insert(File f) {
		final File file = f;
		Log.d(LOGTAG, "ready to insert " + file.toString());
		MediaScanner ms = new MediaScanner(context);
		ms.scanFile(file.toString(), null);
		//try { Thread.sleep(1000); } catch (Exception e) {}
		Log.d(LOGTAG, "about to return!");
		return getContentUriFromFile(MediaStore.Images.Media
				.EXTERNAL_CONTENT_URI, file.toString());
		/*
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DATA, file.toString());
		return contentResolver.insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		*/
	}

	public class MediaScanner {
		private MediaScannerConnection mediaScanConn = null;
		private MediaScannerConnectionClient client = null;
		private String[] files = null;
		private String mimeType = null;

		public MediaScanner(Context context) {
			if (client == null) {
				client = new MediaScannerConnectionClient();
			}
			if (mediaScanConn == null) {
				mediaScanConn = new MediaScannerConnection(context, client);
			}
		}

		public void scanFile(String[] files, String mimeType) {
			this.files = files;
			this.mimeType = mimeType;
			mediaScanConn.connect();
		}

		public void scanFile(String file, String mimeType) { 
			scanFile(new String[] {file}, mimeType);
		}

		class MediaScannerConnectionClient
				implements MediaScannerConnection.MediaScannerConnectionClient {

			@Override
			public void onMediaScannerConnected() {
				Log.d(LOGTAG, "--- connected to msc. now scanning...");
				if (files != null) {
					for (String file: files) {
						mediaScanConn.scanFile(file, mimeType);
					}
				}
				files = null;
				mimeType = null;
			}

			@Override
			public void onScanCompleted(String path, Uri uri) {
				Log.d(LOGTAG, "--- scan completed. path:" + path
						+ " uri: " + uri.toString());
				mediaScanConn.disconnect();
			}
		}
	}
}
/* vim: set ts=4 sw=4: */