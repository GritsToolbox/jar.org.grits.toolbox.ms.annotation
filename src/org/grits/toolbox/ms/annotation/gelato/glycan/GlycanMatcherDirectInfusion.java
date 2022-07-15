package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.AnnotateFragments;
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
 * Extends GlycanMatcher to override methods to handle Direct Infusion data
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GlycanMatcherDirectInfusion extends GlycanMatcher {
	private static final Logger logger = Logger.getLogger(GlycanMatcherDirectInfusion.class);
	HashMap<Integer,String> avoideDuplicateAnnotation = new HashMap<Integer,String>();

	public GlycanMatcherDirectInfusion(int iCurScan, GlycanStructureAnnotation parent) {
		super(iCurScan, parent);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanMatcher#getMatchingSubScans(org.grits.toolbox.ms.om.data.Data, int, org.grits.toolbox.ms.om.data.Peak)
	 */
	public List<Integer> getMatchingSubScans( Data data, int iCurScanNum, Peak parentPeak ) {
		List<Integer> iMatchingScans = new ArrayList<>();
		for( int iHashScanNum : data.getScans().keySet() ) {
			Scan scan = data.getScans().get(iHashScanNum);
			if( scan.getScanNo() < iCurScanNum ) {
				continue;
			}
			List<Integer> lScans = GelatoUtils.getSubScanForPrecursor(getData(), iHashScanNum, parentPeak);
			if( ! lScans.isEmpty() ) {
				for( Integer iSubScanNum : lScans ) {
					iMatchingScans.add(iSubScanNum);
				}
			}
		}
		return iMatchingScans;
	}

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

		Scan parentScan = getData().getScans().get(iParentScanNum);
		if( parentScan.getMsLevel() != 1 ) {
			return super.scorePeakToFeature(data, analyteMz, analyteType, analyte, annotation, 
					scanAnnotation, parentFeature, lParentAdducts, lParentAdductCounts, iParentCharge, 
					lParentIonExchanges, lParentExchangeCounts, iParentExchangeCount, lParentNeutralLosses, 
					lParentNeutralLossCounts, iParentNeutralLossCount, settings, parentPeak, iParentScanNum, tempPath);
		}
		boolean bScored = false;
		try {
			for( int iHashScanNum : getData().getScans().keySet() ) {
				Scan scan = getData().getScans().get(iHashScanNum);
				if( scan.getMsLevel() != 1 ) {
					continue;
				}
//				if( scan.getScanNo() < getScanNum() ) {
				if( scan.getScanNo() < iParentScanNum ) {
					continue;
				}
				List<Integer> lScans = GelatoUtils.getSubScanForPrecursor(getData(), iHashScanNum, parentPeak);
				// no subscans but this is the scan that matched the current feature
				if( lScans.isEmpty() && iHashScanNum == iParentScanNum ) {
					Feature feature = new GlycanFeature();
					GelatoUtils.populateNewFeature(feature,
							data, analyte.getAnalyte().getAnalyteStringRepresentation(), 
							parentPeak.getMz(), analyteMz, iParentCharge, analyteType,
							lParentAdducts, lParentAdductCounts,
							lParentIonExchanges, lParentExchangeCounts,
							lParentNeutralLosses, lParentNeutralLossCounts,
							null, parentPeak.getId(), annotation, parentFeature);
					AnalyteMatcher.addAnnotationToScan(getData(), scanAnnotation, iHashScanNum, annotation, feature);
					continue;
				}

				if( ! lScans.isEmpty() ) {
					for( Integer iSubScanNum : lScans ) {
						Feature feature = new GlycanFeature();
						GelatoUtils.populateNewFeature(feature,
								data, analyte.getAnalyte().getAnalyteStringRepresentation(), 
								parentPeak.getMz(), analyteMz, iParentCharge, analyteType,
								lParentAdducts, lParentAdductCounts,
								lParentIonExchanges, lParentExchangeCounts,
								lParentNeutralLosses, lParentNeutralLossCounts,
								iSubScanNum, parentPeak.getId(), annotation, parentFeature);
						logger.debug("\t\tFeature id: " +  parentPeak.getId() + ", Feature mz: " + feature.getMz());
						AnalyteMatcher.addAnnotationToScan(getData(), scanAnnotation, iHashScanNum, annotation, feature);

						if( iSubScanNum == null ){
							continue;
						}

						if( getData().getScans().get(iSubScanNum) == null || getData().getScans().get(iSubScanNum).getPrecursor() == null ) {
							//							logger.error("No scan data for scan: " + iHashScanNum + ", sub scan: " + iSubScanNum );
							continue;
						}
						if(avoideDuplicateAnnotation.get(iSubScanNum)==null) {
							avoideDuplicateAnnotation.put(iSubScanNum,"seen");
							AnnotateFragments annFragments = getNewAnnotateFragmentsObject();
							annFragments.annotateFragments(
									scanAnnotation, iSubScanNum, 
									feature, 
									lParentAdducts, lParentAdductCounts, iParentCharge, 
									lParentIonExchanges, lParentExchangeCounts, iParentExchangeCount, 
									lParentNeutralLosses, lParentNeutralLossCounts, iParentNeutralLossCount, 
									annotation,  
									data, settings, 
									tempPath);
							calculateScores(scanAnnotation, annotation, feature, iSubScanNum);
							bScored = true;
						} //if avoid
					} // for sub scan
				} 
			}
		} catch( Exception e ) {
			logger.error("Error matching glycans in GlycanMatcherGlycanBased.", e );
		}
		return bScored;
	}
}
