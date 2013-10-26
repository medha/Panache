package com.ghatikesh.panache;

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

	public void loadBitmap(String url, ImageView imageView) {
		final String imageKey = url;

		final Bitmap bitmap = getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else if (cancelPotentialDownload(url, imageView)) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(downloadedDrawable);
			task.execute(url);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return memoryCache.get(key);
	}

	private static boolean cancelPotentialDownload(String url,
			ImageView imageView) {
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

	private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> ivImage;
		String url;

		public BitmapDownloaderTask(ImageView ivImage) {
			this.ivImage = new WeakReference<ImageView>(ivImage);
		}

		protected Bitmap doInBackground(String... addresses) {
			final String imageKey = addresses[0];
			// Check disk diskCache in background thread
			Bitmap bitmap = getBitmapFromDiskCache(imageKey);

			if (bitmap == null) { // Not found in disk diskCache, so Process as
									// normal
				// Convert string to URL
				URL url = getUrlFromString(addresses[0]);
				// Get input stream
				InputStream in = getInputStream(url);
				// Decode bitmap
				bitmap = decodeBitmap(in, url);

				// Add final bitmap to caches
				addBitmapToCache(imageKey, bitmap);
				addBitmapToMemoryCache(String.valueOf(addresses[0]), bitmap);
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

		private Bitmap decodeBitmap(InputStream in, URL url) {
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(in);
				// Close the input stream
				in.close();
			} catch (IOException e) {
				in = null;
				bitmap = null;
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (ivImage != null) {
				ImageView imageView = ivImage.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with
				// it
				if (this == bitmapDownloaderTask) {
					imageView.setImageBitmap(bitmap);
				}
			}

		}
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			memoryCache.put(key, bitmap);
		}
	}

	public static int calculateInSampleSize(
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

	public void addBitmapToCache(String key, Bitmap bitmap) {
		// Add to memory diskCache as before
		if (getBitmapFromMemCache(key) == null) {
			memoryCache.put(key, bitmap);
		}

		// Also add to disk diskCache
		synchronized (diskCacheLock) {
			if (diskCache != null && diskCache.get(key) == null) {
				diskCache.put(key, bitmap);
			}
		}
	}

	public Bitmap getBitmapFromDiskCache(String key) {
		synchronized (diskCacheLock) {
			// Wait while disk diskCache is started from background thread
			if (diskCache != null) {
				return diskCache.get(key);
			}
		}
		return null;
	}

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

	/**
	 * Get max available VM memory, and use 1/6th of it for the memory cache.
	 * Exceeding the max available memory will throw an OutOfMemory exception.
	 * Stored in kilobytes as LruCache takes an int in its constructor.
	 * 
	 * @return LruChache
	 */
	private LruCache<String, Bitmap> setupMemoryCache() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 6;

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
