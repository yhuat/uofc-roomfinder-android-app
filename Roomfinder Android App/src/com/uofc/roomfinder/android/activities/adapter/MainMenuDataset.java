package com.uofc.roomfinder.android.activities.adapter;

/**
 * represents the data of one list element in the main menu
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class MainMenuDataset {

	protected int imageId;
	protected String title;
	protected String detail;

	public MainMenuDataset(int id, String title, String detail) {
		this.imageId = id;
		this.title = title;
		this.detail = detail;
	}

	@Override
	public String toString() {
		return imageId + " " + title + " " + detail;
	}

}
