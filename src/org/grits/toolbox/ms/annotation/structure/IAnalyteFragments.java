package org.grits.toolbox.ms.annotation.structure;

import java.util.List;

import org.grits.toolbox.ms.om.data.AnalyteSettings;

/**
 * Interface to hold a set of Analyte Fragments.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see IAnalyteFragment
 */
public interface IAnalyteFragments {
	public List<IAnalyteFragment> getAnalyteFragments(AnalyteSettings settings);
}
