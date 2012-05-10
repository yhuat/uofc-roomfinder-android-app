package com.uofc.roomfinder.android.views;

import java.util.LinkedList;
import java.util.List;

import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.entities.routing.Gradient;
import com.uofc.roomfinder.entities.routing.RouteSegment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class RouteNavigationBar extends View {

	
	private static final int NAVBAR_TRANSPARENCY = 150; // values: 0-255 (255 = non transparent)

	
	private static final int NAVBAR_PADDING_VERTICAL_IN_DPI = 0;
	private static final int NAVBAR_PADDING_HORIZONTAL_IN_DPI = 2;
	private static final int NAVBAR_IMG_WIDTH_IN_DIP = 38;
	private static final int NAVBAR_HEIGHT_IN_DPI = 32;
	private static final int NAVBAR_BORDER_WIDTH_IN_DPI = 1;
	private static final int NAVBAR_SMALL_SEGMENT_WIDTH_IN_DPI = 40; // smallest width for a navbar segment

	// pixel values are calculated in the init method
	private static int NAVBAR_PADDING_VERTICAL;
	private static int NAVBAR_PADDING_HORIZONTAL;
	private static int NAVBAR_IMG_WIDTH;
	private static int NAVBAR_HEIGHT;
	private static int NAVBAR_BORDER_WIDTH;
	private static int NAVBAR_SMALL_SEGMENT_WIDTH;
	
	private Paint paint = new Paint();
	private List<RouteSegment> routeSegments;
	private List<Rect> navbarParts = new LinkedList<Rect>();
	private int screenWidth;
	private int activeElement;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// get navbar width
		int widthOfImgInPx = NAVBAR_IMG_WIDTH_IN_DIP * (int) getResources().getDisplayMetrics().density;
		int navbarWidth = (this.screenWidth - (2 * (NAVBAR_PADDING_HORIZONTAL + widthOfImgInPx))) + 2 * NAVBAR_BORDER_WIDTH;
		this.setMeasuredDimension(navbarWidth, NAVBAR_HEIGHT + 2 * NAVBAR_BORDER_WIDTH);
	}

	// constructors
	public RouteNavigationBar(Context context) {
		super(context);
		this.init();
	}

	public RouteNavigationBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	public RouteNavigationBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}

	// getter & setter
	public List<Rect> getNavbarParts() {
		return navbarParts;
	}

	public void setActiveElement(int i) {
		this.activeElement = i;
		this.invalidate();
	}

	// methods
	private void init() {
		WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		this.screenWidth = display.getWidth();

		int pixelDensity = (int) getResources().getDisplayMetrics().density;
		
		NAVBAR_PADDING_VERTICAL = NAVBAR_PADDING_VERTICAL_IN_DPI * pixelDensity;
		NAVBAR_PADDING_HORIZONTAL = NAVBAR_PADDING_HORIZONTAL_IN_DPI * pixelDensity;
		NAVBAR_IMG_WIDTH = NAVBAR_IMG_WIDTH_IN_DIP * pixelDensity;
		NAVBAR_HEIGHT = NAVBAR_HEIGHT_IN_DPI * pixelDensity;
		NAVBAR_BORDER_WIDTH = NAVBAR_BORDER_WIDTH_IN_DPI * pixelDensity;
		NAVBAR_SMALL_SEGMENT_WIDTH = NAVBAR_SMALL_SEGMENT_WIDTH_IN_DPI * pixelDensity;
	}

	/**
	 * creates a navbar displays a clickable rectangle for each route segment
	 * 
	 * @param routeSegments
	 */
	public void createNavigationBar(List<RouteSegment> routeSegments) {
		this.routeSegments = routeSegments;
		DataModel.getInstance().getMapActivity().enableNavBarLayout();
		this.invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		this.paint = new Paint();
		this.navbarParts = new LinkedList<Rect>();

		// if segments are set -> draw
		if (routeSegments != null) {

			int navbarWidth = this.screenWidth - (2 * (NAVBAR_PADDING_HORIZONTAL + NAVBAR_IMG_WIDTH));
			double routeLength = DataModel.getInstance().getRoute().getLength();
			double routeLengthWithoutSmallSegments = routeLength;

			// how many small segments are in route?
			int smallSegmentCounter = 0;
			for (RouteSegment segment : this.routeSegments) {
				if (segment.getGradient() == Gradient.DOWN || segment.getGradient() == Gradient.UP) {
					// if it goes up or down -> create small segment
					smallSegmentCounter++;
					routeLengthWithoutSmallSegments -= segment.getLength();
				} else if (segment.getLength() / routeLength * navbarWidth < NAVBAR_SMALL_SEGMENT_WIDTH) {
					// if length of segment is to small -> small segment
					smallSegmentCounter++;
					routeLengthWithoutSmallSegments -= segment.getLength();
				}
			}

			//System.out.println("small segments: " + smallSegmentCounter);

			// draw navbar background (used as boarder)
			paint.setARGB(NAVBAR_TRANSPARENCY, 100, 100, 100);
			paint.setStrokeWidth(3);

			int left = NAVBAR_PADDING_HORIZONTAL - NAVBAR_BORDER_WIDTH;
			int top = NAVBAR_PADDING_VERTICAL - NAVBAR_BORDER_WIDTH;
			int right = navbarWidth + 2 * NAVBAR_BORDER_WIDTH;
			int bottom = NAVBAR_HEIGHT + NAVBAR_PADDING_VERTICAL + 2 * NAVBAR_BORDER_WIDTH;
			canvas.drawRect(left, top, right, bottom, paint);

			// draw each segment
			int horizontalOffset = NAVBAR_PADDING_HORIZONTAL;
			int i = 0;
			for (RouteSegment segment : this.routeSegments) {

				// if segment is the active segment -> change the color
				if (i++ == activeElement) {
					paint.setARGB(NAVBAR_TRANSPARENCY, 109, 178, 100);
				} else {
					paint.setARGB(NAVBAR_TRANSPARENCY, 255, 255, 255);
				}

				// width which is left without small segments
				int navbarWidthWithoutSmallSegments = navbarWidth - (smallSegmentCounter * NAVBAR_SMALL_SEGMENT_WIDTH);
				int currentSegmentWidth;

				if (segment.getGradient() == Gradient.DOWN || segment.getGradient() == Gradient.UP) {
					// if it goes up or down -> create small segment
					currentSegmentWidth = NAVBAR_SMALL_SEGMENT_WIDTH;
				} else if (segment.getLength() / routeLength * navbarWidth < NAVBAR_SMALL_SEGMENT_WIDTH) {
					// if segment would be smaller than SMALL_SEGMENT_WIDTH set it to SMALL_SEGMENT_WIDTH
					currentSegmentWidth = NAVBAR_SMALL_SEGMENT_WIDTH;
				} else {
					// calculate width of current segment
					currentSegmentWidth = (int) Math.round(segment.getLength() / routeLengthWithoutSmallSegments * navbarWidthWithoutSmallSegments);
				}

				
				//System.out.println(currentSegmentWidth + "/" + navbarWidth);

				left = horizontalOffset;
				top = NAVBAR_PADDING_VERTICAL + NAVBAR_BORDER_WIDTH;
				right = horizontalOffset + currentSegmentWidth - NAVBAR_BORDER_WIDTH;
				bottom = NAVBAR_HEIGHT + NAVBAR_PADDING_VERTICAL + NAVBAR_BORDER_WIDTH;

				Rect newRectangle = new Rect(left, top, right, bottom);
				navbarParts.add(newRectangle);

				canvas.drawRect(newRectangle, paint);
				horizontalOffset += currentSegmentWidth;
			}
		}
	}
}
