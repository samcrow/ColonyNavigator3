package org.samcrow.colonynavigator3;

import org.mapsforge.core.model.LatLong;
import org.samcrow.colonynavigator3.data.Colony;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * Transforms coordinates from GPS latitude/longitude into local colony
 * coordinates. <br />
 * To do this, this class takes the GPS coordinates adn:
 * <ol>
 * <li>Translates them</li>
 * <li>Rotates them around the local zero point</li>
 * <li>Scales them around the local zero point</li>
 * </ol>
 * 
 * @author Sam Crow
 */
public class CoordinateTransformer {

	private Matrix matrix = new Matrix();
	
	private Matrix inverse = new Matrix();

	/**
	 * Constructor that uses the local position and latitude/longitude of 2
	 * points to calculate offsets, rotation, & scale
	 * @param points Up to 4 points to use to map coordinates
	 */
	private CoordinateTransformer(MapPoint... points) {

		if(points.length > 4) {
			throw new IllegalArgumentException("Cannot use more than 4 points.");
		}

		float[] sourcePoints = new float[points.length * 2];

		float[] destPoints = new float[points.length * 2];

		int i = 0;
		for(MapPoint point : points) {
			//Source points: latitude/longitude
			sourcePoints[2 * i] = (float) point.getLongitude(); //Longitude = x
			System.out.println("X (lon) "+2*i+" "+sourcePoints[2*i]);
			sourcePoints[2 * i + 1] = (float) point.getLatitude();//Latitude = y
			System.out.println("Y (lat) "+(2*i+1)+" "+sourcePoints[2*i+1]);


			//Destination points: colony coordinates
			destPoints[2 * i] = (float) point.getX();
			System.out.println("Dest X "+2*i+" "+destPoints[2*i]);
			destPoints[2 * i + 1] = (float) point.getY();
			System.out.println("Dest Y "+(2*i+1)+" "+destPoints[2*i+1]);

			i++;
		}

		boolean success = matrix.setPolyToPoly(sourcePoints, 0, destPoints, 0, points.length);
		if(!success) {
			throw new RuntimeException("Could not set up matrix");
		}
		
		boolean invertSuccess = matrix.invert(inverse);
		if(!invertSuccess) {
			throw new RuntimeException("Could not invert matrix");
		}

		System.out.println(matrix);
	}

	/**
	 * Transform given GPS coordinates into local coordinates.
	 * 
	 * @param longitude
	 *            The longitude (X-axis location)
	 * @param latitude
	 *            The latitude (Y-axis location);
	 * @return A point with the transformed coordinates
	 */
	public PointF toLocal(double longitude, double latitude) {
		// Explicitly extract the primitive values from the objects
		double x = longitude;
		double y = latitude;
		float[] points = new float[] { (float) x, (float) y };

		matrix.mapPoints(points);

		return new PointF(points[0], points[1]);
	}

	/**
	 * Transform the given local coordinates into GPS coordinates
	 * 
	 * @param x
	 *            The X location
	 * @param y
	 *            The y location
	 * @return A point with the longitude mapped to x and the latitude mapped to
	 *         y
	 */
	public LatLong toGps(float x, float y) {

		float[] points = new float[] { x, y };

		inverse.mapPoints(points);

		return new LatLong(points[1], points[0]);

	}
	
	private static CoordinateTransformer instance;
	
	public static CoordinateTransformer getInstance() {
		
		if(instance == null) {
			
			final MapPoint topLeft = new MapPoint(new Colony(962, 68, 707, false), 31.87265776, -109.04243);
			final MapPoint bottomRight = new MapPoint(new Colony(980, 1324, 289, false), 31.87087500797029, -109.03870950670428);
			final MapPoint bottomLeft = new MapPoint(new Colony(567, 110, 110, false), 31.871036, -109.042678);
			@SuppressWarnings("unused")
			final MapPoint topRight = new MapPoint(new Colony(442, 110, 782, false), 31.872357, -109.0391114);
			
			instance = new CoordinateTransformer(topLeft, bottomRight, bottomLeft);
		}
		
		return instance;
	}
}
