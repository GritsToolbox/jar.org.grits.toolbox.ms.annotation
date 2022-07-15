package org.grits.toolbox.ms.annotation.structure;

import org.grits.toolbox.ms.om.data.Ion;
import org.grits.toolbox.ms.om.data.Molecule;

public class GlycanPreDefinedOptions {
	public static Ion ION_ADDUCT_HYDROGEN = new Ion("H", new Double(1.007825032), "Hydrogen", Integer.valueOf(1), Boolean.TRUE);
	public static Ion ION_ADDUCT_SODIUM = new Ion("Na", new Double(22.989769670), "Sodium", Integer.valueOf(1), Boolean.TRUE);
	public static Ion ION_ADDUCT_POTASSIUM = new Ion("K", new Double(38.963706900), "Potassium", Integer.valueOf(1), Boolean.TRUE);
	public static Ion ION_ADDUCT_CHLORINE = new Ion("Cl", new Double(34.968852710), "Chlorine", Integer.valueOf(1), Boolean.FALSE);
	public static Ion ION_ADDUCT_LITHIUM = new Ion("Li", new Double(7.016004000), "Lithium", Integer.valueOf(1), Boolean.TRUE);
	public static Ion ION_ADDUCT_ELECTRON = new Ion("e", new Double(0.0005486), "electron", Integer.valueOf(1), Boolean.TRUE);
	public static Ion ION_ADDUCT_NEGHYDROGEN = new Ion("-H", new Double(-1.007825032), "Negative mode Hydrogen", Integer.valueOf(1), Boolean.FALSE);
	public static Ion ION_ADDUCT_CALCIUM = new Ion("Ca", new Double(39.9625906), "Calcium", Integer.valueOf(2), Boolean.TRUE);
	
	public static String DERIVITIZATION_PERMETHYLATED = "perMe";
	public static String DERIVITIZATION_HEAVYPERMETHYLATION = "perMe(C^13)";
	public static String DERIVITIZATION_PERDMETHYLATED = "perDMe";
	public static String DERIVITIZATION_PERACETYLATED = "perAc";
	public static String DERIVITIZATION_PERDACETYLATED = "perDAc";
	public static String DERIVITIZATION_NO_DERIVATIZATION = "None";

	public static String REDUCING_END_FREE_END = "freeEnd";
	public static String REDUCING_END_REDUCING_END = "redEnd";
	public static String REDUCING_END_PA = "PA";
	public static String REDUCING_END_2AB = "2AB";
	public static String REDUCING_END_AA = "AA";
	public static String REDUCING_END_DAP = "DAP";
	public static String REDUCING_END_4AB = "4AB";
	public static String REDUCING_END_DAPMAP = "DAPMAP";
	public static String REDUCING_END_AMC = "AMC";
	public static String REDUCING_END_6AQ = "6AQ";
	public static String REDUCING_END_2AAC = "2AAc";
	public static String REDUCING_END_FMC = "FMC";
	public static String REDUCING_END_DH = "DH";
	public static String REDUCING_END_ME = "Me";
	public static String REDUCING_END_DEOXY = "deoxy";
	
	public static Molecule LOSS_H20 = new Molecule("H20", new Double(18.0101), "Water");
	public static Molecule LOSS_METHYL = new Molecule("CH2", new Double(14.0157), "Methyl");	
	public static Molecule LOSS_SIAL_POS = new Molecule("Sial(Pos)", new Double(291.26), "Sialic acid (pos mode)");	
	public static Molecule LOSS_SIAL_NEG= new Molecule("Sial(Neg)", new Double(290.25), "Sialic acid (neg Mode)");	

	public static String OTHER = "other";
	
	public static String[] getAllLosses() {
		String[] losses = new String[5];
		int i = 0;
		losses[i++] = LOSS_H20.getLabel();
		losses[i++] = LOSS_METHYL.getLabel();
		losses[i++] = LOSS_SIAL_POS.getLabel();
		losses[i++] = LOSS_SIAL_NEG.getLabel();
		losses[i++] = OTHER;
		return losses;
	}
	
	public static String[] getAllReducingEndTypes() {
		String[] redEnds = new String[16];
		int i = 0;
		redEnds[i++] = REDUCING_END_FREE_END;
		redEnds[i++] = REDUCING_END_REDUCING_END;
		redEnds[i++] = REDUCING_END_PA;
		redEnds[i++] = REDUCING_END_2AB;
		redEnds[i++] = REDUCING_END_AA;
		redEnds[i++] = REDUCING_END_DAP;
		redEnds[i++] = REDUCING_END_4AB;
		redEnds[i++] = REDUCING_END_DAPMAP;
		redEnds[i++] = REDUCING_END_AMC;
		redEnds[i++] = REDUCING_END_6AQ;
		redEnds[i++] = REDUCING_END_2AAC;
		redEnds[i++] = REDUCING_END_FMC;
		redEnds[i++] = REDUCING_END_DH;
		redEnds[i++] = REDUCING_END_ME;
		redEnds[i++] = REDUCING_END_DEOXY;
		redEnds[i++] = OTHER;
		return redEnds;
	}
	
	public static String[] getAllDerivitizationTypes() {
		String[] derivTypes = new String[6];
		int i = 0;
		
 		derivTypes[i++] = DERIVITIZATION_PERMETHYLATED;
		derivTypes[i++] = DERIVITIZATION_HEAVYPERMETHYLATION;
		derivTypes[i++] = DERIVITIZATION_PERDMETHYLATED;
		derivTypes[i++] = DERIVITIZATION_PERACETYLATED;
		derivTypes[i++] = DERIVITIZATION_PERDACETYLATED;
		derivTypes[i++] = DERIVITIZATION_NO_DERIVATIZATION;
		
		return derivTypes;
	}
}
