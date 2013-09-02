package so.sauru;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

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

	/** Rotate Bitmap **/
	public static Bitmap rotate(Bitmap b, int degrees) {
		if ( degrees != 0 && b != null ) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth()/2, (float) b.getHeight()/2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0,
						b.getWidth(), b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				// We have no memory to rotate. Return the original bitmap.
			}
		}
		return b;
	}

	/** Create Thumbnail of Image File **/
	public static Bitmap createThumbnail(String filename, int w, int h) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		int img_w = getWidth(filename);
		int img_h = getHeight(filename);
		// TODO size check!

		if (((float) img_w / (float) img_h) > ((float) w/ (float) h)) {
			w = (int) Math.round((float) h * (float) img_w / (float) img_h);
		} else {
			h = (int) Math.round((float) w * (float) img_h / (float) img_w);
		}

		if (img_w > (32 * w)) {
			options.inSampleSize = 16;
		} else if (img_w > (16 * w)) {
			options.inSampleSize = 16;
		} else if (img_w > (8 * w)) {
			options.inSampleSize = 8;
		} else if (img_w > (4 * w)) {
			options.inSampleSize = 4;
		} else if (img_w > (2 * w)) {
			options.inSampleSize = 2;
		}
		Log.d("ImageUtils", "filename:" + filename + " sample:" + options.inSampleSize
				+ " dim:" + img_w + "x" + img_h + " " + w + "x" + h);

		Bitmap src = null;
		Bitmap bmp = null;
		try {
			src = BitmapFactory.decodeFile(filename, options);
			bmp = Bitmap.createScaledBitmap(src, w, h, true);
			// TODO null check!

			ExifInterface exif = new ExifInterface(filename);
			int orientation = Integer.parseInt(exif
					.getAttribute(ExifInterface.TAG_ORIENTATION));
			switch(orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				bmp = rotate(bmp, 90);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				bmp = rotate(bmp, 180);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				bmp = rotate(bmp, 270);
				break;
			}
		} catch (Exception e) {
			Log.d("ImageUtils", "exif error for " + filename);
			e.printStackTrace();
		}
		return bmp;
	}

	/** Create Thumnail of Image URI (just uri2path and call other method) **/
	public static Bitmap createThumbnail(Uri uri, int w, int h, Activity act) {
		String filepath = UriUtils.getFileFromUri(uri, act).getAbsolutePath();
		Bitmap bmp = createThumbnail(filepath, w, h);
		return bmp;
	}
}
/* vim: set ts=4 sw=4: */