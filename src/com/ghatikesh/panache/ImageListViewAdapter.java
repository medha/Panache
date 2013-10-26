package com.ghatikesh.panache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageListViewAdapter extends ArrayAdapter<Image> {

	private ImageView ivImage;
	private LruCache<String, Bitmap> mMemoryCache;
//	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private static final String DISK_CACHE_SUBDIR = "panache";

	private HashMap<String, Bitmap> cache=new HashMap<String, Bitmap>();
   private static File cacheDir;

	public ImageListViewAdapter(Context context, List<Image> images) {
		super(context, R.layout.activity_image_list_view, images);

		// Get max available VM memory, exceeding this amount will throw an
		// OutOfMemory exception. Stored in kilobytes as LruCache takes an
		// int in its constructor.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 6;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

		// Initialize disk cache on background thread
		cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item
		Image image = getItem(position);

		// Populate the data into the template view using the data object
		View view = convertView;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.image_row_item, parent, false);
		}

		TextView tvTitle = (TextView) view.findViewById(R.id.tvImageTitle);
		tvTitle.setText(image.getFilename());

		ivImage = (ImageView) view.findViewById(R.id.ivImage);

		loadBitmap(image.getUrl(), ivImage);

		return view;
	}

	private void loadBitmap(String url, ImageView imageView) {
		final String imageKey = url;

		final Bitmap bitmap = getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			ivImage.setImageBitmap(bitmap);
		} else if (cancelPotentialDownload(url, imageView)) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(downloadedDrawable);
			task.execute(url);
		}
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

	private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> ivImage;
		String url;

		public BitmapDownloaderTask(ImageView ivImage) {
			this.ivImage = new WeakReference<ImageView>(ivImage);
		}

		protected Bitmap doInBackground(String... addresses) {
			final String imageKey = addresses[0];
			// Check disk cache in background thread
			Bitmap bitmap = getBitmapFromDiskCache(imageKey);

			if (bitmap == null) { // Not found in disk cache, so Process as
									// normal
				// Convert string to URL
				URL url = getUrlFromString(addresses[0]);
				// Get input stream
				InputStream in = getInputStream(url);
				// Decode bitmap
				bitmap = decodeBitmap(in);

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
		protected void onPostExecute(Bitmap bitmap) {
			if (ivImage != null) {
				ImageView imageView = ivImage.get();
				BitmapDownloaderTask bitmapDownloaderTask = ImageListViewAdapter
						.getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with
				// it
				if (this == bitmapDownloaderTask) {
					imageView.setImageBitmap(bitmap);
				}
			}

		}
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

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}


	public void addBitmapToCache(String key, Bitmap bitmap) {
		// Add to memory cache as before
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}

		// Also add to disk cache
		synchronized (mDiskCacheLock) {
			if (cache != null && cache.get(key) == null) {
				cache.put(key, bitmap);
			}
		}
	}

	public Bitmap getBitmapFromDiskCache(String key) {
		synchronized (mDiskCacheLock) {
			// Wait while disk cache is started from background thread
			if (cache != null) {
				return cache.get(key);
			}
		}
		return null;
	}

	// Creates a unique subdirectory of the designated app cache directory.
	// Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getDiskCacheDir(Context context, String uniqueName) {
		 //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(), uniqueName);
        } else {
            cacheDir= context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
	}

}
