/*
 * Created on Jan 1, 2001
 *
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.exceptions;

/**
 * @author Mohit Gupta
 * 
 */
public interface ErrorCodes
{

	static final int		UNKNOWN_PROBLEM					= 1;

	static final int		SERVER_PROBLEM					= 2;

	static final int		USER_PROBLEM					= 3;

	static final int		PERSISTENCE_SYSTEM_PROBLEM		= 4;

	static final int		IO_PROBLEM						= 5;

	static final int		RESOURCE_NOT_FOUND				= 6;

	static final int		PARSING_PROBLEM					= 7;

	static final int		EXPRESSION_EVALUATION_PROBLEM	= 7;

	static final String[]	ERROR_DESCRIPTION				=
	{ "Unknow Problem", "Server Internal Problem", "User Created Probelm", "Persistence System Problem", "IO Problem",
			"Resources not found", "Parsing Problem" };

}