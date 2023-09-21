package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.GelatoUtils;
import org.grits.toolbox.ms.annotation.structure.AnalyteStructure;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.file.reader.IMSAnnotationFileReader;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

/**
 * @author Brent Weatherly
 * Extends GlycanStructureAnnotation for use with LC-MS/MS MS Types
 */
public class GlycanStructureAnnotationLCMSMS extends GlycanStructureAnnotation {
	private static final Logger logger = Logger.getLogger(GlycanStructureAnnotationLCMSMS.class);
	protected Data overviewData = null;
	Map<Integer, List<Integer>> scanSubScanMap = new HashMap<>();

	/**
	 * @param data - a org.grits.toolbox.ms.om.data.Data to be used during annotation
	 * @param path - String value of the path for the temporary, intermediate files created during annotation
	 * @param archiveName - String value of the destination path plus the archive name WITHOUT EXTENSION (determined later)
	 * @param msFile - String value for the full path to the mzXML/mzML file for this project
	 */
	public GlycanStructureAnnotationLCMSMS(Data data, String path, String archiveName, MSFile msFile) {
		super(data, path, archiveName, msFile);		
	}
	
	@Override
	public void initializeSubScanMap() {
		// initialize scan/subscan map
		if (msFile.getReader() instanceof IMSAnnotationFileReader) {
			scanSubScanMap = ((IMSAnnotationFileReader) msFile.getReader()).readMSFileForSubscans(msFile);
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getScans(java.lang.String, int, double, java.lang.String, double, java.lang.String)
	 */
	@Override
	protected HashMap<Integer, Scan> getScans(MSFile msFile, int _iScanNumber, double dFragCutoff, String sFragCutoffType, double dPreCutoff, String sPreCutoffType ) {
		if (msFile.getReader() instanceof IMSAnnotationFileReader) {
			List<Scan> scans = ((IMSAnnotationFileReader) msFile.getReader()).readMSFile(msFile, _iScanNumber, scanSubScanMap);
			return GelatoUtils.listToHashMap(scans, dFragCutoff, sFragCutoffType, dPreCutoff, sPreCutoffType);
		}
		return null;
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
			return scans;
		}
		return null;
	}

	/**
	 * @param data - a Data object.
	 */
	private void clearPeakLists( Data data ) {
		for( Scan scan : data.getScans().values() ) {
			if( isCanceled() )
				return;
			if( scan.getAnnotatedPeaks() != null ) {
				scan.getAnnotatedPeaks().clear();
			}
			scan.getPeaklist().clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#addAnnotation(org.grits.toolbox.ms.om.data.GlycanAnnotation)
	 */
	@Override
	protected void addAnnotation(GlycanAnnotation annotation) {
		super.addAnnotation(annotation);
		overviewData.getAnnotation().add(annotation);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#processAnalyteSettings()
	 */
	@Override
	protected boolean processAnalyteSettings() {
		try {
			boolean bRes = super.processAnalyteSettings();
			if( isCanceled() )
				return false;
			if( bRes ) {
				clearPeakLists( data );
				overviewData.getScans().putAll( data.getScans() );
				return true;
			}
		} catch (Exception e) {
			logger.error("Error in processAnalyteSettings", e);
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#processAnalyteSetting(org.grits.toolbox.ms.om.data.AnalyteSettings)
	 */
	@Override
	protected boolean processAnalyteSetting(AnalyteSettings _analyteSettings, List<AnalyteStructure> _structures) {
		try {
			boolean bRes = super.processAnalyteSetting(_analyteSettings, _structures);
			if( isCanceled() )
				return false;
			return bRes;
			//			if( bRes ) {
			//				clearPeakLists( data );
			//				overviewData.getScans().putAll( data.getScans() );
			//				return true;
			//			}
		} catch (Exception e) {
			logger.error("Error in processAnalyteSettings", e);
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#processScans()
	 */
	@Override
	public int processScans() {
		try {
			overviewData = new Data();
			overviewData.setDataHeader( this.dataHeader );		
			int iRes = super.processScans();
			if( isCanceled() )
				return iRes;
			if( iRes == GRITSProcessStatus.OK ) {
				AnnotationWriter writer = new AnnotationWriter();				
				writer.generateScansAnnotationFiles(this.m_tempFilePath, overviewData, getOverviewFileName(), true, true, true, false);
				return GRITSProcessStatus.OK;
			}
		} catch( Exception e ){
			logger.error("Error in processScans", e);
		}
		return GRITSProcessStatus.ERROR;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getFinalArchiveName()
	 */
	@Override
	public String getFinalArchiveName() {
		return  AnnotationWriter.getArchiveFilePath(this.m_preArchive + File.separator + iCurScanNum);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#needsOverview()
	 */
	@Override
	public boolean needsOverview() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getOverviewFileName()
	 */
	@Override
	public String getOverviewFileName() {
		return this.m_finalArchive;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getNewGlycanMatcher()
	 */
	@Override
	protected AnalyteMatcher getNewAnalyteMatcher(int iCurScan) {
		return new GlycanMatcher(iCurScan, this);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#cleanAfterCancel()
	 */
	@Override
	protected void cleanAfterCancel() {
		try {
			File fDir = new File( this.m_preArchive );
			for( File f : fDir.listFiles() ) {
				f.delete();
			}
			fDir.delete();
			File f = new File( getOverviewFileName() );
			f.delete();
		} catch( Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#setArchiveFilePaths()
	 */
	@Override
	protected void setArchiveFilePaths() {
		try {
			new File( this.m_preArchive ).mkdirs();
		} catch( Exception ex ) {
			logger.error("Error creating archive folder.", ex);
		}
		//		this.m_preArchive = _sPreArchiveFilePath;
		this.m_finalArchive = AnnotationWriter.getArchiveFilePath(this.m_preArchive);	
	}

}
