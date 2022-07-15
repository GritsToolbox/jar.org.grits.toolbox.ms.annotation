package org.grits.toolbox.ms.annotation.sugar;

import java.util.HashMap;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class GlycoVisitorGlycanType extends GlycoVisitorResidueBased
{
    protected static final String N_Glycan = "N-Glycan";
    protected static final String O_GalNAc = "O-GalNAc";
    protected static final String O_Man = "O-Man";
    protected static final String O_Fuc = "O-Fuc";
    protected static final String GSL = "GSL";

    protected static final String N_Glycan_complex = "complex";
    protected static final String N_Glycan_hybrid = "hybrid";
    protected static final String N_Glycan_high_man = "high mannose";
    protected static final String N_Glycan_pauci_man = "pauci mannose";
    
    protected static final String GSL_LACTO = "lacto";
    protected static final String GSL_NEO_LACTO = "neo lacto";
    protected static final String GSL_GANGLIO = "ganglio";
    protected static final String GSL_GLOBO = "globo";
    protected static final String GSL_ISOGLOBO = "isoglobo";
    protected static final String GSL_MUCO = "muco";
    protected static final String GSL_ARTHRO = "arthro";
    protected static final String GSL_MOLLU = "mollu";
    protected static final String GSL_GALA = "gala";

    private HashMap<GlycoNode, Boolean> m_hashGlcNac = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashGal = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashMan = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashGalNAc = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashFuc = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashGlc = new HashMap<GlycoNode, Boolean>();

    private String m_type = null;
    private String m_subType = null;
    
    public void clear()
    {
        this.m_hashUsedResidue.clear();
        this.m_hashGlcNac.clear();
        this.m_hashGal.clear();
        this.m_hashMan.clear();
        this.m_hashGalNAc.clear();
        this.m_hashFuc.clear();
        this.m_hashGlc.clear();
        this.m_type = null;
        this.m_subType = null;
    }

    public String getType()
    {
        return m_type;
    }

    public void setType(String a_type)
    {
        m_type = a_type;
    }

    public String getSubType()
    {
        return m_subType;
    }

    public void setSubType(String a_subType)
    {
        m_subType = a_subType;
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_visitor) throws GlycoVisitorException
    {
        return new GlycoTraverserTreeSingle(a_visitor);
    }

    public void start(Sugar a_sugar) throws GlycoVisitorException
    {
        this.clear();
        GlycoTraverser t_traverser = this.getTraverser(this);
        t_traverser.traverseGraph(a_sugar);
        this.figureTypeOut();
    }

    private void figureTypeOut()
    {
        if ( this.testN() )
        {
            return;
        }
        if ( this.testO() )
        {
            return;
        }
        if ( this.testGSL() )
        {
            return;
        }
    }

    private boolean testGSL()
    {
        this.m_hashUsedResidue.clear();
        for (GlycoNode t_glc : this.m_hashGlc.keySet())
        {
            if ( t_glc.getParentEdge() == null )
            {
                // reducing end Glc
                GlycoNode t_gal = this.findChild(t_glc, this.m_hashGal, false);
                if ( t_gal != null )
                {
                    GlycoNode t_glcnac = this.findChild(t_gal, this.m_hashGlcNac, false);
                    if ( t_glcnac != null )
                    {
                        GlycoNode t_gal2 = this.findChild(t_glcnac, this.m_hashGal, false);
                        if ( t_gal2 != null )
                        {
                            if ( this.testLinkage(t_gal2.getParentEdge(), 3) )
                            {
                                this.m_type = GlycoVisitorGlycanType.GSL;
                                this.m_subType = GlycoVisitorGlycanType.GSL_LACTO;
                                return true;
                            }
                            this.m_type = GlycoVisitorGlycanType.GSL;
                            this.m_subType = GlycoVisitorGlycanType.GSL_NEO_LACTO;
                            return true;
                        }
                    }
                    GlycoNode t_galnac = this.findChild(t_gal, this.m_hashGalNAc, false);
                    if ( t_galnac != null )
                    {
                        GlycoNode t_gal2 = this.findChild(t_galnac, this.m_hashGal, false);
                        if ( t_gal2 != null )
                        {
                            this.m_type = GlycoVisitorGlycanType.GSL;
                            this.m_subType = GlycoVisitorGlycanType.GSL_GANGLIO;
                            return true;
                        }
                    }
                    GlycoNode t_gal2 = this.findChild(t_gal, this.m_hashGal, false);
                    GlycoNode t_gal3 = this.findChild(t_gal2, this.m_hashGal, false);
                    if ( t_gal2 != null && t_gal3 != null )
                    {
                        this.m_type = GlycoVisitorGlycanType.GSL;
                        this.m_subType = GlycoVisitorGlycanType.GSL_MUCO;
                        return true;
                    }
                    t_galnac = this.findChild(t_gal2, this.m_hashGalNAc, false);
                    if ( t_gal2 != null && t_galnac != null )
                    {
                        if ( this.testLinkage(t_gal2.getParentEdge(), 3) )
                        {
                            this.m_type = GlycoVisitorGlycanType.GSL;
                            this.m_subType = GlycoVisitorGlycanType.GSL_GLOBO;
                            return true;
                        }
                        this.m_type = GlycoVisitorGlycanType.GSL;
                        this.m_subType = GlycoVisitorGlycanType.GSL_ISOGLOBO;
                        return true;
                    }
                }
                GlycoNode t_man = this.findChild(t_glc, this.m_hashMan, false);
                if ( t_man != null )
                {
                    GlycoNode t_manB = this.findChild(t_man, this.m_hashMan, false);
                    GlycoNode t_glcnac = this.findChild(t_manB, this.m_hashGlcNac, false);
                    if ( t_manB != null && t_glcnac != null )
                    {
                        this.m_type = GlycoVisitorGlycanType.GSL;
                        this.m_subType = GlycoVisitorGlycanType.GSL_MOLLU;
                        return true;
                    }
                    t_glcnac = this.findChild(t_man, this.m_hashGlcNac, false);
                    GlycoNode t_galnac = this.findChild(t_glcnac, this.m_hashGalNAc, false);
                    if ( t_glcnac != null && t_galnac != null )
                    {
                        this.m_type = GlycoVisitorGlycanType.GSL;
                        this.m_subType = GlycoVisitorGlycanType.GSL_ARTHRO;
                        return true;
                    }
                }
            }
        }
        for (GlycoNode t_gal : this.m_hashGal.keySet())
        {
            if ( t_gal.getParentEdge() == null )
            {
                // reducing end Gal
                GlycoNode t_galB = this.findChild(t_gal, this.m_hashGal, false);
                if ( t_galB != null )
                {
                    this.m_type = GlycoVisitorGlycanType.GSL;
                    this.m_subType = GlycoVisitorGlycanType.GSL_GALA;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean testO()
    {
        this.m_hashUsedResidue.clear();
        for (GlycoNode t_galnac : this.m_hashGalNAc.keySet())
        {
            if ( t_galnac.getParentEdge() == null )
            {
                // reducing end GalNAc
                if ( this.testOGalNAc(t_galnac) )
                {
                    return true;
                }
            }
        }
        for (GlycoNode t_fuc : this.m_hashFuc.keySet())
        {
            if ( t_fuc.getParentEdge() == null )
            {
                this.m_type = GlycoVisitorGlycanType.O_Fuc;
                return true;
            }
        }
        for (GlycoNode t_man : this.m_hashMan.keySet())
        {
            if ( t_man.getParentEdge() == null )
            {
                this.m_type = GlycoVisitorGlycanType.O_Man;
                return true;
            }
        }
        return false;
    }

    private boolean testOGalNAc(GlycoNode a_galnac)
    {
        GlycoNode t_gal = this.findChild(a_galnac, this.m_hashGal, false);
        GlycoNode t_glcnac = this.findChild(a_galnac, m_hashGlcNac, false);
        if ( t_gal != null && t_glcnac != null )
        {
            this.m_type = GlycoVisitorGlycanType.O_GalNAc;
            this.m_subType = "core 2";
            return true;
        }
        if ( t_gal != null )
        {
            this.m_type = GlycoVisitorGlycanType.O_GalNAc;
            this.m_subType = "core 1";
            return true;
        }
        if ( t_glcnac != null )
        {
            // core 3,4 or 6
            this.m_hashUsedResidue.put(t_glcnac, Boolean.TRUE);
            GlycoNode t_glcnacB = this.findChild(a_galnac, this.m_hashGlcNac, false);
            if ( t_glcnacB != null )
            {
                this.m_type = GlycoVisitorGlycanType.O_GalNAc;
                this.m_subType = "core 4";
                return true;
            }
            if ( this.testLinkage(t_glcnac.getParentEdge(),3) )
            {
                this.m_type = GlycoVisitorGlycanType.O_GalNAc;
                this.m_subType = "core 3";
                return true;
            }
            this.m_type = GlycoVisitorGlycanType.O_GalNAc;
            this.m_subType = "core 6";
            return true;
        }
        GlycoNode t_galnac = this.findChild(a_galnac, this.m_hashGalNAc, false);
        if ( t_galnac != null )
        {
            // core 5 or 7
            if ( this.testLinkage(t_galnac.getParentEdge(),3) )
            {
                this.m_type = GlycoVisitorGlycanType.O_GalNAc;
                this.m_subType = "core 5";
                return true;
            }
            this.m_type = GlycoVisitorGlycanType.O_GalNAc;
            this.m_subType = "core 7";
            return true;
        }
        return false;
    }

    private boolean testN()
    {
        this.m_hashUsedResidue.clear();
        for (GlycoNode t_glcnacA : this.m_hashGlcNac.keySet())
        {
            if ( t_glcnacA.getParentEdge() == null )
            {
                // found reducing end GlcNAc
                GlycoNode t_glcnacB = this.findChild(t_glcnacA,this.m_hashGlcNac,false);
                if ( t_glcnacB != null )
                {
                    // found chitobiose core
                    GlycoNode t_man = this.findChild(t_glcnacB,this.m_hashMan,false);
                    if ( t_man != null )
                    {
                        // found core mannose
                        GlycoNode t_manA = this.findChild(t_man,this.m_hashMan,false);
                        // its an N-glycan
                        this.m_type = GlycoVisitorGlycanType.N_Glycan;
                        if ( t_manA != null )
                        {
                            // found first man
                            this.m_hashUsedResidue.put(t_manA, Boolean.TRUE);
                            GlycoNode t_manB = this.findChild(t_man,this.m_hashMan,false);
                            if ( t_manB != null )
                            {
                                // figure out the sub type
                                this.m_subType = this.testNsubType(t_manA,t_manB);
                                return true;
                            }
                            else
                            {
                                this.m_subType = GlycoVisitorGlycanType.N_Glycan_pauci_man;
                                return true;
                            }
                        }
                        else
                        {
                            this.m_subType = GlycoVisitorGlycanType.N_Glycan_pauci_man;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String testNsubType(GlycoNode a_manA, GlycoNode a_manB)
    {
        GlycoNode t_glcnacA = this.findChild(a_manA, this.m_hashGlcNac, false);
        GlycoNode t_glcnacB = this.findChild(a_manB, this.m_hashGlcNac, false);
        // both GlcNAc
        if ( t_glcnacA != null && t_glcnacB != null )
        {
            return GlycoVisitorGlycanType.N_Glycan_complex;
        }
        GlycoNode t_manA = this.findChild(a_manA, this.m_hashMan, false);
        GlycoNode t_manB = this.findChild(a_manB, this.m_hashMan, false);
        // both Man
        if ( t_manA != null && t_manB != null )
        {
            return GlycoVisitorGlycanType.N_Glycan_high_man;
        }
        // one Man and one GlcNAc
        if ( (t_manA != null && t_glcnacB != null) || (t_manB != null && t_glcnacA != null) )
        {
            return GlycoVisitorGlycanType.N_Glycan_hybrid;
        }
        // one GlcNAc and no Man
        if ( (t_glcnacA != null && t_manB == null) || (t_glcnacB != null && t_manA == null) )
        {
            // if there are two GlcNAcs => complex otherwise hybrid
            if ( t_glcnacA != null )
            {
                this.m_hashUsedResidue.put(t_glcnacA, Boolean.TRUE);
                GlycoNode t_glcnacA2 = this.findChild(a_manA, this.m_hashGlcNac, false);
                if ( t_glcnacA2 == null )
                {
                    return GlycoVisitorGlycanType.N_Glycan_hybrid;
                }
                else
                {
                    return GlycoVisitorGlycanType.N_Glycan_complex;
                }
            }
            else
            {
                this.m_hashUsedResidue.put(t_glcnacB, Boolean.TRUE);
                GlycoNode t_glcnacB2 = this.findChild(a_manB, this.m_hashGlcNac, false);
                if ( t_glcnacB2 == null )
                {
                    return GlycoVisitorGlycanType.N_Glycan_hybrid;
                }
                else
                {
                    return GlycoVisitorGlycanType.N_Glycan_complex;
                }
            }
            
        }
        // one Man and no GlcNAc 
        if ( (t_glcnacA == null && t_manB != null) || (t_glcnacB == null && t_manA != null) )
        {
            return GlycoVisitorGlycanType.N_Glycan_high_man;
        }
        // no GlcNAc and no Man
        if ( t_glcnacA == null && t_manA == null && t_glcnacB == null && t_manB == null)
        {
            return GlycoVisitorGlycanType.N_Glycan_pauci_man;
        }
        return null;
    }

    public void visit(NonMonosaccharide a_nonMs) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("NonMonosaccharide is not supported."); 
    }

    public void visit(SugarUnitRepeat a_repeat) throws GlycoVisitorException
    {
        // nothing to do
    }

    public void visit(Substituent a_subst) throws GlycoVisitorException
    {
        // nothing to do
    }

    public void visit(SugarUnitCyclic a_cyclic) throws GlycoVisitorException
    {
        // nothing to do
    }

    public void visit(SugarUnitAlternative a_alternative) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("SugarUnitAlternative is not supported."); 
    }

    public void visit(UnvalidatedGlycoNode a_unvalidated) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode is not supported."); 
    }

    public void visit(GlycoEdge a_edge) throws GlycoVisitorException
    {
        // nothing to do
    }

    public void visit(Monosaccharide a_ms) throws GlycoVisitorException
    {
        int t_numberOfSubstituents = this.calcNumberOfSubstituents(a_ms);
        if ( a_ms.getSuperclass().equals(Superclass.HEX) )
        {
            if ( this.isGlucose(a_ms) && a_ms.getModification().size() == 0 )
            {
                if ( t_numberOfSubstituents == 0 )
                {
                    this.m_hashGlc.put(a_ms, Boolean.TRUE);
                }
                else
                {
                    for (GlycoEdge t_edge : a_ms.getChildEdges()) 
                    {
                        if ( this.isPosition(2,t_edge) && this.isSubst(SubstituentType.N_ACETYL,t_edge.getChild()) && this.isLinkageType(LinkageType.DEOXY,t_edge) )
                        {
                            if ( t_numberOfSubstituents == 1 )
                            {
                                this.m_hashGlcNac.put(a_ms,Boolean.TRUE);
                                return;
                            }
                        }
                    }
                }
            }
            if ( this.isGalactose(a_ms) )
            {
                if ( a_ms.getModification().size() == 0 )
                {
                    for (GlycoEdge t_edge : a_ms.getChildEdges()) 
                    {
                        if ( this.isPosition(2,t_edge) && this.isSubst(SubstituentType.N_ACETYL,t_edge.getChild()) && this.isLinkageType(LinkageType.DEOXY,t_edge) )
                        {
                            if ( t_numberOfSubstituents == 1 )
                            {
                                this.m_hashGalNAc.put(a_ms,Boolean.TRUE);
                                return;
                            }
                        }
                    }
                    if ( t_numberOfSubstituents == 0 )
                    {
                        this.m_hashGal.put(a_ms, Boolean.TRUE);
                        return;
                    }
                }
                for (Modification t_modi : a_ms.getModification()) 
                {
                    if ( this.isModiPosition(6,t_modi.getPositionOne()) && t_modi.getModificationType().equals(ModificationType.DEOXY) )
                    {
                        if ( a_ms.getModification().size() == 1 && t_numberOfSubstituents == 0 )
                        {
                            this.m_hashFuc.put(a_ms, Boolean.TRUE);
                            return;
                        }
                    }
                }
            }
            if ( this.isMannose(a_ms) )
            {
                if ( a_ms.getModification().size() == 0 && t_numberOfSubstituents == 0 )
                {
                    this.m_hashMan.put(a_ms, Boolean.TRUE);
                    return;
                }
            }
        }
    }

}