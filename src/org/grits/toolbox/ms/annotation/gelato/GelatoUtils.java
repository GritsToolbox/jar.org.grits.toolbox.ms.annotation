package org.grits.toolbox.ms.annotation.gelato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.structure.AnalyteStructure;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyteCache;
import org.grits.toolbox.ms.file.FileReaderUtils;
import org.grits.toolbox.ms.file.reader.impl.MzXmlReader;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.FeatureSelection;
import org.grits.toolbox.ms.om.data.Ion;
import org.grits.toolbox.ms.om.data.IonAdduct;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Molecule;
import org.grits.toolbox.ms.om.data.MoleculeSettings;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;

/**
 * A collection of static method to be used for GELATO annotation of MS data
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GelatoUtils {
	private static final Logger logger = Logger.getLogger(GelatoUtils.class);

	/**
	 * Static method to fill in the lSettingsToAnalyze and lSettingsToAnalyzeCounts lists with the 
	 * options for positive mode and negative mode ions as well as neutral loss/gain and exchanges.<br><br> 
	 * The lSettingsToConsider object contains the list of adducts (Molecules) specified.
	 * The lSettingsToAnalyze and lSettingsToAnalyzeCounts contain the set of options to consider based on the max ion count specified.
	 * <br><br>
	 * Called from the initializeAdductsAndExchanges(..) method.
	 * 
	 * @param bIsPositive
	 * 		true if the mode for the MS run is positive, false if negative
	 * @param lSettingsToConsider
	 * 		generically described list of Molecule objects
	 * @param iMaxTotalCount
	 * 		total number of ions for all Molecules
	 * @param lSettingsToAnalyze
	 * 		generically described List of List of Molecule objects
	 * @param lSettingsToAnalyzeCounts
	 * 		List of List of Integers for the max number of ions for each Molecule
	 * @see Molecule
	 * @see MoleculeSettings
	 * @see IonSettings
	 * @see Ion
	 * 
	 */
	public static void determineIonSettingSets( 
			Boolean bIsPositive, Object lSettingsToConsider, int iMaxTotalCount, 
			Object lSettingsToAnalyze, List<List<Integer>> lSettingsToAnalyzeCounts ) {
		try {
			// Because IonSettings inherits from Ion which inherits from Molecule
			// And MoleculeSettings inherits from Molecule
			// I couldn't create a common API. I don't want to write 3 methods that basically do the same thing!  
			// So cast to what they are!

			if( lSettingsToConsider == null || ((List<Molecule>) lSettingsToConsider).isEmpty() ) {
				return;
			}
			boolean bDone = false;
			int iStartLevel = 0; // starting charge count
			int[] iCurChargeCnts = new int[((List<Molecule>) lSettingsToConsider).size()];
			List<String> sProcessed = new ArrayList<>(); // stores String-based representation of the parameter set

			// Iterate until all parameter sets are determined
			while( ! bDone ) {
				List<Molecule> lCurSet = new ArrayList<>(); // current set of molecules to consider, e.g. Na + Li
				List<Integer> lCurCounts = new ArrayList<>(); // current set of molecule counts to consider for each molecule, e.g. 1 Na + 2 Li (assuming less than total count/charge)
				int iTotalCount = 0;
				int iLevelCnt = 0;
				String sCombo = "";
				// consider first "iStartLevel" Molecules for the param set
				for( iLevelCnt = 0; iLevelCnt<= iStartLevel; iLevelCnt++ ) {
					// Cast to current candidate ion to Molecule
					Molecule curSetting = ((List<Molecule>) lSettingsToConsider).get(iLevelCnt);
					
					// iLevelCnt is zero-based, so convert to 1-based for count (or charge)
					int iCurCount = iCurChargeCnts[iLevelCnt] + 1;
					
					// if curSetting is instance of IonSettings, then use the IonSettings charge instead of raw count
					if( curSetting instanceof IonSettings ) {
						if( bIsPositive != null ) { // make sure polarity matches!
							if( (((IonSettings) curSetting).getPolarity() && ! bIsPositive) || 
									(!((IonSettings) curSetting).getPolarity() && bIsPositive) ) {
								continue;					
							}
						}
						iCurCount *= ((IonSettings) curSetting).getCharge();
					}					
					
					// to turn off the use of a max total count, pass in -1
					// if the current count/charge doesn't make the the total count/charge for the current parameter set 
					// surpass the max count/charge specified, create add the parameter set to the list of candidates
					if( iMaxTotalCount < 0 || (iTotalCount + iCurCount) <= iMaxTotalCount ) {
						iTotalCount += iCurCount;
						// determine String-based representation of the parameter set
						sCombo += "[" + iLevelCnt + ", " + iCurCount + "+]";
						
						// add the current setting and charge/counts
						lCurSet.add(curSetting);
						lCurCounts.add(iCurChargeCnts[iLevelCnt] + 1);
					} else {				
						;   // do nothing 
					}
				}
				
				
				// if the current total count/charge doesn't surpass the max count/charge specified, create add the parameter set to the list of total candidates
				if( (iMaxTotalCount < 0 || iTotalCount <= iMaxTotalCount) && ! sProcessed.contains(sCombo) ) {
					((List<List<Molecule>>) lSettingsToAnalyze).add(lCurSet);
					lSettingsToAnalyzeCounts.add(lCurCounts);
					sProcessed.add(sCombo);
				} else {
					; // do nothing
				}
				iStartLevel++;
				// if start level hasn't surpassed the number of Molecules to consider, pivot the indices to move to next level
				if( iStartLevel == iCurChargeCnts.length ) {
					for( int j = iCurChargeCnts.length - 1; j >= 0  && iStartLevel == iCurChargeCnts.length; j-- ) {
						Molecule curSetting = ((List<Molecule>) lSettingsToConsider).get(j);
						int iCurCnt = iCurChargeCnts[j]+1;
						int iMaxCnt = -1;
						if( curSetting instanceof IonSettings ) {
							iMaxCnt = ((IonSettings) curSetting).getCounts().get(0);
						} else {
							iMaxCnt = ((MoleculeSettings) curSetting).getCount();
						}
						if( iCurCnt < iMaxCnt ) {
							iCurChargeCnts[j]++;
							iStartLevel = j;
						} else {
							iCurChargeCnts[j] = 0;
						}
					}
				} 

				// stop when you reach end of Molecules to consider
				bDone = iStartLevel == iCurChargeCnts.length;
			}
		} catch( Exception e ) {
			logger.error("Error in determineIonSettingSets", e);
		}
	}
	
	/**
	 * Determines the different sets of settings for GELATO annotation based on the user-specified parameters (e.g. adducts, max charge, etc)
	 * @param lParentSettingsToConsider
	 * @param lParentSettingsCounts
	 * @param iMaxTotalCount
	 * @param lSettingsToAnalyze
	 * @param lSettingsToAnalyzeCounts
	 */
	public static void determineFragmentIonSettingSets( Object lParentSettingsToConsider, List<Integer> lParentSettingsCounts, int iMaxTotalCount, 
			Object lSettingsToAnalyze, List<List<Integer>> lSettingsToAnalyzeCounts ) {
		try {
			if( lParentSettingsToConsider == null || ((List<Molecule>) lParentSettingsToConsider).isEmpty() ) {
				return;
			}
			boolean bDone = false;
			int iStartLevel = 0;			
			int[] iCurChargeCnts = new int[((List<Molecule>) lParentSettingsToConsider).size()];
			List<String> sProcessed = new ArrayList<>();

			while( ! bDone ) {
				List<Molecule> lCurSet = new ArrayList<>();
				List<Integer> lCurCounts = new ArrayList<>();
				int iTotalCharge = 0;
				int iLevelCnt = 0;
				String sCombo = "";
				for( iLevelCnt = 0; iLevelCnt<= iStartLevel; iLevelCnt++ ) {
					Molecule curSetting = ((List<Molecule>) lParentSettingsToConsider).get(iLevelCnt);
					int iCurCount = iCurChargeCnts[iLevelCnt] + 1;
					if( curSetting instanceof IonSettings ) {
						iCurCount *= ((IonSettings) curSetting).getCharge();
					}
					if( iTotalCharge + iCurCount <= iMaxTotalCount ) {
						iTotalCharge += iCurCount;
						//						logger.debug("Adding adduct: [" + iLevelCnt + ", " + iCurCount + "+]");
						sCombo += "[" + iLevelCnt + ", " + iCurCount + "+]";
						lCurSet.add(curSetting);
						lCurCounts.add(iCurChargeCnts[iLevelCnt] + 1);
					} else {				
						//						logger.debug("Skipping adduct: [" + iLevelCnt + ", " + iCurCount + "+]");
					}
				}
				if( iTotalCharge <= iMaxTotalCount && ! sProcessed.contains(sCombo) ) {
					//					logger.debug("\tTotal charge (OK): " + iTotalCharge + "+");
					((List<List<Molecule>>) lSettingsToAnalyze).add(lCurSet);
					lSettingsToAnalyzeCounts.add(lCurCounts);
					sProcessed.add(sCombo);
				} else {
					//					logger.debug("\tTotal charge (SKIP): " + iTotalCharge + "+");								
				}
				iStartLevel++;
				if( iStartLevel == iCurChargeCnts.length ) {
					for( int j = iCurChargeCnts.length - 1; j >= 0  && iStartLevel == iCurChargeCnts.length; j-- ) {
						int iCurCnt = iCurChargeCnts[j]+1;
						int iMaxCnt = lParentSettingsCounts.get(j);
						if( iCurCnt < iMaxCnt ) {
							iCurChargeCnts[j]++;
							iStartLevel = j;
						} else {
							iCurChargeCnts[j] = 0;
						}
					}
				} 

				bDone = iStartLevel == iCurChargeCnts.length;
				//				logger.debug("");
			}
		} catch( Exception e ) {
			logger.error("Error in determineIonSettingSets", e);
		}
	}	
	
	public static double getTargetAccuracy(Method _method, double _dMz, boolean _bIsPrecursor) {
		double targetAccuracy = 0.0;
		if( _bIsPrecursor ) {
			if(_method.getAccuracyPpm()){
				targetAccuracy = (_method.getAccuracy()/1000000) * _dMz;	
			}else{
				targetAccuracy = _method.getAccuracy();	
			}		
		} else {
			if(_method.getFragAccuracyPpm()){
				targetAccuracy = (_method.getFragAccuracy()/1000000) * _dMz;	
			}else{
				targetAccuracy = _method.getFragAccuracy();	
			}					
		}
		return targetAccuracy;
	}

	public static List<Integer> getMatchingSubScans( Data data, Scan scan, int iPeakInx ) {
		Peak peak = scan.getPeaklist().get(iPeakInx);
		List<Integer> iSubScans = scan.getSubScans();
		List<Integer> iMatchingScans = new ArrayList<>();
		for( int i = 0; i < iSubScans.size(); i++ ) {
			Scan subScan = data.getScans().get(iSubScans.get(i));
			if( subScan.getPrecursor().getMz() == peak.getPrecursorMz() ) {
				iMatchingScans.add(subScan.getScanNo());
			}
		}
		return iMatchingScans;
	}

	public static int[] getMatchingPeak( Scan scan, int iPeakInx, double glycanMz, Method method, boolean _bIsPrecursor ) {
		try {
			Peak peak = scan.getPeaklist().get(iPeakInx);
			if( peak == null || peak.getMz() == null || peak.getIntensity() == null) {
				return null;
			}
			//calculate the shift if there is any.
			double peakMz = peak.getMz() + method.getShift();
			double targetAccuracy = GelatoUtils.getTargetAccuracy(method, glycanMz, _bIsPrecursor);

			double dMaxInt = -1;
			int iMaxPeakInx = -1;
			boolean bGo = (Math.abs(peakMz - glycanMz) <= targetAccuracy);
			while( bGo ) {
				if ( peak.getIntensity() > dMaxInt ) {
					dMaxInt  = peak.getIntensity();
					iMaxPeakInx = iPeakInx;
				}
				iPeakInx++;
				if( iPeakInx < scan.getPeaklist().size() ) {
					peak = scan.getPeaklist().get(iPeakInx);
					if( peak == null || peak.getMz() == null || peak.getIntensity() == null) {
						continue;
					}
					peakMz = peak.getMz() + method.getShift();
					bGo = (Math.abs(peakMz - glycanMz) <= targetAccuracy);
				} else {
					bGo = false;
				}
			}
			return new int[] {iPeakInx, iMaxPeakInx};
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
			ex.printStackTrace();
		}
		return null;
	}
	/**
	 * Description: Identifies the scan(s) that produced the precursor generated with the parent scan
	 * Rounds to 100th place and performs equality (they should be very close)
	 * 
	 * @param data - the data object containing the specified parent scan
	 * @param iParentScanNum - the int value of the parent scan number
	 * @param parentPeak - the Peak object in the parent that matches the current precursor
	 * @return
	 */
	public static List<Integer> getSubScanForPrecursor( Data data, int iParentScanNum, Peak parentPeak ) {
		List<Integer> lScans = new ArrayList<>();
		Scan scan = data.getScans().get(iParentScanNum);
		double dMult = 100.0; // See below
		for(Integer iSubScanNum : scan.getSubScans()) {
			try {
				Scan subScan = data.getScans().get(iSubScanNum);
				// in Khalifeh's code, he rounded to nearest integer. I'm rounding to 100th place. The peak and precursor should be VERY close
				if( subScan.getPrecursor().getPrecursorMz() != null && Math.round( subScan.getPrecursor().getPrecursorMz() ) == Math.round( parentPeak.getMz() ) ) {
					lScans.add(iSubScanNum);
					logger.debug("\t\t\tSubscan: " + subScan.getScanNo() + ", precursor m/z: " + subScan.getPrecursor().getPrecursorMz() + ". Parent peak m/z: " + parentPeak.getMz());
				}
			} catch( NullPointerException ex ) {
				logger.error("Null subscan or precursor value in getSubScanForPrecursor", ex);
			}
		}			
		return lScans;
	}

	/**
	 * @param adduct - IonSettings object
	 * @return double - mass of the IonSettings object
	 */
	public static double getAdductMass(IonSettings adduct) {
		return adduct.getMass();
	}

	/**
	 * @param count - int for total count for this IonSettings object
	 * @param adduct - IonSettings object
	 * @return double - total charge of the IonSettings object
	 */
	public static double getChargeFromAdducts(int count, IonSettings adduct) {
		return (double)adduct.getCharge() * count;
	}

	/**
	 * @param ionExchange - IonSettings object
	 * @param count - int for total count for this IonSettings object
	 * @return double - total mass of the IonSettings object
	 */
	public static double getIonExchange(IonSettings ionExchange, int count) {
		return (ionExchange.getMass() * count)-count;
	}

	/**
	 * @param nuetralLoss - MoleculeSettings object
	 * @param count - int for total count for this MoleculeSettings object
	 * @return double - total mass of the MoleculeSettings object
	 */
	public static double getNeutralLoss(MoleculeSettings nuetralLoss, int count) {
		return nuetralLoss.getMass() * count;
	}

	/**
	 * @param mass - double, mass of the structure
	 * @param z - double, charge of structure
	 * @param dAdductMass - double, mass of the structure adduct
	 * @return double - total m/z of the structure
	 */
	public static double getAnalyteStructureMz(double mass, double z, double dAdductMass) {
		if(z == 0)
			z = 1d;
		return (mass + dAdductMass) / z;
	}

	/**
	 * Calculates dAnalyteMz, dSumCharge, dSumExchanges, dSumLosses for the specified Glycan object based
	 * on the adducts, exchanges and loss/gain.
	 * 
	 * @param analyte - an Analyte object
	 * @param analyteSettings - current AnalyteSettings object
	 * @param lAdductsToAnalyze - List<List<IonSettings>> for adducts
	 * @param lAdductsToAnalyzeCounts - List<List<Integer>> ion counts for adducts 
	 * @param lExchangesToAnalyze - List<List<IonSettings>> for ion exchanges
	 * @param lExchangesToAnalyzeCounts - List<List<Integer>> ion counts for ion exchanges
	 * @param lNeutralLossesToAnalyze - List<List<IonSettings>> for neutral loss/gain
	 * @param lNeutralLossesToAnalyzeCounts - List<List<Integer>> ion counts for neutral loss/gain
	 * @return double[] - array containing {dAnalyteMz, dSumCharge, dSumExchanges, dSumLosses};
	 */
	public static double[] getAnalyteMzAndCharge( Analyte analyte, AnalyteSettings analyteSettings, 
			List<IonSettings> lAdductsToAnalyze, List<Integer> lAdductsToAnalyzeCounts, 
			List<IonSettings> lExchangesToAnalyze, List<Integer> lExchangesToAnalyzeCounts,
			List<MoleculeSettings> lNeutralLossesToAnalyze, List<Integer> lNeutralLossesToAnalyzeCounts ) {
		double dMass = 0.0;
		try {
			dMass = analyte.computeMass();
		} catch( Exception e ) {
			dMass = analyte.computeMass();
			logger.error(e.getMessage(), e);
		}
		// add ion exchange mass 
		double dSumExchanges = 0d;
		if( lExchangesToAnalyze != null ) {
			for( int i = 0; i < lExchangesToAnalyze.size(); i++ ) {
				IonSettings ionExchange = lExchangesToAnalyze.get(i);
				int iCnt = lExchangesToAnalyzeCounts.get(i);
				double dMassChange = GelatoUtils.getIonExchange(ionExchange, iCnt);
				dMass +=  dMassChange;
				dSumExchanges += (double) iCnt;
			}
		}
		// add any neutral losses
		double dSumLosses = 0d;
		if( lNeutralLossesToAnalyze != null ) {
			for( int i = 0; i < lNeutralLossesToAnalyze.size(); i++ ) {
				MoleculeSettings neutralLoss = lNeutralLossesToAnalyze.get(i);
				int iCnt = lNeutralLossesToAnalyzeCounts.get(i);
				double dMassChange = GelatoUtils.getNeutralLoss(neutralLoss, iCnt);
				dMass +=  dMassChange;
				dSumLosses += (double) iCnt;
			}
		}

		// some up charges and masses of adducts
		double dSumCharge = 0d;
		double dSumMass = 0d;
		for( int i = 0; i < lAdductsToAnalyze.size(); i++ ) {
			IonSettings adduct = lAdductsToAnalyze.get(i);
			int iCnt = lAdductsToAnalyzeCounts.get(i);
			double z = GelatoUtils.getChargeFromAdducts(iCnt,adduct);
			dSumCharge+=z;
			dSumMass += ((double) iCnt * adduct.getMass());
		}
		double dAnalyteMz = GelatoUtils.getAnalyteStructureMz(dMass,dSumCharge,dSumMass);

		return new double[] {dAnalyteMz, dSumCharge, dSumExchanges, dSumLosses};
	}
	
	public static void populateNewFeature(
			Feature feature,
			Data _data, String sFeatureSequence, double dPeakMz, 
			double glycanMz, double charge, String fragmentType,
			List<IonSettings> lAdductsToAnalyze, List<Integer> lAdductsToAnalyzeCounts, 
			List<IonSettings> lExchangesToAnalyze, List<Integer> lExchangesToAnalyzeCounts,
			List<MoleculeSettings> lNeutralLossesToAnalyze, List<Integer> lNeutralLossesToAnalyzeCounts,
			Integer scanNum, int index, Annotation annotation, Feature parentFeature ) {
		feature.setId(Integer.toString(_data.getNextFeatureIndex()));
//		feature.setSequence(sSeq.substring(0,sSeq.indexOf("$")));
		feature.setSequence(sFeatureSequence);
		feature.setCharge((int)charge);
		double deviation = ((Math.abs(dPeakMz - glycanMz)/glycanMz)*1000000.0);
		feature.setDeviation(deviation);
		feature.setFragmentType(fragmentType);
		if( lAdductsToAnalyze != null ) {
			for( int i = 0; i < lAdductsToAnalyze.size(); i++ ) {
				IonSettings adduct = lAdductsToAnalyze.get(i);
				int count = lAdductsToAnalyzeCounts.get(i);
				IonAdduct ion = new IonAdduct();
				ion.setCharge(adduct.getCharge());
				ion.setCount(count);
				ion.setLabel(adduct.getLabel());
				ion.setMass(adduct.getMass());
				ion.setName(adduct.getName());
				feature.getIons().add(ion);
			}
		}
		if(lExchangesToAnalyze != null){
			for( int i = 0; i < lExchangesToAnalyze.size(); i++ ) {
				IonSettings ionExchange = lExchangesToAnalyze.get(i);
				int exchangeCount = lExchangesToAnalyzeCounts.get(i);
				IonAdduct exchange = new IonAdduct();
				exchange.setCharge(ionExchange.getCharge());
				exchange.setCount(exchangeCount);
				exchange.setLabel(ionExchange.getLabel());
				exchange.setName(ionExchange.getName());
				exchange.setMass(ionExchange.getMass());
				feature.getNeutralexchange().add(exchange);
			}
		}
		if(lNeutralLossesToAnalyze != null){
			for( int i = 0; i < lNeutralLossesToAnalyze.size(); i++ ) {
				MoleculeSettings neutralLoss = lNeutralLossesToAnalyze.get(i);
				int neutralLossCount = lNeutralLossesToAnalyzeCounts.get(i);
				MoleculeSettings featureNeutralLoss = new MoleculeSettings();
				featureNeutralLoss.setCount(neutralLossCount);
				featureNeutralLoss.setLabel(neutralLoss.getLabel());
				featureNeutralLoss.setMass(neutralLoss.getMass());
				featureNeutralLoss.setName(neutralLoss.getName());
				feature.getNeutralLoss().add(featureNeutralLoss);
			}
		}
		feature.setMz(glycanMz);
		String rowId = Feature.getRowId(index, scanNum, true);
		FeatureSelection fSelection = new FeatureSelection();
		fSelection.setRowId(rowId);
		feature.getFeatureSelections().add(fSelection);
		feature.setAnnotationId(annotation.getId());
		feature.setPrecursor(-1);
		if( parentFeature != null ) {
			feature.setParentId(parentFeature.getId());
		}
	}
	
	/**
	 * Attempts to locate an Analyte object based on the AnalyteStructure's id. Null is returned if the Analyte isn't found.
	 * 
	 * @param structure - a AnalyteStructure object
	 * @return Analyte - the Analyte object stored in "GlycanStructureAnnotation.hmStructureSettingsMass".
	 */
	public static Analyte getAnalyteFromGelatoCache(AnalyteStructure structure) {
		try{
			if( ! GelatoAnalyteCache.hmGelatoAnalytesByStructureId.containsKey(structure.getId()) ) {
				throw new Exception("Could not find Glycan object for structure: " + structure.getId());
			} 
			// load the glycan and 
			GelatoAnalyte gelatoAnalyte = GelatoAnalyteCache.hmGelatoAnalytesByStructureId.get(structure.getId());
			if(gelatoAnalyte == null || gelatoAnalyte.getAnalyte() == null) {
				throw new Exception("Could not find Glycan object for structure: " + structure.getId());
			}
			return gelatoAnalyte.getAnalyte();
		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * @param scans - List of org.grits.toolbox.ms.om.data.Scan objects to process
	 * @param dFragCutoff - double value for fragment (non-MS1) scan cutoff
	 * @param sFragCutoffType - String type of cutoff for fragment (non-MS1), can be null if not used
	 * @param dPreCutoff - double value for precursor (MS1) scan cutoff
	 * @param sPreCutoffType - String type of cutoff for precursor (MS1), can be null if not used
	 * @return HashMap<Integer, Scan> - key is the integer scan number, value is the Scan
	 */
	public static HashMap<Integer, Scan> listToHashMap( List<Scan> scans, double dFragCutoff, String sFragCutoffType, double dPreCutoff, String sPreCutoffType ) {
		HashMap<Integer, Scan> scanHash = null;
		String sFragType = sFragCutoffType != null ? sFragCutoffType : MzXmlReader.FILTER_ABSOLUTE;
		double dFragVal = sFragCutoffType != null ? dFragCutoff : 0.0;
		String sPreType = sPreCutoffType != null ? sPreCutoffType : MzXmlReader.FILTER_ABSOLUTE;
		double dPreVal = sPreCutoffType != null ? dPreCutoff : 0.0;

		scanHash = FileReaderUtils.listToFilteredHashMap(scans, dFragVal, sFragType, dPreVal, sPreType);
		return scanHash;
	}
	
}
