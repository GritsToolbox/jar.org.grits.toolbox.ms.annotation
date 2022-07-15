package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.GelatoUtils;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.file.reader.IMSAnnotationFileReader;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;

/**
 * @author Brent Weatherly
 * Extends GlycanStructureAnnotation for use with Direct Infusion MS Types
 */
public class GlycanStructureAnnotationDirectInfusion extends GlycanStructureAnnotation {
	private static final Logger logger = Logger.getLogger(GlycanStructureAnnotationDirectInfusion.class);
	private int iFirstMS1Scan = -1;

	/**
	 * @param data - a org.grits.toolbox.ms.om.data.Data to be used during annotation
	 * @param path - String value of the path for the temporary, intermediate files created during annotation
	 * @param archiveName - String value of the destination path plus the archive name WITHOUT EXTENSION (determined later)
	 * @param msFile - String value for the full path to the mzXML/mzML file for this project
	 */
	public GlycanStructureAnnotationDirectInfusion(Data data, String path, String archiveName, MSFile msFile) {
		super(data, path, archiveName, msFile); 
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#determineScanBounds()
	 */
	@Override
	public List<Integer> determineScanBounds() {
		int iFirstMS1Scan = getFirstMS1Scan();
		List<Integer> firstScan = new ArrayList<>();
		firstScan.add(iFirstMS1Scan);
		return firstScan;
	}

	/**
	 * @return int - scan number of first MS scan. -1 if not found
	 */
	private int getFirstMS1Scan() {
		if( this.iFirstMS1Scan > 0 ) {
			return this.iFirstMS1Scan;
		}
		if (msFile.getReader() instanceof IMSAnnotationFileReader) {
			IMSAnnotationFileReader reader = (IMSAnnotationFileReader) msFile.getReader();
			List<Integer> scans = reader.getScanList(msFile, -1);
			if( scans == null || scans.isEmpty() ) {
				return -1;
			}
			this.iFirstMS1Scan = scans.get(0);
			return getFirstMS1Scan();
		}
		
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#populateScanFeatureData(java.lang.String)
	 */
	@Override
	protected void populateScanFeatureData(String glycanFilesPath) {
		super.populateScanFeatureData(glycanFilesPath);

		int iFirstMS1Scan = getFirstMS1Scan();
		Scan firstMS1Scan = data.getScans().get(iFirstMS1Scan);
		ScanFeatures scan1Features = new ScanFeatures();
		scan1Features.setScanId(iFirstMS1Scan);
		scan1Features.setScanPeaks(new HashSet<Peak>(firstMS1Scan.getPeaklist()));
		scan1Features.setUsesComplexRowId(true);
		for(Integer scanId : data.getScanFeatures().keySet()) {
			if( isCanceled() )
				return;
			Scan scan = data.getScans().get(scanId);
			if(scan.getMsLevel() == 1) {
				ScanFeatures sFeatures = data.getScanFeatures().get(scanId);
				for( Feature f : sFeatures.getFeatures() ) {
					if( ! scan1Features.getFeatures().contains(f) ) {
						scan1Features.getFeatures().add(f);
					}
				}
			}
		}

		for(Integer scanId : data.getScans().keySet()) {	
			if( isCanceled() )
				return;
			Scan scan = data.getScans().get(scanId);
			scan.setNumAnnotatedPeaks( firstMS1Scan.getNumAnnotatedPeaks() );
			if(data.getScans().get(scanId).getMsLevel() == 1) {
				data.getScanFeatures().put(scanId, scan1Features);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getScans(java.lang.String, int, double, java.lang.String, double, java.lang.String)
	 */
	@Override
	protected HashMap<Integer, Scan> getScans(MSFile msFile, int _iScanNumber, double dFragCutoff, String sFragCutoffType, double dPreCutoff, String sPreCutoffType) {
		// scan number ignored
		//List<Scan> scans = reader.readMzXmlFileForDirectInfusion(_sMzXMLFile);
		if (msFile.getReader() instanceof IMSAnnotationFileReader) {
			List<Scan> scans = ((IMSAnnotationFileReader) msFile.getReader()).readMSFile(msFile);
			return GelatoUtils.listToHashMap(scans, dFragCutoff, sFragCutoffType, dPreCutoff, sPreCutoffType);
		}
		return null;
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
		return new GlycanMatcherDirectInfusion(iCurScan, this);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#cleanAfterCancel()
	 */
	@Override
	protected void cleanAfterCancel() {
		try {
			File file = new File( this.m_finalArchive );
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
