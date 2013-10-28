package com.ghatikesh.panache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

public class ImageLoadHelper {

	private LruCache<String, Bitmap> memoryCache;
	private HashMap<String, Bitmap> diskCache = new HashMap<String, Bitmap>();
	private static File cacheDir;
	private static final String DISK_CACHE_SUBDIR = "panache";
	private final Object diskCacheLock = new Object();

	public ImageLoadHelper(Context context) {
		// Setup the memory and disk diskCache
		memoryCache = setupMemoryCache();
		cacheDir = setupDiskCache(context, DISK_CACHE_SUBDIR);

	}

	/**
	 * Load Bitmap for the given url, into the the given ImageView.
	 * @param url
	 * @param imageView
	 */
	public void loadBitmap(String url, ImageView imageView) {
		final String imageKey = url;
		// if image is already in the memory cache, use that
		final Bitmap bitmap = getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			// Cancel another running task if it's already associated with the ImageView.
		} else if (cancelPotentialDownload(url, imageView)) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			// Before executing the BitmapDownloaderTask, you create an
			// DownloadedDrawable and bind it to the target ImageView:
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(downloadedDrawable);
			task.execute(url);
		}
	}

	/**
	 * Checks if another running task is already associated with the ImageView.
	 * If so, it attempts to cancel the previous task by calling cancel(). In a
	 * small number of cases, the new task data matches the existing task and
	 * nothing further needs to happen.
	 */
	private static boolean cancelPotentialDownload(String url,
			ImageView imageView) {
		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				// Cancel previous task
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was cancelled
		return true;
	}

	/**
	 * A helper method to retrieve the task associated with a particular
	 * ImageView
	 * 
	 * @param ImageView
	 *            imageView
	 * @return BitmapDownloaderTask
	 */
	static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	/**
	 * AsyncTask provides an easy way to execute some work in a background
	 * thread and publish the results back on the UI thread.
	 * BitmapDownloaderTask subclasses AsyncTask and overrides the provided
	 * methods.
	 */
	private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> ivImage;
		String url;

		public BitmapDownloaderTask(ImageView ivImage) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			this.ivImage = new WeakReference<ImageView>(ivImage);
		}

		/* Decode image in background. */
		protected Bitmap doInBackground(String... addresses) {
			final String imageKey = addresses[0];
			// Check to see if bitmap is in the disk cache
			Bitmap bitmap = getBitmapFromDiskCache(imageKey);
			if (bitmap == null) { // Not found in disk cache, so process as
									// normal
				// Convert string to URL
				URL url = getUrlFromString(addresses[0]);
				// Get input stream
				InputStream in = getInputStream(url);
				// Decode bitmap
				bitmap = decodeBitmap(in, url);
				// Add final bitmap to both memory and disk cache
				addBitmapToCache(imageKey, bitmap);
			}
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
			return in;
		}

		/*
		 * Load a scaled down version of the Bitmap into memory by reading the
		 * dimensions and type of the image data prior to construction (and
		 * memory allocation) of the bitmap.
		 */
		private Bitmap decodeBitmap(InputStream in, URL url) {
			Bitmap bitmap;
			int reqWidth = 50; // required width of image we want to load
			int reqHeight = 50; // required height of image we want to load
			try {
				// Creating a BufferedInputStream stream as we need to read the
				// input stream twice. InputStream doesn't support that
				// unless you close and reopen it again which takes time.
				BufferedInputStream bis = new BufferedInputStream(in);

				// First decode with inJustDecodeBounds=true to check
				// dimensions. Setting the inJustDecodeBounds property to true
				// while decoding avoids memory allocation, returning null for
				// the bitmap object but setting outWidth and outHeight
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(bis, null, options);

				//Calculate inSampleSize. For example, inSampleSize == 4 returns an image that
				//is 1/4 the width/height of the original, and 1/16 the number of pixels.
				options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;

				bis.reset(); // so that we can read the stream again
				bitmap = BitmapFactory.decodeStream(bis, null, options);
				// Close the streams
				in.close();
				bis.close();
			} catch (IOException e) {
				in = null;
				bitmap = null;
			}
			return bitmap;
		}

		/* Once complete, see if ImageView is still around and set bitmap if
		 * this process is still associated with it.*/
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (ivImage != null) {
				ImageView imageView = ivImage.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with it
				// Check required to prevent multiple tasks from updating the same imageView
				if (this == bitmapDownloaderTask) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	/**
	 * If inSampleSize is set to a value > 1, it requests the decoder to
	 * subsample the original image, returning a smaller image to save memory.
	 * Any value <= 1 is treated the same as 1.
	 * @param Options options
	 * @param int reqWidth
	 * @param int reqHeight
	 * @return int inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee a final image with both dimensions larger than or 
			// equal to the requested height and width.
			if (heightRatio < widthRatio) {
				inSampleSize = heightRatio;
			} else {
				inSampleSize = widthRatio;
			}
		}
		//Using powers of 2 for inSampleSize values is faster and more efficient for the decoder.
		inSampleSize = (int) Math.pow(2d, Math.floor(Math.log(inSampleSize) / Math.log(2d)));
		return inSampleSize;
	}

	/**
	 * Create a dedicated Drawable subclass to store a reference back to the
	 * BitmapDownloaderTask. In this case, a ColorDrawable is used so that a
	 * white placeholder image can be displayed in the ImageView while the task
	 * completes
	 * 
	 */
	private class DownloadedDrawable extends ColorDrawable {
		private final WeakReference<BitmapDownloaderTask> imageDownloaderTaskReference;

		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
			super(Color.WHITE);
			imageDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
					bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return imageDownloaderTaskReference.get();
		}
	}

	/*********** Methods relating to memory and disk cache ***********/

	/**
	 * Add bitmap to memory and disk cache
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			if (bitmap != null) {
				memoryCache.put(key, bitmap); // Add to memory cache
			}
		}

		// Also add to disk cache
		synchronized (diskCacheLock) {
			if (diskCache != null && diskCache.get(key) == null) {
				diskCache.put(key, bitmap);
			}
		}
	}

	/**
	 * Returns Bitmap for the given key if it exists in the memory cache.
	 * Returns null if if doesn't exist.
	 */
	public Bitmap getBitmapFromMemCache(String key) {
		return memoryCache.get(key);
	}

	/**
	 * Returns Bitmap for the given key if it exists in the disk cache.
	 * Returns null if if doesn't exist.
	 */
	public Bitmap getBitmapFromDiskCache(String key) {
		synchronized (diskCacheLock) {
			// Wait while disk cache is started from background thread
			if (diskCache != null) {
				return diskCache.get(key);
			}
		}
		return null;
	}
	
	/**
	 * Get max available VM memory, and use 1/8th of it for the memory cache.
	 * Exceeding the max available memory will throw an OutOfMemory exception.
	 * Stored in kilobytes as LruCache takes an int in its constructor.
	 * 
	 * @return LruChache
	 */
	private LruCache<String, Bitmap> setupMemoryCache() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;

		return new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The diskCache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	/**
	 * Creates a unique subdirectory of the designated app diskCache directory.
	 * Tries to use external but if not mounted, falls back on internal storage.
	 * 
	 * @param context
	 * @param subDir
	 * @return File cacheDir
	 */
	public static File setupDiskCache(Context context, String subDir) {
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					subDir);
		} else {
			cacheDir = context.getCacheDir();
		}
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return cacheDir;
	}

}
