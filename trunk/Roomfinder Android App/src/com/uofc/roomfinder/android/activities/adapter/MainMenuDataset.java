package com.uofc.roomfinder.android.activities.adapter;

/**
 * represents the data of one list element in the main menu
 * 
 * @author benjaminlautenschlaeger
 *
 */
public class MainMenuDataset {

	protected int mId;
	protected String mTitle;
	protected String mDetail;

	public MainMenuDataset(int id, String title, String detail) {
		mId = id;
		mTitle = title;
		mDetail = detail;
	}

	@Override
	public String toString() {
		return mId + " " + mTitle + " " + mDetail;
	}

}
