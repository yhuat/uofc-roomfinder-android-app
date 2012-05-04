package com.uofc.roomfinder.android.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.util.Util;

/**
 * this is the class for the customized map view it has all layers of the campus added and handles layer switching (different layers for different building
 * floors)
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class CampusMapView extends MapView {

	GraphicsLayer graphicsLayer;
	ArcGISDynamicMapServiceLayer roomLayer;
	private String activeFloor;

	// constructors
	public CampusMapView(Context context) {
		super(context);
		this.init();
	}

	public CampusMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	public CampusMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}

	// methods
	public void init() {
		System.out.println("campus map init startet");
		// WTF?????? without this layer no layer is displayed, I have no Idea why!
		// simple workaround: add it and set the visibility to false...
		ArcGISDynamicMapServiceLayer beastLayer = new ArcGISDynamicMapServiceLayer(
				"http://asebeast2.cpsc.ucalgary.ca:7000/ArcGIS/rest/services/RoomFinder/MapServer");
		beastLayer.setVisible(false);
		this.addLayer(beastLayer);

		// layer for rooms
		this.roomLayer = new ArcGISDynamicMapServiceLayer("http://136.159.24.32/ArcGIS/rest/services/Rooms/Rooms/MapServer");
		this.roomLayer.setVisible(false);
		this.addLayer(this.roomLayer);

		// add layer for buildings
		ArcGISDynamicMapServiceLayer buildingLayer = new ArcGISDynamicMapServiceLayer(Constants.MAPSERVER_BUILDINGS_URL);
		buildingLayer.setOpacity(0.4f);
		this.addLayer(buildingLayer);

		this.setMaxResolution(10000.0);

		// graphics layer for routes and POIs
		graphicsLayer = new GraphicsLayer();
		SimpleRenderer sr = new SimpleRenderer(new SimpleFillSymbol(Color.RED));
		graphicsLayer.setRenderer(sr);
		this.addLayer(graphicsLayer);

		// wait until map is initialized
		while (!this.getLayer(1).isInitialized()) {
			try {
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
		ArcGISDynamicMapServiceLayer dynamicLayer = (ArcGISDynamicMapServiceLayer) this.getLayer(1);

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
		}
	}

	// getter & setter
	public GraphicsLayer getGraphicsLayer() {
		return graphicsLayer;
	}

}
