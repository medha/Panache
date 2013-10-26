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
		
		// Model: Create the data source
		ArrayList<Image> images = Image.getImages();
		
		// View: setup the views
		setupViews();
		
		// Adding some styling to the action bar
		addActionBarStyling();
				
		// Controller: Create the adapter to convert the data to a view
		ImageListViewAdapter adapter = new ImageListViewAdapter(this, images);
		
		// Attach the adapter to a ListView
		listView.setAdapter(adapter);
	}

	private void setupViews() {
		listView = (ListView) findViewById(R.id.lvImageList);
	}
	
	private void addActionBarStyling() {
		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);
		
		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.custom_text_view, null);

		TextView actionBarTitle = (TextView) v.findViewById(R.id.title);
		actionBarTitle.setText(this.getTitle());
		
		this.getActionBar().setCustomView(v);
	}

}
