package com.ghatikesh.panache;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
		loadBitmap(address, ivImage);
		
		return view;
	}

	public void loadBitmap(String url, ImageView imageView) {
		if (cancelPotentialDownload(url, imageView)) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
	         DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
	         imageView.setImageDrawable(downloadedDrawable);
	         task.execute(url);
	     }
	}
	
	private static boolean cancelPotentialDownload(String url, ImageView imageView) {
	    BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

	    if (bitmapDownloaderTask != null) {
	        String bitmapUrl = bitmapDownloaderTask.url;
	        if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
	            bitmapDownloaderTask.cancel(true);
	        } else {
	            // The same URL is already being downloaded.
	            return false;
	        }
	    }
	    return true;
	}
	
	private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
	    if (imageView != null) {
	        Drawable drawable = imageView.getDrawable();
	        if (drawable instanceof DownloadedDrawable) {
	            DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
	            return downloadedDrawable.getBitmapDownloaderTask();
	        }
	    }
	    return null;
	}
	
	private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		 private final WeakReference<ImageView> ivImage;
		 private String url;
		
		public BitmapDownloaderTask(ImageView ivImage) {
			this.ivImage = new WeakReference<ImageView>(ivImage	);
		}

		protected Bitmap doInBackground(String... addresses) {
			// Convert string to URL
			URL url = getUrlFromString(addresses[0]);
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
				//bitmap = decodeSampledBitmapFromResource(in, 50, 50);
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
		protected void onPostExecute(Bitmap bitmap) {
			if (ivImage != null) {
			    ImageView imageView = ivImage.get();
			    BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
			    // Change bitmap only if this process is still associated with it
			    if (this == bitmapDownloaderTask) {
			        imageView.setImageBitmap(bitmap);
			    }
			}
			
		}
		
		
		public Bitmap decodeSampledBitmapFromResource(InputStream is, int reqWidth, int reqHeight) {

		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(is, null, options);

		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    Bitmap bitmap =  BitmapFactory.decodeStream(is, null, options); 
		    return bitmap;
		}
		
		public int calculateInSampleSize(
	            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        if( heightRatio < widthRatio ) {
	        	inSampleSize = heightRatio;
	        } else {
	        	inSampleSize = widthRatio;
	        }
	    }

	    return inSampleSize;
	}
	}
	
	static class DownloadedDrawable extends ColorDrawable {
	    private final WeakReference<BitmapDownloaderTask> imageDownloaderTaskReference;

	    public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
	        super(Color.WHITE);
	        imageDownloaderTaskReference =
	            new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
	    }

	    public BitmapDownloaderTask getBitmapDownloaderTask() {
	        return imageDownloaderTaskReference.get();
	    }
	}
}
