package org.grits.toolbox.ms.annotation.gelato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.MoleculeSettings;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScansAnnotation;

/**
 * Description: Matches peaks to analyte fragments and calculates scores
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class FragmentMatcher {
	private static final Logger logger = Logger.getLogger(FragmentMatcher.class);
	protected AnalyteMatcher parentAnalyteMatcher = null;
	protected Map<Double, Integer> mPeakMatched = new HashMap<>();
	protected int curScanNum = -1;
	
	public FragmentMatcher(int curScanNum, AnalyteMatcher parentAnalyteMatcher) {
		this.parentAnalyteMatcher = parentAnalyteMatcher;
		this.curScanNum = curScanNum;
	}
	
	public List<Integer> getMatchingSubScans( Data data, int iCurScanNum, Peak parentPeak ) {
		List<Integer> iMatchingScans = new ArrayList<>();
		List<Integer> lScans = GelatoUtils.getSubScanForPrecursor(data, iCurScanNum, parentPeak);
		if( ! lScans.isEmpty() ) {
			for( Integer iSubScanNum : lScans ) {
				iMatchingScans.add(iSubScanNum);
			}
		}
		return iMatchingScans;
	}
	
	public Map<Double, Integer> getPeakMatched() {
		return mPeakMatched;
	}
	
	public int getCurScanNum() {
		return curScanNum;
	}
	public void setCurScanNum(int curScanNum) {
		this.curScanNum = curScanNum;
	}
	
	/**
	 * Attempts to match a fragment annotation candidate to the peaks in a specified scan
	 * 
	 * @param fragmentScansAnnotation, current ScansAnnotation to add match to
	 * @param fragAnnotation, fragment GlycanAnnotation object
	 * @param parentFeature, parent Feature
	 * @param fragAnalyte, fragment GelatoAnalyte object
	 * @param data, current Data object
	 * @param lFragmentAdductsToAnalyze - List<IonSettings>, list of adducts
	 * @param lFragmentAdductCounts - List<Integer>, list of adduct counts
	 * @param lFragmentExchangesToAnalyze - List<IonSettings>, list of exchanges
	 * @param lFragmentExchangesCounts - List<Integer>, list of exchange counts
	 * @param lFragmentNeutralLossesToAnalyze - List<MoleculeSettings>, list of neutral gain/losses
	 * @param lFragmentNeutralLossCounts - List<Integer>, list of parent gain/loss counts
	 * @param settings - an AnalyteSettings object
	 * @param fragmentType, fragment type
	 * @param tempPath, path to the Glycan-based annotation files
	 * @return true if fragment matches, false otherwise
	 */
	public boolean matchFragment(
			ScansAnnotation fragmentScansAnnotation, int iParentScanNum,
			Annotation fragAnnotation, Feature parentFeature, GelatoAnalyte fragAnalyte, Data data, 
			List<IonSettings> lFragmentAdductsToAnalyze, List<Integer> lFragmentAdductCounts,
			List<IonSettings> lFragmentExchangesToAnalyze, List<Integer> lFragmentExchangesCounts,
			List<MoleculeSettings> lFragmentNeutralLossesToAnalyze, List<Integer> lFragmentNeutralLossCounts,			
			AnalyteSettings settings, String fragmentType, String tempPath){
		boolean bRes = false;
		try {		
			if( getCurScanNum() != iParentScanNum ) { // this is just in case but should never really happen!
				getPeakMatched().clear();
				setCurScanNum(iParentScanNum);
			}
			double[] analyteInfo = GelatoUtils.getAnalyteMzAndCharge(fragAnalyte.getAnalyte(), settings, 
					lFragmentAdductsToAnalyze, lFragmentAdductCounts,
					lFragmentExchangesToAnalyze, lFragmentExchangesCounts,
					lFragmentNeutralLossesToAnalyze, lFragmentNeutralLossCounts);
			double analyteMz = analyteInfo[0];
			int iParentCharge = (int) analyteInfo[1];
			int iNumParentExchanges = (int) analyteInfo[2];
			int iNumParentLosses = (int) analyteInfo[3];
			int iStartInx = 0;
			if( getPeakMatched().containsKey(analyteMz) ) {
				iStartInx = getPeakMatched().get(analyteMz);
			}
			Scan scan = data.getScans().get(iParentScanNum);
			for( int iPeakInx = iStartInx; iPeakInx < scan.getPeaklist().size(); iPeakInx++ ) {
				try {					
					int[] iPeakMatchInfo = GelatoUtils.getMatchingPeak(scan, iPeakInx, analyteMz, data.getDataHeader().getMethod(), false);
					if( iPeakMatchInfo != null && iPeakMatchInfo[1] != -1 ) {
						Peak peak = scan.getPeaklist().get(iPeakMatchInfo[1]);
						boolean bCurRes = parentAnalyteMatcher.scorePeakToFeature(
									data, 
									analyteMz, fragmentType, fragAnalyte, fragAnnotation,
									fragmentScansAnnotation, parentFeature, 
									lFragmentAdductsToAnalyze, lFragmentAdductCounts, iParentCharge,
									lFragmentExchangesToAnalyze, lFragmentExchangesCounts, iNumParentExchanges,
									lFragmentNeutralLossesToAnalyze, lFragmentNeutralLossCounts, iNumParentLosses,
									settings, peak, iParentScanNum, tempPath);
						bRes |= bCurRes;
						getPeakMatched().put(analyteMz, iPeakMatchInfo[1]);
						
						// TODO: is this necessary? need to verify that if we match a peak to a scan, we're done (can't match multiple peaks to same scan)
//						 iPeakInx = iPeakMatchInfo[0] - 1;
						iPeakInx = scan.getPeaklist().size();
					}//if peak

				} catch( Exception e ) {
					logger.error("Error analyzing peaks in FragmentMatcherGlycanBased:matchFragment.", e );
				}
			}//for peak

		} catch( Exception e ) {
			logger.error("Error in FragmentMatcherGlycanBased:matchFragment.", e );
		}
		return bRes;
	}	
}
