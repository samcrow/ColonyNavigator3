package org.samcrow.colonynavigator3.data;

import org.json.JSONObject;

/**
 * An interface for something that can be serialized to and from JSON
 * @author samcrow
 *
 */
public interface JSONSerializable {

	/**
	 * Serialize this object into a {@link JSONObject}.
	 * 
	 * @return A JSON object representation of this object
	 */
	public abstract JSONObject toJSON();

	/**
	 * Set all the parameters of this object using data from a given JSON object
	 * 
	 * @param json
	 *            The object to get data from
	 */
	public void fromJSON(JSONObject json);
}