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
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserNodes;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class GlycoVisitorMotifCounter extends GlycoVisitorResidueBased 
{
    private HashMap<GlycoNode, Boolean> m_hashGlcNac = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashNeuAc = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashGal = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashGalNAc = new HashMap<GlycoNode, Boolean>();
    private HashMap<GlycoNode, Boolean> m_hashFuc = new HashMap<GlycoNode, Boolean>();
    private int m_coreFuc = 0;
    private int m_polySia = 0;
    private int m_lacNac = 0;
    private int m_lacDiNac = 0;
    private int m_galGal = 0;
    private int m_nonCoreFuc = 0;
    private int m_lewisAX = 0;
    private int m_lewisBY = 0;
    private int m_siaLewis = 0;
    private int m_sda = 0;
    private int m_siaHexNAc = 0;
    private int m_siaLacDiNac = 0;
    private int m_monoFucLacDiNac = 0;
    private int m_diFucLacDiNac = 0;

    public int getGalGal()
    {
        return this.m_galGal;
    }

    public void setGalGal(int a_galGal)
    {
        this.m_galGal = a_galGal;
    }

    public void clear() 
    {
        this.m_coreFuc = 0;
        this.m_polySia = 0;
        this.m_lacNac = 0;
        this.m_lacDiNac = 0;
        this.m_galGal = 0;
        this.m_nonCoreFuc = 0;
        this.m_lewisAX = 0;
        this.m_lewisBY = 0;
        this.m_siaLewis = 0;
        this.m_sda = 0;
        this.m_siaHexNAc = 0;
        this.m_siaLacDiNac = 0;
        this.m_monoFucLacDiNac = 0;
        this.m_diFucLacDiNac = 0;
        this.m_hashUsedResidue.clear();
        this.m_hashGlcNac.clear();
        this.m_hashNeuAc.clear();
        this.m_hashGal.clear();
        this.m_hashGalNAc.clear();
        this.m_hashFuc.clear();
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_visitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserNodes(a_visitor);
    }

    public void visit(NonMonosaccharide arg0) throws GlycoVisitorException 
    {
        // nothing to do
    }

    public void visit(SugarUnitCyclic arg0) throws GlycoVisitorException 
    {
        // nothing to do	
    }

    public void visit(SugarUnitAlternative arg0) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("SugarUnitAlternative is not supported.");		
    }

    public void visit(UnvalidatedGlycoNode arg0) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode is not supported.");		
    }

    public void visit(GlycoEdge arg0) throws GlycoVisitorException 
    {
        // nothing to do
    }

    public void start(Sugar a_sugar) throws GlycoVisitorException 
    {
        this.clear();
        GlycoTraverser t_traverser = this.getTraverser(this);
        t_traverser.traverseGraph(a_sugar);
        for (UnderdeterminedSubTree t_tree : a_sugar.getUndeterminedSubTrees()) 
        {
            t_traverser.traverseGraph(t_tree);
        }
        this.findMotifs();
    }

    private void findMotifs()
    {
        // core fuc and non core fuc
        this.findFucStuff();
        // Gal-Gal  & poly Sia
        this.findPolymers();
        // SDA
        this.findSDA();
        // Lewis stuff and LacNAc
        this.findLewis();
        // LacDiNAc stuff and Sia-HexNAc
        this.findLacDiNAc();
    }

    private void findLacDiNAc()
    {
        this.m_hashUsedResidue.clear();
        // Sia-LacDiNAC && Sia-HexNAc
        for (GlycoNode t_sia : this.m_hashNeuAc.keySet())
        {
            GlycoNode t_galNAc = this.findParent(t_sia, this.m_hashGalNAc, false);
            if ( t_galNAc != null )
            {
                GlycoNode t_glcNAc = this.findParent(t_galNAc, this.m_hashGlcNac, false);
                if ( t_glcNAc != null )
                {
                    this.m_siaLacDiNac++;
                    this.m_hashUsedResidue.put(t_galNAc, Boolean.TRUE);
                }
                else
                {
                    this.m_siaHexNAc++;
                }
            }
            else
            {
                GlycoNode t_glcNAc = this.findParent(t_sia, this.m_hashGlcNac, false);
                if ( t_glcNAc != null )
                {
                    this.m_siaHexNAc++;
                }
            }
        }
        // di-Fuc LacDiNAc
        for (GlycoNode t_galNAc : this.m_hashGalNAc.keySet())
        {
            if ( this.m_hashUsedResidue.get(t_galNAc) == null )
            {
                GlycoNode t_glcNAc = this.findParent(t_galNAc, this.m_hashGlcNac, false);
                if ( t_glcNAc != null )
                {
                    GlycoNode t_fucGlcNAc = this.findChild(t_glcNAc, this.m_hashFuc, false);
                    GlycoNode t_fucGalNAc = this.findChild(t_galNAc, this.m_hashFuc, false);
                    if ( t_fucGlcNAc != null && t_fucGalNAc != null )
                    {
                        this.m_diFucLacDiNac++;
                    }
                    else if ( t_fucGlcNAc != null || t_fucGalNAc != null )
                    {
                        this.m_monoFucLacDiNac++;
                    }
                    else
                    {
                        this.m_lacDiNac++;
                    }
                }
            }
        }
        this.m_hashUsedResidue.clear();
    }

    private void findLewis()
    {
        this.m_hashUsedResidue.clear();
        // Sial Lewis 
        for (GlycoNode t_sia : this.m_hashNeuAc.keySet())
        {
            GlycoNode t_gal = this.findParent(t_sia, this.m_hashGal, false);
            if ( t_gal != null )
            {
                GlycoNode t_glcNAc = this.findParent(t_gal, this.m_hashGlcNac,false);
                if ( t_glcNAc != null )
                {
                    GlycoNode t_fuc = this.findChild(t_glcNAc, this.m_hashFuc, false);
                    if ( t_fuc != null )
                    {
                        this.m_siaLewis++;
                        this.m_hashUsedResidue.put(t_gal, Boolean.TRUE);
                    }
                }
            }
        }
        // Lewis BY && Lewis AX && LacNAc
        for ( GlycoNode t_gal : this.m_hashGal.keySet() )
        {
            GlycoNode t_glcNAc = this.findParent(t_gal, this.m_hashGlcNac,false);
            if ( t_glcNAc != null )
            {
                GlycoNode t_fucGlcNAc = this.findChild(t_glcNAc, this.m_hashFuc, false);
                GlycoNode t_fucGal = this.findChild(t_gal, this.m_hashFuc, false);
                this.m_hashUsedResidue.put(t_gal, Boolean.TRUE);
                if ( t_fucGlcNAc != null && t_fucGal != null )
                {
                    this.m_lewisBY++;
                }
                else if ( t_fucGlcNAc != null )
                {
                    this.m_lewisAX++;
                }
                else
                {
                    this.m_lacNac++;
                }
            }
        }
        this.m_hashUsedResidue.clear();
    }

    private void findSDA()
    {
        this.m_hashUsedResidue.clear();
        for (GlycoNode t_ms : this.m_hashNeuAc.keySet())
        {
            if ( t_ms.getChildEdges().size() == 0 )
            {
                GlycoNode t_gal = this.findParent(t_ms, this.m_hashGal, false);
                GlycoNode t_galNAc = this.findSibling(t_ms, this.m_hashGalNAc);
                if ( t_gal != null && t_galNAc != null )
                {
                    GlycoNode t_parent = this.findParent(t_gal, this.m_hashGlcNac, false);
                    if ( t_parent != null )
                    {
                        t_parent = this.findParent(t_parent, this.m_hashGal, false);
                        if ( t_parent != null )
                        {
                            this.m_sda++;
                            this.m_hashUsedResidue.put(t_gal, Boolean.TRUE);
                            this.m_hashUsedResidue.put(t_parent, Boolean.TRUE);
                        }
                    }
                }
            }
        }
        this.m_hashUsedResidue.clear();
    }

    private void findPolymers()
    {
        this.m_hashUsedResidue.clear();
        // gal-gal
        for (GlycoNode t_gal : this.m_hashGal.keySet())
        {
            if ( t_gal.getChildEdges().size() == 0 && this.m_hashUsedResidue.get(t_gal) == null )
            {
                GlycoNode t_parentGal = this.findParent(t_gal, this.m_hashGal, false);
                if ( t_parentGal != null )
                {
                    this.m_galGal++;
                    this.m_hashUsedResidue.put(t_gal, Boolean.TRUE);
                    this.m_hashUsedResidue.put(t_parentGal, Boolean.TRUE);
                }
            }
        }
        // poly-sia
        for (GlycoNode t_sia : this.m_hashNeuAc.keySet())
        {
            if ( t_sia.getChildEdges().size() == 0 && this.m_hashUsedResidue.get(t_sia) == null )
            {
                GlycoNode t_parentSia = this.findParent(t_sia, this.m_hashNeuAc, false);
                if ( t_parentSia != null )
                {
                    this.m_polySia++;
                    this.m_hashUsedResidue.put(t_sia, Boolean.TRUE);
                    this.m_hashUsedResidue.put(t_parentSia, Boolean.TRUE);
                }
            }
        }
        this.m_hashUsedResidue.clear();
    }

    private void findFucStuff()
    {
        this.m_hashUsedResidue.clear();
        // core Fuc
        for (GlycoNode t_ms : this.m_hashFuc.keySet())
        {
            GlycoNode t_glcNAc = this.findParent(t_ms, this.m_hashGlcNac, false);
            if ( t_glcNAc != null )
            {
                if ( t_glcNAc.getParentEdge() == null )
                {
                    this.m_coreFuc++;
                }
            }
        }
        // non core Fuc
        this.m_nonCoreFuc = this.m_hashFuc.keySet().size() - this.m_coreFuc;
    }

    public void visit(SugarUnitRepeat a_repeat) throws GlycoVisitorException 
    {
        GlycoTraverser t_traverser = this.getTraverser(this);
        t_traverser.traverseGraph(a_repeat);
        for (UnderdeterminedSubTree t_tree : a_repeat.getUndeterminedSubTrees()) 
        {
            t_traverser.traverseGraph(t_tree);
        }	
    }

    public void visit(Substituent a_subst) throws GlycoVisitorException 
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
        }
        else if ( a_ms.getSuperclass().equals(Superclass.NON) )
        {
            for (GlycoEdge t_edge : a_ms.getChildEdges()) 
            {
                if ( this.isPosition(5,t_edge) && this.isLinkageType(LinkageType.DEOXY,t_edge) && this.isSubst(SubstituentType.N_ACETYL,t_edge.getChild()) )
                {
                    if ( t_numberOfSubstituents == 1 )
                    {
                        this.m_hashNeuAc.put(a_ms,Boolean.TRUE);
                        return;
                    }
                }
            }
        }
    }

    public int getCoreFuc()
    {
        return this.m_coreFuc;
    }

    public void setCoreFuc(int a_coreFuc)
    {
        this.m_coreFuc = a_coreFuc;
    }

    public int getPolySia()
    {
        return this.m_polySia;
    }

    public void setPolySia(int a_polySia)
    {
        this.m_polySia = a_polySia;
    }

    public int getLacNac()
    {
        return this.m_lacNac;
    }

    public void setLacNac(int a_lacNac)
    {
        this.m_lacNac = a_lacNac;
    }

    public int getLacDiNac()
    {
        return this.m_lacDiNac;
    }

    public void setLacDiNac(int a_lacDiNac)
    {
        this.m_lacDiNac = a_lacDiNac;
    }

    public int getNonCoreFuc()
    {
        return m_nonCoreFuc;
    }

    public void setNonCoreFuc(int a_nonCoreFuc)
    {
        m_nonCoreFuc = a_nonCoreFuc;
    }

    public int getLewisAX()
    {
        return m_lewisAX;
    }

    public void setLewisAX(int a_lewisAX)
    {
        m_lewisAX = a_lewisAX;
    }

    public int getLewisBY()
    {
        return m_lewisBY;
    }

    public void setLewisBY(int a_lewisBY)
    {
        m_lewisBY = a_lewisBY;
    }

    public int getSiaLewis()
    {
        return m_siaLewis;
    }

    public void setSiaLewis(int a_siaLewis)
    {
        m_siaLewis = a_siaLewis;
    }

    public int getSda()
    {
        return m_sda;
    }

    public void setSda(int a_sda)
    {
        m_sda = a_sda;
    }

    public int getSiaHexNAc()
    {
        return m_siaHexNAc;
    }

    public void setSiaHexNAc(int a_siaHexNAc)
    {
        m_siaHexNAc = a_siaHexNAc;
    }

    public int getSiaLacDiNac()
    {
        return m_siaLacDiNac;
    }

    public void setSiaLacDiNac(int a_siaLacDiNac)
    {
        m_siaLacDiNac = a_siaLacDiNac;
    }

    public int getMonoFucLacDiNac()
    {
        return m_monoFucLacDiNac;
    }

    public void setMonoFucLacDiNac(int a_monoFucLacDiNac)
    {
        m_monoFucLacDiNac = a_monoFucLacDiNac;
    }

    public int getDiFucLacDiNac()
    {
        return m_diFucLacDiNac;
    }

    public void setDiFucLacDiNac(int a_diFucLacDiNac)
    {
        m_diFucLacDiNac = a_diFucLacDiNac;
    }
}
