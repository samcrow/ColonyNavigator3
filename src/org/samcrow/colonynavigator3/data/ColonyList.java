package org.samcrow.colonynavigator3.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * A list of colonies
 * @author Sam Crow
 *
 */
public class ColonyList implements List<Colony> {
	
	private List<Colony> list;

	public ColonyList() {
		list = new ArrayList<Colony>();
	}

	public ColonyList(Collection<? extends Colony> collection) {
		list = new ArrayList<Colony>(collection);
		resort();
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
	 * Sorts the list to allow binary searching
	 */
	private void resort() {
		Collections.sort(this);
	}
	
	/**
	 * Returns a colony with the given ID.
	 * @param colonyId
	 * @return
	 * @throws NoSuchColonyException If no colony was found
	 */
	public Colony getById(int colonyId) {
		// Create a dummy Colony with the right ID
		// that will compare equal to any other colony with that ID
		final Colony dummyColony = new Colony(colonyId, 0, 0, false);
		
		int index = Collections.binarySearch(list, dummyColony);
		if(index >= 0) {
			return list.get(index);
		}
		else {
			throw new NoSuchColonyException();
		}
	}

	public boolean add(Colony arg0) {
		boolean result = list.add(arg0);
		resort();
		return result;
	}

	public void add(int location, Colony object) {
		list.add(location, object);
		resort();
	}

	public boolean addAll(Collection<? extends Colony> arg0) {
		boolean result = list.addAll(arg0);
		resort();
		return result;
	}

	public boolean addAll(int arg0, Collection<? extends Colony> arg1) {
		boolean result = list.addAll(arg0, arg1);
		resort();
		return result;
	}

	public void clear() {
		list.clear();
		resort();
	}

	public boolean contains(Object object) {
		return list.contains(object);
	}

	public boolean containsAll(Collection<?> arg0) {
		return list.containsAll(arg0);
	}

	public boolean equals(Object object) {
		return list.equals(object);
	}

	public Colony get(int location) {
		return list.get(location);
	}

	public int hashCode() {
		return list.hashCode();
	}

	public int indexOf(Object object) {
		return list.indexOf(object);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<Colony> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object object) {
		return list.lastIndexOf(object);
	}

	public ListIterator<Colony> listIterator() {
		return list.listIterator();
	}

	public ListIterator<Colony> listIterator(int location) {
		return list.listIterator(location);
	}

	public Colony remove(int location) {
		Colony result = list.remove(location);
		resort();
		return result;
	}

	public boolean remove(Object object) {
		boolean result = list.remove(object);
		resort();
		return result;
	}

	public boolean removeAll(Collection<?> arg0) {
		boolean result = list.removeAll(arg0);
		resort();
		return result;
	}

	public boolean retainAll(Collection<?> arg0) {
		boolean result = list.retainAll(arg0);
		resort();
		return result;
	}

	public Colony set(int location, Colony object) {
		Colony result = list.set(location, object);
		resort();
		return result;
	}

	public int size() {
		return list.size();
	}

	public List<Colony> subList(int start, int end) {
		return list.subList(start, end);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <T> T[] toArray(T[] array) {
		return list.toArray(array);
	}
	
	

}
