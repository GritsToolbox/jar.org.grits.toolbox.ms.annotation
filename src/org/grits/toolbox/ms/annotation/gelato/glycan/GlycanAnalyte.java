package org.grits.toolbox.ms.annotation.gelato.glycan;

import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.ms.annotation.gelato.Analyte;

/**
 * Implementation of Analyte for Glycan objects. 
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see Glycan
 */
public class GlycanAnalyte extends Analyte {
	protected Glycan glycan;
	
	public GlycanAnalyte(String analyteSequence, Glycan glycan) {
		super(analyteSequence);
		this.glycan = glycan;
	}
	
	@Override
	public double computeMass() {
		return glycan.computeMass();
	}

	public Glycan getGlycan() {
		return glycan;
	}
	public void setGlycan(Glycan glycan) {
		this.glycan = glycan;
	}
}
