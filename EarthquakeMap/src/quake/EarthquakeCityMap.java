package quake;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 */
public class EarthquakeCityMap extends PApplet {
	
	//It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	// HashMap new defining to see the country name associated with the number of earthquakes
	// in the country
	private HashMap<String, Integer> dangerMap;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
	
	    map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
		
		MapUtils.createDefaultEventDispatcher(this, map);
		
		
		earthquakesURL = "result1.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		map.addMarkers(countryMarkers);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
	    dangerMap = printQuakes();
	    shadeCountries();
	 		
	    // (3) Add markers to map
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;	
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	private void selectMarkerIfHover(List<Marker> markers)
	{
		boolean found = false;

	    for (Marker marker : markers){
	    	if (!found) {
	    		if (lastSelected == null && marker.isInside(map, mouseX, mouseY)) {
	    			lastSelected = (CommonMarker) marker;
	    			lastSelected.setSelected(true);
	    			found = true;
	    		}
	    else
	    	found = false;
	    	}	
	    }
	}
	
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	/** The event handler for mouse clicks

	* It will display an earthquake and its threat circle of cities

	* Or if a city is clicked, it will display all the earthquakes 

	* where the city is in the threat circle

	*/

	@Override

	public void mouseClicked(){
	if (lastClicked != null) {
		unhideMarkers();
		lastClicked = null;
	}
	else {
		earthquakeMarkerClicked();
	    if (lastClicked == null)
	    cityMarkerClicked();
	}
}
	
	private void earthquakeMarkerClicked() {
		for (Marker earthquake : quakeMarkers) {
			EarthquakeMarker earthquakeMarker = (EarthquakeMarker) earthquake;
			if (earthquakeMarker.isInside(map, mouseX, mouseY)) {
				for (Marker temp : quakeMarkers) {
					/*if (earthquakeMarker.getDistanceTo(temp.getLocation()) <= earthquakeMarker.threatCircle()) {
						temp.setHidden(false);
					}
					else*/
						temp.setHidden(true);
				}//end for

				for (Marker temp2 : cityMarkers) {
					if (earthquakeMarker.getDistanceTo(temp2.getLocation()) <= earthquakeMarker.threatCircle()) {
						temp2.setHidden(false);
					}
					else
						temp2.setHidden(true);
				}//end for
				lastClicked = earthquakeMarker;
				earthquake.setHidden(false);

			}// end if

		}// end for	
	}
	
	private void cityMarkerClicked() {
		for (Marker cityMarker: cityMarkers) {
			//EarthquakeMarker earthquakeMarker = (EarthquakeMarker) earthquake;
			if (cityMarker.isInside(map, mouseX, mouseY)) {
				for (Marker temp : quakeMarkers) {
					if (cityMarker.getDistanceTo(temp.getLocation()) <= ((EarthquakeMarker) temp).threatCircle()) {
						temp.setHidden(false);
			        }
					else
						temp.setHidden(true);
				}//end for
				for (Marker temp2 : cityMarkers) {
					temp2.setHidden(true);
				}//end for
				lastClicked = (CommonMarker) cityMarker;
				cityMarker.setHidden(false);
			}// end if
		}// end for
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);
		
		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);

	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  
	private boolean isLand(PointFeature earthquake) 
	{
		for (Marker country: countryMarkers){
			if (isInCountry(earthquake, country))
				return true;
			}

		// not inside any country
		return false;
		}
	
	// helper method to visualize the statistics of quakes in each country
	private HashMap<String, Integer> printQuakes() 
	{
		HashMap<String, Integer> dangerMap = new HashMap<String, Integer>();
		int sumLandCount = 0;
		for (Marker countryMarker : countryMarkers) {
			String countryName = countryMarker.getStringProperty("name");
			int landCount = 0;
			for (Marker quake : quakeMarkers){
				if (quake instanceof LandQuakeMarker){
					if (countryMarker.getProperty("name").equals(quake.getProperty("country")))
						landCount++;
					}
				}
			sumLandCount += landCount;
			if (landCount > 0) {
				dangerMap.put(countryName, landCount);
				System.out.println(countryMarker.getProperty("name") + ": " + landCount);
			}
		}
		System.out.println("OCEAN QUAKES:" + (quakeMarkers.size() - sumLandCount));
		return dangerMap;
	// test

	/*Marker country = countryMarkers.get(0);

	System.out.println("countryMarkers' properties: " + country.getProperties());

	Marker quake = quakeMarkers.get(0);

	System.out.println("quakeMarkers' properties: " + quake.getProperties());

	*/
	}
	
	private void shadeCountries() {
		for (Marker marker : countryMarkers) {
		// Find data for country of the current marker
		//String countryId = marker.getId();
		String name = marker.getStringProperty("name");
		//System.out.println(countryId);
		//System.out.println(marker.getStringProperty("name"));
			if (dangerMap.containsKey(name)) {
				int num = dangerMap.get(name);
				//System.out.println("number of quakes: " + num);
				// Encode value as brightness (values range: 40-90)
				int colorLevel = (int) map(num, 1, 20, 10, 255);
				System.out.println("colorlevel = " + colorLevel);
				marker.setColor(color(255-colorLevel, 100, colorLevel));
			}
			else {
				marker.setColor(color(150,150,150));
			}
		}

	}


	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake 
	// feature if it's in one of the countries.
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
