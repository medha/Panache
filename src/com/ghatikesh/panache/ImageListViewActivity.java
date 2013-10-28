package com.ghatikesh.panache;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
		
		// Check if the phone is connected to the Internet before attempting to download images
		if (isConnected(this)) {
			// Controller: Create the adapter to convert the data to a view
			ImageListViewAdapter adapter = new ImageListViewAdapter(this, images);
		
			// Attach the adapter to a ListView
			listView.setAdapter(adapter);
		} else {
			Toast.makeText(this, "No Internet connection. Please close the app, check your connection and launch the app again.", Toast.LENGTH_LONG).show();
		}
	}

	private void setupViews() {
		listView = (ListView) findViewById(R.id.lvImageList);
	}
	
	/**
	 * Need min sdk version 11 to add ActionBar styling.
	 * 
	 * Future work: Switch to a sherlock support version that supports lower sdks.
	 */
	private void addActionBarStyling() {
		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);
		
		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.custom_text_view, null);

		TextView actionBarTitle = (TextView) v.findViewById(R.id.title);
		actionBarTitle.setText(this.getTitle());
		
		this.getActionBar().setCustomView(v);
	}
	
	/**
	 * Check if the phone is connected to the Internet.
	 * 
	 * @param context
	 * @return true if connected. false if not.
	 */
	public static boolean isConnected(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}

}
