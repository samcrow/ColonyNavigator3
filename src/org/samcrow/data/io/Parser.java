package org.samcrow.data.io;

import org.samcrow.colonynavigator3.data.Colony;

/**
 * An interface for a class that can convert between
 * string and in-memory representations of colonies
 * @author Sam Crow
 * @param <T> The class to parse
 */
public interface Parser<T> {

	/**
	 * Parse a string representation of one colony into a {@link Colony}.
	 * If the string could not be parsed, this method should return <code>null</code>.
	 * @param oneString The string to parse
	 * @return The parsed colony
	 */
	public T parseOne(String oneString);

	/**
	 * Encode one colony into a string representation
	 * @param value The colony to encode
	 * @return A string representation of the colony
	 */
	public String encodeOne(T value);

}
