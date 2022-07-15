package org.grits.toolbox.ms.annotation.structure;

import java.util.HashMap;

import org.grits.toolbox.ms.om.data.Annotation;

/**
 * Provides a cache for Gelato Analytes that are read from a database.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GelatoAnalyteCache {	
	public final static HashMap<String, GelatoAnalyte> hmGelatoAnalytesByStructureId = new HashMap<>();
	public final static HashMap<String, Annotation> hmCachedExtraSettings = new HashMap<>(); // improves performance, key is the analyte id (structure)
}
