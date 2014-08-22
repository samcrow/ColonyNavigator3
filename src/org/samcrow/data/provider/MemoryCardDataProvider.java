package org.samcrow.data.provider;

import java.io.File;
import java.io.IOException;

import org.samcrow.colonynavigator3.data.Colony;
import org.samcrow.colonynavigator3.data.ColonyList;
import org.samcrow.data.io.CSVFileParser;
import org.samcrow.data.io.FileParser;
import org.samcrow.data.io.FocusColonyFinder;
import org.samcrow.data.io.JSONFileParser;

/**
 * Provides colonies from data stored on the memory card.
 * This class first looks for a CSV file named colonies.csv in the directory specified by {@link #kDir}.
 * It parses that data.
 * Then it looks for a JSON file named colonies.json in the same directory and parses that data.
 * In the event of any conflict between the two files, the version in colonies.json takes precedence.
 * 
 * When writing colony data, this implementation writes it to colonies.json. It does not modify colonies.csv.
 * 
 * @author Sam Crow
 */
public class MemoryCardDataProvider implements ColonyProvider {

	private ColonyList colonies = new ColonyList();

	/**
	 * The absolute path to the folder where data should be read and written.
	 * This must begin and end with a slash.
	 * The folders don't need to exist on the file system. This implementation
	 * will try to create them if necessary.
	 */
	private static final String kDir = "/mnt/extSdCard/";

	/**
	 * The name, including the file extension, of the CSV file to use
	 */
	private static final String kCsvFileName = "colonies.csv";

	/**
	 * The name, including the file extension, of the JSON file to use
	 */
	private static final String kJsonFileName = "colonies.json";

	public MemoryCardDataProvider() {
		File dir = new File(kDir);
		//Create the directory if it doesn't already exist
		dir.mkdirs();

		File csvFile = new File(kDir+kCsvFileName);
		File jsonFile = new File(kDir+kJsonFileName);

		//Verify that this application has permission to write each of the files
		if(csvFile.exists()) assert csvFile.canWrite();
		if(jsonFile.exists()) assert jsonFile.canWrite();

		//Case 1: Application hasn't been run before
		//colonies.csv exists, colonies.json does not
		if(csvFile.exists() && !jsonFile.exists()) {

			//Read the CSV and get the colonies into memory
			FileParser<Colony> csvParser = new CSVFileParser(csvFile);
			colonies = new ColonyList(csvParser.parse());

			//Write the JSON file from memory
			FileParser<Colony> jsonParser = new JSONFileParser(jsonFile);
			jsonParser.write(colonies);
		}

		//Case 2: both files exist
		else if(csvFile.exists() && jsonFile.exists()) {


			FileParser<Colony> csvParser = new CSVFileParser(csvFile);
			ColonyList csvColonies = new ColonyList(csvParser.parse());

			//Write the JSON file from memory
			FileParser<Colony> jsonParser = new JSONFileParser(jsonFile);
			ColonyList jsonColonies = new ColonyList(jsonParser.parse());

			//Put into memory the colonies from the CSV updated with colonies from the JSON file
			colonies = extend(csvColonies, jsonColonies);

			//Write the JSON file from memory
			jsonParser.write(colonies);
		}

		//Cases 3: CSV doesn't exist, JSON does
		else if(!csvFile.exists() && jsonFile.exists()) {
			//Use the JSON file
			FileParser<Colony> jsonParser = new JSONFileParser(jsonFile);
			colonies.clear();
			colonies.addAll(jsonParser.parse());
		}

		else {
			String message = "Neither "+csvFile.getAbsolutePath()+" or "+jsonFile.getAbsolutePath()+" exists! Failed to get colonies from the memory card.";
			System.err.println(message);
		}
		

		//Look for focus_colonies.txt
		File focusFile = new File(kDir+"focus_colonies.txt");
		if(focusFile.exists() && focusFile.canRead()) {
			try {
				new FocusColonyFinder(focusFile, colonies).updateColonies();
			} catch (IOException e) {
				System.err.println("Could not read focus colonies file");
				e.printStackTrace();
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#getColonies()
	 */
	@Override
	public ColonyList getColonies() {
		return colonies;
	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#updateColonies()
	 */
	@Override
	public void updateColonies() throws UnsupportedOperationException {
		new FileWriteTask().start();
	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#updateColony(org.samcrow.data.Colony)
	 */
	@Override
	public void updateColony(Colony colony)
			throws UnsupportedOperationException {
		new FileWriteTask().start();

	}

	/**
	 * Extend a set of colonies to reflect changes from a supplemental set
	 * This is based on jQuery's <code>jQuery.extend()</code> function.
	 * 
	 * This method will create and return a new set with the following:
	 * <ul>
	 * <li>Every colony in supplement but not base included as-is</li>
	 * <li>Every colony in base but not supplement included as-is</li>
	 * <li>For every colony in both sets, the copy from base will be ignored
	 * and the copy from supplement will be used</li>
	 * </ul>
	 * Colonies are considered equal if their IDs as returned by {@link Colony#getId()}
	 * are the same.
	 * 
	 * @param base The base set of colonies
	 * @param supplement The supplement set of colonies
	 * @return A new set of colonies reflecting the changes to base made by supplement.
	 * This set will contain references to the same colony objects referred to by
	 * the input sets.
	 */
	private ColonyList extend(ColonyList base, ColonyList supplement) {
		//Note: base and supplement contain references to different colony objects with the same IDs

		ColonyList finalSet = new ColonyList();

		for(Colony colony : base) {
			int id = colony.getId();

			Colony supplementColony = supplement.getById(id);
			if(supplementColony != null) {
				//It's in the supplement set, so just use the version from the supplement
				finalSet.add(supplementColony);
			}
			else {
				//This colony isn't in the supplement. Use the version from the base.
				finalSet.add(colony);
			}
		}
		//Add every colony that's in the supplement
		for(Colony supplementColony : supplement) {
			int id = supplementColony.getId();

			Colony baseColony = base.getById(id);
			if(baseColony == null) {
				//If this colony is in the base set, it's already been added.
				//Here, it isn't in the base set, so it's added to the final set.
				finalSet.add(supplementColony);
			}
		}

		return finalSet;
	}

	/**
	 * A thread that writes the colonies to the JSON file
	 * 
	 * @author Sam Crow
	 */
	private class FileWriteTask extends Thread {

		@Override
		public void run() {
			File file = new File(kDir+kJsonFileName);

			FileParser<Colony> parser = new JSONFileParser(file);
			parser.write(colonies);

		}
	}
}
