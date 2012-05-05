package com.uofc.roomfinder.android.activities.adapter;

/**
 * represents the data of one list element in the quicklinks menu
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class QuicklinkDataset {

	protected int imageId;
	protected String title;
	private String destRoom;
	private String destBuilding;

	// constructor
	public QuicklinkDataset(int imageId, String title, String destBuilding, String destRoom) {
		super();
		this.imageId = imageId;
		this.title = title;
		this.destRoom = destRoom;
		this.destBuilding = destBuilding;
	}

	// getter & setter
	public String getDestRoom() {
		return destRoom;
	}

	public String getDestBuilding() {
		return destBuilding;
	}

	@Override
	public String toString() {
		return imageId + " " + title + this.getDestRoom();
	}

}
