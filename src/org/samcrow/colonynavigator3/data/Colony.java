package org.samcrow.colonynavigator3.data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.core.model.LatLong;
import org.samcrow.colonynavigator3.CoordinateTransformer;
import org.samcrow.colonynavigator3.map.ColonyMarker;

/**
 * Stores data for one colony
 * 
 * @author Sam Crow
 */
public class Colony implements Serializable, JSONSerializable,
		Comparable<Colony> {

	private static final long serialVersionUID = 1L;

	/**
	 * A listener that can be notified when a colony's drawable changes
	 * 
	 * @author samcrow
	 * 
	 */
	public static interface ColonyChangeListener {
		public void onColonyChanged();
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            The colony's identifier
	 * @param x
	 *            The colony's X location in meters
	 * @param y
	 *            The colony's Y location in meters
	 */
	public Colony(int id, double x, double y, boolean active) {

		this.id = id;
		this.x = x;
		this.y = y;
		this.active = active;
	}

	public Colony() {
		this(0, 0, 0, false);
	}

	/** The colony's identifier */
	protected int id;

	/** The colony's X-coordinate in meters east of the southwest corner */
	protected double x;
	/** The colony's Y-coordinate in meters north of the southwest corner */
	protected double y;

	/** If the colony is currently active */
	protected boolean active;

	/** If the colony has been visited by a human */
	protected boolean visited;

	/** If the colony is a focus colony */
	protected boolean focus;

	/** If the colony is currently the selected colony in the user interface */
	protected boolean selected;
	
	/**
	 * The latitude/longitude position of this colony
	 */
	private transient LatLong latLon = null;

	/**
	 * Additional attributes of this colony
	 */
	private Map<String, Object> attributes = new HashMap<String, Object>();

	/**
	 * The date/time that this colony was modified. If it was not modified by
	 * Colony Navigator since it was imported from the CSV file, this should be
	 * null. Otherwise, it should be the date when the user last modified the
	 * data using the Colony Navigator application.
	 */
	private Date modified = null;

	private transient ColonyChangeListener listener = null;
	
	private transient ColonyMarker marker = null;

	/**
	 * Get the colony's X-coordinate in meters east of the southwest corner
	 * 
	 * @return the colony's X-coordinate in meters east of the southwest corner
	 */
	public double getX() {
		return x;
	}

	/**
	 * Set the colony's X-coordinate in meters east of the southwest corner
	 * 
	 * @param x
	 *            The colony's X-coordinate location
	 */
	public void setX(double x) {
		this.x = x;
		updateModifiedDate();
		latLon = null;
		
		notifyChanged();
	}

	/**
	 * Get the colony's Y-coordinate in meters north of the southwest corner
	 * 
	 * @return the colony's Y-coordinate in meters north of the southwest corner
	 */
	public double getY() {
		return y;
	}

	/**
	 * Set the colony's Y-coordinate in meters north of the southwest corner
	 * 
	 * @param y
	 *            The colony's Y-coordinate location
	 */
	public void setY(double y) {
		this.y = y;
		updateModifiedDate();
		latLon = null;
		
		notifyChanged();
	}

	/**
	 * Get if the colony is active
	 * 
	 * @return if the colony is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set if the colony is active
	 * 
	 * @param active
	 *            if the colony is active
	 */
	public void setActive(boolean active) {
		this.active = active;
		updateModifiedDate();
		notifyChanged();
	}

	/**
	 * Get if the colony has been visited
	 * 
	 * @return if the colony has been visited
	 */
	public boolean isVisited() {
		return visited;
	}

	/**
	 * Set if this colony has been visited
	 * 
	 * @param visited
	 *            If the colony has been visited
	 */
	public void setVisited(boolean visited) {
		this.visited = visited;
		updateModifiedDate();
		notifyChanged();
	}

	/**
	 * Get the colony's ID
	 * 
	 * @return the ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Update the modified date/time and set it to now. Every method that sets a
	 * field should call this method.
	 */
	protected final void updateModifiedDate() {
		modified = new Date();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.samcrow.data.JSONSerializable#toJSON()
	 */
	@Override
	public JSONObject toJSON() {
		JSONObject object = new JSONObject();

		try {

			object.put("id", id);
			object.put("x", x);
			object.put("y", y);
			object.put("active", active);
			object.put("visited", visited);

			// Visited date/time: Should be JSON's NULL if null, or formatted

			if (modified == null) {
				object.put("modified", JSONObject.NULL);
			} else {
				object.put("modified",
						ISODateTimeFormat.dateTime().print(modified.getTime()));
			}

			// Other attributes
			if(!attributes.isEmpty()) {
				object.put("attributes", attributes);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return object;
	}

	@Override
	public void fromJSON(JSONObject json) {
		id = json.optInt("id", id);
		x = json.optDouble("x", x);
		y = json.optDouble("y", y);
		active = json.optBoolean("active", active);
		visited = json.optBoolean("visited", visited);

		Object modifiedObject = json.opt("modified");
		if (modifiedObject == null || JSONObject.NULL.equals(modifiedObject)) {
			// Modified time specified as null; make it so
			modified = null;
		} else if (modifiedObject instanceof String) {
			// Modified time given; parse it
			try {
				modified = ISODateTimeFormat.dateTimeParser()
						.parseDateTime((String) modifiedObject).toDate();
			} catch (IllegalArgumentException e) {
				// Parse error
				modified = null;
			}
		}

		// Attributes
		Object attributesObject = json.opt("attributes");

		try {

			if (attributesObject != null) {
				if (attributesObject instanceof JSONObject) {
					@SuppressWarnings("rawtypes")
					Iterator keyIterator = ((JSONObject) attributesObject)
							.keys();
					while (keyIterator.hasNext()) {
						final String key = (String) keyIterator.next();
						attributes.put(key,
								((JSONObject) attributesObject).get(key));
					}
				}
			}

		} catch (JSONException ex) {
			ex.printStackTrace();
		}

		notifyChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Colony #" + id + " at (" + x + ", " + y + "), "
				+ (active ? "active" : "inactive") + ", "
				+ (visited ? "visited" : "not visited");
	}

	/**
	 * Set this colony's ID
	 * 
	 * @param id
	 *            the ID to set
	 */
	public void setId(int id) {
		this.id = id;

		notifyChanged();
	}

	/**
	 * Determines if this colony is a focus colony, as defined in the file
	 * 
	 * @return
	 */
	public boolean isFocusColony() {
		return focus;
	}

	public void setFocusColony(boolean focus) {
		this.focus = focus;

		notifyChanged();
	}

	public LatLong getLatLon() {
		if (latLon == null) {
			latLon = CoordinateTransformer.getInstance().toGps((float) x,
					(float) y);
		}
		return latLon;
	}

	/**
	 * Deletes the current cached drawable and notifies the listener that the
	 * drawable has changed
	 */
	private void notifyChanged() {
		if (listener != null) {
			listener.onColonyChanged();
		}
	}

	public void setOnChange(
			ColonyChangeListener changelistener) {
		listener = changelistener;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		
		notifyChanged();
	}

	/**
	 * Compares two colonies by their ID
	 * 
	 * @see Comparable#compareTo(Object)
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(Colony other) {
		if (this.getId() < other.getId()) {
			return -1;
		}
		if (this.getId() > other.getId()) {
			return 1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + (focus ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result
				+ ((modified == null) ? 0 : modified.hashCode());
		result = prime * result + (visited ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Colony)) {
			return false;
		}
		Colony other = (Colony) obj;
		if (active != other.active) {
			return false;
		}
		if (focus != other.focus) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (modified == null) {
			if (other.modified != null) {
				return false;
			}
		} else if (!modified.equals(other.modified)) {
			return false;
		}
		if (visited != other.visited) {
			return false;
		}
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}

	public ColonyMarker getMarker() {
		return marker;
	}

	public void setMarker(ColonyMarker marker) {
		this.marker = marker;
	}
}
