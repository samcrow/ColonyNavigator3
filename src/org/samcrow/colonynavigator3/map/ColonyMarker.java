package org.samcrow.colonynavigator3.map;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;
import org.samcrow.colonynavigator3.data.Colony;

public class ColonyMarker extends Marker {
	
	private Colony colony;
	
	
	public ColonyMarker(Colony colony) {
		super(colony.getLatLon(), bitmapForColony(colony), 0, 0);
		
		// Not memory-optimal:
		// Create a new drawable to find the correct offsets
		setHorizontalOffset(new ColonyDrawable(colony).getXOffset());
		
		this.colony = colony;
		// Make a link back
		this.colony.setMarker(this);
		
		// Change the bitmap when the colony's drawable changes
		colony.setOnChange(new Colony.ColonyChangeListener() {
			@Override
			public void onColonyChanged() {
				setBitmap(bitmapForColony(ColonyMarker.this.colony));
			}
		});
	}
	
	@Override
	public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
		
		// Check if this colony was tapped
		// TODO
		
		return false;
	}
	
	private static Bitmap bitmapForColony(Colony colony) {
		return AndroidGraphicFactory.convertToBitmap(new ColonyDrawable(colony));
	}
}
