package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.GelatoUtils;
import org.grits.toolbox.ms.annotation.structure.AnalyteStructure;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.file.reader.IMSAnnotationFileReader;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;

/**
 * @author Brent Weatherly
 * Extends GlycanStructureAnnotation for use with MS Profile MS Types
 */
public class GlycanStructureAnnotationMSProfile extends GlycanStructureAnnotation {
	private static final Logger logger = Logger.getLogger(GlycanStructureAnnotationMSProfile.class);
	protected HashMap<Integer, Scan> hmPeakIdToScan;
	protected int iPhonySubScanCounter;

	/**
	 * @param data - a org.grits.toolbox.ms.om.data.Data to be used during annotation
	 * @param path - String value of the path for the temporary, intermediate files created during annotation
	 * @param archiveName - String value of the destination path plus the archive name WITHOUT EXTENSION (determined later)
	 * @param msFile - MS file to be processed
	 */
	public GlycanStructureAnnotationMSProfile(Data data, String path, String archiveName, MSFile msFile) {
		super(data, path, archiveName, msFile);
	}
	
	/**
	 * @param iFirstMS1Scan - int value of first scan number
	 */
	public void init( int iFirstMS1Scan ) {
		iPhonySubScanCounter = iFirstMS1Scan + 1;
		hmPeakIdToScan = new HashMap<>();
	}

	/**
	 * @param iPhonySubScanCounter - an int
	 */
	public void setPhonySubScanCounter(int iPhonySubScanCounter) {
		this.iPhonySubScanCounter = iPhonySubScanCounter;
	}

	/**
	 * @return
	 */
	public int getPhonySubScanCounter() {
		return iPhonySubScanCounter;
	}

	/**
	 * Description: MS Profile data has no sub-scans, but if we match a structure to a peak, create a "phony" sub-scan 
	 * so the scan organization is valid for GRITS.
	 * 
	 * @param parentPeak - the peak in the MS1 scan to treat as the "precursor"
	 * @param parentScan - the parent MS1 scan
	 * @return Scan - a Scan object
	 */
	protected Scan getPhonySubScan(Peak parentPeak, Scan parentScan) {
		Scan subScan = null;
		try {
			if( ! hmPeakIdToScan.containsKey(parentPeak.getId()) ) {
				int iSubScanNum = getPhonySubScanCounter();
				if( parentScan.getSubScans() != null && ! parentScan.getSubScans().isEmpty() ) {
					iSubScanNum = parentScan.getSubScans().get( parentScan.getSubScans().size() - 1 ) + 1;
				} else {
					setPhonySubScanCounter(iSubScanNum+1);					
				}
				subScan = new Scan();
				subScan.setScanNo(iSubScanNum);
				subScan.setPrecursor(parentPeak);
				subScan.setRetentionTime(parentScan.getRetentionTime());
				parentPeak.setIsPrecursor(true);
				subScan.setParentScan(parentScan.getScanNo());
				subScan.setMsLevel(parentScan.getMsLevel() + 1);
				if( ! data.getScans().containsKey( iSubScanNum ) ) {
					parentScan.getSubScans().add(iSubScanNum);
					data.getScans().put(iSubScanNum, subScan);
				} else {
					throw new Exception("Subscan already added (" + iSubScanNum + ")");
				}
				hmPeakIdToScan.put(parentPeak.getId(), subScan);
			} else {
				subScan = hmPeakIdToScan.get(parentPeak.getId());
//				logger.debug("Found prev scan for peak: " + parentPeak.getId() + ", scan: " + subScan.getScanNo());
			}		
			return subScan;
		}
		catch( Exception ex ) {
			logger.error("Error getting Phony SubScan.", ex);			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getScans(java.lang.String, int, double, java.lang.String, double, java.lang.String)
	 */
	@Override
	protected HashMap<Integer, Scan> getScans(MSFile msFile, int _iScanNumber, double dFragCutoff, String sFragCutoffType, double dPreCutoff, String sPreCutoffType ) {
		if (msFile.getReader() instanceof IMSAnnotationFileReader) {
			List<Scan> scans = ((IMSAnnotationFileReader) msFile.getReader()).readMSFile(msFile, _iScanNumber);
			return GelatoUtils.listToHashMap(scans, dFragCutoff, sFragCutoffType, dPreCutoff, sPreCutoffType);
		} 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#processScan(org.grits.toolbox.ms.om.data.Method, int)
	 */
	@Override
	protected boolean processScan(Method _method, int iInx) {
		int iCurScanNum = scansToProcess.get(iInx);
		init( iCurScanNum );
		return super.processScan(_method, iInx);
	}


	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#determineScanBounds()
	 */
	@Override
	public List<Integer> determineScanBounds() {
		if (msFile.getReader() instanceof IMSAnnotationFileReader) {
			List<Integer> scans = ((IMSAnnotationFileReader) msFile.getReader()).getScanList(msFile, -1);		
			if( scans == null || scans.isEmpty() ) {
				return null;
			}
			// mostly for testing, but this makes sure we only return the first MS1 scan
			List<Integer> firstScan = new ArrayList<>();
			firstScan.add(scans.get(0));
			return firstScan;
		} 
		return null;
	}

	/**
	 * Creates a peak list for the "iCurScanNum" w/ only precursor peaks.
	 */
	protected void setMatchedPeakList() {
		Scan curScan = data.getScans().get(iCurScanNum);
		List<Peak> newPeakList = new ArrayList<>();
		for( int i = 0; i < curScan.getPeaklist().size(); i++ ) {
			if( isCanceled() )
				return;
			Peak peak = curScan.getPeaklist().get(i);
			if( peak.getIsPrecursor() ) {
				newPeakList.add(peak);
			}
		}
		curScan.setPeaklist(newPeakList);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#writeScanFeaturesToArchive()
	 */
	@Override
	protected boolean writeScanFeaturesToArchive() {
		setMatchedPeakList();
		return super.writeScanFeaturesToArchive();
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#processAnalyteSetting(org.grits.toolbox.ms.om.data.AnalyteSettings)
	 */
	@Override
	protected boolean processAnalyteSetting(AnalyteSettings _analyteSettings, List<AnalyteStructure> _structures) {
		try {
			boolean bRes = processStructures(_analyteSettings, _structures);
			if( isCanceled() )
				return false;
			return bRes;
		} catch( Exception e ){
			logger.error("Error in processAnalyteSettings", e);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getFinalArchiveName()
	 */
	@Override
	public String getFinalArchiveName() {
		return this.m_finalArchive;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#needsOverview()
	 */
	@Override
	public boolean needsOverview() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getOverviewFileName()
	 */
	@Override
	public String getOverviewFileName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getNewGlycanMatcher()
	 */
	@Override
	protected AnalyteMatcher getNewAnalyteMatcher(int iCurScan) {
		return new GlycanMatcherMSProfile(iCurScan, this);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#cleanAfterCancel()
	 */
	@Override
	protected void cleanAfterCancel() {
		try {
			File file = new File( getFinalArchiveName() );
			file.delete();
		} catch( Exception e) {
			logger.error(e.getMessage(), e);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#setArchiveFilePaths()
	 */
	@Override
	protected void setArchiveFilePaths() {
		this.m_finalArchive = AnnotationWriter.getArchiveFilePath(this.m_preArchive);	
	}

}
