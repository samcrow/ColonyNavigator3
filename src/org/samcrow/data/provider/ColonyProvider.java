package org.samcrow.data.provider;

import org.samcrow.colonynavigator3.data.Colony;
import org.samcrow.colonynavigator3.data.ColonyList;

/**
 * An interface for a class that can get colonies and update their information.
 * @author Sam Crow
 */
public interface ColonyProvider {

	/**
	 * Get the colonies.
	 * This method should not block.
	 * @return The colonies, or null if the colonies are not currently available
	 */
	public ColonyList getColonies();

	/**
	 * Take all the colonies (the same reference as returned by {@link #getColonies()})
	 * and write them to this provider's persistence mechanism.
	 * This method should not block.
	 * @throws UnsupportedOperationException if this provider does not support
	 * persistent storage
	 */
	public void updateColonies() throws UnsupportedOperationException;

	/**
	 * Write the information on a selected colony to the persistence mechanism.
	 * This is similar to {@link #updateColonies()}, but this method may allow
	 * implementations to optimize their handling of partial updates.
	 * This method should not block.
	 * @param colony The colony to update
	 * @throws UnsupportedOperationException if this provider does not support
	 * persistent storage
	 */
	public void updateColony(Colony colony) throws UnsupportedOperationException;
}
