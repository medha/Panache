package com.ghatikesh.panache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class ImageListViewAdapter extends ArrayAdapter<String> {

	private ImageView ivImage;

	public ImageListViewAdapter(Context context, List<String> images) {
		super(context, R.layout.activity_image_list_view, images);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// Get the data item
		String address = getItem(position);
		System.out.println("Get View method: get item position: " + position + " address: " +  address  );
		// Populate the data into the template view using the data object
		View view;

		if (convertView == null) { // Check if an existing view is being reused,
									// otherwise inflate the view
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.image_row_item, parent, false);
		} else {
			view = convertView;
		}

		// TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		// tvTitle.setText(image.getFilename());

		
		ivImage = (ImageView) view.findViewById(R.id.ivImage);
		System.out.println("ImageView ID: " + ivImage.getId());

		System.out.println("Calling image download task for: " + position);
		new ImageDownloadTask().execute(address);
		System.out.println("End Execute for: " + position);

		return view;
	}

	private class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

		protected void onPreExecute() {
			// Runs on the UI thread before doInBackground
			// Good for toggling visibility of a progress indicator
		}

		protected Bitmap doInBackground(String... addresses) {
			// Convert string to URL
			URL url = getUrlFromString(addresses[0]);
			System.out.println("Doing in background. Url= " + url);
			// Get input stream
			InputStream in = getInputStream(url);
			// Decode bitmap
			Bitmap bitmap = decodeBitmap(in);
			// Return bitmap result
			return bitmap;
		}

		private URL getUrlFromString(String address) {
			URL url;
			try {
				url = new URL(address);
			} catch (MalformedURLException e1) {
				url = null;
			}
			System.out.println("reutrning url for address: " + address);
			return url;
		}

		private InputStream getInputStream(URL url) {
			InputStream in;
			// Open connection
			URLConnection conn;
			try {
				conn = url.openConnection();
				conn.connect();
				in = conn.getInputStream();
			} catch (IOException e) {
				in = null;
			}
			System.out.println("Returning Input Stream for url: " + url);
			return in;
		}

		private Bitmap decodeBitmap(InputStream in) {
			Bitmap bitmap;
			try {
				// Turn response into Bitmap
				bitmap = BitmapFactory.decodeStream(in);
				// Close the input stream
				in.close();
			} catch (IOException e) {
				in = null;
				bitmap = null;
			}
			System.out.println("Returning bitmap");
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// Set bitmap image for the result
			if (ivImage != null) {
				ivImage.setImageBitmap(result);
				System.out.println("Setting image view to bitmap: " + ivImage.getId());
			}
		}
	}

}
