package quake;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;


public abstract class CommonMarker extends SimplePointMarker {
	
	public CommonMarker(Location location) {
		super(location);
	}
	
	public CommonMarker(Location location, java.util.HashMap<java.lang.String,java.lang.Object> properties) {
		super(location, properties);
	}
	

	// Common piece of drawing method for markers; 
	public void draw(PGraphics pg, float x, float y) {
		// For starter code just drawMaker(...)
		if (!hidden) {
			drawMarker(pg, x, y);
			if (selected) {
				showTitle(pg, x, y); 
			}
		}
	}
	public abstract void drawMarker(PGraphics pg, float x, float y);
	public abstract void showTitle(PGraphics pg, float x, float y);
}