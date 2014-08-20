package org.samcrow.colonynavigator3;

import java.io.File;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layer.MyLocationOverlay;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.samcrow.colonynavigator3.data.Colony;
import org.samcrow.colonynavigator3.data.ColonyList;
import org.samcrow.colonynavigator3.data.ColonyList.NoSuchColonyException;
import org.samcrow.colonynavigator3.data.ColonySelection;
import org.samcrow.colonynavigator3.map.ColonyMarker;
import org.samcrow.data.provider.ColonyProvider;
import org.samcrow.data.provider.MemoryCardDataProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.applantation.android.svg.SVG;
import com.applantation.android.svg.SVGParseException;
import com.applantation.android.svg.SVGParser;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements
		OnSharedPreferenceChangeListener {

	/**
	 * The initial position of the map
	 */
	private static final MapPosition START_POSITION = new MapPosition(
			new LatLong(31.872176, -109.040983), (byte) 17);

	private static final File MAP_FILE = new File(
			"/mnt/extSdCard/new-mexico.map");

	private PreferencesFacade preferencesFacade;

	private MapView mapView;

	private TileCache tileCache;

	private LayerManager layerManager;
	
	private MyLocationOverlay locationOverlay;

	private ColonyProvider provider;

	private ColonyList colonies;
	/**
	 * The current selected colony
	 */
	private ColonySelection selection = new ColonySelection();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {

			// Set up GraphicFactory
			AndroidGraphicFactory.createInstance(getApplication());

			createSharedPreferences();

			setContentView(R.layout.activity_main);

			setTitle("Map");

			setUpMap();

			// Add colonies
			provider = new MemoryCardDataProvider();

			colonies = provider.getColonies();
			for (Colony colony : colonies) {
				layerManager.getLayers().add(new ColonyMarker(colony));
			}

		} catch (Exception ex) {
			// Show a dialog, then quit
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(ex.getClass().getSimpleName())
					.setMessage(ex.getMessage())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setNeutralButton("Quit", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Close this activity
							finish();
						}
						
					})
					.show();
		}

	}

	private void setUpMap() {

		mapView = new ColonyMapView(this);

		Model model = mapView.getModel();
		model.init(this.preferencesFacade);


		// Put the map view in the layout
		FrameLayout layout = (FrameLayout) findViewById(R.id.map_view_frame);
		layout.addView(mapView);

		// Create a tile cache
		tileCache = AndroidUtil.createTileCache(this, getPersistableId(),
				mapView.getModel().displayModel.getTileSize(),
				getScreenRatio(),
				mapView.getModel().frameBufferModel.getOverdrawFactor());

		layerManager = mapView.getLayerManager();

		// Create a tile layer for OpenStreetMap data
		TileRendererLayer tileRendererLayer = createTileRendererLayer(
				tileCache,
				initializePosition(mapView.getModel().mapViewPosition),
				MAP_FILE, InternalRenderTheme.OSMARENDER, false);

		layerManager.getLayers().add(tileRendererLayer);

		// Add a display of the user's location
		locationOverlay = new MyLocationOverlay(this,
				mapView.getModel().mapViewPosition,
				AndroidGraphicFactory.convertToBitmap(getMyLocationDrawable()));
		layerManager.getLayers().add(locationOverlay);
		// locationOverlay.enableMyLocation() gets called in onResume().
	}

	private MapViewPosition initializePosition(MapViewPosition mvp) {
		LatLong center = mvp.getCenter();

		if (center.equals(new LatLong(0, 0))) {
			mvp.setMapPosition(START_POSITION);
		}
		return mvp;
	}

	private void createSharedPreferences() {
		SharedPreferences sp = this.getSharedPreferences(getPersistableId(),
				MODE_PRIVATE);
		this.preferencesFacade = new AndroidPreferences(sp);
	}

	/**
	 * @return the id that is used to save this mapview
	 */
	private String getPersistableId() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return the screen ratio that the mapview takes up (for cache
	 *         calculation)
	 */
	private float getScreenRatio() {
		return 1.0f;
	}

	private static TileRendererLayer createTileRendererLayer(
			TileCache tileCache, MapViewPosition mapViewPosition, File mapFile,
			XmlRenderTheme renderTheme, boolean hasAlpha) {
		TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache,
				mapViewPosition, hasAlpha, AndroidGraphicFactory.INSTANCE);
		tileRendererLayer.setMapFile(mapFile);
		tileRendererLayer.setXmlRenderTheme(renderTheme);
		tileRendererLayer.setTextScale(1.5f);
		return tileRendererLayer;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences,
			String key) {

	}

	private Drawable getMyLocationDrawable() {
		try {
			SVG svg = SVGParser.getSVGFromResource(getResources(),
					R.raw.my_location);
			return svg.createPictureDrawable();
		} catch (SVGParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		// Search box/item
		final MenuItem searchItem = menu.findItem(R.id.search_box);
		searchItem.expandActionView();
		final SearchView searchView = (SearchView) searchItem.getActionView();
		// Configure: Only expect numbers
		searchView.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_VARIATION_NORMAL);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				// Search for the colony
				try {
					int colonyId = Integer.valueOf(query);

					Colony newSelectedColony = colonies.getById(colonyId);

					// Deselect the current selected colony and select the new one
					selection.setSelectedColony(newSelectedColony);

					// Remove the focus from the search field
					searchView.clearFocus();

					// Center the map view on the colony
					mapView.getModel().mapViewPosition.animateTo(newSelectedColony.getLatLon());
					
					return true;
				} catch (NumberFormatException e) {
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("Invalid query")
							.setMessage("The search query is not a number")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setNeutralButton("OK", DIALOG_CLICK_NOOP).show();
				} catch (NoSuchColonyException e) {
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("Not found")
							.setMessage("No colony with that number exists")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setNeutralButton("OK", DIALOG_CLICK_NOOP).show();
				}
				return false;
			}

		});

		// Edit item
		final MenuItem editItem = menu.findItem(R.id.edit_item);
		editItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				
				ColonyEditDialogFragment editor = ColonyEditDialogFragment.newInstance(selection.getSelectedColony());
				editor.show(getFragmentManager(), "editor");
				return true;
			}
			
		});
		
		// My location toggle item
		final MenuItem myLocationItem = menu.findItem(R.id.my_location_item);
		// Change the check state and toggle snap-to-location when pressed
		myLocationItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				myLocationItem.setChecked(!myLocationItem.isChecked());
				
				if(myLocationItem.isChecked()) {
					myLocationItem.setIcon(R.drawable.ic_menu_my_location_blue);
					locationOverlay.setSnapToLocationEnabled(true);
				}
				else {
					myLocationItem.setIcon(R.drawable.ic_menu_my_location_gray);
					locationOverlay.setSnapToLocationEnabled(false);
				}
				
				return true;
			}
		});
		
		return true;
	}

	private static final DialogInterface.OnClickListener DIALOG_CLICK_NOOP = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface arg0, int arg1) {

		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		// Pause location updates
		locationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Start location updates
		locationOverlay.enableMyLocation(false);
	}
}
