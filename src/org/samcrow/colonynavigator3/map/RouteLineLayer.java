package org.samcrow.colonynavigator3.map;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class RouteLineLayer extends Polyline {

	private final NotifyingMyLocationOverlay locationLayer;
	
	private Marker destinationMarker = null;
	
	public RouteLineLayer(NotifyingMyLocationOverlay location) {
		super(getPaint(), AndroidGraphicFactory.INSTANCE);
		
		this.locationLayer = location;
		// Set up location callback
		locationLayer.setLocationListener(new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				updatePositions();
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		});
		
		// Make room for a start point and an end point
		getLatLongs().add(new LatLong(0, 0));
		getLatLongs().add(new LatLong(0, 0));
		
		// Invisible until a destination marker is set
		setVisible(false);
	}
	
	public void setDestination(Marker destination) {
		destinationMarker = destination;
		updatePositions();
	}

	private void updatePositions() {
		// Get latitude/longitude from location
		if(locationLayer != null) {
			Location location = locationLayer.getLastLocation();
			if(location != null) {
				setStartPoint(new LatLong(location.getLatitude(), location.getLongitude()));
			}
			else {
				setVisible(false);
				return;
			}
		}
		else {
			setVisible(false);
			return;
		}
		
		if(destinationMarker != null) {
			LatLong endPosition = destinationMarker.getLatLong();
			if(endPosition != null) {
				setEndPoint(endPosition);
			}
			else {
				setVisible(false);
				return;
			}
		}
		else {
			setVisible(false);
			return;
		}
		// None of those failed, so the line should be visible
		setVisible(true);
		requestRedraw();
	}
	
	private void setStartPoint(LatLong point) {
		getLatLongs().set(0, point);
	}
	private void setEndPoint(LatLong point) {
		getLatLongs().set(1, point);
	}

	private static Paint getPaint() {
		Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(4);
		paint.setStyle(Style.STROKE);
		
		return paint;
	}
}
