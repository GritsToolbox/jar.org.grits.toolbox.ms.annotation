package org.grits.toolbox.ms.annotation.gelato;

/**
 * Abstract class to represent a particular Analyte.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * 
 */
public abstract class Analyte {
	// Assuming that the Analyte has a text-based representation (like a sequence)
	protected String stringRepresentation = null;
	
	public Analyte(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}
	
	/**
	 * Every analyte must be able to calculate it's mass so this can be matched to a peak m/z
	 * 
	 * @return double value of Analyte m/z
	 */
	public abstract double computeMass();
	
	/**
	 * @return the String representation of the Analyte
	 */
	public String getAnalyteStringRepresentation() {
		return stringRepresentation;
	}
	
	/**
	 * @param stringRepresentation
	 */
	public void setAnalyteStringRepresentation(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}
}
