package com.ghatikesh.panache;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageListViewAdapter extends ArrayAdapter<Image> {

	private ImageView ivImage;
	private ImageLoadHelper imageLoadHelper;

	public ImageListViewAdapter(Context context, List<Image> images) {
		super(context, R.layout.activity_image_list_view, images);

		// Using a helper class. Keeps the adapter clean and tidy.
		imageLoadHelper = new ImageLoadHelper(context);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		Image image = getItem(position);

		// Check if an existing view is being reused, otherwise inflate the view
		View view = convertView;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.image_row_item, parent, false);
		}

		// Populate the data into the template view using the data object
		TextView tvTitle = (TextView) view.findViewById(R.id.tvImageTitle);
		tvTitle.setText(image.getFilename());

		// Using a helper class 'ImageLoadHelper' to handle the image loading
		// logic in a background thread that does not freeze the UI thread.
		ivImage = (ImageView) view.findViewById(R.id.ivImage);
		imageLoadHelper.loadBitmap(image.getUrl(), ivImage);

		// Return the completed view to render on screen
		return view;
	}
}
