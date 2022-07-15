package org.grits.toolbox.ms.annotation.structure;

import java.util.ArrayList;
import java.util.List;

import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.eurocarbdb.application.glycanbuilder.Fragmenter;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.grits.toolbox.ms.annotation.gelato.glycan.GlycanStructureAnnotation;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Fragment;
import org.grits.toolbox.ms.om.data.FragmentPerActivationMethod;
import org.grits.toolbox.ms.om.data.FragmentPerMsLevel;

/**
 * Implementation of IAnalyteFragments for GlycanAnalyteFragments objects. 
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GlycanAnalyteFragments implements IAnalyteFragments{
	protected List<IAnalyteFragment> fragments = null;
	protected String sequence;
	protected String scanActivationMethod;
	protected int scanMSLevel;
	protected boolean isMonoisotopic;
	
	public GlycanAnalyteFragments() {
		fragments = new ArrayList<>();
	}	
	
	/**
	 * @param sequence
	 */
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	/**
	 * @return String representation of sequence
	 */
	public String getSequence() {
		return sequence;
	}
	
	/**
	 * @param scanActivationMethod, activation method of current MS scan
	 */
	public void setScanActivationMethod(String scanActivationMethod) {
		this.scanActivationMethod = scanActivationMethod;
	}
	/**
	 * @return String representation of activation of current MS scan
	 */
	public String getScanActivationMethod() {
		return scanActivationMethod;
	}
	
	/**
	 * @param scanMSLevel, MS level of current scan
	 */
	public void setScanMSLevel(int scanMSLevel) {
		this.scanMSLevel = scanMSLevel;
	}
	/**
	 * @return MS level of current scan
	 */
	public int getScanMSLevel() {
		return scanMSLevel;
	}
	
	/**
	 * @param isMonoisotopic, whether the m/z of the MS peaks are in monoisotopic (true) or average (false)
	 */
	public void setMonoisotopic(boolean isMonoisotopic) {
		this.isMonoisotopic = isMonoisotopic;
	}
	/**
	 * @return whether the m/z of the MS peaks are in monoisotopic (true) or average (false)
	 */
	public boolean isMonoisotopic() {
		return isMonoisotopic;
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.ms.annotation.structure.IAnalyteFragments#getAnalyteFragments(org.grits.toolbox.ms.om.data.AnalyteSettings)
	 */
	@Override
	public List<IAnalyteFragment> getAnalyteFragments(AnalyteSettings settings) {
		List<IAnalyteFragment> fragments = null;
		boolean match = false;
		//check the different fragment settings		
		if(settings.getGlycanSettings().getPerActivation().size() != 0){
			for(FragmentPerActivationMethod f : settings.getGlycanSettings().getPerActivation())
				if(f.getActivationMethod().equals(scanActivationMethod)){
					fragments = GlycanAnalyteFragments.generateFragments( 
							sequence, 
							settings,
							f.getFragments(),
							f.getMaxNumOfCleavages(),
							f.getMaxNumOfCrossRingCleavages(),
							isMonoisotopic);
					match = true;
					break;
				}
			if(!match){
				fragments = GlycanAnalyteFragments.generateFragments(
						sequence,
						settings,
						settings.getGlycanSettings().getGlycanFragments(),
						settings.getGlycanSettings().getMaxNumOfCleavages(),
						settings.getGlycanSettings().getMaxNumOfCrossRingCleavages(),
						isMonoisotopic);
			}

		}else{
			if(settings.getGlycanSettings().getPerMsLevel().size() != 0){
				for(FragmentPerMsLevel f : settings.getGlycanSettings().getPerMsLevel())
					if(f.getMsLevel() == scanMSLevel){
						fragments = GlycanAnalyteFragments.generateFragments(
								sequence,								
								settings,
								f.getFragments(),
								f.getM_maxNumOfCleavages(),
								f.getM_maxNumOfCrossRingCleavages(), 
								isMonoisotopic);
						match = true;
						break;
					}
				if(!match){
					fragments = GlycanAnalyteFragments.generateFragments(
							sequence,
							settings,
							settings.getGlycanSettings().getGlycanFragments(),
							settings.getGlycanSettings().getMaxNumOfCleavages(),
							settings.getGlycanSettings().getMaxNumOfCrossRingCleavages(),
							isMonoisotopic);
				}
			}else{
				fragments = GlycanAnalyteFragments.generateFragments(
						sequence,
						settings,
						settings.getGlycanSettings().getGlycanFragments(),
						settings.getGlycanSettings().getMaxNumOfCleavages(),
						settings.getGlycanSettings().getMaxNumOfCrossRingCleavages(),
						isMonoisotopic);
			}
		}
		return fragments;
	}
	
	/**
	 * @param dMz
	 * @return
	 */
	public static String getAutoString( double dMz) {
		int iIntMass = (int) (dMz * 100.0);
		return "Frag-" + iIntMass;
	}
		
	/**
	 * @param fragments
	 */
	public void setFragments(List<IAnalyteFragment> fragments) {
		this.fragments = fragments;
	}
	/**
	 * @return
	 */
	public List<IAnalyteFragment> getFragments() {
		return fragments;
	}
	
	/**
	 * @param sequence
	 * @param settings
	 * @param fragments
	 * @param maxNumClvg
	 * @param maxNumCr
	 * @param isMonoisotopic
	 * @return
	 */
	public static List<IAnalyteFragment> generateFragments(
			String sequence, AnalyteSettings settings, List<Fragment> fragments, int maxNumClvg, int maxNumCr, boolean isMonoisotopic ){
		BuilderWorkspace bw = new BuilderWorkspace(new GlycanRendererAWT());

		// allow all types of fragments
		Fragmenter t_fragmenter = new Fragmenter();
		//initialize all of the fragment types false to avoid the default value which is true
		t_fragmenter.setComputeBFragments(false);
		t_fragmenter.setComputeYFragments(false);
		t_fragmenter.setComputeCFragments(false);
		t_fragmenter.setComputeZFragments(false);
		t_fragmenter.setComputeAFragments(false);
		t_fragmenter.setComputeXFragments(false);

		for(Fragment fragment : fragments){
			if(fragment.getType().equals(Fragment.TYPE_B)){
				t_fragmenter.setComputeBFragments(true);
			}
			if(fragment.getType().equals(Fragment.TYPE_Y)){
				t_fragmenter.setComputeYFragments(true);
			}
			if(fragment.getType().equals(Fragment.TYPE_C)){
				t_fragmenter.setComputeCFragments(true);
			}
			if(fragment.getType().equals(Fragment.TYPE_Z)){

				t_fragmenter.setComputeZFragments(true);
			}
			if(fragment.getType().equals(Fragment.TYPE_A)){
				t_fragmenter.setComputeAFragments(true);
			} 
			if(fragment.getType().equals(Fragment.TYPE_X)){
				t_fragmenter.setComputeXFragments(true);
			}   
		}       

		// set the number of allowed fragments
		t_fragmenter.setMaxNoCleavages(maxNumClvg);
		t_fragmenter.setMaxNoCrossRings(maxNumCr);
		try{
			// load the glycan and 
			Glycan t_glycan = Glycan.fromString(sequence);
			t_glycan.setMassOptions(GlycanStructureAnnotation.collectMassOptions(isMonoisotopic, settings));
			FragmentCollection fc = t_fragmenter.computeAllFragments(t_glycan);
			List<IAnalyteFragment> lFrags = GlycanAnalyteFragment.convertGWBFragments(fc);
			return lFrags;
		}catch(Exception e){
			e.printStackTrace();  
			return null;
		}
	}
	
	/**
	 * @param sequence
	 * @return
	 */
	public static String getComparableSequence(String sequence) {
		if( sequence == null ) {
			return sequence;
		}
		ArrayList<Integer> alDashIndices = new ArrayList<>();
		int iFirstInx = 0;
		while( iFirstInx > -1 ) {
			alDashIndices.add(iFirstInx);
			iFirstInx = sequence.indexOf("--", (iFirstInx+1));
		}
		alDashIndices.add( sequence.length() );
		
		ArrayList<Integer> alCleaveIndices = new ArrayList<>();
		iFirstInx = sequence.indexOf("cleavage");
		while( iFirstInx > -1 ) {
			alCleaveIndices.add(iFirstInx);
			iFirstInx = sequence.indexOf("cleavage", (iFirstInx+1));
		}
		if( alCleaveIndices.isEmpty() ) {
			return sequence;
		}
		
//		String sSwapText = "?1D-Hex,p/#";
		String sSwapText1 = "?1D";
		String sSwapText2 = "-Hex,p/#";
		String sCompSeq = "";
		int iCurDash = 1;
		int iLastInx = 0;
		
		for( Integer iCleaveInx : alCleaveIndices ) {
			
			for( int i = iCurDash; i < alDashIndices.size(); i++ ) {
				if( alDashIndices.get(i) > iCleaveInx ) {
					
					int iStartInx = alDashIndices.get(i-1);
					if( iLastInx != iStartInx ) {
						sCompSeq += sequence.substring(iLastInx, iStartInx);
					}
//					if( iStartInx > 0 ) {
//						sCompSeq += "--?";
//					} 
//					sCompSeq += sSwapText;
					
					int iEndInx = sequence.indexOf("-", (iStartInx+2));
					if( iStartInx == 0 || i == (alDashIndices.size()-1) ) { // terminal cleavage
						if( iStartInx > 0 ) {
							sCompSeq += "--";
						} 
						sCompSeq += sSwapText1;
					} else {
						String sSeq = sequence.substring(iStartInx, iEndInx);
						if( ! sSeq.startsWith("--") ) {
							sCompSeq += "--";
						}
						sCompSeq += sequence.substring(iStartInx, iEndInx);						
					}
					sCompSeq += sSwapText2;
					String sCleavage = sequence.substring(iCleaveInx-1, iCleaveInx+8);
					sCompSeq += sCleavage;
					
					iLastInx = iCleaveInx+8;	
					iCurDash = i;
					break;
				}
			}
		}
		if( iLastInx > 0 ){
			sCompSeq += sequence.substring(iLastInx);
		}
		return sCompSeq;
	}	
}
