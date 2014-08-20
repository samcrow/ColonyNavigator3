package org.samcrow.colonynavigator3;

import org.mapsforge.map.android.view.MapView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ColonyMapView extends MapView {
	
	private static final byte ZOOM_MIN = (byte) 15;
	
	private static final byte ZOOM_MAX = (byte) 23;

	public ColonyMapView(Context context) {
		super(context);
		colonyMapInit(context);
	}

	public ColonyMapView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		colonyMapInit(context);
	}
	
	private void colonyMapInit(Context context) {
		// Disable hardware acceleration
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		
		setClickable(true);
		getMapScaleBar().setVisible(true);
		setBuiltInZoomControls(true);
		getMapZoomControls().setZoomLevelMin(ZOOM_MIN);
		getMapZoomControls().setZoomLevelMax(ZOOM_MAX);
	}

}
