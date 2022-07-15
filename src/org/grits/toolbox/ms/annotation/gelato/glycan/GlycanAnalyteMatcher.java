package org.grits.toolbox.ms.annotation.gelato.glycan;

import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.AnalyteStructureAnnotation;
import org.grits.toolbox.ms.annotation.gelato.AnnotateFragments;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanFeature;

/**
 * Implementation of AnalyteMatcher for Glycans.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GlycanAnalyteMatcher extends AnalyteMatcher {

	public GlycanAnalyteMatcher(int iCurScan, AnalyteStructureAnnotation parent) {
		super(iCurScan, parent);
	}

	/**
	 * Instantiate a GlycanFeature object when Feature is needed
	 **/
	@Override
	public Feature getNewFeatureObject() {
		return new GlycanFeature();
	}

	/**
	 * Instantiate a GlycanAnnotateFragments object when AnnotateFragments is needed
	 **/
	@Override
	public AnnotateFragments getNewAnnotateFragmentsObject() {
		return new GlycanAnnotateFragments(this);
	}

}
