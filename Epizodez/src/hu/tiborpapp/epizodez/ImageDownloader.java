package hu.tiborpapp.epizodez;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
	ImageView imageView;

	/**
	 * Constructor of ImageDownloader.
	 */
	public ImageDownloader(ImageView bmImage) {
		this.imageView = bmImage;
	}

	
	/**
	 * Downloads the image from the given URL.
	 */
	protected Bitmap doInBackground(String... urls) {
		String urldisplay = urls[0];
		Bitmap bitm = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			bitm = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		return bitm;
	}

	/**
	 * Sets the result Bitmap as content of ImageView.
	 */
	protected void onPostExecute(Bitmap result) {
		imageView.setImageBitmap(result);
	}
}