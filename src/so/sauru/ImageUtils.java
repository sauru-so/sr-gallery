package so.sauru;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageUtils {
	/** Get Bitmap's Width **/
	public static int getWidth(String filename) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filename, options);
			return options.outWidth;
		} catch(Exception e) {
			return 0;
		}
	}
	
 	/** Get Bitmap's height **/
	public static int getHeight(String filename) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filename, options);
				return options.outHeight;
		} catch(Exception e) {
			return 0;
		}
	}

	/** Create Thumbnail of Image File **/
	public static Bitmap createThumbnail(String filename, int w, int h) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (getWidth(filename) > (4 * w) || getHeight(filename) > (4 * h)) {
			options.inSampleSize = 4;
		}
		Bitmap src = BitmapFactory.decodeFile(filename, options);
		return Bitmap.createScaledBitmap(src, w, h, true);
	}

	/** Create Thumnail of Image URI **/
	public static Bitmap createThumbnail(Uri uri, int w, int h, Activity activity) {
		return createThumbnail(UriUtils
				.getFileFromUri(uri, activity).getAbsolutePath(), w, h);
	}
}
/* vim: set ts=4 sw=4: */