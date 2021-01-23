package org.vedantatree.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This class is to collect following data from two files:
 * 
 * <ul>
 * <li>All common lines
 * <li>All unique lines
 * <li>All lines exist in first, but not in second
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

/*
 * psuedo code::
 * 
 * Keep three collections for storing
 * -- common lines
 * -- unique lines
 * -- exists in first but not in second
 * 
 * Initialize second file pointer - parsing second file first, as it will help to collect the third required collection
 * in same iteration
 * 
 * Need to parse the file. Assumptions are that each new line is separated by a new line character. And we need
 * to ignore the spaces before or after the line contents.
 * 
 * Read the file and store all lines in a collection called 'secondFileLines'
 * Close the file pointer
 * 
 * Open file pointer for first file
 * Read the file based on above assumptions
 * Read one line
 * Process this line to collect data like common lines, unique lines, if exist in first file only. Better to have
 * separate method
 * 
 * Now after above processing, we have complete data for common lines and all lines exist in first file only. However,
 * unique lines are not completed as unique lines of second file are not yet in. For it,
 * 
 * We have all lines for second file
 * We have common lines for first and second file
 * Get unique lines for second file as
 * --All lines for second file - common lines
 * 
 * Add these lines to unique lines collection
 * 
 * Data collection is done
 * 
 * Print the collections
 */
public class TestFileInput
{

	/**
	 * Collection of second file lines
	 */
	private Collection<String>	secondFileLines			= new ArrayList<String>();

	/**
	 * Collection of lines common in both files
	 */
	private Collection<String>	commonLines				= new ArrayList<String>();

	/**
	 * Collection of lines which are unique for both files
	 */
	private Collection<String>	uniqueLines				= new ArrayList<String>();

	/**
	 * Collection of lines which exist in first file only
	 */
	private Collection<String>	linesExistInFirstOnly	= new ArrayList<String>();

	/**
	 * This method will process the data of both files and collect the data i.e. common lines, unique lines for both
	 * files and lines which exist in first file only. After collecting the data, this method will print the collected
	 * data before exiting.
	 * 
	 * <p>
	 * Currently algorithm is using the instance variable for collections to make code bit cleaner. In case, if we
	 * design this method as Utility method, all of these collections can be initialized in method itself and can be
	 * passed to further operations. Main method can be made static.
	 * 
	 * <p>
	 * No custom exception has been used to keep it simple
	 * 
	 * @param firstFilePath Path of first file
	 * @param secondFilePath Path of second file
	 * @throws Exception If there is any problem during execution. Most probable reason could be any problem in file
	 *         operations; others might be due to implementation bug.
	 */
	public void processFileData( String firstFilePath, String secondFilePath ) throws Exception
	{
		// assert if given file paths are valid. We are checking validity based on length of path only. Whether a file
		// exist or not, will be checked at the time of reading the file. Any problem at this time will raise the
		// exception.
		assertValidFilePath( firstFilePath, "First File Path" );
		assertValidFilePath( secondFilePath, "Second File Path" );

		try
		{
			// read and cache all lines from second file
			secondFileLines = collectFileLines( secondFilePath );

			// read first file line by line and process each line to collect the required data
			BufferedReader fileReader = new BufferedReader( new FileReader( new File( firstFilePath ) ) );
			String firstFileLine = null;

			while( ( firstFileLine = fileReader.readLine() ) != null )
			{
				// function call to collect the relevant data
				collectLineData( firstFileLine.trim() );
			}

			// we have already got unique lines for first file, now let us collect unique lines for second file also
			Collection<String> secondFileUniqueLines = getUniqueLinesForSecondFile();

			// second file lines cache can be flushed here

			// collect all unique lines in one collection
			uniqueLines.addAll( secondFileUniqueLines );

			// print the collected data
			printCollectedData();
		}
		finally
		{
			// let us empty the cache. Although currently main test object will be destroyed once execution is
			// completed, still it is good to clean the data.
			secondFileLines = null;
			commonLines = null;
			uniqueLines = null;
			linesExistInFirstOnly = null;
		}
	}

	/**
	 * Method to collect all the lines of a file in a collection. Assumption is that lines will be separated by new
	 * line character.
	 * 
	 * @param filePath Path of the file
	 * @return Collection of all lines from the file
	 * @throws Exception If there is any problem during operation
	 */
	private Collection<String> collectFileLines( String filePath ) throws Exception
	{
		BufferedReader fileReader = new BufferedReader( new FileReader( new File( filePath ) ) );

		String fileLine = null;
		Collection<String> fileLines = new ArrayList<String>();

		while( ( fileLine = fileReader.readLine() ) != null )
		{
			fileLines.add( fileLine.trim() );
		}

		return fileLines;
	}

	/**
	 * Method to collect data based on all lines from second file and one/current line from first file
	 * 
	 * @param firstFileLine line of first file to process
	 */
	private void collectLineData( String firstFileLine )
	{
		// if current line exists with second file lines, it is a common line
		if( secondFileLines.contains( firstFileLine ) )
		{
			commonLines.add( firstFileLine );
		}
		else
		{
			// else if not common then it must be unique
			uniqueLines.add( firstFileLine );

			// and hence it exist with first file only
			linesExistInFirstOnly.add( firstFileLine );
		}
		// will collect all unique lines of second file later
	}

	/**
	 * Method to collect the unique lines for second files.
	 * 
	 * <p>
	 * Assumptions are that we already have collection of lines which are common in both files and a collection of
	 * all lines of second file.
	 * 
	 * @return Collection of lines which are unique in second file
	 */
	private Collection<String> getUniqueLinesForSecondFile()
	{
		// all lines in second file - common lines

		List<String> secondFileUniqueLines = new ArrayList<String>( secondFileLines );
		secondFileUniqueLines.removeAll( commonLines );

		return secondFileUniqueLines;
	}

	/**
	 * Utility method to assert the validation for file path
	 * 
	 * @param filePath file path to validate
	 * @param comment If caller want to pass any other information to show with error message
	 */
	private void assertValidFilePath( String filePath, String comment )
	{
		if( filePath == null || filePath.length() == 0 )
		{
			throw new IllegalArgumentException(
					"Path of file is not valid. filePath[" + filePath + "] comment[" + comment + "]" );
		}
	}

	/**
	 * Print the collected data
	 */
	private void printCollectedData()
	{
		System.out.println( "Common Lines :: \n" );
		printCollection( commonLines );

		System.out.println( "\nUnique Lines :: \n" );
		printCollection( uniqueLines );

		System.out.println( "\nLines exit in first file only :: \n" );
		printCollection( linesExistInFirstOnly );
	}

	/**
	 * Method to print a collection elements with line numbers
	 * 
	 * @param dataCollection Collection of data to print
	 */
	private void printCollection( Collection<String> dataCollection )
	{
		int lineNumber = 1;
		for( Iterator<String> iterator = dataCollection.iterator(); iterator.hasNext(); lineNumber++ )
		{
			System.out.println( lineNumber + "). " + iterator.next() );
		}
	}

	/**
	 * Main method to execute this test case
	 * 
	 * @param args No argument as of now. File paths can be passed as arguments later.
	 */
	public static void main( String[] args )
	{
		TestFileInput testFileInput = new TestFileInput();
		try
		{
			testFileInput.processFileData( "D:/temp/file1.txt", "D:/temp/file2.txt" );
		}
		catch( Exception e )
		{
			System.err.println(
					"Some error occured while processing the file data. errorMessage[" + e.getMessage() + "]" );
			e.printStackTrace();
		}
	}

}
