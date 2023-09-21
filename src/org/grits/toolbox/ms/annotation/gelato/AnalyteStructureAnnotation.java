package org.grits.toolbox.ms.annotation.gelato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.structure.AnalyteStructure;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyteCache;
import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.DataHeader;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.MoleculeSettings;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;
import org.grits.toolbox.ms.om.data.ScansAnnotation;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;
import org.grits.toolbox.widgets.progress.IProgressListener;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.INotifyingProcess;
import org.grits.toolbox.widgets.tools.NotifyingProcessUtil;

/**
 * Top-level abstract class for GELATO. Should be extended for particular MS types.
 * 
 * @author D Brent Weatherly - dbrentw@uga.edu
 */
public abstract class AnalyteStructureAnnotation implements INotifyingProcess {
	private static final Logger logger = Logger.getLogger(AnalyteStructureAnnotation.class);

	protected List<INotifyingProcess> notifyingProcesses = new ArrayList();
	protected DataHeader dataHeader = null;
	protected List<List<AnalyteStructure>> structures = null;
	protected Data data;

	public final static CustomExtraData countingScoreData = new CustomExtraData( "counting_score", "Counting Score", 
			"Generic Method", CustomExtraData.Type.Double, "0.00" );
	public final static CustomExtraData intensityScoreData = new CustomExtraData( "intensity_score", "Intensity Score", 
			"Second Method", CustomExtraData.Type.Double, "0.00" );

	public final static CustomExtraData rawCountingScoreData = new CustomExtraData( "raw_counting_score", "Raw Counting Score", 
			"Generic Method", CustomExtraData.Type.Double, "0.0000" );
	public final static CustomExtraData rawIntensityScoreData = new CustomExtraData( "raw_intensity_score", "Raw Intensity Score", 
			"Second Method", CustomExtraData.Type.Double, "0.0000" );

	protected String m_tempFilePath = null;
	protected String m_preArchive = null;
	protected String m_finalArchive = null;
	protected List<Integer> scansToProcess;
	protected int iTotalSize;
	protected MSFile msFile;
	public static int iAnnotationIDCount;
	protected int iCurProgressValue;
	protected String sCurProgressMsg = null;
	protected int iCurScanNum;

	protected List<List<IonSettings>> lPosModeSettingsToAnalyze = null;
	protected List<List<Integer>> lPosModeSettingsToAnalyzeCounts = null;
	protected List<List<IonSettings>> lNegModeSettingsToAnalyze = null;
	protected List<List<Integer>> lNegModeSettingsToAnalyzeCounts = null;
	protected List<List<IonSettings>> lExchangesoAnalyze = null;
	protected List<List<Integer>> lExchangesToAnalyzeCounts = null;
	protected List<List<MoleculeSettings>> lNeutralLossesToAnalyze = null;
	protected List<List<Integer>> lNeutralLossesToAnalyzeCounts = null;

	protected abstract HashMap<Integer, Scan> getScans(MSFile msFile, int _iScanNumber, double dFragCutoff, String sFragCutoffType, double dPreCutoff, String sPreCutoffType );
	public abstract List<Integer> determineScanBounds();
	public abstract void initializeSubScanMap();
	public abstract String getFinalArchiveName();
	public abstract boolean needsOverview();	
	public abstract String getOverviewFileName();
	
	protected abstract AnalyteMatcher getNewAnalyteMatcher(int iCurScan);
	protected abstract void cleanAfterCancel();
	protected abstract void setArchiveFilePaths();

	protected List<IProgressListener> myProgressListeners = null;
	protected List<IProgressListener> matcherProgressListeners = null;

	protected boolean bCancel = false;

	public AnalyteStructureAnnotation() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param data
	 * 		data to be used during annotation
	 * @param path
	 * 		the path for the temporary, intermediate files created during annotation
	 * @param archiveName
	 * 		the destination path plus the archive name WITHOUT EXTENSION (determined later)
	 * @param msFile
	 * 		the full path to the mzXML/mzML file for this project
	 */
	public AnalyteStructureAnnotation( Data data, String path, String archiveName, MSFile msFile) {
		this.data = data;
		this.dataHeader = data.getDataHeader();
		this.m_tempFilePath = path;
		this.m_preArchive = archiveName;
		addFeatureCustomExtraData();		
		addAnnotationCustomExtraData();
		iTotalSize = -1;
		iAnnotationIDCount = 1;
		this.msFile = msFile;
	}
	
	/**
	 * @param settings - An AnalyteSettings object to describe the parameters for GELATO
	 * @return List of AnalyteStructure objects
	 */
	protected abstract List<AnalyteStructure> loadAnalyteSettingsFromDB(AnalyteSettings settings);	
	
	
	/**
	 * Facilitates creation of an Annotation object for a particular methodology (Annotation sub-class)
	 * @return a new Annotation object
	 */
	protected abstract Annotation getNewAnnotationObject();
	
	/**
	 * Applies the filter specified in this.filterSetting to the Glycan object for the specified structure.
	 * Called from "applyFilters()"
	 * 
	 * @param structure - The org.grits.toolbox.ms.annotation.structure.GlycanStructure under consideration
	 * @param _analyteSettings - The current org.grits.toolbox.ms.om.data.AnalyteSettings object
	 * @return boolean - pass/fail
	 */
	protected abstract boolean passesFilters(AnalyteStructure structure, AnalyteSettings _analyteSettings);
	
	/**
	 * Annotation Step 6.<br>
	 * Facilitates processing of a particular GelatoAnalyte object for a particular methodology (AnalyteSettings sub-class)
	 *
	 * @param _analyteSettings - the current AnalyteSettings object to use for GELATO annotation of the MS file
	 * @param structure - GelatoAnalyte under consideration
	 * @return - pass/fail
	 */
	protected abstract boolean processStructure( AnalyteSettings _analyteSettings, GelatoAnalyte gelatoAnalyte);

	/**
	 * Facilitates reading of ScansAnnotation objects for a particular methodology (ScansAnnotation sub-class)
	 * 
	 * @param reader - AnnotationReader to read Annotation data from a particular path
	 * @param filePath - path containing temporary Annotation data
	 * @param annId - Id of the Annotation object to read
	 * @return ScansAnnotation object present in the filePath
	 */
	protected abstract ScansAnnotation readScansAnnotation( AnnotationReader reader, String filePath, String annId );
	
	/**
	 * Facilitates creation of a GelatoAnalyte object for a particular methodology (GelatoAnalyte sub-class)
	 * 
	 * @param structure - current AnalyteStructure object
	 * @return a new GelatoAnalyte object
	 */
	protected abstract GelatoAnalyte getNewGelatoAnalyteObject( AnalyteStructure structure );

	protected Data getData() {
		return data;
	}

	/**
	 * @return int
	 * 		the number of progress listeners. Only using 1 for the "Structure" loop
	 */
	public static int getNumListenersNeeded() {
		return 1; // for Structure 
	}

	/**
	 * @param matcherProgressListeners 
	 * 		listeners that might want to keep up with progress for GlycanMatcher
	 */
	public void setMatcherProgressListener(List<IProgressListener> matcherProgressListeners) {
		this.matcherProgressListeners = matcherProgressListeners;
	}

	/**
	 * @return List<IProgressListener>
	 * 		listeners that might want to keep up with progress for GlycanMatcher
	 */
	public List<IProgressListener> getMatcherProgressListener() {
		return matcherProgressListeners;
	}

	/**
	 * Value for iTotalSize is calculated in initialize() method
	 * @return int
	 * 		total number of steps for the main progress listeners (getProgressListeners()). 
	 */
	public int getTotal() {
		return iTotalSize;
	}

	
	/**
	 * Adds the GlycanAnnotation object to the list of annotations for the current Data object.
	 * @param annotation
	 */
	protected void addAnnotation( GlycanAnnotation annotation ) {
		data.getAnnotation().add(annotation);		
	}

	public List<INotifyingProcess> getNotifyingProcesses() {
		return notifyingProcesses;
	}
	
	/**
	 * Called as a first step for annotation to initialize list of scans to process, the archive file path, 
	 * and the total number of steps for the progress listener
	 */
	public void initialize() {
		setProgressType(ProgressType.Indeterminant);
		scansToProcess = determineScanBounds();
		initializeSubScanMap();
		setArchiveFilePaths();
		iTotalSize = 0;
		int iNumScans = scansToProcess.size();
		initializeStructures();
		for( int i = 0; i < structures.size(); i++ ) {
			List<AnalyteStructure> settingStructures = structures.get(i);
			iTotalSize += (iNumScans * settingStructures.size()) ;
		}
		setProgressType(ProgressType.Determinant);
	}


	/**
	 * Annotation Step 1. <br>
	 * Iterates over each scan in the "scansToProcess" list.
	 * 
	 * @return int - a GRITSProcessStatus value
	 * 
	 */
	public int processScans() {
		try {
			boolean bRes = true;
			setMaxValue(getTotal());
			iCurProgressValue = 0;
			updateListeners("Processing", 0);
			Method method = getData().getDataHeader().getMethod();
			for( int iInx = 0; iInx < scansToProcess.size(); iInx++ ) {
				sCurProgressMsg = "Scan # " + scansToProcess.get(iInx);
				if( isCanceled() )
					return GRITSProcessStatus.CANCEL;
				bRes = processScan(method, iInx);
			}		
			updateListeners("Done!", getTotal());
			if (!bRes) { 
				if (isCanceled())
					return GRITSProcessStatus.CANCEL;
				return GRITSProcessStatus.ERROR;
			}
			return GRITSProcessStatus.OK;
		} catch( Exception e ) {
			logger.error("Error in processScans", e);
		}
		return GRITSProcessStatus.ERROR;
	}

	/**
	 * Annotation Step 2. <br>
	 * Processes each scan. Must first get the peak info (read MS File) and then call processAnalyteSettings. No loop.
	 * @param method - Method to use for GELATO annotation
	 * @param iInx - int value for the scan index in the "scansToProcess" data structure
	 * @return boolean - pass/fail
	 * 
	 */
	protected boolean processScan(Method method, int iInx) {
		if( isCanceled() )
			return false;
		Double dCutoffVal = method.getIntensityCutoff();
		String sCutoffType = method.getIntensityCutoffType();
		Double dPrecursorCutoffVal = method.getPrecursorIntensityCutoff();
		String sPrecursoCutoffType = method.getPrecursorIntensityCutoffType();

		iCurScanNum = scansToProcess.get(iInx);
		// clear out the data for each scan 
		// then retrieve scan data from mzxml
		data.getScanFeatures().clear();
		data.getScans().clear();
		data.getAnnotation().clear();
		data.getAnnotatedScan().clear();
		logger.debug("Processing scan #: " + iCurScanNum);
		data.setScans( getScans(this.msFile, iCurScanNum, dCutoffVal, sCutoffType, dPrecursorCutoffVal, sPrecursoCutoffType) );
		return processAnalyteSettings();
	}

	/**
	 * Annotation Step 3.<br>
	 * Iterate of each analyte setting option to process the current scan.
	 * @return boolean - pass/fail
	 * 
	 */
	protected boolean processAnalyteSettings() {
		try {
			String prevProgMsg = sCurProgressMsg;
			Method method = getData().getDataHeader().getMethod();
			boolean bRes = false;
			for( int analyteSettingsInx = 0; analyteSettingsInx < method.getAnalyteSettings().size(); analyteSettingsInx++ ) {
				if( isCanceled() )
					return false;
				AnalyteSettings analyteSettings = method.getAnalyteSettings().get(analyteSettingsInx);
				List<AnalyteStructure> analyteStructures = structures.get(analyteSettingsInx); // these should have the same index (see initializeStructures() method)
				initializeAdductsAndExchanges(analyteSettings);
				sCurProgressMsg = prevProgMsg + ", analyte settings " + (analyteSettingsInx+1) + " of " + method.getAnalyteSettings().size();
				bRes |= processAnalyteSetting(analyteSettings, analyteStructures);
			}// for AnalyteSettings		
			if( bRes ) {
				return writeScanFeaturesToArchive();
			}
		} catch( Exception e ){
			logger.error("Error in processAnalyteSettings", e);
		}
		return false;
	}

	/**
	 * Writes the temporary analyte-centric files to the final archive
	 * 
	 * @return true if no error, false otherwise
	 */
	protected boolean writeScanFeaturesToArchive() {
		try {
			AnalyteStructureAnnotation.iAnnotationIDCount++;
			populateScanFeatureData(m_tempFilePath);
			AnnotationWriter writer = new AnnotationWriter();
			writer.generateScansAnnotationFiles(this.m_tempFilePath, data, getFinalArchiveName(), ! needsOverview(), ! needsOverview(), true, true );
			return true;
		} catch( Exception e ){
			logger.error("Error in processAnalyteSettings", e);
		}
		return false;
	}

	/**
	 * Initializes all the data structures with the varius adducts, loss/gain, exchnanges, and their counts.<br>
	 * <i>Technically, we should have different adducts, exchanges, etc 
	 * per analyte settings object, but this isn't implemented yet!</i>
	 * 
	 * @param _analayteSettings - the current AnalyteSettings object to use for GELATO annotation of the MS file
	 * 
	 */
	protected void initializeAdductsAndExchanges( AnalyteSettings _analayteSettings ) {
		Method method = getData().getDataHeader().getMethod();
		lPosModeSettingsToAnalyze = new ArrayList<>();
		lPosModeSettingsToAnalyzeCounts = new ArrayList<>();
		GelatoUtils.determineIonSettingSets(true, method.getIons(), method.getMaxIonCount(), lPosModeSettingsToAnalyze, lPosModeSettingsToAnalyzeCounts);

		lNegModeSettingsToAnalyze = new ArrayList<>();
		lNegModeSettingsToAnalyzeCounts = new ArrayList<>();
		GelatoUtils.determineIonSettingSets(false, method.getIons(), method.getMaxIonCount(), lNegModeSettingsToAnalyze, lNegModeSettingsToAnalyzeCounts);

		lExchangesoAnalyze = new ArrayList<>();
		lExchangesToAnalyzeCounts = new ArrayList<>();
		GelatoUtils.determineIonSettingSets(null, method.getIonExchanges(), method.getMaxIonCount(), lExchangesoAnalyze, lExchangesToAnalyzeCounts);

		lNeutralLossesToAnalyze = new ArrayList<>();
		lNeutralLossesToAnalyzeCounts = new ArrayList<>();
		GelatoUtils.determineIonSettingSets(null, method.getNeutralLoss(), -1, lNeutralLossesToAnalyze, lNeutralLossesToAnalyzeCounts);
	}

	/**
	 * Annotation Step 4.<br>
	 * Calls "processStructures" for current AnalyteSetting object. <br>
	 * If there were matches, then call populateScanFeatureData (Step 4b) to load the feature info from the Data object
	 * and the temporary, analyte-centric files.<br> 
	 * Then call writer.generateScansAnnotationFiles(..) to write the compiled results to the archive.
	 * 
	 * Note: the temporary, analyte-centric files are removed at this step (in writer.generateScansAnnotationFiles)
	 * 
	 * @param _analyteSettings - the current AnalyteSettings object to use for GELATO annotation of the MS file
	 * @param _structures - a list of AnalyteStructure to be searched
	 * @return boolean - pass/fail
	 * 
	 */
	protected boolean processAnalyteSetting(AnalyteSettings _analyteSettings, List<AnalyteStructure> _structures) {
		try {
			boolean bRes = processStructures(_analyteSettings, _structures);
			return bRes;
		} catch( Exception e ){
			logger.error("Error in processAnalyteSettings", e);
		}
		return false;
	}

	/**
	 * Annotation Step 5.<br>
	 * Iterates over all structures to find a match to the current scan + analyte settings combination.
	 * Calls "passesFilter" for each structure. If the structure passes the filter, then calls
	 * "processStructure(...)" to determine if the structure matches the spectra in the scan based on the 
	 * adducts, loss/gain, exchange options determine.
	 * 
	 * @param _analyteSettings - the current AnalyteSettings object to use for GELATO annotation of the MS file
	 * @return boolean - pass/fail
	 * 
	 */
	protected boolean processStructures ( AnalyteSettings _analyteSettings, List<AnalyteStructure> structures) {
		try {
			//this will loop over the pre-Flags given by the user, if any one is not satisfied the glycan structure is not considered.		
			String prevProgMsg = sCurProgressMsg;
			boolean bRes = false;
			for (AnalyteStructure structure : structures) {
				if( isCanceled() )
					return false;
				sCurProgressMsg = prevProgMsg + ", structure: " + structure.getId();
				GelatoAnalyte gelatoAnalyte = GelatoAnalyteCache.hmGelatoAnalytesByStructureId.get(structure.getId());
				if( gelatoAnalyte == null || gelatoAnalyte.getAnalyteStructure() == null ) {
					throw new Exception("Unable to location analyte structure with id: " + structure.getId());
				}
				bRes |= processStructure(_analyteSettings, gelatoAnalyte);				
				updateListeners(sCurProgressMsg, ++iCurProgressValue);
			}// for GlycanStructure
			return bRes;
		} catch( Exception e ) {
			logger.error("Error in processStructure", e);
		}
		return false;
	}
	
	/**
	 * Instantiates a new Annotation object via call to "getNewAnnotationObject()" (which should be implemented by sub-classes)
	 * 
	 * @param sAnnotId, the ID assigned to the new Annotation object
	 * @param sSequence, the sequence of the Annotation object
	 * @param sSeqFormat, the format of the sequence
	 * 
	 * @return the new Annotation object
	 */
	protected Annotation getNewAnnotation( String sAnnotId, String sSequence, String sSeqFormat ) {
		Annotation annotation = getNewAnnotationObject();
		annotation.setStringId(sAnnotId);
		annotation.setSequence(sSequence);
		annotation.setSequenceFormat(sSeqFormat);
		return annotation;
	}
	
	/**
	 * Annotation Step 7. <br>
	 * Iterates over all ion settings and calls the matcher to see if there is a match for the specified structure
	 * 
	 * @param lPosModeSettingsToAnalyze - List<List<IonSettings>> for positive mode adducts
	 * @param lPosModeSettingsToAnalyzeCounts - List<List<Integer>> ion counts for positive mode adducts 
	 * @param lNegModeSettingsToAnalyze - List<List<IonSettings>> for negative mode adducts
	 * @param lNegModeSettingsToAnalyzeCounts - List<List<Integer>> ion counts for negative mode adducts
	 * @param lExchangesToAnalyze - List<List<IonSettings>> for ion exchanges
	 * @param lExchangesToAnalyzeCounts - List<List<Integer>> ion counts for ion exchanges
	 * @param lNeutralLossesToAnalyze - List<List<IonSettings>> for neutral loss/gain
	 * @param lNeutralLossesToAnalyzeCounts - List<List<Integer>> ion counts for neutral loss/gain
	 * @param scansAnnotation - current ScansAnnotation object
	 * @param analyteSettings - current AnalyteSetting object
	 * @param gelatoAnalyte - current GelatoAnalyte object
	 * @param annotation - current Annotation object
	 * 
	 * @return boolean - pass/fail
	 */
	protected boolean processAllStructureOptions(
			List<List<IonSettings>> lPosModeSettingsToAnalyze, List<List<Integer>> lPosModeSettingsToAnalyzeCounts,
			List<List<IonSettings>> lNegModeSettingsToAnalyze, List<List<Integer>> lNegModeSettingsToAnalyzeCounts, 
			List<List<IonSettings>> lExchangesToAnalyze, List<List<Integer>> lExchangesToAnalyzeCounts,
			List<List<MoleculeSettings>> lNeutralLossesToAnalyze, List<List<Integer>> lNeutralLossesToAnalyzeCounts,
			ScansAnnotation scansAnnotation,
			AnalyteSettings analyteSettings, GelatoAnalyte gelatoAnalyte, 
			Annotation annotation) {
		try {

			AnalyteMatcher analyteMatcher = getNewAnalyteMatcher(iCurScanNum);
			getNotifyingProcesses().add(analyteMatcher);
			
			int iCnt = 0;
			boolean bRes = false;
			for( int polCnt = 0; polCnt < 2; polCnt++ ) {
				if( isCanceled() )
					return false;

				boolean bPolarity = (polCnt == 0);
				List<List<IonSettings>> lSettingsToAnalyze = (bPolarity? lPosModeSettingsToAnalyze : lNegModeSettingsToAnalyze);
				List<List<Integer>> lSettingsToAnalyzeCounts = (bPolarity ? lPosModeSettingsToAnalyzeCounts : lNegModeSettingsToAnalyzeCounts);				
				if ( lSettingsToAnalyze == null || lSettingsToAnalyze.isEmpty() ) {
					continue;
				}
				int iNumAdducts = lSettingsToAnalyze.size(); // this better not be null!
				int iNumExchanges = (lExchangesToAnalyze != null ? lExchangesToAnalyze.size() : 0);
				int iNumLosses = (lNeutralLossesToAnalyze != null ? lNeutralLossesToAnalyze.size() : 0);
				for( int i = 0; i < iNumAdducts; i++ ) {				
					for( int j = -1; j < iNumExchanges; j++ ) {
						for( int k = -1; k < iNumLosses; k++ ) {
							List<IonSettings> lAdducts = null;
							List<Integer> lAdductCounts = null;
							if( lSettingsToAnalyze == null || lSettingsToAnalyze.isEmpty() || lSettingsToAnalyzeCounts == null || lSettingsToAnalyzeCounts.isEmpty() ) {
								continue;
							}
							lAdducts = lSettingsToAnalyze.get(i);
							lAdductCounts = lSettingsToAnalyzeCounts.get(i);
							if( lAdducts == null || lAdducts.isEmpty() || lAdductCounts == null || lAdductCounts.isEmpty() ) {
								continue;
							}
							List<IonSettings> lExchanges = null;
							List<Integer> lExchangeCounts = null;
							if( j > -1 ) {
								lExchanges = lExchangesToAnalyze.get(j);
								lExchangeCounts = lExchangesToAnalyzeCounts.get(j);
							}
							List<MoleculeSettings> lNeutralLosses = null;
							List<Integer> lNeutralLossCounts = null;
							if( k > -1 ) {
								lNeutralLosses = lNeutralLossesToAnalyze.get(k);
								lNeutralLossCounts = lNeutralLossesToAnalyzeCounts.get(k);
							}

							bRes |= analyteMatcher.matchAnalyteStructure( bPolarity, data, iCurScanNum, scansAnnotation, gelatoAnalyte, 
									null, annotation, null, lAdducts, lAdductCounts, lExchanges, lExchangeCounts, lNeutralLosses, lNeutralLossCounts,
									analyteSettings, this.m_tempFilePath);
							iCnt++;
						}
					}
				}
			}
			logger.debug("Number of options for structure: " + iCnt);
			
			getNotifyingProcesses().remove(analyteMatcher);
			return true;
		} catch( Exception e ) {
			logger.error("Error in processAdducts", e);
		}
		return false;
	}

	/**
	 * Annotation Step 4b. <br>
	 * Called from "processAnalyteSetting(..)" when there is at least one analyte match for the current scan.
	 * Calls "reader.readglycanAnnotation(..) to read the temporary, intermediate analyte-centric files
	 * 
	 * @param tempFilePath - String value for the location of the temporary, intermediate analyte-centric files
	 */
	protected void populateScanFeatureData(String tempFilePath) {
		//define objects to gather the MS1 annotation while processing MS2
		AnnotationReader reader = new AnnotationReader();
		ScansAnnotation scansAnnotation = new ScansAnnotation();
		ScanFeatures scanFeatures = null;
		HashMap<String, Integer> seen = new HashMap<>();
		for( Integer scanId : data.getAnnotatedScan().keySet() ) {
			if( isCanceled() ) {
				return;
			}
			for(String analyteId : data.getAnnotatedScan().get(scanId)){
				if( seen.containsKey(analyteId) ) {
					continue;
				}
				scansAnnotation = readScansAnnotation(reader, tempFilePath, analyteId);
				if(scansAnnotation != null ) {
					for( Integer scanId2 : scansAnnotation.getScanAnnotations().keySet() ) {
						if( isCanceled() )
							return;
						if( data.getScanFeatures().containsKey(scanId2) ) {
							scanFeatures = data.getScanFeatures().get(scanId2);
						} else {
							scanFeatures = new ScanFeatures();
							scanFeatures.setScanId(scanId2);
							scanFeatures.setUsesComplexRowId(true);
							scanFeatures.setScanPeaks(new HashSet<Peak>(data.getScans().get(scanId2).getPeaklist()));
							data.getScanFeatures().put(scanId2, scanFeatures);		
						}
						if( scansAnnotation.getScanAnnotations().get(scanId2) != null) {				
							List<Feature> lF = scansAnnotation.getScanAnnotations().get(scanId2);
							for( Feature f : scansAnnotation.getScanAnnotations().get(scanId2) ) {
								if( ! scanFeatures.getFeatures().contains(f) ) {
									scanFeatures.getFeatures().add(f);
								}
							}
						}
					}
				}//if glycanAnnotations
				seen.put(analyteId, 1);
			}
		}
	}

	/**
	 * Adds the GELATO custom extra data into DataHeader appropriate for "Features". <br>
	 * Currently, we have the "counting score" and the "intensity score".
	 */
	protected void addFeatureCustomExtraData() {
		dataHeader.getFeatureCustomExtraData().add(countingScoreData);
		dataHeader.getFeatureCustomExtraData().add(intensityScoreData);		
		dataHeader.getFeatureCustomExtraData().add(rawCountingScoreData);
		dataHeader.getFeatureCustomExtraData().add(rawIntensityScoreData);		
	}

	/**
	 * Adds the GELATO custom extra data into DataHeader appropriate for "Annotations". <br>
	 * Currently, these are stored in the class "GlycanExtraInfo"
	 */
	protected void addAnnotationCustomExtraData() {
		List<CustomExtraData> lCED = GlycanExtraInfo.getColumns();
		for( CustomExtraData ced : lCED ) {
			dataHeader.getAnnotationCustomExtraData().add(ced);
		}
	}

	/**
	 * In case the sequence needs to be reformatted for specific use...
	 * 
	 * @param _sStructureSequence, the sequence to be formatted
	 * @return the corrected sequence
	 */
	protected String correctSequence( String _sStructureSequence ) {
		return _sStructureSequence;
	}

	
	/**
	 * Initializes the lists of AnalyteStructures for each AnalyteSettings object.
	 */
	protected void initializeStructures() {
		Method method = getData().getDataHeader().getMethod();
		structures = new ArrayList<>();
		for( AnalyteSettings settings : method.getAnalyteSettings() ) {
			List<AnalyteStructure> settingStructures = loadAnalyteSettingsFromDB(settings);
			if(structures != null) {
				structures.add(settingStructures);
			}
		}

		// 02-21-18: Check for id redundant ids in each db, correcting if necessary
		for( int i = 0; i < method.getAnalyteSettings().size(); i++ ) {
			List<AnalyteStructure> settingStructures1 = structures.get(i);
			Map<String, Boolean> mMatched = new HashMap<>();
			for( int iStruc1 = 0; iStruc1 < settingStructures1.size(); iStruc1++ ) {
				AnalyteStructure struc1 = settingStructures1.get(iStruc1);
				if( mMatched.containsKey(struc1.getId()) ) {
					continue;
				}
				int iMatchCnt = 0;
				for( int iStruc2 = iStruc1+1; iStruc2 < settingStructures1.size(); iStruc2++ ) {
					AnalyteStructure struc2 = settingStructures1.get(iStruc2);
					if( struc1.getId().equals(struc2.getId()) ) {
						String sNewId = struc2.getId() + "." + (++iMatchCnt+1);
						struc2.setId( sNewId );
						mMatched.put(sNewId, Boolean.TRUE);
					}
				}
				if( iMatchCnt > 0 ) {
					logger.warn("Duplicate id in database: " + struc1.getId());
					String sNewId = struc1.getId() + ".1";
					struc1.setId( sNewId );
					mMatched.put(sNewId, Boolean.TRUE);		
				}
			}
		}

		// 02-21-18: Check for id redundant ids across dbs, correcting if necessary
		for( int i = 0; i < method.getAnalyteSettings().size(); i++ ) {
			List<AnalyteStructure> settingStructures1 = structures.get(i);
			for( AnalyteStructure struc1 : settingStructures1 ) {
				for( int j = i + 1; j < method.getAnalyteSettings().size(); j++ ) {
					List<AnalyteStructure> settingStructures2 = structures.get(j);				
					for( AnalyteStructure struc2 : settingStructures2 ) {
						if( struc1.getId().equals(struc2.getId()) ) {
							if( ! struc1.getId().startsWith("(DB") ) {
								struc1.setId( "(DB" + (i+1) + ") " + struc1.getId());
							}
							if( ! struc2.getId().startsWith("(DB") ) {
								struc2.setId( "(DB" + (j+1) + ") " + struc2.getId());
							}
							break;
						}
					}
				}
			}
		}		
	}
	
	/**
	 * For the AnalyteStructure objects in the "structures" variable (read from the database xml file),
	 * Instantiate the GelatoAnalyte objects and store in static HashMap "GelatoAnalyteCache.hmStructureSettingsMass". 
	 * 
	 */
	public void populateGelatoAnalyteObjects() {
		int iProgress = 0;		
		//		setMaxValue(structures.size());
		int iTotalStructures = 0;
		for( List<AnalyteStructure> lStructures : structures ) {
			iTotalStructures += lStructures.size();
		}
		setMaxValue(iTotalStructures);
		for( List<AnalyteStructure> lStructures : structures ) {
			for (AnalyteStructure structure : lStructures) {
				try {
					if( isCanceled() )
						return;
					updateListeners("Structure: " + structure.getId(), iProgress++);
					GelatoAnalyte gelatoGlycan = getNewGelatoAnalyteObject(structure);
					if( gelatoGlycan != null ) {
						GelatoAnalyteCache.hmGelatoAnalytesByStructureId.put(structure.getId(), gelatoGlycan);
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		updateListeners("Done!", structures.size());

	}	

	/**
	 * For the AnalyteStructure objects in the "structures" variable (read from the database xml file)
	 * apply any database filters,
	 * updates the structures with the filtered structures if there is a filter
	 */
	public void applyFilters() {
		List<List<AnalyteStructure>> filteredStructures = new ArrayList<>();

		int iProgress = 0;		
		int iTotalStructures = 0;
		for( List<AnalyteStructure> lStructures : structures ) {
			iTotalStructures += lStructures.size();
		}
		setMaxValue(iTotalStructures);

		Method method = getData().getDataHeader().getMethod();
		int i=0;
		for (AnalyteSettings settings: method.getAnalyteSettings()) {
			if (settings.getGlycanSettings().getFilterSetting() != null) {
				List<AnalyteStructure> filtered = new ArrayList<>();
				for (AnalyteStructure structure : structures.get(i)) {
					if( isCanceled() )
						return;
					updateListeners("Structure: " + structure.getId(), iProgress++);
					if (passesFilters(structure, settings)) {
						filtered.add(structure);
					}
				}
				filteredStructures.add(filtered);
			} else {
				filteredStructures.add(structures.get(i));
			}
			i++;
		}
		updateListeners("Done!", structures.size());
		this.structures = filteredStructures;
	}

	/**
	 * 
	 * @return the MS file object
	 */
	public MSFile getMsFile() {
		return msFile;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean bCancel) {
		this.bCancel = bCancel;
		if( bCancel ) {
			for( INotifyingProcess process : getNotifyingProcesses() ) {
				process.setCanceled(true);
			}
		}
	}	

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#isCanceled()
	 */
	public boolean isCanceled() {
		return bCancel;
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
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#addProgressListeners(org.grits.toolbox.widgets.progress.IProgressListener)
	 */
	@Override
	public void addProgressListeners(IProgressListener lProgressListener) {
		this.myProgressListeners.add(lProgressListener);	
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#getProgressListeners()
	 */
	@Override
	public List<IProgressListener> getProgressListeners() {
		return myProgressListeners;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setProgressListeners(java.util.List)
	 */
	@Override
	public void setProgressListeners(List<IProgressListener> lProgressListeners) {
		this.myProgressListeners = lProgressListeners;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setProgressType(org.grits.toolbox.widgets.progress.IProgressListener.ProgressType)
	 */
	@Override
	public void setProgressType(ProgressType progressType) {
		NotifyingProcessUtil.setProgressType(getProgressListeners(), progressType);

	}
}


