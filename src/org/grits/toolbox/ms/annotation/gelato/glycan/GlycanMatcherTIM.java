package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.MoleculeSettings;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.ScansAnnotation;

/**
 * Extends GlycanMatcherDirectInfusion to override methods to handle TIM data
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GlycanMatcherTIM extends GlycanMatcherDirectInfusion {
	public GlycanMatcherTIM(int iCurScan, GlycanStructureAnnotation parent) {
		super(iCurScan, parent);
	}

	private static final Logger logger = Logger.getLogger(GlycanMatcherTIM.class);

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
		return super.scorePeakToFeature(data, analyteMz, analyteType, analyte, annotation, scanAnnotation,  parentFeature,
				lParentAdducts, lParentAdductCounts, iParentCharge, 
				lParentIonExchanges, lParentExchangeCounts, iParentExchangeCount, 
				lParentNeutralLosses, lParentNeutralLossCounts, iParentNeutralLossCount, 
				settings, parentPeak, iParentScanNum, tempPath);
	}
}
