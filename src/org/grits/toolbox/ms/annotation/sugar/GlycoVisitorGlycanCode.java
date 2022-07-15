package org.grits.toolbox.ms.annotation.sugar;

import java.util.HashMap;

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
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public class GlycoVisitorGlycanCode implements GlycoVisitor 
{
    private HashMap<GlycoNode, Boolean> m_hUsedResidue = new HashMap<GlycoNode, Boolean>();
    private Integer m_iHex = 0;
    private Integer m_iHexA = 0;
    private Integer m_iHexNAc = 0;
    private Integer m_i6dHex = 0;
    private Integer m_iNeuAc = 0;
    private Integer m_iNeuGc = 0;
    private Integer m_iKDN = 0;
    private Integer m_iS = 0;
    private Integer m_iOther = 0;

    public void clear() 
    {
        this.m_iHex = 0;
        this.m_iHexA = 0;
        this.m_iHexNAc = 0;
        this.m_i6dHex = 0;
        this.m_iNeuAc = 0;
        this.m_iNeuGc = 0;
        this.m_iKDN = 0;
        this.m_iS = 0;
        this.m_iOther = 0;
        this.m_hUsedResidue.clear();
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_visitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserTreeSingle(a_visitor);
    }

    public void visit(NonMonosaccharide arg0) throws GlycoVisitorException 
    {
        this.m_iOther++;		
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
                this.m_iS++;
            }
            else
            {
                this.m_iOther++;
            }
        }
    }

    public void visit(Monosaccharide a_ms) throws GlycoVisitorException 
    {
        int t_numberSubst = this.calcNumberSubst(a_ms);
        if ( a_ms.getSuperclass().equals(Superclass.HEX) )
        {
            for (Modification t_modi : a_ms.getModification()) 
            {
                if ( this.isModiPosition(6,t_modi.getPositionOne()) && t_modi.getModificationType().equals(ModificationType.DEOXY) )
                {
                    if ( a_ms.getModification().size() == 1 && t_numberSubst == 0 )
                    {
                        this.m_i6dHex++;
                        return;
                    }
                }
                if ( this.isModiPosition(6,t_modi.getPositionOne()) && t_modi.getModificationType().equals(ModificationType.ACID) )
                {
                    if ( a_ms.getModification().size() == 1 && t_numberSubst == 0 )
                    {
                        this.m_iHexA++;
                        return;
                    }
                }
            }
            if (a_ms.getModification().size() != 0 )
            {
                this.m_iOther++;
                return;
            }
            for (GlycoEdge t_edge : a_ms.getChildEdges()) 
            {
                if ( this.isPosition(2,t_edge) && this.isSubst(SubstituentType.N_ACETYL,t_edge.getChild()) && this.isLinkageType(LinkageType.DEOXY,t_edge) )
                {
                    if ( t_numberSubst == 1 )
                    {
                        this.m_hUsedResidue.put(t_edge.getChild(), Boolean.TRUE);
                        this.m_iHexNAc++;
                        return;
                    }
                }
            }
            if ( t_numberSubst == 0 )
            {
                this.m_iHex++;
                return;
            }
        }
        else if ( a_ms.getSuperclass().equals(Superclass.NON) )
        {
            for (GlycoEdge t_edge : a_ms.getChildEdges()) 
            {
                if ( this.isPosition(5,t_edge) && this.isLinkageType(LinkageType.DEOXY,t_edge) && t_numberSubst == 1)
                {
                    if ( this.isSubst(SubstituentType.N_ACETYL,t_edge.getChild()) )
                    {
                        this.m_hUsedResidue.put(t_edge.getChild(), Boolean.TRUE);
                        this.m_iNeuAc++;
                        return;
                    }
                    if ( this.isSubst(SubstituentType.N_GLYCOLYL,t_edge.getChild() ))
                    {
                        this.m_hUsedResidue.put(t_edge.getChild(), Boolean.TRUE);
                        this.m_iNeuGc++;
                        return;
                    }
                }
            }
            if ( t_numberSubst == 0 )
            {
                this.m_iKDN++;
                return;
            }
        }
        this.m_iOther++;
    }

    private int calcNumberSubst(Monosaccharide a_ms) throws GlycoVisitorException
    {
        int t_counter = 0;
        GlycoVisitorNodeType t_visitorType = new GlycoVisitorNodeType();
        for (GlycoNode t_node : a_ms.getChildNodes()) 
        {
            if ( t_visitorType.isSubstituent(t_node) )
            {
                t_counter++;
            }
        }
        return t_counter;
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

    public String getCode() 
    {
        // #Hex #HexNAc # deoxHex #NeuAc #NeuGC #KDn #HexA #S #other
        return String.format("%d.%d.%d.%d.%d.%d.%d.%d.%d", this.m_iHex, this.m_iHexNAc, this.m_i6dHex, 
                this.m_iNeuAc, this.m_iNeuGc, this.m_iKDN, this.m_iHexA, this.m_iS, this.m_iOther);
    }
    
    public String getCompositionString()
    {
        // #Hex #HexNAc # deoxHex #HexA #Sia(Ac, Gc,Kdn) #other
        StringBuffer t_buffer = new StringBuffer("");
        if ( this.m_iHex > 0 )
        {
            t_buffer.append(this.m_iHex.toString() + "Hex");
        }
        if ( this.m_iHexNAc > 0 )
        {
            t_buffer.append(this.m_iHexNAc.toString() + "HexNAc");
        }
        if ( this.m_i6dHex > 0 )
        {
            t_buffer.append(this.m_i6dHex.toString() + "dHex");
        }
        if ( this.m_iHexA > 0 )
        {
            t_buffer.append(this.m_iHexA.toString() + "HexA");
        }
        Integer t_sia = this.m_iKDN + this.m_iNeuAc + this.m_iNeuGc;
        if ( t_sia > 0 )
        {
            t_buffer.append(t_sia.toString() + "Sia(Ac, Gc,Kdn)");
        }
        if ( this.m_iOther > 0 )
        {
            t_buffer.append(" + " + this.m_iOther + " other");
        }
        return t_buffer.toString();
    }
}
