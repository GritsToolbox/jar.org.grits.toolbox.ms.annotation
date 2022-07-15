package org.grits.toolbox.ms.annotation.structure;

import org.grits.toolbox.ms.annotation.gelato.Analyte;

/**
 * GRITS databases store sequence information only, and a AnalyteStructure object is created for each. 
 * Upon reading a GRITS database, a function is called to create Analyte objects, which are used to fill out
 * missing sequence information for each structure. The structure is then stored in a hashmap cache.
 * Later, the analyte object is needed again or the database might be re-read, so it is helpful to do this once.
 * This object is used in the cache now so both Analyte and AnalyteStructure are available at the same time and are
 * determined only once.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GelatoAnalyte {
	private AnalyteStructure analyteStructure;
	private Analyte analyte;
	private int iId = 0;
	private String sId = "";
	
	public GelatoAnalyte() {
		// TODO Auto-generated constructor stub
	}
	
	public GelatoAnalyte( Analyte analyte, AnalyteStructure analyteStructure ) {
		this.analyte = analyte;
		this.analyteStructure = analyteStructure;
	}
		
	public void setId(int iId) {
		this.iId = iId;
	}
	public int getId() {
		return iId;
	}
	
	public void setStringId(String sId) {
		this.sId = sId;
	}
	public String getStringId() {
		return sId;
	}
		
	public void setAnalyte(Analyte analyte) {
		this.analyte = analyte;
	}
	public Analyte getAnalyte() {
		return analyte;
	}
	
	public void setAnalyteStructure(AnalyteStructure analyteStructure) {
		this.analyteStructure = analyteStructure;
	}
	public AnalyteStructure getAnalyteStructure() {
		return analyteStructure;
	}
}
