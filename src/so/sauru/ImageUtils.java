package so.sauru;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
		int img_w = getWidth(filename);
		int img_h = getHeight(filename);
		if (img_w > (4 * w) || img_h > (4 * h)) {
			options.inSampleSize = 4;
		}
		if (img_w/img_h > w/h) {
			w = h * img_w/img_h;
		} else {
			h = w * img_h/img_w;
		}
		Bitmap src = BitmapFactory.decodeFile(filename, options);
		return Bitmap.createScaledBitmap(src, w, h, true);
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

	/** Create Thumnail of Image URI **/
	public static Bitmap createThumbnail(Uri uri, int w, int h, Activity act) {
		String filepath = UriUtils.getFileFromUri(uri, act).getAbsolutePath();
		Bitmap bmp = createThumbnail(filepath, w, h);
		try {
			ExifInterface exif = new ExifInterface(filepath);
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
		}
		return bmp;
	}
}
/* vim: set ts=4 sw=4: */