package org.grits.toolbox.ms.annotation.gelato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.gelato.glycan.GlycanStructureAnnotation;
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
import org.grits.toolbox.widgets.progress.IProgressListener;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;
import org.grits.toolbox.widgets.tools.INotifyingProcess;
import org.grits.toolbox.widgets.tools.NotifyingProcessUtil;

/**
 * Abstract class to provide framework for matching an Analyte to a scan in a Data object
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * 
 * @see Data
 * @see AnalyteStructureAnnotation
 * 
 */
public abstract class AnalyteMatcher implements INotifyingProcess {
	private static final Logger logger = Logger.getLogger(AnalyteMatcher.class);

	protected Data data;
	protected boolean isCanceled = false;
	protected List<IProgressListener> lProgressListeners;
	protected boolean bCancel = false;
	protected AnalyteStructureAnnotation parent = null;
	protected Map<Double, Integer> mPeakMatched = new HashMap<>();
	protected int curScanNum = -1;

	public abstract Feature getNewFeatureObject();
	public abstract AnnotateFragments getNewAnnotateFragmentsObject();
	
	public AnalyteMatcher(int curScanNum, AnalyteStructureAnnotation parent) {
		this.parent = parent;
		this.curScanNum = curScanNum;
	}
	
	/**
	 * @return Data - the "data" member variable
	 */
	public Data getData() {
		return data;
	}

	/**
	 * @return int - the "iScanNum" member variable
	 */
	public int getScanNum() {
		return curScanNum;
	}

	/**
	 * Description: Calculates the Counting and Intensity scores for a match of a precursor to an analyte
	 * 
	 * @param scanAnnotations - a ScansAnnotation object 
	 * @param annotation - a Annotation object
	 * @param feature - a Feature object
	 * @param iPrecursorScan - the int value of the sub-scan number containing the fragments being matched
	 * @return true if at least one feature was scored, false otherwise
	 */ 
	protected boolean calculateScores( ScansAnnotation scanAnnotations, Annotation annotation, 
			Feature feature, int iPrecursorScan ) {
		return AnalyteMatcher.calculateScores(getData(), scanAnnotations, annotation, feature, iPrecursorScan);
	}

	/**
	 * Description: Calculates the Counting and Intensity scores for a match of a precursor to an analyte
	 * 
	 * @param data - the GRITS Data object
	 * @param scanAnnotations - a ScansAnnotation object 
	 * @param annotation - a Annotation object
	 * @param feature - a Feature object
	 * @param iPrecursorScan - the int value of the sub-scan number containing the fragments being matched
	 * @return true if at least one feature was scored, false otherwise
	 */ 
	public static boolean calculateScores( Data data,  
			ScansAnnotation scanAnnotations, Annotation annotation, 
			Feature feature, int iPrecursorScan) {
		try {
			double dCountingScore = 0.0;
			double dIntensityScore = 0.0;
			double dRawCountingScore = 0.0;
			double dRawIntensityScore = 0.0;
			if(iPrecursorScan > 0 && scanAnnotations.getScanAnnotations().get(iPrecursorScan) != null ){

				HashMap<String,Integer> uniqueAnnotations = new HashMap<String,Integer>();
				for(Feature f : scanAnnotations.getScanAnnotations().get(iPrecursorScan)){
					//remove duplicates from annotations to calculate counting score
					if( f.getParentId() != null && ! f.getParentId().equals(feature.getId()) ) {
						continue;
					}
					String sRowId = f.getFeatureSelections().get(0).getRowId();
					int iInx = sRowId.indexOf(":");
					if( iInx > 0 ) {
						sRowId = sRowId.substring(0, iInx);
					}
					uniqueAnnotations.put(sRowId, 1);
				}
				
				double dNumAnnotPeaks = (double)uniqueAnnotations.keySet().size();
				//				double dNumAnnotPeaks = (double) glycanScanAnnotations.getFragmentScansAnnotations().get(iScanNum).size();
				dCountingScore = dNumAnnotPeaks / data.getScans().get(iPrecursorScan).getPeaklist().size();
				dRawCountingScore = dNumAnnotPeaks / (double) data.getScans().get(iPrecursorScan).getTotalNumPeaks();

				//		    			feature.getScores().put(GlycanFeature.COUNTING_SCORE, score);
				double annotatedIntensity = 0.0;
				double totalIntensity = 0.0;

				for(Peak p : data.getScans().get(iPrecursorScan).getPeaklist()){
					totalIntensity += p.getIntensity();
				}
				if(uniqueAnnotations.keySet() != null && uniqueAnnotations.keySet().size() != 0){
					for(Peak p : data.getScans().get(iPrecursorScan).getPeaklist()) {
						if( p.getIntensity() == null || p.getIntensity() < 0 ) {
							continue;
						}
						String sPeakId = Integer.toString( p.getId() );
						if( uniqueAnnotations.containsKey(sPeakId) ) {
							annotatedIntensity += p.getIntensity();
						}
					}
				}
				dIntensityScore = (double)annotatedIntensity/totalIntensity;
				dRawIntensityScore = (double)annotatedIntensity / (double) data.getScans().get(iPrecursorScan).getTotalIntensity();
				//		    			feature.getScores().put(GlycanFeature.INTENSITY_SCORE, intensityScore);
			}//if glycanScanAnnotations.getScanAnnotations().get(i)		
			annotation.getScores().put(Integer.toString(iPrecursorScan), dCountingScore);
			feature.getDoubleProp().put(GlycanStructureAnnotation.countingScoreData.getKey(), dCountingScore); // DBW: 05-29
			feature.getDoubleProp().put(GlycanStructureAnnotation.intensityScoreData.getKey(), dIntensityScore);
			feature.getDoubleProp().put(GlycanStructureAnnotation.rawCountingScoreData.getKey(), dRawCountingScore); // DBW: 05-30-17
			feature.getDoubleProp().put(GlycanStructureAnnotation.rawIntensityScoreData.getKey(), dRawIntensityScore);
			return true;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return false;
	}

	/**
	 * Description: not supported yet
	 * @param parentPeak - the Peak object from the parent
	 */
	protected void handleUnscoredPeak(Peak parentPeak) {
		;
	}

	public List<Integer> getMatchingSubScans( Data data, int iCurScanNum, Peak parentPeak ) {
		List<Integer> iMatchingScans = new ArrayList<>();
		List<Integer> lScans = GelatoUtils.getSubScanForPrecursor(getData(), iCurScanNum, parentPeak);
		if( ! lScans.isEmpty() ) {
			for( Integer iSubScanNum : lScans ) {
				iMatchingScans.add(iSubScanNum);
			}
		}
		return iMatchingScans;
	}
	
	/**
	 * Description: when a Peak is matched to a Feature in GlycanStructureAnnotation, this method is called to score the match
	 * 
	 * @param data - the current Data object
	 * @param glycanScanAnnotations - a GlycanScansAnnotation object
	 * @param structure - a GlycanStructure object
	 * @param lParentAdducts - List<IonSettings>, list of parent adducts
	 * @param lParentAdductCounts - List<Integer>, list of parent adduct counts
	 * @param iParentCharge - int, actual charge of the match
	 * @param lParentIonExchanges - List<IonSettings>, list of parent exchanges
	 * @param lParentExchangeCounts - List<Integer>, list of parent exchange counts
	 * @param iParentExchangeCount - int, actual exchange count of the match
	 * @param lParentNeutralLosses - List<MoleculeSettings>, list of parent neutral gain/losses
	 * @param lParentNeutralLossCounts - List<Integer>, list of parent neutral gain/loss counts
	 * @param iParentNeutralLossCount - int, actual neutural gain/loss count of the match
	 * @param settings - an AnalyteSettings object
	 * @param annotation - the GlycanAnnotation match
	 * @param parentPeak - the matching parent Peak
	 * @param feature - the matching GlycanFeature
	 * @return boolean - whether scoring was successful (no errors)
	 */
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

		// This code is at the level of intact structure. We have the matching peak from the MS1 scan (parentPeak), now find
		// the sub-scans (MS2s) that were triggered on this peak.
		// Iterate over these subscans and annotate
		try {
			List<Integer> lScans = GelatoUtils.getSubScanForPrecursor(data, iParentScanNum, parentPeak);
			if( lScans.isEmpty() ) {
				Feature feature = getNewFeatureObject();
				GelatoUtils.populateNewFeature(feature,
						data, analyte.getAnalyte().getAnalyteStringRepresentation(), 
						parentPeak.getMz(), analyteMz, iParentCharge, analyteType,
						lParentAdducts, lParentAdductCounts,
						lParentIonExchanges, lParentExchangeCounts,
						lParentNeutralLosses, lParentNeutralLossCounts,
						null, parentPeak.getId(), annotation, parentFeature);
				AnalyteMatcher.addAnnotationToScan(data, scanAnnotation, iParentScanNum, annotation, feature);
				return false;
			}
			Scan scan = data.getScans().get(iParentScanNum);
			boolean bRes = false;
			//			if( ! lScans.isEmpty() ) {
			//
			for( Integer iSubScanNum : lScans ) {
				Feature feature = new GlycanFeature();
				GelatoUtils.populateNewFeature(feature,
						data, analyte.getAnalyte().getAnalyteStringRepresentation(), 
						parentPeak.getMz(), analyteMz, iParentCharge, analyteType,
						lParentAdducts, lParentAdductCounts,
						lParentIonExchanges, lParentExchangeCounts,
						lParentNeutralLosses, lParentNeutralLossCounts,
						iSubScanNum, parentPeak.getId(), annotation, parentFeature);
				AnalyteMatcher.addAnnotationToScan(data, scanAnnotation, iParentScanNum, annotation, feature);

				if(iSubScanNum == null || data.getScans().get(iSubScanNum) == null || data.getScans().get(iSubScanNum).getPrecursor() == null ) {
					continue;
				}
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


				boolean bCurRes = AnalyteMatcher.calculateScores(data, scanAnnotation, annotation, feature, iSubScanNum);
				bRes |= bCurRes;
			}
			return bRes;
			//			}
		} catch( Exception e ) {
			logger.error("Error matching glycans in matchGlycanStructure2.", e );
		}
		return false;
	}

	/**
	 * @param lPeaks - a List of Peak objects
	 * @return Peak - Peak with greatest intensity
	 */
	protected Peak getMostAbundantPeak( List<Peak> lPeaks ) {
		double dMaxInt = Double.MIN_VALUE;
		Peak maxPeak = null;
		for( Peak peak : lPeaks ) {
			if ( peak.getIntensity() > dMaxInt ) {
				dMaxInt  = peak.getIntensity();
				maxPeak = peak;
			}
		}
		return maxPeak;
	}

	/**
	 * Description: adds the GlycanFeature to the list of Features in the GlycanScansAnnotation object. Adds the 
	 * structure id to the list of structure ids in the Data's annotated scan's list. 
	 * 
	 * @param data - the current Data object
	 * @param glycanScanAnnotations - a GlycanScansAnnotation object
	 * @param iScanNum - the scan number that is being annotated
	 * @param structure - the GlycanStructure of the annotation
	 * @param feature - the GlycanFeature of the annotation
	 */
	public static void addAnnotationToScan(Data data, ScansAnnotation scanAnnotations, int iScanNum, 
			Annotation annotation, Feature feature) {
		try {
			if ( scanAnnotations.getScanAnnotations().get(iScanNum) == null ) {
				List<Feature> features = new ArrayList();
				features.add(feature);
				scanAnnotations.getScanAnnotations().put(iScanNum, features);
			} else if( ! scanAnnotations.getScanAnnotations().get(iScanNum).contains(feature) ) {
				scanAnnotations.getScanAnnotations().get(iScanNum).add(feature);
			}
			if ( data.getAnnotatedScan().get(iScanNum) == null ) {
				List<String> ids = new ArrayList<String>();
				ids.add(annotation.getStringId());
				data.getAnnotatedScan().put(iScanNum, ids);
			}
			else if( ! data.getAnnotatedScan().get(iScanNum).contains( annotation.getStringId() )){
				data.getAnnotatedScan().get(iScanNum).add(annotation.getStringId());
			}		
			if( ! data.getAnnotation().contains(annotation) ) {
				data.getAnnotation().add(annotation);
//				GlycanStructureAnnotation.iAnnotationIDCount++;				
			}
		} catch( Exception e ) {
			logger.error("Error matching glycans in matchGlycanStructure.", e );
		}
	}

	public Map<Double, Integer> getPeakMatched() {
		return mPeakMatched;
	}
	
	public void setScanNum(int curScanNum) {
		this.curScanNum = curScanNum;
	}
	
	/**
	 * Called from GlycanStructureAnnotation to match an annotation candidate to the peaks in a specified scan .
	 * 
	 * @param _polarity - true = positive polarity, false = negativie polarity
	 * @param data - the current Data object
	 * @param iParentScanNum - the scan number of the precursor being considered
	 * @param glycanScanAnnotations - the candidate GlycanScansAnnotation object
	 * @param gelatoAnalyte - the candidate Glycan object
	 * @param structure - a GlycanStructure object
	 * @param lAdductsToAnalyze - List<IonSettings>, list of adducts
	 * @param lAdductsToAnalyzeCounts - List<Integer>, list of adduct counts
	 * @param lExchangesToAnalyze - List<IonSettings>, list of exchanges
	 * @param lExchangesToAnalyzeCounts - List<Integer>, list of exchange counts
	 * @param lNeutralLossesToAnalyze - List<MoleculeSettings>, list of neutral gain/losses
	 * @param lNeutralLossesToAnalyzeCounts - List<Integer>, list of parent gain/loss counts
	 * @param settings - an AnalyteSettings object
	 * @param annotation - the GlycanAnnotation match
	 * 
	 * @return true if at least one peak matched to the candidate glycan, false otherwise
	 */
	public boolean matchAnalyteStructure( 
			boolean _polarity, Data data, int iParentScanNum, 
			ScansAnnotation scanAnnotations, GelatoAnalyte gelatoAnalyte, String glycanType, Annotation annotation,  
			Feature parentFeature, 			
			List<IonSettings> lAdductsToAnalyze, List<Integer> lAdductsToAnalyzeCounts, 
			List<IonSettings> lExchangesToAnalyze, List<Integer> lExchangesToAnalyzeCounts,
			List<MoleculeSettings> lNeutralLossesToAnalyze, List<Integer> lNeutralLossesToAnalyzeCounts,
			AnalyteSettings settings, String tempPath){
		try {
			logger.debug("Structure: " + gelatoAnalyte.getAnalyte().getAnalyteStringRepresentation());
			this.data = data;
			if( getScanNum() != iParentScanNum ) { // this is just in case but should never really happen!
				getPeakMatched().clear();
				setScanNum(iParentScanNum);
			}
			double[] analyteInfo = GelatoUtils.getAnalyteMzAndCharge(gelatoAnalyte.getAnalyte(), settings, lAdductsToAnalyze, 
					lAdductsToAnalyzeCounts, lExchangesToAnalyze, lExchangesToAnalyzeCounts, lNeutralLossesToAnalyze, 
					lNeutralLossesToAnalyzeCounts);
			double analyteMz = analyteInfo[0];
			int iParentCharge = (int) analyteInfo[1];
			int iNumParentExchanges = (int) analyteInfo[2];
			int iNumParentLosses = (int) analyteInfo[3];
			int iStartInx = 0;
			if( getPeakMatched().containsKey(analyteMz) ) {
				iStartInx = getPeakMatched().get(analyteMz);
			}
			Scan scan = getData().getScans().get(getScanNum());
			setMaxValue(scan.getPeaklist().size());
			boolean bRes = false;
			for( int iPeakInx = iStartInx; iPeakInx < scan.getPeaklist().size(); iPeakInx++ ) {
				if( isCanceled ) {
					return false;
				}
				Peak aPeak = scan.getPeaklist().get(iPeakInx);
				updateListeners("Processing peak m/z: " + (aPeak != null ? aPeak.getMz() : "null"), iPeakInx );
				if( scan.getPolarity() != null ) {
					// if polarities don't match, return w/out attempting to id the structure
					if ( ! scan.getPolarity().equals(Boolean.valueOf(_polarity)) ) {
						continue;
					}
				}
				int[] iPeakMatchInfo = GelatoUtils.getMatchingPeak(scan, iPeakInx, analyteMz, data.getDataHeader().getMethod(), true);
				if( iPeakMatchInfo != null && iPeakMatchInfo[1] != -1 ) {
					//					getMyProgressListener().setProgressMessage("Processing peak m/z: " + (aPeak != null ? aPeak.getMz() : "null") + ", matched glycan: " + glycan);
					logger.debug("Glycan: " + gelatoAnalyte + ", glycan m/z: " + analyteMz + ", Scan: " + getScanNum());
					logger.debug("\tPeak match:  peak id: " + aPeak.getId() + ", peak m/z: " + aPeak.getMz());
					Peak peak = scan.getPeaklist().get(iPeakMatchInfo[1]);
					logger.debug("\t\tFinal peak id: " + peak.getId() + ", Final peak mz: " + peak.getMz());
					if(getData().getDataHeader().getMethod().getTrustMzCharge()){
						int iPrecursorCharge = -1;
						if( peak.getPrecursorCharge() != null ) {
							iPrecursorCharge = peak.getPrecursorCharge();
						}
						// DBW 10/03/17: if user wants to trust charge in mzml but precursor charge is unknown
						// we want to consider all charge states so don't skip if precursor charge == -1
						// otherwise, the precursor charge must equal the candidate glycan charge
						if( iPrecursorCharge != -1 && peak.getPrecursorCharge() != iParentCharge ) {
							updateListeners("Processing peak m/z: " + (aPeak != null ? aPeak.getMz() : "null"), (iPeakInx+1) );
							continue;
						}
					}
					bRes = true;

					boolean bScored = scorePeakToFeature(
							data,
							analyteMz,
							glycanType,
							gelatoAnalyte, 
							annotation, 
							scanAnnotations, parentFeature,
							lAdductsToAnalyze, lAdductsToAnalyzeCounts, iParentCharge,
							lExchangesToAnalyze, lExchangesToAnalyzeCounts, iNumParentExchanges,
							lNeutralLossesToAnalyze, lNeutralLossesToAnalyzeCounts, iNumParentLosses,
							settings, peak, getScanNum(), tempPath);
					if( ! getData().getScans().get(getScanNum()).getAnnotatedPeaks().containsKey(peak.getId()) || 
							! getData().getScans().get(getScanNum()).getAnnotatedPeaks().get(peak.getId())) {
						int iNumAnnot = getData().getScans().get(getScanNum()).getNumAnnotatedPeaks() != null ? getData().getScans().get(getScanNum()).getNumAnnotatedPeaks() : 0;
						getData().getScans().get(getScanNum()).setNumAnnotatedPeaks(iNumAnnot+1);
						getData().getScans().get(getScanNum()).getAnnotatedPeaks().put(peak.getId(), Boolean.TRUE);
					}
					getPeakMatched().put(analyteMz, iPeakMatchInfo[1]);					
					// TODO: do we need to continue? we can't match the same analyte again, right?
//					iPeakInx = iPeakMatchInfo[0] - 1;
					iPeakInx = scan.getPeaklist().size();
				} else {
					handleUnscoredPeak(aPeak);
				}
			} 
			updateListeners("Done!", scan.getPeaklist().size());
			return bRes;
		} catch( Exception e ) {
			logger.error("Error matching glycans in matchGlycanStructure.", e );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setProgressListeners(java.util.List)
	 */
	@Override
	public void setProgressListeners(List<IProgressListener> lProgressListeners) {
		this.lProgressListeners = lProgressListeners;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#addProgressListeners(org.grits.toolbox.widgets.progress.IProgressListener)
	 */
	@Override
	public void addProgressListeners(IProgressListener lProgressListener) {
		this.lProgressListeners.add(lProgressListener);	
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#getProgressListeners()
	 */
	@Override
	public List<IProgressListener> getProgressListeners() {
		return lProgressListeners;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return this.bCancel;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean bCancel) {
		this.bCancel = bCancel;	
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#updateListeners(java.lang.String, int)
	 */
	@Override
	public void updateListeners( String _sMsg, int _iVal ) {
		NotifyingProcessUtil.updateListeners(getProgressListeners(), _sMsg, _iVal);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#updateErrorListener(java.lang.String)
	 */
	@Override
	public void updateErrorListener(String _sMsg) {
		NotifyingProcessUtil.updateErrorListener(getProgressListeners(), _sMsg);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#updateErrorListener(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void updateErrorListener(String _sMsg, Throwable t) {
		NotifyingProcessUtil.updateErrorListener(getProgressListeners(), _sMsg, t);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setMaxValue(int)
	 */
	@Override
	public void setMaxValue(int _iVal) {
		NotifyingProcessUtil.setMaxValue(getProgressListeners(), _iVal);		
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setProgressType(org.grits.toolbox.widgets.progress.IProgressListener.ProgressType)
	 */
	@Override
	public void setProgressType(ProgressType progressType) {
		NotifyingProcessUtil.setProgressType(getProgressListeners(), progressType);

	}
}
