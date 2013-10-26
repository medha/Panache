package com.ghatikesh.panache;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class ImageListViewActivity extends Activity {

	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_list_view);
		
		ActionBar actionBar = getActionBar();
//		actionBar.set
		
		//Model View Controller pattern
		
		// M: Create the data source
		ArrayList <String> images = getImages();
		
		// V: setup the views
		setupViews();
		
		// C: Create the adapter to convert the data to a view
		ImageListViewAdapter adapter = new ImageListViewAdapter(this, images);
		// Attach the adapter to a ListView
		
		listView.setAdapter(adapter);
	}

	private ArrayList<String> getImages() {
		ArrayList<String> images = new ArrayList<String>();
		images.add("http://i.imgur.com/IFD14.jpg");
		images.add("http://i.imgur.com/LjFRNha.jpg");
		images.add("http://i.imgur.com/EwNtfRg.jpg");
		images.add("http://i.imgur.com/mvvvzev.jpg");
		images.add("http://i.imgur.com/tSnq7DN.jpg");
		images.add("http://i.imgur.com/xZluhvb.jpg");
		images.add("http://i.imgur.com/Sa4051z.jpg");
		images.add("http://i.imgur.com/fZVtcV8.jpg");
		images.add("http://i.imgur.com/5o5CWCH.jpg");
		images.add("http://i.imgur.com/czV6ehQ.jpg");
		images.add("http://i.imgur.com/EUKwR9q.jpg");
		images.add("http://i.imgur.com/F4ism2b.jpg");
		images.add("http://i.imgur.com/v4kuFJA.jpg");
		images.add("http://i.imgur.com/dD07qwb.jpg");
		images.add("http://i.imgur.com/l73Dt6e.jpg");
		images.add("http://i.imgur.com/04g2yoo.jpg");
		images.add("http://i.imgur.com/6g9l7g0.jpg");
		images.add("http://i.imgur.com/9CUEgtX.jpg");
		images.add("http://i.imgur.com/vrxUHRW.jpg");
		images.add("http://i.imgur.com/zJ38KNr.jpg");
		return images;
	}

	private void setupViews() {
		listView = (ListView) findViewById(R.id.lvImageList);
	}
	
}
