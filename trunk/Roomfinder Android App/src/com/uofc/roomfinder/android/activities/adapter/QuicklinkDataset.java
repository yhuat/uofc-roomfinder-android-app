package com.uofc.roomfinder.android.activities.adapter;

import com.uofc.roomfinder.entities.Point3D;

/**
 * represents the data of one list element in the quicklinks menu
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class QuicklinkDataset {

	protected int imageId;
	protected String title;
	private Point3D destination;

	// constructor
	public QuicklinkDataset(int imageId, String title, Point3D destination) {
		super();
		this.imageId = imageId;
		this.title = title;
		this.destination = destination;
	}

	// getter & setter
	public Point3D getDestination() {
		return destination;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return imageId + " " + title + " - zcoord: " + this.getDestination().getZ();
	}

}
