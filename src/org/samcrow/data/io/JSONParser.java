package org.samcrow.data.io;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.samcrow.colonynavigator3.data.Colony;

/**
 * Parses and encodes JSON
 * @author samcrow
 */
public class JSONParser implements Parser<Colony> {

	@Override
	public Colony parseOne(String oneString) {
		Colony colony = new Colony();

		try {
			colony.fromJSON(new JSONObject(oneString));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return colony;
	}

	/**
	 * Parse a JSON array of colonies into a set of colonies
	 * @param colonyArray The colonies to parse
	 * @return The parsed colonies
	 */
	public Set<Colony> parseAll(JSONArray colonyArray) {
		Set<Colony> colonies = new HashSet<Colony>();

		for(int i = 0, max = colonyArray.length(); i < max; i++) {
			try {

				JSONObject colonyObject = colonyArray.getJSONObject(i);
				Colony colony = new Colony();
				colony.fromJSON(colonyObject);
				colonies.add(colony);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return colonies;
	}

	/**
	 * Encode all the colonies in a set into a JSON array of data
	 * @param colonies The colonies to encode
	 * @return The data in JSON array format
	 */
	public JSONArray encodeAll(Set<Colony> colonies) {
		JSONArray array = new JSONArray();

		return array;
	}

	@Override
	public String encodeOne(Colony value) {
		return value.toJSON().toString();
	}

}
