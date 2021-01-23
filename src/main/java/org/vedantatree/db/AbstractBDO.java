package org.vedantatree.db;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * Abstract implementation for all Business Data Objects.
 * 
 * It provides the implementation for some common attributes, we may need in all Business Data Objects. Still use of
 * properties is at the wish of extending object. So we are not defining the ORM tags in this class.
 * 
 * Extending classes can define the tags only for those properties, which they want to use.
 * 
 * TODO
 * Move ORM package to Components, as we may need to use Security objects here. So it should be in components only.
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public abstract class AbstractBDO implements Serializable
{

	private static final long	serialVersionUID	= -3108592727245467135L;

	/**
	 * Whether this object is active or not. Some application may want to show only active objects. This will help for
	 * that use case.
	 */
	private Boolean				active				= Boolean.TRUE;

	/**
	 * The filter criteria for objects. Application may use it to show some specific type of objects to users based on
	 * this filter.
	 * 
	 * TODO: This should be of object type. Like then SmartFMS can use AdminContext as ObjectGroup.
	 */
	private String				objectGroup;

	/**
	 * Time when this object was created
	 */
	private Timestamp			createdOn;

	/**
	 * Name/Identity of User who has created this object
	 */
	private String				createdBy;

	/**
	 * Time when this object was updated lastly
	 */
	private Timestamp			updatedOn;

	/**
	 * Name/Identity of User who has updated this object lastly
	 */
	private String				updatedBy;

	public boolean isActive()
	{
		return active;
	}

	public void setActive( Boolean active )
	{
		this.active = active;
	}

	public String getObjectGroup()
	{
		return objectGroup;
	}

	public void setObjectGroup( String objectGroup )
	{
		this.objectGroup = objectGroup;
	}

	public Timestamp getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn( Timestamp createdOn )
	{
		this.createdOn = createdOn;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy( String createdBy )
	{
		this.createdBy = createdBy;
	}

	public Timestamp getUpdatedOn()
	{
		return updatedOn;
	}

	public void setUpdatedOn( Timestamp updatedOn )
	{
		this.updatedOn = updatedOn;
	}

	public String getUpdatedBy()
	{
		return updatedBy;
	}

	public void setUpdatedBy( String updatedBy )
	{
		this.updatedBy = updatedBy;
	}
}
