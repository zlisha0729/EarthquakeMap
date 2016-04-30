package quake;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

/** Implements a visual marker for earthquakes on an earthquake map
 * 
 *
 */
public abstract class EarthquakeMarker extends CommonMarker
{
	
	protected boolean isOnLand;

	// The radius of the Earthquake marker
	protected float radius;
	
	protected static final float kmPerMile = 1.6f;
	/** Greater than or equal to this threshold is a moderate earthquake */
	public static final float THRESHOLD_MODERATE = 5;
	/** Greater than or equal to this threshold is a light earthquake */
	public static final float THRESHOLD_LIGHT = 4;

	/** Greater than or equal to this threshold is an intermediate depth */
	public static final float THRESHOLD_INTERMEDIATE = 70;
	/** Greater than or equal to this threshold is a deep depth */
	public static final float THRESHOLD_DEEP = 300;

	
	// abstract method implemented in derived classes
	public abstract void drawEarthquake(PGraphics pg, float x, float y);
		
	
	// constructor
	public EarthquakeMarker (PointFeature feature) 
	{
		super(feature.getLocation());
		// Add a radius property and then set the properties
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2*magnitude );
		setProperties(properties);
		this.radius = 1.75f*getMagnitude();
	}
	

	// calls abstract method drawEarthquake and then checks age and draws X if needed
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		// save previous styling
		pg.pushStyle();
			
		colorDetermine(pg);
		
		// call abstract method implemented in child class to draw marker shape
		drawEarthquake(pg, x, y);
			
		
		// reset to previous styling
		pg.popStyle();
		
	}
	
	// determine color of marker from depth
	// Deep: red, intermediate: blue, shallow: yellow
	private void colorDetermine(PGraphics pg) {
		if (getDepth() <= 70 && getDepth() > 0)
			pg.fill(255, 255, 0);
		else if (getDepth() <= 300 && getDepth() > 70)
			pg.fill(0, 0, 255);
		else
			pg.fill(255, 0, 0);
	}
	
	/** Show the title of the earthquake if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y)
	{
		String title = getTitle();
		pg.pushStyle();
		
		pg.rectMode(PConstants.CORNER);
		
		pg.stroke(110);
		pg.fill(255,255,255);
		pg.rect(x, y + 15, pg.textWidth(title) +6, 18, 5);
		
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title, x + 3 , y +18);
		
		
		pg.popStyle();
		
	}
	
	public double threatCircle() {	
		double miles = 20.0f * Math.pow(1.8, 2*getMagnitude()-5);
		double km = (miles * kmPerMile);
		return km;
	}

	/*
	 * getters for earthquake properties
	 */
	
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());}
	
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());	
	}
	
	public String getTitle() {
		return (String) getProperty("title");	
		
	}
	
	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}
	
	public boolean isOnLand()
	{
		return isOnLand;
	}
	
	
}
