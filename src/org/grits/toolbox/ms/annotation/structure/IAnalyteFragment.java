package org.grits.toolbox.ms.annotation.structure;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * 
 * Interface for an Analyte Fragment. This is mostly a shell but is assumed that will be held in an array of Analyte Fragments
 *
 * @see IAnalyteFragments
 */
public interface IAnalyteFragment {
	public long getId();
	public String getType();
}
