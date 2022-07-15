package org.grits.toolbox.ms.annotation.sugar;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.Glyde.SugarExporterGlydeII;
import org.eurocarbdb.MolecularFramework.io.Glyde.SugarImporterGlydeII;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.IonCloud;
import org.eurocarbdb.application.glycanbuilder.MassOptions;
import org.eurocarbdb.application.glycanbuilder.ResidueDictionary;
import org.eurocarbdb.application.glycanbuilder.ResidueType;
import org.grits.toolbox.ms.annotation.gelato.glycan.GlycanStructureAnnotation;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.CustomExtraData.Type;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;

public class GlycanExtraInfo 
{
	private static final Logger logger = Logger.getLogger(GlycanExtraInfo.class);

	public static final String COMPOSITON ="gg.comp";
	public static final String CODE ="gg.gcode";
	public static final String GLYCAN_CHARGE ="gg.gch";
	public static final String BISECTION ="gg.bisec";
	public static final String N_RESIDUES ="gg.n.res";
	public static final String TYPE ="gg.type";
	public static final String SUBTYPE ="gg.subtype";
	public static final String N_BRANCHES ="gg.n.nbranch";
	public static final String N_HEX ="gg.n.hex";
	public static final String N_HEXNAC ="gg.n.hexnac";
	public static final String N_NEUAC ="gg.n.neuac";
	public static final String N_FUC ="gg.n.fuc";
	public static final String N_S ="gg.n.s";
	public static final String N_NEUGC ="gg.n.neugc";
	public static final String N_P ="gg.n.p";
	public static final String N_HEXA ="gg.n.hexa";
	public static final String N_OTHERS ="gg.n.other";
	public static final String CORE_FUC ="gg.corefuc";
	public static final String NON_CORE_FUC ="gg.ncfuc";
	public static final String N_GALGAL ="gg.n.galgal";
	public static final String N_POLY_SIA ="gg.n.polysia";
	public static final String N_LACNAC ="gg.n.lacnac";
	public static final String N_LACDINAC ="gg.n.lacdinac";
	public static final String N_LEWISAX ="gg.n.lewisax";
	public static final String N_LEWISBY ="gg.n.lewisby";
	public static final String N_SIA_LEWIS_AX ="gg.n.sialewax";
	public static final String N_SDA ="gg.n.sda";
	public static final String N_SIA_HEXNAC ="gg.n.shexnac";
	public static final String N_SIA_LACDINAC ="gg.n.slacdinac";
	public static final String N_FUC_LACDINAC ="gg.n.flacdinac";
	public static final String N_FUCFUC_LACDINAC ="gg.n.fflacdinac";
	public static final String GLYCAN_PME_MASS_PLUS_NA ="gg.pme+na";

	private static final Pattern reducing_end_pattern = Pattern.compile("^([^\\=]+)\\=(\\d+\\.*\\d*)u\\-\\-\\?(.*)");

	public static void populateGlycanExtraInformation(GlycanAnnotation a_annotation, Sugar a_sugar){
		try{
			fillOutTheHashMapWithTheNewInfo(a_annotation,a_sugar);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	public static void cloneGlycanExtraInformation(GlycanAnnotation a_sourceAnnotation, GlycanAnnotation a_targetAnnotation){
		try{
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_RESIDUES) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_RESIDUES) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_RESIDUES, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_RESIDUES) );
			}
			if( a_sourceAnnotation.getStringProp().containsKey(GlycanExtraInfo.CODE) && 
					a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.CODE) != null ) {
				a_targetAnnotation.getStringProp().put( GlycanExtraInfo.CODE, a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.CODE) );
			}
			if( a_sourceAnnotation.getStringProp().containsKey(GlycanExtraInfo.COMPOSITON) && 
					a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.COMPOSITON) != null ) {
				a_targetAnnotation.getStringProp().put( GlycanExtraInfo.COMPOSITON, a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.COMPOSITON) );
			}
			/*
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.GLYCAN_CHARGE) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.GLYCAN_CHARGE) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.GLYCAN_CHARGE, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.GLYCAN_CHARGE) );
			}
			*/
			if( a_sourceAnnotation.getStringProp().containsKey(GlycanExtraInfo.TYPE) && 
					a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.TYPE) != null ) {
				a_targetAnnotation.getStringProp().put( GlycanExtraInfo.TYPE, a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.TYPE) );
			}
			if( a_sourceAnnotation.getStringProp().containsKey(GlycanExtraInfo.SUBTYPE) && 
					a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.SUBTYPE) != null ) {
				a_targetAnnotation.getStringProp().put( GlycanExtraInfo.SUBTYPE, a_sourceAnnotation.getStringProp().get(GlycanExtraInfo.SUBTYPE) );
			}
			if( a_sourceAnnotation.getBooleanProp().containsKey(GlycanExtraInfo.BISECTION) && 
					a_sourceAnnotation.getBooleanProp().get(GlycanExtraInfo.BISECTION) != null ) {
				a_targetAnnotation.getBooleanProp().put( GlycanExtraInfo.BISECTION, a_sourceAnnotation.getBooleanProp().get(GlycanExtraInfo.BISECTION) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_BRANCHES) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_BRANCHES) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_BRANCHES, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_BRANCHES) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_HEX) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_HEX) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_HEX, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_HEX) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_HEXNAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_HEXNAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_HEXNAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_HEXNAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_NEUAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_NEUAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_NEUAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_NEUAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_FUC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_FUC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_FUC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_FUC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_S) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_S) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_S, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_S) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_NEUGC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_NEUGC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_NEUGC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_NEUGC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_P) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_P) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_P, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_P) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_HEXA) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_HEXA) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_HEXA, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_HEXA) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_OTHERS) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_OTHERS) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_OTHERS, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_OTHERS) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_GALGAL) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_GALGAL) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_GALGAL, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_GALGAL) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_POLY_SIA) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_POLY_SIA) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_POLY_SIA, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_POLY_SIA) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_LACNAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LACNAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_LACNAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LACNAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_LACDINAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LACDINAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_LACDINAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LACDINAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_LEWISAX) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LEWISAX) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_LEWISAX, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LEWISAX) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_LEWISBY) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LEWISBY) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_LEWISBY, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_LEWISBY) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_SIA_LEWIS_AX) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SIA_LEWIS_AX) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_SIA_LEWIS_AX, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SIA_LEWIS_AX) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_SDA) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SDA) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_SDA, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SDA) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_SIA_HEXNAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SIA_HEXNAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_SIA_HEXNAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SIA_HEXNAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_SIA_LACDINAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SIA_LACDINAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_SIA_LACDINAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_SIA_LACDINAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_FUC_LACDINAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_FUC_LACDINAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_FUC_LACDINAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_FUC_LACDINAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.N_FUCFUC_LACDINAC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_FUCFUC_LACDINAC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.N_FUCFUC_LACDINAC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.N_FUCFUC_LACDINAC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.CORE_FUC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.CORE_FUC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.CORE_FUC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.CORE_FUC) );
			}
			if( a_sourceAnnotation.getIntegerProp().containsKey(GlycanExtraInfo.NON_CORE_FUC) && 
					a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.NON_CORE_FUC) != null ) {
				a_targetAnnotation.getIntegerProp().put( GlycanExtraInfo.NON_CORE_FUC, a_sourceAnnotation.getIntegerProp().get(GlycanExtraInfo.NON_CORE_FUC) );
			}
			if( a_sourceAnnotation.getDoubleProp().containsKey(GlycanExtraInfo.GLYCAN_PME_MASS_PLUS_NA) && 
					a_sourceAnnotation.getDoubleProp().get(GlycanExtraInfo.GLYCAN_PME_MASS_PLUS_NA) != null ) {
				a_targetAnnotation.getDoubleProp().put( GlycanExtraInfo.GLYCAN_PME_MASS_PLUS_NA, a_sourceAnnotation.getDoubleProp().get(GlycanExtraInfo.GLYCAN_PME_MASS_PLUS_NA) );
			}
		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	public static void populateGlycanExtraInformation(GlycanAnnotation a_annotation) throws SugarImporterException
	{
		Sugar t_sugar;
		try{
			if( a_annotation.getSequenceGWB() != null && ! a_annotation.getSequenceGWB().equals("") ) {
				t_sugar = GlycanExtraInfo.gwbToSugar(a_annotation.getSequenceGWB());
			} else if( a_annotation.getSequenceFormat().equals( GlycanAnnotation.SEQ_FORMAT_GLYDEII ) ) {
				t_sugar = GlycanExtraInfo.glydeToSugar(a_annotation.getSequence());
			} else if( a_annotation.getSequenceFormat().equals( GlycanAnnotation.SEQ_FORMAT_GLYCOCT_CONDENSED ) ) {
				t_sugar = GlycanExtraInfo.glydeToSugar(a_annotation.getSequence());
			} else if( a_annotation.getSequenceFormat().equals( GlycanAnnotation.SEQ_FORMAT_GLYCOCT_XML ) ) {
				t_sugar = GlycanExtraInfo.glydeToSugar(a_annotation.getSequence());
			} else {
				throw new Exception("Unsupported sequence type: " + a_annotation.getSequenceFormat());
			}
			fillOutTheHashMapWithTheNewInfo(a_annotation,t_sugar);
		} catch (GlycoVisitorException e) {
			logger.error(e.getMessage(), e);		
		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	public static void populateGlycanExtraInformation(List<GlycanAnnotation> a_annotations) throws SugarImporterException
	{
		for( GlycanAnnotation t_annotation : a_annotations ) {
			Sugar t_sugar;
			try {
				t_sugar = GlycanExtraInfo.glydeToSugar(t_annotation.getSequence());
				fillOutTheHashMapWithTheNewInfo(t_annotation,t_sugar);
			} catch (GlycoVisitorException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static List<CustomExtraData> getColumns()
	{
		List<CustomExtraData> t_columns = new ArrayList<CustomExtraData>();
		t_columns.add( new CustomExtraData(GlycanExtraInfo.COMPOSITON, "Composition", null, Type.String));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.CODE, "Glycan Code", null, Type.String));
//		t_columns.add( new CustomExtraData(GlycanExtraInfo.GLYCAN_CHARGE, "Glycan Charge", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.BISECTION, "Bisection", null, Type.Boolean));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_RESIDUES, "# Residues", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.TYPE, "Glycan Type", null, Type.String));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.SUBTYPE, "Glycan Subtype", null, Type.String));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_BRANCHES, "# N-Glycan Branches", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_HEX, "# Hex", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_HEXNAC, "# HexNAc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_NEUAC, "# NeuAc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_FUC, "# Fuc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_S, "# Sulfate", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_NEUGC, "# NeuGc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_P, "# Phosphate", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_HEXA, "# HexA", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_OTHERS, "# Other Residues", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.CORE_FUC, "# Core fucosylation", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.NON_CORE_FUC, "# non Core Fuc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_GALGAL, "# Gal-Gal", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_POLY_SIA, "# Poly Sia", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_LACNAC, "# LacNAc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_LACDINAC, "# LacDiNAc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_LEWISAX, "# Lewis A/X", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_LEWISBY, "# Lewis B/Y", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_SIA_LEWIS_AX, "# Sialyl Lewis A/X", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_SDA, "# SDA", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_SIA_HEXNAC, "# Sialyl HexNAc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_SIA_LACDINAC, "# Sialyl LacDiNAc", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_FUC_LACDINAC, "# LacDiNAc (Fuc)", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.N_FUCFUC_LACDINAC, "# LacDiNAc (2Fuc)", null, Type.Integer));
		t_columns.add( new CustomExtraData(GlycanExtraInfo.GLYCAN_PME_MASS_PLUS_NA, "M(pMe)+Na", null, Type.Double));
		return t_columns;
	}

	public static Sugar annotationToSugar(GlycanAnnotation a_annotation) throws GlycoVisitorException, SugarImporterException {
		try{
			if( a_annotation.getSequenceGWB() != null && ! a_annotation.getSequenceGWB().equals("") ) {
				return GlycanExtraInfo.gwbToSugar(a_annotation.getSequenceGWB());
			} else if( a_annotation.getSequenceFormat().equals( GlycanAnnotation.SEQ_FORMAT_GLYDEII ) ) {
				return GlycanExtraInfo.glydeToSugar(a_annotation.getSequence());
			} else if( a_annotation.getSequenceFormat().equals( GlycanAnnotation.SEQ_FORMAT_GLYCOCT_CONDENSED ) ) {
				return GlycanExtraInfo.glycoCTCondensedToSugar(a_annotation.getSequence());			
			} else if( a_annotation.getSequenceFormat().equals( GlycanAnnotation.SEQ_FORMAT_GLYCOCT_XML ) ) {
				return GlycanExtraInfo.glycoCTToSugar(a_annotation.getSequence());			
			} else {
				throw new Exception("Unsupported sequence type: " + a_annotation.getSequenceFormat());
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return null;
	}	

	public static Glycan gwbToGlycan(String a_gwb) throws GlycoVisitorException, SugarImporterException {
		try{
			String t_seq = a_gwb;
			ResidueType gwbRT = null;
			if( a_gwb.contains("=" ) ) {
				Matcher m = GlycanExtraInfo.reducing_end_pattern.matcher(a_gwb);
				if( m.matches() ) {
					String sRedEndLabel = m.group(1);
					String sRedEndMass = m.group(2);
					//					t_seq = "test=1.0u--?" + m.group(3);
					//					ResidueType gwbRT = null;
					gwbRT = ResidueDictionary.findResidueType(sRedEndLabel);
					if( gwbRT == null ) { // something went wrong. try again as other
						t_seq = "other=" + sRedEndMass + "u--?" + m.group(3);
						gwbRT = ResidueType.createOtherReducingEnd(sRedEndLabel, Double.parseDouble(sRedEndMass));			
					} 

					//					t_massOptions.setReducingEndType(gwbRT);
				}
			}
			//			Glycan glycan = Glycan.fromString(t_seq,t_massOptions);
			Glycan glycan = Glycan.fromString(t_seq);	
			if( gwbRT != null ){
				glycan.setReducingEndType(gwbRT);
			}
			return glycan;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}	

	public static Sugar gwbToSugar(String a_gwb) throws GlycoVisitorException, SugarImporterException {
		try{
			Glycan glycan = GlycanExtraInfo.gwbToGlycan(a_gwb);
			Sugar t_sugar = glycan.toSugar();
			return t_sugar;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}	

	public static Sugar glycoCTToSugar(String a_glycoCT) throws GlycoVisitorException, SugarImporterException {
		try{
			SugarImporterGlycoCT t_importer = new SugarImporterGlycoCT();
			Sugar t_sugar = t_importer.parse(a_glycoCT);
			return t_sugar;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}	

	public static Sugar glycoCTCondensedToSugar(String a_glycoCTCondensed) throws GlycoVisitorException, SugarImporterException {
		try{
			SugarImporterGlycoCTCondensed t_importer = new SugarImporterGlycoCTCondensed();
			Sugar t_sugar = t_importer.parse(a_glycoCTCondensed);
			return t_sugar;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}	

	public static Sugar glydeToSugar(String a_glyde) throws GlycoVisitorException, SugarImporterException {
		try{
			SugarImporterGlydeII t_importer = new SugarImporterGlydeII();
			Sugar t_sugar = t_importer.parse(a_glyde);
			return t_sugar;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}	

	public static String sugarToGlycoCT(Sugar a_sugar) throws GlycoVisitorException, SugarImporterException {
		try{
			SugarExporterGlycoCTCondensed t_exporter = new SugarExporterGlycoCTCondensed();
			t_exporter.start(a_sugar);
			return t_exporter.getHashCode();
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}	

	public static String sugarToGlyde(Sugar a_sugar) throws GlycoVisitorException, SugarImporterException {
		try{
			SugarExporterGlydeII t_exporter = new SugarExporterGlydeII();
			t_exporter.start(a_sugar);
			return t_exporter.getXMLCode();
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}	


	private static void fillOutTheHashMapWithTheNewInfo(GlycanAnnotation a_glycanAnnotation,Sugar a_sugar)
	{
		// number of residues
		a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_RESIDUES,a_sugar.getNodes().size());
		// glycan code
		try 
		{
			GlycoVisitorGlycanCode t_visitorGlycanCode = new GlycoVisitorGlycanCode();
			t_visitorGlycanCode.start(a_sugar);
			a_glycanAnnotation.getStringProp().put(GlycanExtraInfo.CODE, t_visitorGlycanCode.getCode());
			a_glycanAnnotation.getStringProp().put( GlycanExtraInfo.COMPOSITON, t_visitorGlycanCode.getCompositionString());
		}
		catch (Exception e) 
		{
			logger.error("Unable to create glycan code for " + a_glycanAnnotation.getStringId(), e);
		}

		/* removed 03/10/16
		// glycan charge
		a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.GLYCAN_CHARGE, createCharge(a_glycanAnnotation, a_sugar));
		*/
		// glycan type and sub type
		String t_glycanType = null;
		try
		{
			GlycoVisitorGlycanType t_visitorType = new GlycoVisitorGlycanType();
			t_visitorType.start(a_sugar);
			t_glycanType = t_visitorType.getType(); 
			if ( t_glycanType != null )
			{
				a_glycanAnnotation.getStringProp().put(GlycanExtraInfo.TYPE,t_glycanType);
			}
			String t_sub = t_visitorType.getSubType(); 
			if ( t_sub != null )
			{
				a_glycanAnnotation.getStringProp().put(GlycanExtraInfo.SUBTYPE,t_sub);
			}
		}
		catch (GlycoVisitorException e)
		{
			logger.error("Unable to find glycan type for " + a_glycanAnnotation.getStringId(),e);
		}
		// fill NGlycan Related Info
		if ( t_glycanType != null )
		{
			if ( t_glycanType.equals(GlycoVisitorGlycanType.N_Glycan) )
			{
				try
				{
					GlycoVisitorNGlycanInformation t_visitor = new GlycoVisitorNGlycanInformation();
					t_visitor.start(a_sugar);
					if ( t_visitor.isNGlycan() )
					{
						a_glycanAnnotation.getBooleanProp().put(GlycanExtraInfo.BISECTION,t_visitor.getBisection());
						a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_BRANCHES,t_visitor.getNGlycanBranches());
					}
				}
				catch (GlycoVisitorException e)
				{
					logger.error("Unable to find N-glycan informaiton for " + a_glycanAnnotation.getStringId(), e);
				}
			}
		}
		// Number of single residues 
		try
		{
			GlycoVisitorResidueCounter t_visitor = new GlycoVisitorResidueCounter();
			t_visitor.start(a_sugar);
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_HEX,t_visitor.getHex());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_HEXNAC,t_visitor.getHexNAc());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_NEUAC,t_visitor.getNeuAc());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_FUC,t_visitor.getHex6D());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_S,t_visitor.getSulfate());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_NEUGC,t_visitor.getNeuGc());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_P,t_visitor.getPhosphate());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_HEXA,t_visitor.getHexA());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_OTHERS,t_visitor.getOther());
		}
		catch (GlycoVisitorException e)
		{
			logger.error("Unable to count residue information for " + a_glycanAnnotation.getStringId(), e);
		}
		// Number of motifs
		try
		{
			GlycoVisitorMotifCounter t_visitor = new GlycoVisitorMotifCounter();
			t_visitor.start(a_sugar);
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_GALGAL,t_visitor.getGalGal());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_POLY_SIA,t_visitor.getPolySia());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_LACNAC,t_visitor.getLacNac());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_LACDINAC,t_visitor.getLacDiNac());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_LEWISAX,t_visitor.getLewisAX());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_LEWISBY,t_visitor.getLewisBY());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_SIA_LEWIS_AX,t_visitor.getSiaLewis());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_SDA,t_visitor.getSda());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_SIA_HEXNAC,t_visitor.getSiaHexNAc());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_SIA_LACDINAC,t_visitor.getSiaLacDiNac());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_FUC_LACDINAC,t_visitor.getMonoFucLacDiNac());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.N_FUCFUC_LACDINAC,t_visitor.getDiFucLacDiNac());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.CORE_FUC,t_visitor.getCoreFuc());
			a_glycanAnnotation.getIntegerProp().put(GlycanExtraInfo.NON_CORE_FUC,t_visitor.getNonCoreFuc());
		}
		catch (GlycoVisitorException e)
		{
			logger.error("Unable to count motif information for " + a_glycanAnnotation.getStringId(),e);
		}
		// native mass + Na
		try
		{
			logger.debug("Calculating mass options for glycan: " + a_glycanAnnotation.getStringId());
			Glycan t_glycan = null;
			if (a_glycanAnnotation.getSequenceGWB() != null )
			{
				t_glycan = Glycan.fromString(a_glycanAnnotation.getSequenceGWB());
			}
			else
			{
				SugarExporterGlycoCTCondensed t_exporter = new SugarExporterGlycoCTCondensed();
				t_exporter.start(a_sugar);
				t_glycan = Glycan.fromGlycoCTCondensed(t_exporter.getHashCode());
			}
			MassOptions t_massOptions = new MassOptions();
			t_massOptions.setDerivatization(MassOptions.PERMETHYLATED);
			t_massOptions.setIsotope(MassOptions.ISOTOPE_MONO);
			t_massOptions.ION_CLOUD = new IonCloud();
			
			//Reducing End
			ResidueType gwbRT = GlycanStructureAnnotation.getResidueTypeForReducingEnd(a_glycanAnnotation.getReducingEnd());		
			t_massOptions.setReducingEndType(gwbRT);
			t_glycan.setMassOptions(t_massOptions);
			a_glycanAnnotation.getDoubleProp().put(GlycanExtraInfo.GLYCAN_PME_MASS_PLUS_NA,t_glycan.computeMass() + 22.989769D);
		}
		catch (Exception e)
		{
			logger.error("Unable to calculate native mass " + a_glycanAnnotation.getStringId(),e);
		}
	}

	private static Integer createCharge(GlycanAnnotation a_annotation, Sugar a_sugar)
	{
		try
		{
			GlycoVisitorChargeCalculator t_visitorCharge = new GlycoVisitorChargeCalculator();
			t_visitorCharge.start(a_sugar);
			return t_visitorCharge.getCharge();
		}
		catch (Exception e) 
		{
			logger.error("Unable to calculate glycan charge for " + a_annotation.getStringId(),e);
		}
		return null;
	}

}
