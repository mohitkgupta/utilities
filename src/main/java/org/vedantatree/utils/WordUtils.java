/*
 * Created on Jan 18, 2006
 * 
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils;

/**
 * This class converts the numeric value to word representation.
 * 
 * @author Mohit Gupta
 * @version 1.0
 */
public class WordUtils
{

	public static String[]	RANK			= new String[]
	{ "", "", "hundred", "thousand", "", "lakh", "", "crore" };

	public static String[]	ONE_NINE		= new String[]
	{ "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine" };

	public static String[]	TEN_NINETEEN	= new String[]
	{ "Ten", "Eleven", "Tweleve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Ninteen" };

	public static String[]	TWENTY_NINETY	= new String[]
	{ "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninty" };

	/**
	 * Convert the numeric value to word string
	 * 
	 * @param number
	 * @return
	 */
	public static String convertToWord( long number )
	{
		String word = "";
		if( number > 999999999 )
		{
			return null;
		}
		long[] digits = new long[9];
		int i = 0;
		while( number != 0 )
		{
			digits[i++] = number % 10;
			number /= 10;
		}
		i--;
		while( i >= 0 )
		{
			if( i == 10 || i == 8 || i == 6 || i == 4 || i == 1 )
			{
				if( digits[i] == 1 )
				{
					word += TEN_NINETEEN[(int) digits[i - 1]] + " ";
					i--;
				}
				else
				{
					word += TWENTY_NINETY[(int) digits[i]] + " ";
				}
			}
			else if( i == 2 || i == 3 || i == 5 || i == 7 || i == 9 )
			{
				word += ONE_NINE[(int) digits[i]] + " ";
			}
			if( i == 2 || i == 3 || i == 5 || i == 7 || i == 9 )
			{
				word += RANK[i] + " ";
			}
			i--;
		}
		return word;
	}

	public static void main( String[] args )
	{
		System.out.println( "Word : " + convertToWord( 12014500 ) );
	}
}