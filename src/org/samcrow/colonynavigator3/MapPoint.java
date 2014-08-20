package org.samcrow.colonynavigator3;

import org.samcrow.colonynavigator3.data.Colony;



/**
 * A latitude and longitude that is mapped to the location of a colony
 * 
 * @author Sam Crow
 */
public class MapPoint {

	/**
	 * A reference to the colony that this is the location of
	 */
	private Colony colony;

	private double latitude;

	private double longitude;

	/**
	 * Constructor
	 * 
	 * @param colony
	 * @param latitude
	 * @param longitude
	 */
	public MapPoint(Colony colony, double latitude, double longitude) {
		this.colony = colony;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Get the GPS latitude of this point
	 * 
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get the GPS longitude of this point
	 * 
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Get the local X-axis location of this point
	 * 
	 * @return The X location
	 */
	public double getX() {
		return colony.getX();
	}

	/**
	 * Get the local Y-axis location of this point
	 * 
	 * @return The Y location
	 */
	public double getY() {
		return colony.getY();
	}

}
