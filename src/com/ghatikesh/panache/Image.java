package com.ghatikesh.panache;

import java.util.ArrayList;

public class Image {
	private String filename;
	private String url;

	public Image(String url, String title) {
		this.url = url;
		this.filename = title;
	}

	public static ArrayList<Image> getImages() {
		ArrayList<Image> images = new ArrayList<Image>();
		images.add(new Image("http://i.imgur.com/IFD14.jpg", "IFD14.jpg"));
		images.add(new Image("http://i.imgur.com/LjFRNha.jpg", "LjFRNha.jpg"));
		images.add(new Image("http://i.imgur.com/EwNtfRg.jpg", "EwNtfRg.jpg"));
		images.add(new Image("http://i.imgur.com/mvvvzev.jpg", "mvvvzev.jpg"));
		images.add(new Image("http://i.imgur.com/tSnq7DN.jpg", "tSnq7DN.jpg"));
		images.add(new Image("http://i.imgur.com/xZluhvb.jpg", "xZluhvb.jpg"));
		images.add(new Image("http://i.imgur.com/Sa4051z.jpg", "Sa4051z.jpg"));
		images.add(new Image("http://i.imgur.com/fZVtcV8.jpg", "fZVtcV8.jpg"));
		images.add(new Image("http://i.imgur.com/5o5CWCH.jpg", "5o5CWCH.jpg"));
		images.add(new Image("http://i.imgur.com/czV6ehQ.jpg", "czV6ehQ.jpg"));
		images.add(new Image("http://i.imgur.com/EUKwR9q.jpg", "EUKwR9q.jpg"));
		images.add(new Image("http://i.imgur.com/F4ism2b.jpg", "F4ism2b.jpg"));
		images.add(new Image("http://i.imgur.com/v4kuFJA.jpg", "v4kuFJA.jpg"));
		images.add(new Image("http://i.imgur.com/dD07qwb.jpg", "dD07qwb.jpg"));
		images.add(new Image("http://i.imgur.com/l73Dt6e.jpg", "l73Dt6e.jpg"));
		images.add(new Image("http://i.imgur.com/04g2yoo.jpg", "04g2yoo.jpg"));
		images.add(new Image("http://i.imgur.com/6g9l7g0.jpg", "6g9l7g0.jpg"));
		images.add(new Image("http://i.imgur.com/9CUEgtX.jpg", "9CUEgtX.jpg"));
		images.add(new Image("http://i.imgur.com/vrxUHRW.jpg", "vrxUHRW.jpg"));
		images.add(new Image("http://i.imgur.com/zJ38KNr.jpg", "zJ38KNr.jpg"));
		return images;
	}
	
	public String getFilename() {
		return filename;
	}

	public String getUrl() {
		return url;
	}
	
//	private ArrayList<String> getImages() {
//		ArrayList<String> images = new ArrayList<String>();
//		images.add("http://i.imgur.com/IFD14.jpg");
//		images.add("http://i.imgur.com/LjFRNha.jpg");
//		images.add("http://i.imgur.com/EwNtfRg.jpg");
//		images.add("http://i.imgur.com/mvvvzev.jpg");
//		images.add("http://i.imgur.com/tSnq7DN.jpg");
//		images.add("http://i.imgur.com/xZluhvb.jpg");
//		images.add("http://i.imgur.com/Sa4051z.jpg");
//		images.add("http://i.imgur.com/fZVtcV8.jpg");
//		images.add("http://i.imgur.com/5o5CWCH.jpg");
//		images.add("http://i.imgur.com/czV6ehQ.jpg");
//		images.add("http://i.imgur.com/EUKwR9q.jpg");
//		images.add("http://i.imgur.com/F4ism2b.jpg");
//		images.add("http://i.imgur.com/v4kuFJA.jpg");
//		images.add("http://i.imgur.com/dD07qwb.jpg");
//		images.add("http://i.imgur.com/l73Dt6e.jpg");
//		images.add("http://i.imgur.com/04g2yoo.jpg");
//		images.add("http://i.imgur.com/6g9l7g0.jpg");
//		images.add("http://i.imgur.com/9CUEgtX.jpg");
//		images.add("http://i.imgur.com/vrxUHRW.jpg");
//		images.add("http://i.imgur.com/zJ38KNr.jpg");
//		return images;
//	}

}
