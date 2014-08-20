package org.samcrow.data.io;

import org.samcrow.colonynavigator3.data.Colony;

/**
 * Parses/encodes comma-delimited CSV representations of colonies
 * @author Sam Crow
 */
public class CSVParser implements Parser<Colony> {

	/**
	 * The character that separates data fields
	 */
	protected static final char separator = ',';

	/**
	 * Parse a line of CSV into a colony object.
	 * The line must contain the fields id, x, y
	 */
	@Override
	public Colony parseOne(String line) {

		//Split the line into parts separated by commas
		String[] parts = line.split("\\s*,\\s*");
		try {
			assert parts.length >= 3;//Require 3 parts: number, X, Y

			int colonyNumber = Integer.valueOf(parts[0]);
			int x = Integer.valueOf(parts[1]);
			int y = Integer.valueOf(parts[2]);

			//Ignore active, assume each colony is inactive for the census
			//			//Active if part 3 is A (case insensitive), otherwise false
			//			boolean active = parts[3].compareToIgnoreCase("A") == 0;

			return new Colony(colonyNumber, x, y, false);
		} catch(Throwable e) {
			//If anything went wrong, return null
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.io.Parser#encodeOne(org.samcrow.data.Colony)
	 */
	@Override
	public String encodeOne(Colony colony) {

		int xInt = (int) Math.round(colony.getX());
		int yInt = (int) Math.round(colony.getY());

		char activeChar = colony.isActive() ? 'A' : ' ';

		//Format: id,x,y,active,,
		return String.valueOf(colony.getId()) + separator + xInt + separator + yInt + separator + activeChar + separator + separator;
	}

}
