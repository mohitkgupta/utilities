/*
 * Created on Nov 27, 2005
 * 
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils.swings;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;


/**
 * This class provides the useful APIs required in any swings environment.
 */
public final class XSwingsUtils
{

	/**
	 * Constructs the XSwingsUtils
	 * This is a private constructors. This
	 * class is contracted for static use.
	 */
	private XSwingsUtils()
	{
		/**
		 * Nothing to do here.
		 */
	}

	/**
	 * Shows the window at the center of screen.
	 * Center is determined on the basiss of screen size and
	 * windo size.
	 * 
	 * @param window the window to be displed at the center
	 *        of the screen
	 * @throws IllegalArgumentException if the passed window
	 *         object is null.
	 */
	public final static void showWindow( Window window )
	{
		if( window == null )
		{
			throw new IllegalArgumentException( "Passed window is null." );
		}
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = window.getSize();
		window.setLocation( ( screenSize.width - windowSize.width ) / 2,
				( screenSize.height - windowSize.height ) / 2 );
		window.setVisible( true );
	}
}