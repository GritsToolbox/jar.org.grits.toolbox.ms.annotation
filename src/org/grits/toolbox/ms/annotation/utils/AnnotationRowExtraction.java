package org.grits.toolbox.ms.annotation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.FeatureSelection;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;

public class AnnotationRowExtraction {
	private static final Logger logger = Logger.getLogger(AnnotationRowExtraction.class);

	public final static ArrayList<Scan> getPrecursorScan( Data data, Integer iParentScan, Integer iParentPeakNo,
			HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> htParentScanToParentPeaksToSubScan ) {
		Scan precursorScan = null;
		ArrayList<Scan> alScans = new ArrayList<>();
		if( iParentScan != null && htParentScanToParentPeaksToSubScan.containsKey(iParentScan) ) {
			HashMap<Integer, ArrayList<Integer>> htParentPeaksToSubScans = htParentScanToParentPeaksToSubScan.get(iParentScan);
			if( iParentPeakNo != null && htParentPeaksToSubScans.containsKey(iParentPeakNo) ) {
				ArrayList<Integer> alSubScans = htParentPeaksToSubScans.get(iParentPeakNo);
				for( Integer iScan : alSubScans ) {
					precursorScan = data.getScans().get( iScan );
					alScans.add(precursorScan);
				}
			}
		}

		if( alScans.isEmpty() ) {
			alScans.add(null);
		}
		return alScans;
	}

	public static HashMap<String, List<Feature>> createRowIdToFeatureHash( ScanFeatures _features ) {
		try {
			HashMap<String, List<Feature>> htRetData =new HashMap<String, List<Feature>>();
			for( Feature feature : _features.getFeatures() ) {
				if ( feature == null || feature.getFeatureSelections() == null || feature.getFeatureSelections().isEmpty() ) 
					continue;
				for( FeatureSelection selection : feature.getFeatureSelections() ) {
					List<Feature> alFeatureList = null;
					if( htRetData.containsKey(selection.getRowId()) ) {
						alFeatureList = htRetData.get(selection.getRowId());
					} else {
						alFeatureList = new ArrayList<Feature>();
						htRetData.put(selection.getRowId(), alFeatureList);
					}
					if( ! alFeatureList.contains(feature) ) {
						alFeatureList.add(feature);
					}

				}
			}		
			return htRetData;
		} catch(Exception e) {
			logger.log(Level.ERROR, "Exception in createPeakIdToFeatureHash", e);
			e.printStackTrace();
		}
		return null;
	}

	// Peak Ids are no longer used to map to features. Use Row Ids instead. For older projects, conversion may be necessary
	// Note that the original method returned a Map w/ Integer keys, but to make compatible w/ new version, I convert the peak IDs to Strings....
	@Deprecated
	public static HashMap<String, List<Feature>> createPeakIdToFeatureHash( ScanFeatures _features ) {
		try {
			HashMap<String, List<Feature>> htRetData =new HashMap<String, List<Feature>>();
			for( Feature feature : _features.getFeatures() ) {
				if ( feature == null || feature.getPeaks() == null || feature.getPeaks().isEmpty() ) 
					continue;
				for( Integer rowId : feature.getPeaks() ) {
					List<Feature> alFeatureList = null;
					if( htRetData.containsKey(rowId.toString()) ) {
						alFeatureList = htRetData.get(rowId.toString());
					} else {
						alFeatureList = new ArrayList<Feature>();
						htRetData.put(rowId.toString(), alFeatureList);
					}
					if( ! alFeatureList.contains(feature) ) {
						alFeatureList.add(feature);
					}
				}
			}		
			return htRetData;
		} catch(Exception e) {
			logger.log(Level.ERROR, "Exception in createPeakIdToFeatureHash", e);
			e.printStackTrace();
		}
		return null;
	}

	public static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> createParentScanToParentPeaksToSubScanHash( Data _data ) {
		try {
			HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> htRetData = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
			Set<Integer> setParentScanNos = _data.getScans().keySet();
			Object[] arParentScanNos = setParentScanNos.toArray();
			for(int i = 0; i < arParentScanNos.length; i++ ) {
				Integer iParentScan = (Integer) arParentScanNos[i];
				Scan curParentScan = _data.getScans().get(iParentScan);
				if( curParentScan == null || curParentScan.getSubScans() == null || curParentScan.getSubScans().isEmpty() ) {
					continue;
				}
				HashMap<Integer, ArrayList<Integer>> htParentPeaks = null;
				if( htRetData.containsKey(iParentScan) ) {
					htParentPeaks = htRetData.get(iParentScan);
				} else {
					htParentPeaks = new HashMap<Integer, ArrayList<Integer>>();
					htRetData.put(iParentScan, htParentPeaks);
				}    		
				List<Integer> iSubScanNos = curParentScan.getSubScans();
				for(int iSubScan = 0; iSubScan < iSubScanNos.size(); iSubScan++ ) {
					Scan curSubScan = _data.getScans().get(iSubScanNos.get(iSubScan));
					if( curSubScan == null || curSubScan.getPrecursor() == null ) {
						continue;
					}
					Integer iParentPeak = curSubScan.getPrecursor().getId();
					ArrayList<Integer> alSubScans = null;
					if( htParentPeaks.containsKey(iParentPeak) ) {
						alSubScans = htParentPeaks.get(iParentPeak);
					} else {
						alSubScans = new  ArrayList<Integer>();
						htParentPeaks.put(iParentPeak, alSubScans);
					}    		

					alSubScans.add(curSubScan.getScanNo());
				}
			}		
			return htRetData;
		} catch(Exception e) {
			logger.log(Level.ERROR, "Exception in createParentScanToParentPeaksToSubScanHash", e);
			e.printStackTrace();
		}
		return null;
	}

	public static void updateParentScanToParentPeaksToSubScanHashForDirectInfusion( 
			HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> htParentScanToParentPeaksToSubScanHash, Data _data ) {
		try {
			Set<Integer> setParentScanNos = _data.getScans().keySet();
			Object[] arParentScanNos = setParentScanNos.toArray();
			//			Set<Integer> setParentScanNos = htParentScanToParentPeaksToSubScanHash.keySet();
			//			Object[] arParentScanNos = setParentScanNos.toArray();
			for(int i = 0; i < arParentScanNos.length; i++ ) {
				Integer iThatParentScan = (Integer) arParentScanNos[i];
				Scan sThatParentScan = _data.getScans().get(iThatParentScan);
				if( sThatParentScan.getMsLevel() != 1 )
					continue;
				HashMap<Integer, ArrayList<Integer>> htThatParentPeaksToSubScanHash = null;
				if( htParentScanToParentPeaksToSubScanHash.containsKey(iThatParentScan) ) {
					htThatParentPeaksToSubScanHash = htParentScanToParentPeaksToSubScanHash.get(iThatParentScan);
				} else { // this scan had no sub scans...but it will inherit all of the others, so add the hash here
					htThatParentPeaksToSubScanHash = new HashMap<>();
					htParentScanToParentPeaksToSubScanHash.put(iThatParentScan, htThatParentPeaksToSubScanHash);
				}   
				for(int j = 0; j < arParentScanNos.length; j++ ) {
					if( i == j ) {
						continue;
					}				
					Integer iThisParentScan = (Integer) arParentScanNos[j];
					Scan sThisParentScan = _data.getScans().get(iThisParentScan);
					if( sThisParentScan.getMsLevel() != 1 )
						continue;
					HashMap<Integer, ArrayList<Integer>> htThisParentPeaksToSubScansHash = null;
					if( htParentScanToParentPeaksToSubScanHash.containsKey(iThisParentScan) ) {
						htThisParentPeaksToSubScansHash = htParentScanToParentPeaksToSubScanHash.get(iThisParentScan);
					} else { // if I don't exist yet, well then skip me
						//						throw new Exception("No parent peaks to sub scans for parentscan: " + iThisParentScan );
						continue;
					}    		
					for( Integer iThisParentPeak : htThisParentPeaksToSubScansHash.keySet() ) {
						ArrayList<Integer> alThoseSubScans = null;
						if( htThatParentPeaksToSubScanHash.containsKey(iThisParentPeak) ) {
							alThoseSubScans = htThatParentPeaksToSubScanHash.get(iThisParentPeak);
						} else {
							alThoseSubScans = new ArrayList<>();
							htThatParentPeaksToSubScanHash.put(iThisParentPeak, alThoseSubScans);
						}
						ArrayList<Integer> alTheseSubScans = htThisParentPeaksToSubScansHash.get(iThisParentPeak);
						for( Integer iThisSubScan : alTheseSubScans ) {
							if( ! alThoseSubScans.contains(iThisSubScan) ) {
								alThoseSubScans.add(iThisSubScan);
							}
						}
						//						htThatParentPeaksToSubScanHash.put( iThisParentPeak, alTheseSubScans);
					}
				}
			}
		} catch(Exception e) {
			logger.log(Level.ERROR, "Exception in updateParentScanToParentPeaksToSubScanHashForDirectInfusion", e);
			e.printStackTrace();
		}
	}

	
	public static boolean convertPeakIdsToRowIds( Data data, ScanFeatures scanFeatures, int iParentScan, Integer iPeakId,
			ArrayList<Scan> precursorScans,
			HashMap<String, List<Feature>> htPeakToFeatures ) {

		// need to just check the first feature to see if row_ids list is empty while peak_id list is not
		boolean bConverted = false;
		boolean bNullPrecursor = false;
		for( Scan precursorScan : precursorScans ) {
			String sRowId1 = iPeakId.toString();
			if( precursorScan == null ) {
				bNullPrecursor = true;
			}
			String sRowId2 = bNullPrecursor ? sRowId1 : Feature.getRowId(iPeakId, precursorScan.getScanNo(), true /*scanFeatures.getUsesComplexRowId()*/);
			// if the rowid contains the scan number but the hashmap only contains the peak id, then we convert the ids
//			if( ! sRowId1.equals(sRowId2) && htPeakToFeatures.containsKey(sRowId1) ) {
			if( htPeakToFeatures.containsKey(sRowId1) ) {
				List<Feature> alFeatures = htPeakToFeatures.get(sRowId1);
				List<Feature> alNewFeatures = new ArrayList<>();
				for( Feature feature : alFeatures ) {
					if( feature.getPeaks().contains(iPeakId) ) {
						FeatureSelection fSelection = new FeatureSelection();
						fSelection.setRowId(sRowId2);
						fSelection.setManuallySelected(feature.getManuallySelected());
						fSelection.setSelected(feature.getSelected());
						feature.getFeatureSelections().add(fSelection);
//						feature.getPeaks().remove(iPeakId);
						alNewFeatures.add(feature);
						bConverted = true;
					}
				}
				if( ! alNewFeatures.isEmpty() ) {
					htPeakToFeatures.put(sRowId2, alNewFeatures);
				}
			}
		}
		// clean up
		if( bConverted ) {
			if( htPeakToFeatures.containsKey(iPeakId.toString()) ) {
				List<Feature> alFeatures = htPeakToFeatures.get(iPeakId.toString());
				for( Feature feature : alFeatures ) {
					if( feature.getPeaks().contains(iPeakId) ) {
						feature.getPeaks().remove(iPeakId);
					}
				}
				if( ! bNullPrecursor ) {
					htPeakToFeatures.remove(iPeakId.toString());
				}
			}
			scanFeatures.setUsesComplexRowId(true);
		}
		return bConverted;
	}
	
	public static ArrayList<Integer> getUniqueAnnotationList( ScanFeatures _features ) {
		ArrayList<Integer> alList = new ArrayList<Integer>();
		for( Feature feature : _features.getFeatures() ) {
			if ( feature == null ) 
				continue;
			if ( ! alList.contains( feature.getAnnotationId() ) ) {
				alList.add( feature.getAnnotationId() );
			}
		}
		return alList;
	}

	public static Annotation getAnnotation( Data _data, Integer annId ) {
		for (Annotation ann : _data.getAnnotation() ) {
			if ( ann.getId().equals(annId) ) {
				return ann;
			}
		}
		return null;
	}
}
