package com.ghatikesh.panache;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ImageListViewActivity extends Activity {

	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_list_view);
		
		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);
		
		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.custom_text_view, null);

		//if you need to customize anything else about the text, do it here.
		//I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
		TextView titleView = (TextView) v.findViewById(R.id.title);
		titleView.setText(this.getTitle());
		
		//assign the view to the actionbar
		this.getActionBar().setCustomView(v);
		
		//Model View Controller pattern
		
		// M: Create the data source
		ArrayList<Image> images = Image.getImages();
		
		// V: setup the views
		setupViews();
		
		// C: Create the adapter to convert the data to a view
		ImageListViewAdapter adapter = new ImageListViewAdapter(this, images);
		// Attach the adapter to a ListView
		
		listView.setAdapter(adapter);
	}

	private void setupViews() {
		listView = (ListView) findViewById(R.id.lvImageList);
	}
	
}
