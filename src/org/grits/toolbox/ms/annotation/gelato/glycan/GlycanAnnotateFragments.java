package org.grits.toolbox.ms.annotation.gelato.glycan;

import org.apache.log4j.Logger;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.ms.annotation.gelato.Analyte;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.AnnotateFragments;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.annotation.structure.GlycanAnalyteFragment;
import org.grits.toolbox.ms.annotation.structure.GlycanAnalyteFragments;
import org.grits.toolbox.ms.annotation.structure.GlycanStructure;
import org.grits.toolbox.ms.annotation.structure.IAnalyteFragment;
import org.grits.toolbox.ms.annotation.structure.IAnalyteFragments;

/**
 * Implementation of AnnotateFragments for Glycans.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GlycanAnnotateFragments extends AnnotateFragments {
	private static final Logger logger = Logger.getLogger(GlycanAnnotateFragments.class);
	
	public GlycanAnnotateFragments(AnalyteMatcher parentAnalyteMatcher) {
		super(parentAnalyteMatcher);
	}

	/**
	 * Instantiate a GlycanAnalyteFragments object when IAnalyteFragments is needed
	 **/
	@Override
	public IAnalyteFragments getNewAnalyteFragmentObject() {
		return new GlycanAnalyteFragments();
	}

	/**
	 * Instantiate a GelatoAnalyte assuming the fragment is a GlycanAnalyteFragment
	 * @param fragment, should be a GlycanAnalyteFragment
	 * @param sId, id to assign to the fragment
	 * @return GelatoAnalyte
	 * @see GlycanAnalyteFragment
	 **/
	@Override
	protected GelatoAnalyte getNewFragmentGelatoAnalyteObject(IAnalyteFragment fragment, String sId) {
		GlycanAnalyteFragment gfa = (GlycanAnalyteFragment) fragment;
		Glycan fragGlycan = Glycan.fromString(gfa.getFragmentEntry().getStructure());

//		Glycan fragGlycan = Glycan.fromString(analyte.getAnalyteStringRepresentation());

		GlycanStructure fragStructure = new GlycanStructure();
		fragStructure.setGWBSequence(gfa.getFragmentEntry().getStructure());
//		fragStructure.setGWBSequence(analyte.getAnalyteStringRepresentation());
		fragStructure.setId(sId);	
		Analyte glycanAnalyte = new GlycanAnalyte(fragStructure.getGWBSequence(), fragGlycan);
		
		GelatoAnalyte fragGelatoAnalyte = new GelatoAnalyte(glycanAnalyte, fragStructure);
		return fragGelatoAnalyte;
	}
	
	
}
