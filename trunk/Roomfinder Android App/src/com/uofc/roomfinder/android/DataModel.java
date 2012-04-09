/**
 * 
 */
package com.uofc.roomfinder.android;

import com.uofc.roomfinder.entities.routing.RoutePoint;

/**
 * @author benjaminlautenschlaeger
 * 
 */
public class DataModel {

	private static DataModel instance = null;
	
	RoutePoint currentPosition;
	
	
	//constructor
	protected DataModel() {

	}

	//singleton
	public static DataModel getInstance() {
		if (instance == null) {
			instance = new DataModel();
		}
		return instance;
	}

	
	
	//getter&setter
	public RoutePoint getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(RoutePoint currentPosition) {
		this.currentPosition = currentPosition;
	}

}
