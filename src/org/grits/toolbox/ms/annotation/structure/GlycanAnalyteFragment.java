package org.grits.toolbox.ms.annotation.structure;

import java.util.ArrayList;
import java.util.List;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.eurocarbdb.application.glycanbuilder.FragmentEntry;

/**
 * Implementation of IAnalyteFragment for GlycanAnalyteFragment objects. 
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see FragmentEntry
 */
public class GlycanAnalyteFragment implements IAnalyteFragment {
	protected FragmentEntry gwbFragment = null;

	public GlycanAnalyteFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getType() {
		return gwbFragment.getName();
	}
	
	public long getId() {
		if( getFragmentEntry() == null ){
			return 0;
		}
		return (long) (getFragmentEntry().getMZ() * 1000);
	}
	
	public FragmentEntry getFragmentEntry() {
		return gwbFragment;
	}
	
	public void setGWBFragment(FragmentEntry gwbFragment) {
		this.gwbFragment = gwbFragment;		
	}
	
	public static List<IAnalyteFragment> convertGWBFragments( FragmentCollection fc ) {
		List<IAnalyteFragment> lFrags = new ArrayList<>();
		for( FragmentEntry fe : fc.getFragments() ) {
			GlycanAnalyteFragment aFrag = new GlycanAnalyteFragment();
			aFrag.setGWBFragment(fe);
			lFrags.add(aFrag);
		}
		return lFrags;
	}
	
}
