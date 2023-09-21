package org.grits.toolbox.ms.annotation.gelato.glycan;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.eurocarbdb.application.glycanbuilder.MassOptions;
import org.eurocarbdb.application.glycanbuilder.ResidueDictionary;
import org.eurocarbdb.application.glycanbuilder.ResidueType;
import org.grits.toolbox.ms.annotation.gelato.Analyte;
import org.grits.toolbox.ms.annotation.gelato.AnalyteMatcher;
import org.grits.toolbox.ms.annotation.gelato.AnalyteStructureAnnotation;
import org.grits.toolbox.ms.annotation.gelato.GelatoUtils;
import org.grits.toolbox.ms.annotation.structure.AnalyteStructure;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyte;
import org.grits.toolbox.ms.annotation.structure.GelatoAnalyteCache;
import org.grits.toolbox.ms.annotation.structure.GlycanPreDefinedOptions;
import org.grits.toolbox.ms.annotation.structure.GlycanStructure;
import org.grits.toolbox.ms.annotation.structure.StructureHandlerException;
import org.grits.toolbox.ms.annotation.structure.StructureHandlerJarFile;
import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.GlycanFilter;
import org.grits.toolbox.ms.om.data.GlycanScansAnnotation;
import org.grits.toolbox.ms.om.data.GlycanSettings;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.ReducingEnd;
import org.grits.toolbox.ms.om.data.ScansAnnotation;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;
import org.grits.toolbox.util.structure.glycan.filter.GlycanFilterOperator;
import org.grits.toolbox.widgets.tools.INotifyingProcess;

/**
 * Extends AnalyteStructureAnnotation for use with Glycan annotation
 * 
 * @author D Brent Weatherly - dbrentw@uga.edu
 */
public abstract class GlycanStructureAnnotation extends AnalyteStructureAnnotation implements INotifyingProcess {
	private static final Logger logger = Logger.getLogger(GlycanStructureAnnotation.class);
	private GlycanMatcher curGlycanMatcher = null;
	//	protected List<List<GlycanStructure>> structures = null;

	public GlycanStructureAnnotation() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param data
	 * 		data to be used during annotation
	 * @param path
	 * 		the path for the temporary, intermediate files created during annotation
	 * @param archiveName
	 * 		the destination path plus the archive name WITHOUT EXTENSION (determined later)
	 * @param filterSetting
	 * 		options that might filter our results, can be null
	 * @param msFile
	 * 		the full path to the mzXML/mzML file for this project
	 * @throws StructureHandlerException
	 * @see BuilderWorkspace
	 * @see StructureHandlerJarFile
	 */
	public GlycanStructureAnnotation( Data data, String path, String archiveName, MSFile msFile) {
		super(data, path, archiveName, msFile);
		BuilderWorkspace bw = new BuilderWorkspace(new GlycanRendererAWT());
	}

	protected List<AnalyteStructure> loadAnalyteSettingsFromDB(AnalyteSettings settings) {
		List<AnalyteStructure> settingStructures = GlycanStructureAnnotation.getGlycanStructures(settings.getGlycanSettings().getFilter());	
		return settingStructures;
	}

	/**
	 * Uses a StructureHandler to generate the lists of GlycanStructures associated with the specified GlycanFilter.
	 * 
	 * @param gf, a GlycanFilter object
	 * @return a list of GlycanStructures
	 */
	public static List<AnalyteStructure> getGlycanStructures( GlycanFilter gf ) {
		List<AnalyteStructure> structures = null;
		try {
			StructureHandlerJarFile fj = new StructureHandlerJarFile();
			structures = fj.getStructures(gf);
		} catch (StructureHandlerException e) {
			logger.error(e.getMessage(), e);
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);			
		}
		return structures;
	}

	/**
	 * Applies the filter specified in this.filterSetting to the Glycan object for the specified structure.
	 * Called from "applyFilters()"
	 * 
	 * @param structure - The org.grits.toolbox.ms.annotation.structure.GlycanStructure under consideration
	 * @param analyteSettings - The current org.grits.toolbox.ms.om.data.AnalyteSettings object
	 * @return boolean - pass/fail
	 */
	protected boolean passesFilters(AnalyteStructure structure, AnalyteSettings analyteSettings) {
		try {
			Analyte analyte = GelatoUtils.getAnalyteFromGelatoCache(structure);
			if (analyteSettings.getGlycanSettings().getFilterSetting() != null) {
				return GlycanFilterOperator.evaluate(((GlycanAnalyte) analyte).getGlycan().toSugar(), 
						analyteSettings.getGlycanSettings().getFilterSetting().getFilter());
			} 
		} catch (Exception e) {
			logger.error("Exception evaluating filters.", e);
		}
		return true;
	}

	protected Annotation getNewAnnotationObject() {
		return new GlycanAnnotation();
	}

	protected Annotation getNewGlycanAnnotation( String sAnnotId, String sSequence, String sSeqFormat, 
			String sGWBSequence, String sPerDerivType, String glytoucanId, ReducingEnd sRedEnd ) {
		GlycanAnnotation annotation = (GlycanAnnotation) super.getNewAnnotation(sAnnotId, sSequence, sSeqFormat);
		annotation.setSequenceGWB(sGWBSequence);
		annotation.setPerDerivatisationType(sPerDerivType);
		annotation.setReducingEnd(sRedEnd);
		annotation.setGlytoucanId(glytoucanId);
		return annotation;
	}

	/**
	 * Annotation Step 6.<br>
	 * Calls "processAllStructureOptions(..)" to analyze the sets of adducts, loss/gain, exchanges for the current structure and scan (this.iCurScanNum).
	 * If there are matches, call addAnnotation(annotation) and then write out the temporary glycan-centric XML files for later use.
	 *
	 * @param analyteSettings - the current AnalyteSettings object to use for GELATO annotation of the MS file
	 * @param structure - The org.grits.toolbox.ms.annotation.structure.GlycanStructure under consideration
	 * @return - pass/fail
	 */
	protected boolean processStructure( AnalyteSettings analyteSettings, GelatoAnalyte gelatoAnalyte) {
		Annotation annotation = null;
		ScansAnnotation glycanScansAnnotation = null;
		//		double plainMass = 0.0;
		try {
			Method method = getData().getDataHeader().getMethod();
			GlycanStructure glycanStructure = (GlycanStructure) gelatoAnalyte.getAnalyteStructure();

			annotation = getNewGlycanAnnotation(
					glycanStructure.getId(),
					glycanStructure.getSequence(),
					glycanStructure.getSequenceFormat(),
					glycanStructure.getGWBSequence().substring(0,glycanStructure.getGWBSequence().indexOf("$")),
					analyteSettings.getGlycanSettings().getPerDerivatisationType(),
					glycanStructure.getGlytoucanId(),
					analyteSettings.getGlycanSettings().getReducingEnd());

			glycanScansAnnotation = new GlycanScansAnnotation();
			glycanScansAnnotation.setAnnotationId(GlycanStructureAnnotation.iAnnotationIDCount);
			glycanScansAnnotation.setStringAnnotationId(glycanStructure.getId());
			((GlycanAnnotation) annotation).setGlycanId(glycanStructure.getId());
//			analyteIDs.add(glycanStructure.getId());
			annotation.setId(GlycanStructureAnnotation.iAnnotationIDCount);
			GlycanStructureAnnotation.iAnnotationIDCount++;
			int currentFeatureIndex = data.getFeatureIndex();//to be used later to know if the structure used to annotate any peak

			//			Glycan glycan = GlycanStructureAnnotation.getGlycan(gelatoGlycan.getGlycanStructure());
			Glycan glycan = ((GlycanAnalyte) gelatoAnalyte.getAnalyte()).getGlycan();

			//			GlycanExtraInfo.populateGlycanExtraInformation(annotation, glycan.toSugar());
			GlycanStructureAnnotation.populateExtraInfo((GlycanAnnotation)annotation, glycan);
			//, method.getMonoisotopic(), _analyteSettings
			GlycanStructureAnnotation.setGlycanMassOptions(glycan, method.getMonoisotopic(), analyteSettings);
			boolean bRes = false;

			// the first option is used if the user wants to use only the Reducing End and Derivitization settings 
			// for the glycans in the database
			if( analyteSettings.getGlycanSettings().getFilter().getUseDatabaseStructureMetaInfo() ) {
				AnalyteSettings customAnalyteSettings = new AnalyteSettings();
				customAnalyteSettings.setPeptideSettings(analyteSettings.getPeptideSettings());
				GlycanSettings gSettings = new GlycanSettings();
				gSettings.setAllowInnerFragments( analyteSettings.getGlycanSettings().getAllowInnerFragments() );
				gSettings.setFilter( analyteSettings.getGlycanSettings().getFilter() );
				gSettings.setGlycanFragments( analyteSettings.getGlycanSettings().getGlycanFragments() );
				gSettings.setMaxNumOfCleavages( analyteSettings.getGlycanSettings().getMaxNumOfCleavages() );
				gSettings.setMaxNumOfCrossRingCleavages( analyteSettings.getGlycanSettings().getMaxNumOfCrossRingCleavages() );
				gSettings.setPerActivation( analyteSettings.getGlycanSettings().getPerActivation() );
				gSettings.setPerMsLevel( analyteSettings.getGlycanSettings().getPerMsLevel() );
				gSettings.setPerDerivatisationType( getGelatoPerDerivatisationType(glycan.getMassOptions().getDerivatization()) );
				gSettings.setReducingEnd( getGelatoReducingEnd(glycan.getMassOptions().getReducingEndTypeString()) );
				customAnalyteSettings.setGlycanSettings(gSettings);
				bRes = processAllStructureOptions(
						this.lPosModeSettingsToAnalyze, this.lPosModeSettingsToAnalyzeCounts,
						this.lNegModeSettingsToAnalyze, this.lNegModeSettingsToAnalyzeCounts, 
						this.lExchangesoAnalyze, this.lExchangesToAnalyzeCounts, 
						this.lNeutralLossesToAnalyze, this.lNeutralLossesToAnalyzeCounts, 
						glycanScansAnnotation,
						customAnalyteSettings, gelatoAnalyte, 
						annotation);

			} else {
				bRes = processAllStructureOptions(
						this.lPosModeSettingsToAnalyze, this.lPosModeSettingsToAnalyzeCounts,
						this.lNegModeSettingsToAnalyze, this.lNegModeSettingsToAnalyzeCounts, 
						this.lExchangesoAnalyze, this.lExchangesToAnalyzeCounts, 
						this.lNeutralLossesToAnalyze, this.lNeutralLossesToAnalyzeCounts, 
						glycanScansAnnotation,
						analyteSettings, gelatoAnalyte, 
						annotation);

			}
			if( bRes ) {
				//				if(currentFeatureIndex != data.getFeatureIndex()) {//means there is new annotations added using the given glycan structure
				//					addAnnotation(annotation);
				//				}
				if( ! glycanScansAnnotation.getScanAnnotations().keySet().isEmpty() ) {
					AnnotationWriter writer = new AnnotationWriter();
					writer.writeAnnotationsPerAnalyte(glycanScansAnnotation,this.m_tempFilePath);
				}
				return true;
			}

		} catch( Exception e ) {
			logger.error("Error in processScans", e);
		}
		return false;
	}


	@Override
	protected AnalyteMatcher getNewAnalyteMatcher(int iCurScan) {
		return new GlycanMatcher(iCurScan, this);
	}

	@Override
	protected ScansAnnotation readScansAnnotation( AnnotationReader reader, String filePath, String annId ) {
		ScansAnnotation scansAnnotation = reader.readAnnotation(filePath, annId, GlycanScansAnnotation.class);
		return scansAnnotation;
	}

	/**
	 * Sometimes the GlycoCT sequence has errors. We don't have a great solution, so this is a hack to make it work...
	 * 
	 * @param _sStructureSequence, the sequence to be formatted
	 * @return the corrected sequence
	 */
	protected String correctSequence( String _sStructureSequence ) {
		final Pattern p1 = Pattern.compile("(\\()(\\-1)(\\+)");
		final Pattern p2 = Pattern.compile("(\\+)(\\-1)(\\))");
		Matcher m1 = p1.matcher(_sStructureSequence);
		if( m1.find() ) {
			StringBuffer sb = new StringBuffer(_sStructureSequence.length());
			m1.reset();
			while( m1.find() ) {
				String match1 = m1.group(1);
				String match2 = m1.group(2);
				String match3 = m1.group(3);
				match2 = match2.replace("-1", "1");
				String sNew = match1 + Matcher.quoteReplacement(match2) + match3;
				m1.appendReplacement(sb, sNew);
			}
			m1.appendTail(sb);
			return sb.toString().trim();
		} 
		Matcher m2 = p2.matcher(_sStructureSequence);
		if( m2.find() ) {
			StringBuffer sb = new StringBuffer(_sStructureSequence.length());
			m2.reset();
			while( m2.find() ) {
				String match1 = m2.group(1);
				String match2 = m2.group(2);
				String match3 = m2.group(3);
				match2 = match2.replace("-1", "1");
				String sNew = match1 + Matcher.quoteReplacement(match2) + match3;
				m2.appendReplacement(sb, sNew);
			}
			m2.appendTail(sb);
			return sb.toString().trim();

		}
		return _sStructureSequence;

	}

	@Override
	protected GelatoAnalyte getNewGelatoAnalyteObject(AnalyteStructure structure) {
		Glycan glycan = null;
		GelatoAnalyte gelatoGlycan = null;
		Sugar t_sugar = null;
		if( GelatoAnalyteCache.hmGelatoAnalytesByStructureId.containsKey(structure.getId()) ) {
			return null;
		} else {
			gelatoGlycan = new GelatoAnalyte();						
		}				
		try {
			GlycanStructure glycanStructure = (GlycanStructure) structure;
			if( glycanStructure.getGWBSequence() != null && ! glycanStructure.getGWBSequence().equals("")) {
				glycan = Glycan.fromString(glycanStructure.getGWBSequence());			
				if( structure.getSequenceFormat() == null || ! structure.getSequenceFormat().equals(GlycanAnnotation.SEQ_FORMAT_GLYCOCT_CONDENSED) ) {
					structure.setSequence( glycan.toGlycoCTCondensed() );
				}
			} else if( structure.getSequence() != null && ! structure.getSequence().equals("") ){
				if( structure.getSequenceFormat() == null || structure.getSequenceFormat().equals(GlycanAnnotation.SEQ_FORMAT_GLYDEII) ) {
					t_sugar = GlycanExtraInfo.glydeToSugar(structure.getSequence());							
					String t_glycoCT = GlycanExtraInfo.sugarToGlycoCT(t_sugar);
					glycan = Glycan.fromGlycoCTCondensed(t_glycoCT);
					structure.setSequence(glycan.toGlycoCTCondensed());
				} else if( structure.getSequenceFormat().equals(GlycanAnnotation.SEQ_FORMAT_GLYCOCT_CONDENSED) )  {
					glycan = Glycan.fromGlycoCTCondensed(structure.getSequence() );
				} else if( structure.getSequenceFormat().equals(GlycanAnnotation.SEQ_FORMAT_GLYCOCT_XML) )  {
					glycan = Glycan.fromGlycoCT(structure.getSequence() );
					structure.setSequence(glycan.toGlycoCTCondensed());
				} else {
					throw new Exception("Unsupported sequence type (" + structure.getSequenceFormat() + ") for structure: " + structure.getId());
				}
			} else {
				throw new Exception("Sequence not specfied for structure: " + structure.getId());
			}	
			String sCorrectedSeq = correctSequence(structure.getSequence());
			while( ! sCorrectedSeq.equals(structure.getSequence()) ) {
				structure.setSequence(sCorrectedSeq);
				sCorrectedSeq = correctSequence(sCorrectedSeq);
			}
			structure.setSequenceFormat(GlycanAnnotation.SEQ_FORMAT_GLYCOCT_CONDENSED);
			if( glycan != null && glycanStructure.getGWBSequence() == null ) {
				glycanStructure.setGWBSequence( glycan.toString() );
				String sOrdered = glycan.toStringOrdered(false);

			}
			GlycanAnalyte glycanAnalyte = new GlycanAnalyte(glycanStructure.getGWBSequence(), glycan);
			gelatoGlycan.setAnalyte(glycanAnalyte);
			gelatoGlycan.setAnalyteStructure(structure);	

		} catch (GlycoVisitorException e) {
			logger.error(e.getMessage());
		} catch (SugarImporterException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return gelatoGlycan;
	}


	/**
	 * @param glycan - a Glycan object.
	 * @return double - the computed mass of the Glycan
	 */
	public static double calculateMass(Glycan glycan) {
		return glycan.computeMass();
	}

	/**
	 * Attempts to locate a Glycan object based on the GlycanStructure's id. Null is returned if the Glycan isn't found.
	 * 
	 * @param structure - a GlycanStructure object
	 * @return Glycan - the Glycan object stored in "GlycanStructureAnnotation.hmStructureSettingsMass".
	 */
	public static GlycanAnalyte getGlycan(GlycanStructure structure) {
		try{
			if( ! GelatoAnalyteCache.hmGelatoAnalytesByStructureId.containsKey(structure.getId()) ) {
				throw new Exception("Could not find Glycan object for structure: " + structure.getId());
			} 
			// load the glycan and 
			GelatoAnalyte gelatoGlycan = GelatoAnalyteCache.hmGelatoAnalytesByStructureId.get(structure.getId());
			if(gelatoGlycan == null || gelatoGlycan.getAnalyte() == null) {
				throw new Exception("Could not find Glycan object for structure: " + structure.getId());
			}
			return (GlycanAnalyte) gelatoGlycan.getAnalyte();
		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * @param _glycan - a glycan object
	 * @param _bIsMono - whether or not to use monoisotopic mass
	 * @param _analyteSettings - current AnalyteSettings object
	 */
	public static void setGlycanMassOptions(Glycan _glycan, boolean _bIsMono, AnalyteSettings _analyteSettings) {
		try{
			if( ! _analyteSettings.getGlycanSettings().getFilter().getUseDatabaseStructureMetaInfo() ) {
				_glycan.setMassOptions(GlycanStructureAnnotation.collectMassOptions(_bIsMono, _analyteSettings)); 		
			}
		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * populate the GlycanAnnotation object with "extra info".<br>
	 * Cached settings for the specified Glycan are used so the "populateGlycanExtraInformation(..)" method is 
	 * called only once for each glycan structure (toString() is key). <br>
	 * If already calculated, calls "cloneGlycanExtraInformation(..)" to populate the passed Glycan object with what was stored.
	 * 
	 * @param annotation - a GlycanAnnotation object
	 * @param glycan - a Glycan object
	 */
	public static void populateExtraInfo(GlycanAnnotation annotation, Glycan glycan) {
		try{
			String sKey = glycan.toString();
			if( ! GelatoAnalyteCache.hmCachedExtraSettings.containsKey(sKey) ) {
				// change the reducing end to free end to make sure composition counts are working properly
				glycan.setReducingEndType(ResidueType.createFreeReducingEnd());
				GlycanExtraInfo.populateGlycanExtraInformation(annotation, glycan.toSugar());
				GelatoAnalyteCache.hmCachedExtraSettings.put(sKey, annotation);
			} else {
				logger.debug("Cloning extra info for: " + sKey);
				GlycanAnnotation sourceAnnotation = (GlycanAnnotation) GelatoAnalyteCache.hmCachedExtraSettings.get(sKey);
				GlycanExtraInfo.cloneGlycanExtraInformation(sourceAnnotation, annotation);
			}
		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	protected AnalyteSettings getGlycanSpecificAnalyteSettings( Glycan glycan ) {
		return null; // TODO
	}

	/**
	 * @param _gwbReducingEnd - String representation of reducing end.
	 * @return org.grits.toolbox.ms.om.data.ReducingEnd 
	 */
	public static ReducingEnd getGelatoReducingEnd( String _gwbReducingEnd ) {
		ReducingEnd re = new ReducingEnd();
		re.setLabel(_gwbReducingEnd);
		if( _gwbReducingEnd.contains("=") ) {
			String[] sToks = _gwbReducingEnd.split("=");
			re.setLabel(sToks[0]);
			sToks[1]= sToks[1].replace("u", ""); // TODO: verify unit
			re.setMass(Double.parseDouble(sToks[1]));

		} else {
			ResidueType type = ResidueDictionary.findResidueType(_gwbReducingEnd);
			re.setLabel(type.getName());
			re.setMass(type.getMass());
		}
		re.setType(_gwbReducingEnd);
		return re;
	}

	/**
	 * @param _gwbDerivatisationType - String representation of Derivitization type.
	 * 
	 * @return String - The static derivitization type as stored in GlycanPreDefinedOptions
	 */
	public static String getGelatoPerDerivatisationType(String _gwbDerivatisationType) {
		if( _gwbDerivatisationType.equals(MassOptions.PERMETHYLATED) ) {
			return GlycanPreDefinedOptions.DERIVITIZATION_PERMETHYLATED;
		}
		if( _gwbDerivatisationType.equals(MassOptions.HEAVYPERMETHYLATION) ) {
			return GlycanPreDefinedOptions.DERIVITIZATION_HEAVYPERMETHYLATION;
		}
		if( _gwbDerivatisationType.equals(MassOptions.PERDMETHYLATED) ) {
			return GlycanPreDefinedOptions.DERIVITIZATION_PERDMETHYLATED;
		}
		if( _gwbDerivatisationType.equals(MassOptions.PERACETYLATED) ) {
			return GlycanPreDefinedOptions.DERIVITIZATION_PERACETYLATED;
		}
		if( _gwbDerivatisationType.equals(MassOptions.PERDACETYLATED) ) {
			return GlycanPreDefinedOptions.DERIVITIZATION_PERDACETYLATED;
		}
		if( _gwbDerivatisationType.equals(MassOptions.NO_DERIVATIZATION) ) {
			return GlycanPreDefinedOptions.DERIVITIZATION_NO_DERIVATIZATION;
		}
		return GlycanPreDefinedOptions.DERIVITIZATION_NO_DERIVATIZATION;
	}

	/**
	 * @param _isMono - boolean, whether mass is monoisotopic or not
	 * @param _analyteSettings - current AnalyteSettings object
	 * @return MassOptions - class including derivitization, reducing end, mass type
	 */
	public static MassOptions collectMassOptions(boolean _isMono, AnalyteSettings _analyteSettings) {
		MassOptions t_massOptions = new MassOptions();
		//Map the derivitasiation type
		if(_analyteSettings.getGlycanSettings().getPerDerivatisationType().equals(GlycanPreDefinedOptions.DERIVITIZATION_PERMETHYLATED))
			t_massOptions.setDerivatization(MassOptions.PERMETHYLATED);
		else
			if(_analyteSettings.getGlycanSettings().getPerDerivatisationType().equals(GlycanPreDefinedOptions.DERIVITIZATION_HEAVYPERMETHYLATION))
				t_massOptions.setDerivatization(MassOptions.HEAVYPERMETHYLATION);
			else
				if(_analyteSettings.getGlycanSettings().getPerDerivatisationType().equals(GlycanPreDefinedOptions.DERIVITIZATION_PERDMETHYLATED))
					t_massOptions.setDerivatization(MassOptions.PERDMETHYLATED);
				else
					if(_analyteSettings.getGlycanSettings().getPerDerivatisationType().equals(GlycanPreDefinedOptions.DERIVITIZATION_PERACETYLATED))
						t_massOptions.setDerivatization(MassOptions.PERACETYLATED);
					else
						if(_analyteSettings.getGlycanSettings().getPerDerivatisationType().equals(GlycanPreDefinedOptions.DERIVITIZATION_PERDACETYLATED))
							t_massOptions.setDerivatization(MassOptions.PERDACETYLATED);
						else
							t_massOptions.setDerivatization(MassOptions.NO_DERIVATIZATION);
		//Map the Monoisotopic
		if(_isMono)
			t_massOptions.setIsotope(MassOptions.ISOTOPE_MONO);
		else
			t_massOptions.setIsotope(MassOptions.ISOTOPE_AVG);

		//Reducing End
		ResidueType gwbRT = GlycanStructureAnnotation.getResidueTypeForReducingEnd(_analyteSettings.getGlycanSettings().getReducingEnd());		
		t_massOptions.setReducingEndType(gwbRT);

		return t_massOptions;
	}

	/**
	 * @param _reducingEnd - ReducingEnd object
	 * @return ResidueType - the type of the reducing end as stored in the ResidueDictionary (if not other)
	 */
	public static ResidueType getResidueTypeForReducingEnd( ReducingEnd _reducingEnd ) {
		ResidueType gwbRT = null;
		if( _reducingEnd.getType().equals(GlycanPreDefinedOptions.OTHER) ) {
			gwbRT =  ResidueType.createOtherReducingEnd(_reducingEnd.getLabel(), _reducingEnd.getMass());
		} else {
			gwbRT = ResidueDictionary.findResidueType(_reducingEnd.getType());
			if( gwbRT == null ) { // something went wrong. try again as other
				logger.warn("Unable to look-up reducing end \"" + _reducingEnd.getLabel() + "\"");
				_reducingEnd.setType(GlycanPreDefinedOptions.OTHER);
				return GlycanStructureAnnotation.getResidueTypeForReducingEnd(_reducingEnd);				
			}
		}		
		return gwbRT;
	}

	/**
	 * @param settings - current AnalyteSettings object
	 * @return double - mass of the reducing end if "other"
	 */
	public static double getOtherReducingEnd(AnalyteSettings settings) {
		try {
			double changeValue = 0.0;
			if(settings.getGlycanSettings().getReducingEnd().getType().equals(GlycanPreDefinedOptions.OTHER)){
				changeValue += settings.getGlycanSettings().getReducingEnd().getMass();
				return changeValue;
			}
		}
		catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return 0d;

	}


	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean bCancel) {
		this.bCancel = bCancel;
		if( bCancel && this.curGlycanMatcher != null ) {
			this.curGlycanMatcher.setCanceled(true);
		}		
	}	

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.INotifyingProcess#isCanceled()
	 */
	public boolean isCanceled() {
		return bCancel;
	}
	
	@Override
	public void initializeSubScanMap() {
		// do nothing
	}

}


