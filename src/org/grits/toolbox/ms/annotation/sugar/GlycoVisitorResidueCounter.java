package org.grits.toolbox.ms.annotation.sugar;

import java.util.HashMap;
import java.util.List;

import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
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
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public class GlycoVisitorResidueCounter implements GlycoVisitor 
{
    private HashMap<GlycoNode, Boolean> m_hUsedResidue = new HashMap<GlycoNode, Boolean>();
    private HashMap<Monosaccharide, Boolean> m_hashGlcNac = new HashMap<Monosaccharide, Boolean>();
    private HashMap<Monosaccharide, Boolean> m_hashSia = new HashMap<Monosaccharide, Boolean>();
    private HashMap<Monosaccharide, Boolean> m_hashGal = new HashMap<Monosaccharide, Boolean>();
    private int m_hex = 0;
    private int m_hexNAc = 0;
    private int m_hex6D = 0;
    private int m_neuAc = 0;
    private int m_sulfate = 0;
    private int m_neuGc = 0;
    private int m_phosphate = 0;
    private int m_hexA = 0;
    private int m_other = 0;

    public void clear() 
    {
        this.m_hex = 0;
        this.m_hexNAc = 0;
        this.m_hex6D = 0;
        this.m_neuAc = 0;
        this.m_sulfate = 0;
        this.m_neuGc = 0;
        this.m_phosphate = 0;
        this.m_hexA = 0;
        this.m_other = 0;
        this.m_hUsedResidue.clear();
        this.m_hashGlcNac.clear();
        this.m_hashSia.clear();
        this.m_hashGal.clear();
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_visitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserNodes(a_visitor);
    }

    public void visit(NonMonosaccharide arg0) throws GlycoVisitorException 
    {
        this.m_other++;		
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
        if ( this.m_hUsedResidue.get(a_subst) == null )
        {
            // not used before
            if ( a_subst.getSubstituentType().equals(SubstituentType.SULFATE) )
            {
                this.m_sulfate++;
                return;
            }
            if ( a_subst.getSubstituentType().equals(SubstituentType.PHOSPHATE) )
            {
                this.m_phosphate++;
                return;
            }
            this.m_other++;
        }
    }

    public void visit(Monosaccharide a_ms) throws GlycoVisitorException 
    {
        if ( a_ms.getSuperclass().equals(Superclass.HEX) )
        {
            for (Modification t_modi : a_ms.getModification()) 
            {
                if ( this.isModiPosition(6,t_modi.getPositionOne()) && t_modi.getModificationType().equals(ModificationType.DEOXY) )
                {
                    if ( a_ms.getModification().size() == 1 )
                    {
                        this.m_hex6D++;
                        return;
                    }
                }
                else if ( this.isModiPosition(6,t_modi.getPositionOne()) && t_modi.getModificationType().equals(ModificationType.ACID) )
                {
                    if ( a_ms.getModification().size() == 1 )
                    {
                        this.m_hexA++;
                        return;
                    }
                }
            }
            if (a_ms.getModification().size() != 0 )
            {
                this.m_other++;
                return;
            }
            for (GlycoEdge t_edge : a_ms.getChildEdges()) 
            {
                if ( this.isPosition(2,t_edge) && this.isSubst(SubstituentType.N_ACETYL,t_edge.getChild()) && this.isLinkageType(LinkageType.DEOXY,t_edge) )
                {
                    this.m_hUsedResidue.put(t_edge.getChild(), Boolean.TRUE);
                    this.m_hexNAc++;
                    if ( this.isGlucose(a_ms) )
                    {
                        this.m_hashGlcNac.put(a_ms,Boolean.TRUE);
                    }
                    return;
                }
            }
            this.m_hex++;
            if ( this.isGalactose(a_ms) )
            {
                this.m_hashGal.put(a_ms, Boolean.TRUE);
            }
            return;
        }
        else if ( a_ms.getSuperclass().equals(Superclass.NON) )
        {
            for (GlycoEdge t_edge : a_ms.getChildEdges()) 
            {
                if ( this.isPosition(5,t_edge) && this.isLinkageType(LinkageType.DEOXY,t_edge) && this.isSubst(SubstituentType.N_ACETYL,t_edge.getChild()) )
                {
                    this.m_hUsedResidue.put(t_edge.getChild(), Boolean.TRUE);
                    this.m_neuAc++;
                    this.m_hashSia.put(a_ms,Boolean.TRUE);
                    return;
                }
                if ( this.isPosition(5,t_edge) && this.isLinkageType(LinkageType.DEOXY,t_edge) && this.isSubst(SubstituentType.N_GLYCOLYL,t_edge.getChild()) )
                {
                    this.m_hUsedResidue.put(t_edge.getChild(), Boolean.TRUE);
                    this.m_neuGc++;
                    this.m_hashSia.put(a_ms,Boolean.TRUE);
                    return;
                }
            }			
        }
        this.m_other++;
    }

    private boolean isGlucose(Monosaccharide a_ms)
    {
        List<BaseType> t_base = a_ms.getBaseType();
        if ( t_base.size() != 1 )
        {
            return false;
        }
        for (BaseType t_baseType : t_base)
        {
            if ( t_baseType.equals(BaseType.DGLC) || t_baseType.equals(BaseType.LGLC) || t_baseType.equals(BaseType.XGLC) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean isGalactose(Monosaccharide a_ms)
    {
        List<BaseType> t_base = a_ms.getBaseType();
        if ( t_base.size() != 1 )
        {
            return false;
        }
        for (BaseType t_baseType : t_base)
        {
            if ( t_baseType.equals(BaseType.DGAL) || t_baseType.equals(BaseType.LGAL) || t_baseType.equals(BaseType.XGAL) )
            {
                return true;
            }
        }
        return false;
    }


    private boolean isLinkageType(LinkageType a_soll, GlycoEdge a_edge) 
    {
        if ( a_edge.getGlycosidicLinkages().size() != 1 )
        {
            return false;
        }
        for (Linkage t_linkage : a_edge.getGlycosidicLinkages()) 
        {
            if ( t_linkage.getParentLinkageType().equals(a_soll) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean isSubst(SubstituentType a_soll, GlycoNode a_nodeChild) throws GlycoVisitorException 
    {
        GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
        Substituent t_subst = t_visNodeType.getSubstituent(a_nodeChild);
        if ( t_subst == null )
        {
            return false;
        }
        if ( t_subst.getSubstituentType().equals(a_soll))
        {
            return true;
        }
        return false;
    }

    private boolean isPosition(int a_iSollPosition, GlycoEdge a_edge) 
    {
        if ( a_edge.getGlycosidicLinkages().size() != 1 )
        {
            return false;
        }
        for (Linkage t_linkage : a_edge.getGlycosidicLinkages()) 
        {
            for (Integer t_iPos : t_linkage.getParentLinkages()) 
            {
                if ( t_iPos.equals(a_iSollPosition) )
                {
                    return true;
                }
                if ( t_iPos.equals(Linkage.UNKNOWN_POSITION) )
                {
                    return true;
                }
            } 
        }
        return false;
    }

    private boolean isModiPosition(int a_iSollPosition, int a_iIstPosition) 
    {
        if ( a_iSollPosition == a_iIstPosition )
        {
            return true;
        }
        if ( a_iIstPosition == Modification.UNKNOWN_POSITION )
        {
            return true;
        }
        return false;
    }

    public HashMap<GlycoNode, Boolean> gethUsedResidue()
    {
        return this.m_hUsedResidue;
    }

    public void sethUsedResidue(HashMap<GlycoNode, Boolean> a_hUsedResidue)
    {
        this.m_hUsedResidue = a_hUsedResidue;
    }

    public int getHex()
    {
        return this.m_hex;
    }

    public void setHex(int a_hex)
    {
        this.m_hex = a_hex;
    }

    public int getHexNAc()
    {
        return this.m_hexNAc;
    }

    public void setHexNAc(int a_hexNAc)
    {
        this.m_hexNAc = a_hexNAc;
    }

    public int getHex6D()
    {
        return this.m_hex6D;
    }

    public void setHex6D(int a_hex6d)
    {
        this.m_hex6D = a_hex6d;
    }

    public int getNeuAc()
    {
        return this.m_neuAc;
    }

    public void setNeuAc(int a_neuAc)
    {
        this.m_neuAc = a_neuAc;
    }

    public int getSulfate()
    {
        return this.m_sulfate;
    }

    public void setSulfate(int a_sulfate)
    {
        this.m_sulfate = a_sulfate;
    }

    public int getNeuGc()
    {
        return this.m_neuGc;
    }

    public void setNeuGc(int a_neuGc)
    {
        this.m_neuGc = a_neuGc;
    }

    public int getPhosphate()
    {
        return this.m_phosphate;
    }

    public void setPhosphate(int a_phosphate)
    {
        this.m_phosphate = a_phosphate;
    }

    public int getHexA()
    {
        return this.m_hexA;
    }

    public void setHexA(int a_hexA)
    {
        this.m_hexA = a_hexA;
    }

    public int getOther()
    {
        return this.m_other;
    }

    public void setOther(int a_other)
    {
        this.m_other = a_other;
    }
}
