package com.uofc.roomfinder.android.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.core.geometry.Envelope;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.routing.RoutePoint;
import com.uofc.roomfinder.util.Util;

/**
 * this is the class for the customized map view it has all layers of the campus added and handles layer switching (different layers for different building
 * floors)
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class CampusMapView extends MapView {

	private static final int BUILDING_LAYER_INDEX = 0;

	GraphicsLayer graphicsLayer;
	ArcGISDynamicMapServiceLayer roomLayer;
	ArcGISDynamicMapServiceLayer buildingLayer;
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

		// layer for rooms
		this.roomLayer = new ArcGISDynamicMapServiceLayer("http://136.159.24.32/ArcGIS/rest/services/Rooms/Rooms/MapServer");
		this.roomLayer.setVisible(false);
		this.addLayer(this.roomLayer);

		// add layer for buildings
		this.buildingLayer = new ArcGISDynamicMapServiceLayer(Constants.MAPSERVER_BUILDINGS_URL);
		this.buildingLayer.setOpacity(0.4f);
		this.addLayer(this.buildingLayer);

		this.setMaxResolution(10000.0);

		// graphics layer for routes and POIs
		graphicsLayer = new GraphicsLayer();
		SimpleRenderer sr = new SimpleRenderer(new SimpleFillSymbol(Color.RED));
		graphicsLayer.setRenderer(sr);
		this.addLayer(graphicsLayer);

		// initial extent
		Envelope initialExtent = new Envelope(700943.040902211, 5662584.8982894, 701206.536124156, 5662724.644069);
		this.setExtent(initialExtent); // } }

		// wait until map is initialized
		while (!this.getLayer(0).isInitialized()) {
			try {
				System.out.println("sleep 100");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		displayLayer("01");

		System.out.println("campus map init ended");
	}

	/**
	 * 
	 * @param floor
	 */
	private void displayLayer(String floor) {
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
		System.out.println("setting floor to: " + floorNumber);
		this.activeFloor = floorNumber;
		displayLayer(this.activeFloor);
	}

	/**
	 * sets active height and switches the room layer to corresponding floor
	 * 
	 * @param heightInMeter
	 */
	public void setActiveHeight(double heightInMeter) {
		long longMeter = Math.round(heightInMeter);

		if (longMeter % 4 == 0) {
			long longFloor = longMeter / 4 + 1;
			String strFloor = Util.rPad("" + longFloor, 2, '0');
			this.setActiveFloor(strFloor);
		}else{
			System.err.println("height could not be parsed: " + heightInMeter);
		}
	}
	
	/**
	 * sets active height and switches the room layer to corresponding floor
	 * 
	 * @param point
	 */
	public void setActiveHeight(RoutePoint point) {
		setActiveHeight(point.getZ());
	}

	// getter & setter
	public GraphicsLayer getGraphicsLayer() {
		return graphicsLayer;
	}

	

}
