package com.uofc.roomfinder.android.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.core.geometry.Envelope;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.threads.MapViewUpdater;
import com.uofc.roomfinder.entities.Point3D;
import com.uofc.roomfinder.util.Util;

/**
 * this is the class for the customized map view it has all layers of the campus added and handles layer switching (different layers for different building
 * floors)
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class CampusMapView extends MapView {

	private static final int BUILDING_LAYER_INDEX = 1;

	GraphicsLayer graphicsLayer;
	GraphicsLayer graphicsLayerLocations;
	ArcGISDynamicMapServiceLayer roomLayer;
	ArcGISDynamicMapServiceLayer buildingLayer;
	ArcGISDynamicMapServiceLayer aerialLayer;
	private String activeFloor;

	// constructors
	public CampusMapView(Context context) {
		super(context);
		init();
	}

	public CampusMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CampusMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	// methods
	public void init() {
		System.out.println("campus map init startet");
		/*
		 * // WTF?????? without this layer no layer is displayed, I have no Idea why! // simple workaround: add it and set the visibility to false...
		 * ArcGISDynamicMapServiceLayer beastLayer = new ArcGISDynamicMapServiceLayer(
		 * "http://asebeast2.cpsc.ucalgary.ca:7000/ArcGIS/rest/services/RoomFinder/MapServer"); beastLayer.setVisible(false); //this.addLayer(beastLayer);
		 */

		// layer for arial image
		// this.aerialLayer = new ArcGISDynamicMapServiceLayer(Constants.MAPSERVER_AERIAL_URL);
		// this.aerialLayer.setVisible(true);
		// this.aerialLayer.setOpacity(0.8f);
		// this.addLayer(this.aerialLayer);

		// layer for buildings
		this.buildingLayer = new ArcGISDynamicMapServiceLayer(Constants.MAPSERVER_BUILDINGS_URL);
		this.buildingLayer.setOpacity(0.4f);
		this.addLayer(this.buildingLayer);

		// layer for rooms
		this.roomLayer = new ArcGISDynamicMapServiceLayer(Constants.MAPSERVER_ROOMS_URL);
		this.roomLayer.setVisible(false);
		this.addLayer(this.roomLayer);

		// graphics layer for routes and POIs
		graphicsLayer = new GraphicsLayer();
		SimpleRenderer sr = new SimpleRenderer(new SimpleFillSymbol(Color.RED));
		graphicsLayer.setRenderer(sr);
		this.addLayer(graphicsLayer);

		// graphics layer for location points
		graphicsLayerLocations = new GraphicsLayer();
		this.addLayer(graphicsLayerLocations);

		// zoom out limit
		this.setMaxResolution(10000.0);

		// initial extent
		Envelope initialExtent = new Envelope(700943.040902211, 5662584.8982894, 701206.536124156, 5662724.644069);
		this.setExtent(initialExtent); // } }

		// wait until map is initialized, max 15sec
		int i = 0;
		while (!this.getLayer(BUILDING_LAYER_INDEX).isInitialized() && i < 15) {
			try {
				System.out.println("waiting for map init, sleep 1000");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// display ground layer on initial load
		this.activeFloor = "01";
		displayLayer("01");

		// init map drawer thread (draws current location on map view)
		Thread backgroundThread = new Thread(new MapViewUpdater());
		backgroundThread.start();

		System.out.println("campus map init ended");
	}

	/**
	 * 
	 * @param floor
	 */
	private void displayLayer(String floor) {
		System.out.println("displaying layer of floor: " + floor);

		// Query dynamic map service layer
		ArcGISDynamicMapServiceLayer dynamicLayer = (ArcGISDynamicMapServiceLayer) this.getLayer(BUILDING_LAYER_INDEX);

		// Retrieve layer info for each sub-layer of the dynamic map service layer.
		ArcGISLayerInfo[] layerinfos = dynamicLayer.getLayers();

		// set each layer invisible
		for (ArcGISLayerInfo info : layerinfos) {
			info.setVisible(false);
		}

		// set the one layer visible
		for (ArcGISLayerInfo info : layerinfos) {
			if (info.getName().contains(floor)) {
				System.out.println("layer found, setting to visible");
				info.setVisible(true);
			}
		}
		dynamicLayer.setVisible(true);
		dynamicLayer.refresh();
	}

	/**
	 * sets active height and switches the room layer to corresponding floor
	 * 
	 * @param floorNumber
	 */
	public void setActiveFloor(String floorNumber) {
		// if new floor is different
		if (!this.activeFloor.equals(floorNumber)) {
			this.activeFloor = floorNumber;
			displayLayer(this.activeFloor);
		}
	}

	/**
	 * sets active height and switches the room layer to corresponding floor
	 * 
	 * @param heightInMeter
	 */
	public void setActiveHeight(double heightInMeter) {
		System.out.println("setting height in meters to: " + heightInMeter);
		long longMeter = Math.round(heightInMeter);

		// determine floor name
		if (longMeter % 4 == 0) {
			long longFloor = longMeter / 4 + 1;
			String strFloor = Util.rPad("" + longFloor, 2, '0');
			this.setActiveFloor(strFloor);
		} else {
			System.err.println("height could not be parsed: " + heightInMeter);
		}
	}

	/**
	 * sets active height and switches the room layer to corresponding floor
	 * 
	 * @param point
	 */
	public void setActiveHeight(Point3D point) {
		setActiveHeight(point.getZ());
	}

	// getter & setter
	public GraphicsLayer getGraphicsLayer() {
		return graphicsLayer;
	}

	public boolean isArialEnabled() {
		return this.aerialLayer.isVisible();
	}

	public void enableArialImage() {
		this.aerialLayer.setVisible(true);
		this.aerialLayer.refresh();
		System.out.println("enable");
	}

	public void disableArialImage() {
		this.aerialLayer.setVisible(false);
		this.aerialLayer.refresh();
		System.out.println("disable");
	}

	public GraphicsLayer getGraphicsLayerLocations() {
		return graphicsLayerLocations;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		System.out.println("map touch");
		return false;
	}
	

}
