/*
 * Created on Sep 25, 2005
 * 
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils.swings;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;


/**
 * This class provides the util methods for icon support. It loads the icon
 * configurationfrom the input stream which is an property file format's tream.
 * Proerty file contains the mapping as icon name and icon path.
 */
public final class IconUtils
{

	/**
	 * This is the properties container which holds the icon name
	 * and icon path mapping.
	 */
	private static Properties properties;

	/**
	 * Constructs the IconUtils
	 * This constructor is made private to restrict
	 * its use to singlton class.
	 */
	private IconUtils()
	{
		/**
		 * Nothing to do here.
		 */
	}

	/**
	 * Loads the mapping file. It throws RuntimeException if
	 * unable to load the mappings.
	 * 
	 * @param is This is the input stream of the mapping file.
	 */
	public final static void loadMappings( InputStream is )
	{
		// Create the properties object.
		properties = new Properties();
		try
		{
			// Loads the properties from mapping file stream.
			properties.load( is );
		}
		catch( IOException ex )
		{
			throw new RuntimeException( ex );
		}
	}

	/**
	 * Gets the icon image for the given icon name. If the
	 * icon name is not found then it returns <code>null</code>
	 * 
	 * @param name the icon name specified in mapping.
	 * @return the icon image if icon name mapping found else returns <code>null</code>
	 */
	public final static ImageIcon getIcon( String name )
	{
		ImageIcon icon = null;
		// Gets the icon path for icon name.
		String iconPath = properties.getProperty( name );
		if( iconPath != null && iconPath.trim().length() > 0 )
		{
			// Gets the url for icon path
			URL url = IconUtils.class.getClassLoader().getResource( iconPath );
			if( url != null )
			{
				// Loads the icon.
				icon = new ImageIcon( url );
			}
		}
		return icon;
	}

	/**
	 * Gets the the image for the given icon name. If the
	 * icon name is not found then it returns <code>null</code>.
	 * 
	 * @param name the icon name specified in mapping.
	 * @return the image if icon name found in mapping else returns <code>null</code>.
	 */
	public final static Image getImage( String name )
	{
		Image image = null;
		// Gets the icon path for icon name
		String iconPath = properties.getProperty( name );
		if( iconPath != null && iconPath.trim().length() > 0 )
		{
			// Gets the url for icon path.
			URL url = IconUtils.class.getClassLoader().getResource( iconPath );
			if( url != null )
			{
				// Load the image from the url.
				image = Toolkit.getDefaultToolkit().getImage( url );
			}
		}
		return image;
	}
}