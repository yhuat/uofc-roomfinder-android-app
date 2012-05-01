package com.uofc.roomfinder.android.views;

import java.util.LinkedList;
import java.util.List;

import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.entities.routing.RouteSegment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class MapNavBarView extends View {

	private final int NAVBAR_PADDING_VERTICAL = 30;
	private final int NAVBAR_PADDING_HORIZONTAL = 30;
	private final int NAVBAR_HEIGHT = 60;

	private Paint paint = new Paint();
	private List<RouteSegment> routeSegments;
	private List<Rect> navbarParts = new LinkedList<Rect>();
	private int screenWidth;
	private int activeElement;

	// constructors
	public MapNavBarView(Context context) {
		super(context);
		this.init();
	}

	public MapNavBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	public MapNavBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}

	// getter & setter
	public List<Rect> getNavbarParts() {
		return navbarParts;
	}

	public void setActiveElement(int i) {
		this.invalidate();
		this.activeElement = i;
	}

	// methods
	private void init() {
		WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		this.screenWidth = display.getWidth();
	}

	/**
	 * creates a navbar displays a clickable rectangle for each route segment
	 * 
	 * @param routeSegments
	 */
	public void createNavigationBar(List<RouteSegment> routeSegments) {
		System.out.println("create nav bar");
		this.routeSegments = routeSegments;

	}

	@Override
	public void onDraw(Canvas canvas) {
		this.paint = new Paint();
		this.navbarParts = new LinkedList<Rect>();

		int horizontalOffset = NAVBAR_PADDING_HORIZONTAL;

		// if segments are set -> draw
		if (routeSegments != null) {

			// screen width minus padding on the left and on the right side of the bar
			int navbarWidth = this.screenWidth - (2 * NAVBAR_PADDING_HORIZONTAL);
			double routeLength = DataModel.getInstance().getRoute().getLength();

			// draw navbar
			// paint.setColor(Color.BLUE);
			paint.setARGB(128, 100, 100, 100);

			paint.setStrokeWidth(3);
			canvas.drawRect(NAVBAR_PADDING_HORIZONTAL - 3, NAVBAR_PADDING_VERTICAL - 3, this.screenWidth - NAVBAR_PADDING_HORIZONTAL, NAVBAR_HEIGHT
					+ NAVBAR_PADDING_VERTICAL + 3, paint);

			paint.setARGB(128, 255, 255, 255);

			int i = 0;
			for (RouteSegment segment : this.routeSegments) {
				//if segment is the active segment -> change the color
				if (i++ == activeElement){
					paint.setARGB(128, 155, 155, 155);		
				}else{
					paint.setARGB(128, 255, 255, 255);
				}
				
				int x = navbarWidth / this.routeSegments.size();
				Rect newRectangle = new Rect(horizontalOffset, NAVBAR_PADDING_VERTICAL, horizontalOffset + x - 3, NAVBAR_HEIGHT + NAVBAR_PADDING_VERTICAL);
				navbarParts.add(newRectangle);

				canvas.drawRect(newRectangle, paint);
				horizontalOffset += x;
			}
		}
	}
}
