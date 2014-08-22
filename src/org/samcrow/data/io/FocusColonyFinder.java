package org.samcrow.data.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.samcrow.colonynavigator3.data.ColonyList;

/**
 * Reads colony numbers, one per line, from the a file and marks
 * the identified colonies as focus colonies
 * @author samcrow
 *
 */
public class FocusColonyFinder {

	private final ColonyList colonies;
	private final File focusFile;
	
	public FocusColonyFinder(File focusFile, ColonyList colonies) {
		this.focusFile = focusFile;
		this.colonies = colonies;
	}
	
	/** Marks required colonies from the colony set as focused */
	public void updateColonies() throws IOException {
		

		BufferedReader reader = new BufferedReader(new FileReader(focusFile));
		try {
			while(true) {
				String line = reader.readLine();
				if(line == null) {
					break;
				}
				if(line.isEmpty()) {
					continue;
				}
				
				try {
					int colonyId = Integer.valueOf(line);
					
					colonies.getById(colonyId).setFocusColony(true);
				
				} catch (NumberFormatException ex) {
					continue;
				}
			}
		}
		finally {
			if(reader != null) {
				reader.close();
			}
		}
		
	}

}
