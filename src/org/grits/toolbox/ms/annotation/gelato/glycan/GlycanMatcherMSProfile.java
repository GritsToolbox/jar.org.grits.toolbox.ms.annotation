package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.GelatoUtils;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanFeature;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.MoleculeSettings;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScansAnnotation;

/**
 * Extends GlycanMatcher to override methods to handle MS Profile data
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GlycanMatcherMSProfile extends GlycanMatcher {
	private static final Logger logger = Logger.getLogger(GlycanMatcherMSProfile.class);

	public GlycanMatcherMSProfile(int iCurScan, GlycanStructureAnnotation parent) {
		super(iCurScan, parent);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanMatcher#handleUnscoredPeak(org.grits.toolbox.ms.om.data.Peak)
	 */
	@Override
	protected void handleUnscoredPeak(Peak parentPeak) {
		Scan parentScan = getData().getScans().get(getScanNum());
		try {
			Scan subScan = ((GlycanStructureAnnotationMSProfile) parent).getPhonySubScan(parentPeak, parentScan);
			int iSubScanNum = subScan.getScanNo();
		} catch( Exception ex ) {
			logger.error("Error matching glycan structure to phony scan num.", ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanMatcher#scorePeakToFeature(org.grits.toolbox.ms.om.data.GlycanScansAnnotation, org.grits.toolbox.ms.annotation.structure.GlycanStructure, java.util.List, java.util.List, int, java.util.List, java.util.List, int, java.util.List, java.util.List, int, org.grits.toolbox.ms.om.data.AnalyteSettings, org.grits.toolbox.ms.om.data.GlycanAnnotation, org.grits.toolbox.ms.om.data.Peak, org.grits.toolbox.ms.om.data.GlycanFeature)
	 */
	@Override
	public boolean scorePeakToFeature(
			Data data, 
			double analyteMz,
			String analyteType,
			GelatoAnalyte analyte,
			Annotation annotation,
			ScansAnnotation scanAnnotation,
			Feature parentFeature, 
			List<IonSettings> lParentAdducts, List<Integer> lParentAdductCounts, int iParentCharge,
			List<IonSettings> lParentIonExchanges, List<Integer> lParentExchangeCounts, int iParentExchangeCount,
			List<MoleculeSettings> lParentNeutralLosses, List<Integer> lParentNeutralLossCounts, int iParentNeutralLossCount,			
			AnalyteSettings settings,  
			Peak parentPeak, int iParentScanNum, 
			String tempPath ){

		Scan parentScan = getData().getScans().get(getScanNum());
		try {			
			Scan subScan = ((GlycanStructureAnnotationMSProfile) parent).getPhonySubScan(parentPeak, parentScan);
			int iSubScanNum = subScan.getScanNo();
			Feature feature = new GlycanFeature();
			GelatoUtils.populateNewFeature(feature,
					data, analyte.getAnalyte().getAnalyteStringRepresentation(), 
					parentPeak.getMz(), analyteMz, iParentCharge, analyteType,
					lParentAdducts, lParentAdductCounts,
					lParentIonExchanges, lParentExchangeCounts,
					lParentNeutralLosses, lParentNeutralLossCounts,
					iSubScanNum, parentPeak.getId(), annotation, parentFeature);
			logger.debug("\t\tFeature id: " +  parentPeak.getId() + ", Feature mz: " + feature.getMz());
			AnalyteMatcher.addAnnotationToScan(getData(), scanAnnotation, getScanNum(), annotation, feature);
			
			calculateScores(scanAnnotation, annotation, feature, iSubScanNum);
			if(getData().getAnnotatedScan().get(iSubScanNum) == null){
				List<String> ids = new ArrayList<String>();
				ids.add(annotation.getStringId());
				getData().getAnnotatedScan().put(iSubScanNum, ids);
			}//if data
			else{
				getData().getAnnotatedScan().get(iSubScanNum).add(annotation.getStringId());
			}//else

		} catch( Exception ex ) {
			logger.error("Error matching glycan structure to phony scan num.", ex);
		}
		return true; // with MS Profiling we won't have subscans, so if we got here, the peak matched which is good enough
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanMatcher#calculateScores(org.grits.toolbox.ms.om.data.GlycanScansAnnotation, org.grits.toolbox.ms.om.data.GlycanAnnotation, org.grits.toolbox.ms.om.data.GlycanFeature, int)
	 */
	@Override
	protected boolean calculateScores( ScansAnnotation scanAnnotations, Annotation annotation, 
			Feature feature, int iPrecursorScanNum ) {
		annotation.getScores().put(Integer.toString(iPrecursorScanNum), -1.0);
		feature.getDoubleProp().put(GlycanStructureAnnotation.countingScoreData.getKey(), -1.0); // DBW: 05-29
		feature.getDoubleProp().put(GlycanStructureAnnotation.intensityScoreData.getKey(), -1.0);
		feature.getDoubleProp().put(GlycanStructureAnnotation.rawCountingScoreData.getKey(), -1.0); // DBW: 05-29
		feature.getDoubleProp().put(GlycanStructureAnnotation.rawIntensityScoreData.getKey(), -1.0);
		return true;
	}

}
