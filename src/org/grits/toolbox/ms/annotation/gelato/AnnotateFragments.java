package org.grits.toolbox.ms.annotation.gelato;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.annotation.structure.GlycanAnalyteFragments;
import org.grits.toolbox.ms.annotation.structure.IAnalyteFragment;
import org.grits.toolbox.ms.annotation.structure.IAnalyteFragments;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.MoleculeSettings;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScansAnnotation;

/**
 * Top-level abstract class for annotation of analyte fragments in GELATO. Should be extended for particular MS types.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public abstract class AnnotateFragments {
	private static final Logger logger = Logger.getLogger(AnnotateFragments.class);
	protected AnalyteMatcher parentAnalyteMatcher = null;
		
	protected List<List<IonSettings>> lFragmentAdductsToAnalyze = new ArrayList<>();
	protected List<List<Integer>> lFragmentAdductCounts = new ArrayList<>();
	protected List<List<IonSettings>> lFragmentExchangesToAnalyze = new ArrayList<>();
	protected List<List<Integer>> lFragmentExchangesCounts = new ArrayList<>();
	protected List<List<MoleculeSettings>> lFragmentNeutralLossesToAnalyze = new ArrayList<>();
	protected List<List<Integer>> lFragmentNeutralLossCounts = new ArrayList<>();

	protected abstract IAnalyteFragments getNewAnalyteFragmentObject();
	protected abstract GelatoAnalyte getNewFragmentGelatoAnalyteObject(IAnalyteFragment aFrag, String sId);
	

	public AnnotateFragments(AnalyteMatcher parentAnalyteMatcher) {
		this.parentAnalyteMatcher = parentAnalyteMatcher;
	}	

	/**
	 * Initializes all the data structures with the various adducts, loss/gain, exchanges, and their counts.<br>
	 * The possible sets are subsets of what the parent information specifies (i.e. charge state of the parent is max for fragment)
	 * 
	 * @param lParentAdducts
	 * @param lParentAdductCounts
	 * @param iParentCharge
	 * @param lParentIonExchanges
	 * @param lParentExchangeCounts
	 * @param iParentExchangeCount
	 * @param lParentNeutralLosses
	 * @param lParentNeutralLossCounts
	 * @param iParentNeutralLossCount
	 */
	protected void initializeAdductsAndExchanges( 
			List<IonSettings> lParentAdducts, List<Integer> lParentAdductCounts, int iParentCharge,
			List<IonSettings> lParentIonExchanges, List<Integer> lParentExchangeCounts, int iParentExchangeCount, 
			List<MoleculeSettings> lParentNeutralLosses, List<Integer> lParentNeutralLossCounts, int iParentNeutralLossCount) {
		lFragmentAdductsToAnalyze.clear();
		lFragmentAdductCounts.clear();
		lFragmentExchangesToAnalyze.clear();
		lFragmentExchangesCounts.clear();
		lFragmentNeutralLossesToAnalyze.clear();
		lFragmentNeutralLossCounts.clear();
		GelatoUtils.determineFragmentIonSettingSets(lParentAdducts, lParentAdductCounts, iParentCharge, lFragmentAdductsToAnalyze, lFragmentAdductCounts);
		GelatoUtils.determineFragmentIonSettingSets(lParentIonExchanges, lParentExchangeCounts, iParentExchangeCount, lFragmentExchangesToAnalyze, lFragmentExchangesCounts);
		GelatoUtils.determineFragmentIonSettingSets(lParentNeutralLosses, lParentNeutralLossCounts, iParentNeutralLossCount, lFragmentNeutralLossesToAnalyze, lFragmentNeutralLossCounts);
	}
	
	/**
	 * Determines the fragments for the specified sequence and then initiates GELATO to annotate them. Can be called recursively.
	 * 
	 * @param scanAnnotation, ScansAnnotation object to populate
	 * @param scanNum, the current parent scan number
	 * @param parentFeature, the parent GlycanFeature
	 * @param lParentAdducts, list of adducts
	 * @param lParentAdductCounts, list of adduct counts
	 * @param iParentCharge, max adduct charge
	 * @param lParentIonExchanges, list of ion exchanges
	 * @param lParentExchangeCounts, list of exchange counts
	 * @param iParentExchangeCount, max exchange count
	 * @param lParentNeutralLosses, list of neutral losses
	 * @param lParentNeutralLossCounts, list of loss counts
	 * @param iParentNeutralLossCount, max neutral loss count
	 * @param annotation, current Annotation
	 * @param data, current Data object
	 * @param settings, user preferences in AnalyteSettings
	 * @param tempPath, path to temporary Annalyte-based annotation files
	 */
	public void annotateFragments(
			ScansAnnotation scanAnnotation,
			int scanNum, Feature parentFeature, 
			List<IonSettings> lParentAdducts, List<Integer> lParentAdductCounts, int iParentCharge,
			List<IonSettings> lParentIonExchanges, List<Integer> lParentExchangeCounts, int iParentExchangeCount,
			List<MoleculeSettings> lParentNeutralLosses, List<Integer> lParentNeutralLossCounts, int iParentNeutralLossCount,			
			Annotation parentAnnotation, Data data, AnalyteSettings settings, 
			String tempPath ) {
			
		Scan scan = data.getScans().get(scanNum);
		String scanActMethod = scan.getActivationMethode().trim();
		int scanMSLevel = scan.getMsLevel();
		boolean isMono = data.getDataHeader().getMethod().getMonoisotopic();
		GlycanAnalyteFragments analyteFragments = (GlycanAnalyteFragments) getNewAnalyteFragmentObject();
		analyteFragments.setMonoisotopic(isMono);
		analyteFragments.setScanActivationMethod(scanActMethod);
		analyteFragments.setScanMSLevel(scanMSLevel);
		analyteFragments.setSequence(parentFeature.getSequence());
		List<IAnalyteFragment> lFrags = analyteFragments.getAnalyteFragments(settings);
		
		if(lFrags == null){
			return;
		}
		
		initializeAdductsAndExchanges(
				lParentAdducts, lParentAdductCounts, iParentCharge, 
				lParentIonExchanges, lParentExchangeCounts, iParentExchangeCount, 
				lParentNeutralLosses, lParentNeutralLossCounts, iParentNeutralLossCount);
		int iCnt = 0;
		int iNumAdducts = lFragmentAdductsToAnalyze.size(); // this better not be null!
		int iNumExchanges = (lFragmentExchangesToAnalyze != null ? lFragmentExchangesToAnalyze.size() : 0);
		int iNumLosses = (lFragmentNeutralLossesToAnalyze != null ? lFragmentNeutralLossesToAnalyze.size() : 0);
		
		FragmentMatcher matcher = new FragmentMatcher( scanNum, this.parentAnalyteMatcher );
		for( int iFragCnt = 0; iFragCnt < lFrags.size(); iFragCnt++ ) {
			IAnalyteFragment aFrag = lFrags.get(iFragCnt);
			GelatoAnalyte fragGelatoAnalyte = getNewFragmentGelatoAnalyteObject(aFrag, parentAnnotation.getStringId());
						
			for( int i = 0; i < iNumAdducts; i++ ) {				
				for( int j = -1; j < iNumExchanges; j++ ) {
					for( int k = -1; k < iNumLosses; k++ ) {
						List<IonSettings> lAdducts = null;
						List<Integer> lAdductCounts = null;
						if( lFragmentAdductsToAnalyze == null || lFragmentAdductsToAnalyze.isEmpty() || lFragmentAdductCounts == null || lFragmentAdductCounts.isEmpty() ) {
							continue;
						}
						lAdducts = lFragmentAdductsToAnalyze.get(i);
						lAdductCounts = lFragmentAdductCounts.get(i);

						List<IonSettings> lExchanges = null;
						List<Integer> lExchangeCounts = null;
						if( j > -1 ) {
							lExchanges = lFragmentExchangesToAnalyze.get(j);
							lExchangeCounts = lFragmentExchangesCounts.get(j);
						}

						List<MoleculeSettings> lNeutralLosses = null;
						List<Integer> lNeutralLossCounts = null;
						if( k > -1 ) {
							lNeutralLosses = lFragmentNeutralLossesToAnalyze.get(k);
							lNeutralLossCounts = lFragmentNeutralLossCounts.get(k);
						}

						boolean bRes = matcher.matchFragment( 
								scanAnnotation, scanNum, parentAnnotation, parentFeature,
								fragGelatoAnalyte, data,
								lAdducts, lAdductCounts, lExchanges, lExchangeCounts, 
								lNeutralLosses, lNeutralLossCounts,
								settings, aFrag.getType(), tempPath );
						iCnt++;

					}
				}
			}
		}
		logger.debug("Done with fragments. Total options considered: " + iCnt);

	}//annotateFragments
}
