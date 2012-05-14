package com.uofc.roomfinder.android.activities.adapter;

import com.uofc.roomfinder.entities.routing.RoutePoint;

/**
 * represents the data of one list element in the quicklinks menu
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class QuicklinkDataset {

	protected int imageId;
	protected String title;
	private RoutePoint destination;

	// constructor
	public QuicklinkDataset(int imageId, String title, RoutePoint destination) {
		super();
		this.imageId = imageId;
		this.title = title;
		this.destination = destination;
	}

	// getter & setter
	public RoutePoint getDestination() {
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
