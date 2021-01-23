package org.vedantatree.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * This class is to calculate and print various number combinations whose sum is equal to the given number. Conditions
 * are:
 * 
 * <ul>
 * <li>All numbers used in making the combination should be smaller or equal to the given number
 * <li>All combinations should be unique
 * <li>Difference in positioning of number does not make it unique
 * </ul>
 * 
 * <p>
 * In implementation, we are using List of Integers to collect the various combinations. Reason is that it is helping us
 * to sort the collected elements and hence in finding the duplicate entries. Other approach could be to use
 * StringBuffer, and collect numbers as String with comma separated values. But then we need to apply different
 * algorithm to find duplicate entries.
 * 
 * <p>
 * Scope of improvement is always there!!
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class TestNumberCombinations
{

	/**
	 * Comparator instance to sort the numbers in Descending order
	 */
	private NumberComparator numberComparator = new NumberComparator();

	/**
	 * Method to create combinations of numbers which can sum up to the specified number. The numbers in combinations
	 * should be lesser or equal to the given number.
	 * 
	 * @param finalNumber number for which we need to create the combinations which can sum up to its value
	 * @param combinations Collection which will contain the various number combinations produced by this method
	 */
	public Set<List<Integer>> createCombinations( int finalNumber, Set<List<Integer>> combinations )
	{
		if( combinations == null )
		{
			combinations = new HashSet<List<Integer>>();
		}

		List<Integer> oneCombination = new ArrayList<Integer>();

		// adding the combination using number itself
		oneCombination.add( finalNumber );
		combinations.add( oneCombination );

		for( int counter = finalNumber - 1; counter >= 1; counter-- )
		{
			oneCombination = new ArrayList<Integer>();

			// get the difference between final number and current counter number
			int otherPart = finalNumber - counter;

			oneCombination.add( otherPart );
			oneCombination.add( counter );

			// sort to find duplicate combinations
			Collections.sort( oneCombination, numberComparator );

			// continue if combination is already collected
			if( combinations.contains( oneCombination ) )
			{
				continue;
			}

			// otherwise add to the combinations collection and try to create various combinations of two parts
			// created above
			combinations.add( oneCombination );
			createCombinationsWithFixedPart( counter, otherPart, combinations );
			createCombinationsWithFixedPart( otherPart, counter, combinations );
		}

		return combinations;
	}

	/**
	 * Method to create number combinations which can sum up to the specified number value.
	 * 
	 * The difference between this and above method is, here we have one fixed part which should be considered while
	 * creating any combination as fixed.
	 * 
	 * @param fixedPart
	 * @param numberForCombinations
	 * @param combinations
	 */
	private void createCombinationsWithFixedPart( int fixedPart, int numberForCombinations,
			Set<List<Integer>> combinations )
	{
		// return if number specified for combination is already zero or 1. There is no further combination possible.
		if( numberForCombinations <= 1 )
		{
			return;
		}

		// otherwise let us create various combinations
		List<Integer> postAppendCombinations = new ArrayList<Integer>();
		List<Integer> oneCombination = null;

		for( int counter = numberForCombinations; counter > 1; counter-- )
		{
			oneCombination = new ArrayList<Integer>();

			oneCombination.add( counter - 1 );
			postAppendCombinations.add( 1 );
			oneCombination.addAll( postAppendCombinations );
			oneCombination.add( fixedPart );

			// sort to find the duplicate
			Collections.sort( oneCombination, numberComparator );
			if( !combinations.contains( oneCombination ) )
			{
				// if combination is not added yet, add it
				combinations.add( oneCombination );
			}
		}

	}

	/**
	 * Method to print the various sum combinations made for specified number. This method
	 * 
	 * @param combinations Number combinations to print
	 */
	private static void printCombinations( Set<List<Integer>> combinations )
	{
		System.out.println( "Combinations are : \n" );
		for( Iterator<List<Integer>> iterator = combinations.iterator(); iterator.hasNext(); )
		{
			List<Integer> combination = (List<Integer>) iterator.next();
			System.out.println( combination );

		}
	}

	/**
	 * Main method to run the test program
	 * 
	 * @param args No value as of now
	 */
	public static void main( String[] args )
	{
		TestNumberCombinations numberSum = new TestNumberCombinations();

		Set<List<Integer>> combinations = new HashSet<List<Integer>>();

		numberSum.createCombinations( 9, combinations );
		// numberSum.createCombinations( 2, 5, combinations );

		printCombinations( combinations );

	}

	/**
	 * Inner class defining a type of Comparator to sort the integer values in descending order
	 */
	private class NumberComparator implements Comparator<Integer>
	{

		public int compare( Integer numberOne, Integer numberTwo )
		{
			if( numberOne > numberTwo )
			{
				return -1;
			}
			else if( numberOne == numberTwo )
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}

	}

}
