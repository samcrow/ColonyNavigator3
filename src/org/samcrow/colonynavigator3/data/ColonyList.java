package org.samcrow.colonynavigator3.data;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A list of colonies
 * @author Sam Crow
 *
 */
public class ColonyList extends ArrayList<Colony> {
	
	private static final long serialVersionUID = -153763541331354522L;

	
	
	public ColonyList() {
		super();
	}



	public ColonyList(Collection<? extends Colony> collection) {
		super(collection);
	}


	public static class NoSuchColonyException extends IllegalArgumentException {
		static final long serialVersionUID = 1L;

		public NoSuchColonyException() {
			super();
		}

		public NoSuchColonyException(String message, Throwable cause) {
			super(message, cause);
		}

		public NoSuchColonyException(String detailMessage) {
			super(detailMessage);
		}

		public NoSuchColonyException(Throwable cause) {
			super(cause);
		}
		
	}

	/**
	 * Returns a colony with the given ID.
	 * @param colonyId
	 * @return
	 * @throws NoSuchColonyException If no colony was found
	 */
	public Colony getById(int colonyId) {
		for(Colony colony : this) {
			if(colony.getId() == colonyId) {
				return colony;
			}
		}
		throw new NoSuchColonyException();
	}
	
	

}
