/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

package org.mixare;

import java.text.DecimalFormat;

import org.mixare.data.DataSource;
import org.mixare.gui.PaintScreen;
import org.mixare.gui.TextObj;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.location.Location;

/**
 * This markers represent the points of interest. On the screen they appear as circles, since this class inherits the draw method of the Marker.
 * 
 * @author hannes
 * 
 */
public class FriendMarker extends Marker {

	public static final int MAX_OBJECTS = 20;
	public static final int OSM_URL_MAX_OBJECTS = 5;

	MixContext ctx;

	public FriendMarker(String title, double latitude, double longitude, double altitude, String URL, DataSource datasource, MixContext ctx) {
		super(title, latitude, longitude, altitude, URL, datasource);
		this.ctx = ctx;
	}

	@Override
	public void update(Location curGPSFix) {
		super.update(curGPSFix);
	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

	@Override
	public void drawCircle(PaintScreen dw) {

		// if (isVisible) {
		// float maxHeight = dw.getHeight();
		// dw.setStrokeWidth(maxHeight / 100f);
		// dw.setFill(false);
		//
		// dw.setColor(getColour());
		// // dw.setColor(Marker.MARKER_CIRCLE_COLOR);
		//
		// // draw circle with radius depending on distance
		// // 0.44 is approx. vertical fov in radians
		// double angle = 2.0 * Math.atan2(10, distance);
		// // double radius = Marker.MARKER_CIRCLE_SIZE;
		// double radius = Math.max(Math.min(angle / 0.44 * maxHeight, maxHeight), maxHeight / 25f);
		//
		// /*
		// * distance 100 is the threshold to convert from circle to another shape
		// */
		// if (distance < 100.0)
		// otherShape(dw);
		// else
		// dw.paintCircle((cMarker.x), (cMarker.y), (float) radius);
		//
		// }
	}

	@Override
	public void drawTextBlock(PaintScreen dw) {
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
		// TODO: change textblock only when distance changes

		String textStr = "";

		double d = distance;
		DecimalFormat df = new DecimalFormat("@#");
		if (d < 1000.0) {
			textStr = title + "\n (" + df.format(d) + "m)";
		} else {
			d = d / 1000.0;
			textStr = title + "\n (" + df.format(d) + "km)";
		}

		// textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250, dw, underline);
		textBlock = new TextObj(textStr, 20, 250, dw, underline);

		if (isVisible) {
			// based on the distance set the colour
			if (distance < 100.0) {
				textBlock.setBgColor(Color.argb(180, 52, 52, 52));
				textBlock.setBorderColor(Color.rgb(255, 104, 91));
			} else {
				textBlock.setBgColor(Color.argb(180, 0, 0, 0));
				textBlock.setBorderColor(Color.rgb(255, 255, 255));
			}
			// dw.setColor(DataSource.getColor(type));

			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);
			txtLab.prepare(textBlock);
			dw.setStrokeWidth(1f);
			dw.setFill(true);
			// dw.paintObjectBottom(txtLab, dw.getWidth()/2 - txtLab.getWidth() / 2, (float) (dw.getHeight() * 0.75), currentAngle + 90, 1);

			Bitmap friendSymbol = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.friend);

			// rotate img
			// Matrix matrix = new Matrix();
			// matrix.postRotate(270);
			// friendSymbol = Bitmap.createBitmap(friendSymbol, 0, 0, friendSymbol.getWidth(), friendSymbol.getHeight(), matrix, true);

			// resize img
			int width = friendSymbol.getWidth();
			int height = friendSymbol.getHeight();
			float scaleWidth = ((float) 100) / width;
			float scaleHeight = ((float) 100) / height;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			friendSymbol = Bitmap.createBitmap(friendSymbol, 0, 0, width, height, matrix, false);

			// canvas.drawColor(Color.BLACK);
			dw.paintBitmap(friendSymbol, signMarker.x, signMarker.y - 200, currentAngle + 90);

			// dw.paintCircle((cMarker.x), (cMarker.y), (float) 5);

			// dw.paintBitmap(bitmap, left, top)
			dw.paintObj(txtLab, signMarker.x - txtLab.getWidth() / 2, signMarker.y + (maxHeight + 40) - 200, currentAngle + 90, 1);
			// dw.paintObj(txtLab, signMarker.x , signMarker.y , currentAngle + 90, 1);

		}
	}

	public void otherShape(PaintScreen dw) {
		// This is to draw new shape, triangle
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		dw.setColor(getColour());
		float radius = maxHeight / 1.5f;
		dw.setStrokeWidth(dw.getHeight() / 100f);
		dw.setFill(false);

		Path tri = new Path();
		float x = 0;
		float y = 0;
		tri.moveTo(x, y);
		tri.lineTo(x - radius, y - radius);
		tri.lineTo(x + radius, y - radius);

		tri.close();
		dw.paintPath(tri, cMarker.x, cMarker.y, radius * 2, radius * 2, currentAngle + 90, 1);
	}

}
