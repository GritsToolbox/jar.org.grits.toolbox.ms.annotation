package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.GelatoUtils;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.file.reader.IMSAnnotationFileReader;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;

/**
 * @author Brent Weatherly
 * Extends GlycanStructureAnnotation for use with TIM MS Types
 */
public class GlycanStructureAnnotationTIM extends GlycanStructureAnnotation {
	private static final Logger logger = Logger.getLogger(GlycanStructureAnnotationTIM.class);
	
	/**
	 * @param data - a org.grits.toolbox.ms.om.data.Data to be used during annotation
	 * @param gf - a org.grits.toolbox.ms.om.data.GlycanFilter that determines the type of Glycans (O, N) etc
	 * @param path - String value of the path for the temporary, intermediate files created during annotation
	 * @param archiveName - String value of the destination path plus the archive name WITHOUT EXTENSION (determined later)
	 * @param msFile - MS File to be processed
	 */
	public GlycanStructureAnnotationTIM(Data data, String path, String archiveName, MSFile msFile) {
		super(data, path, archiveName, msFile); 
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#getScans(java.lang.String, int, double, java.lang.String, double, java.lang.String)
	 */
	@Override
	protected HashMap<Integer, Scan> getScans(MSFile msFile, int _iScanNumber, double dFragCutoff, String sFragCutoffType, double dPreCutoff, String sPreCutoffType) {
		// scan number ignored
		if (msFile.getReader() instanceof IMSAnnotationFileReader) {
			List<Scan> scans = ((IMSAnnotationFileReader) msFile.getReader()).readMSFile(msFile);
			return GelatoUtils.listToHashMap(scans, dFragCutoff, sFragCutoffType, dPreCutoff, sPreCutoffType);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation#determineScanBounds()
	 */
	@Override
	public List<Integer> determineScanBounds() {
		List<Integer> l = new ArrayList<>();
		l.add(0);
		return l;
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
		return new GlycanMatcherTIM(iCurScan, this);
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
