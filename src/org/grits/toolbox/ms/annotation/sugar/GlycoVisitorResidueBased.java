package org.grits.toolbox.ms.annotation.sugar;

import java.util.HashMap;
import java.util.List;

import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public abstract class GlycoVisitorResidueBased implements GlycoVisitor
{
    protected HashMap<GlycoNode, Boolean> m_hashUsedResidue = new HashMap<GlycoNode, Boolean>();

    protected boolean isSubst(SubstituentType a_soll, GlycoNode a_nodeChild) throws GlycoVisitorException 
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

    protected boolean isPosition(int a_iSollPosition, GlycoEdge a_edge) 
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

    protected boolean isModiPosition(int a_iSollPosition, int a_iIstPosition) 
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

    protected boolean isGlucose(Monosaccharide a_ms)
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

    protected boolean isGalactose(Monosaccharide a_ms)
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

    protected boolean isMannose(Monosaccharide a_ms)
    {
        List<BaseType> t_base = a_ms.getBaseType();
        if ( t_base.size() != 1 )
        {
            return false;
        }
        for (BaseType t_baseType : t_base)
        {
            if ( t_baseType.equals(BaseType.DMAN) || t_baseType.equals(BaseType.LMAN) || t_baseType.equals(BaseType.XMAN) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isLinkageType(LinkageType a_soll, GlycoEdge a_edge) 
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

    protected int calcNumberOfSubstituents(Monosaccharide a_ms) throws GlycoVisitorException
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

    protected GlycoNode findSibling(GlycoNode a_ms, HashMap<GlycoNode, Boolean> a_mapResidues)
    {
        GlycoNode t_parent = a_ms.getParentNode();
        if ( t_parent == null )
        {
            return null;
        }
        for (GlycoNode t_child : t_parent.getChildNodes())
        {
            if ( t_child != a_ms )
            {
                if ( this.m_hashUsedResidue.get(t_parent) == null )
                {
                    if ( a_mapResidues.get(t_child) != null )
                    {
                        return t_child;
                    }
                }
            }
        }
        return null;
    }

    protected GlycoNode findChild(GlycoNode a_parent, HashMap<GlycoNode, Boolean> a_mapResidues, boolean a_ignoreUsed)
    {
        if ( a_parent == null )
        {
            return null;
        }
        for (GlycoNode t_child : a_parent.getChildNodes())
        {
            if ( !a_ignoreUsed )
            {
                if ( this.m_hashUsedResidue.get(t_child) == null )
                {
                    if ( a_mapResidues.get(t_child) != null )
                    {
                        return t_child;
                    }
                }
            }
            else
            {
                if ( a_mapResidues.get(t_child) != null )
                {
                    return t_child;
                }
            }
        }
        return null;
    }

    protected GlycoNode findParent(GlycoNode a_ms, HashMap<GlycoNode, Boolean> a_mapResidues, boolean a_ignoreUsed)
    {
        GlycoNode t_parent = a_ms.getParentNode();
        if ( t_parent == null )
        {
            return null;
        }
        if ( !a_ignoreUsed )
        {
            if ( this.m_hashUsedResidue.get(t_parent) != null )
            {
                return null;
            }
        }
        if ( a_mapResidues.get(t_parent) != null )
        {
            return t_parent;
        }
        return null;
    }

    protected boolean testLinkage(GlycoEdge a_edge, int a_position)
    {
        for (Linkage t_linkage : a_edge.getGlycosidicLinkages())
        {
            for (Integer t_position : t_linkage.getParentLinkages())
            {
                if ( t_position.equals(a_position) )
                {
                    return true;
                }
                if ( t_position.equals(Linkage.UNKNOWN_POSITION) )
                {
                    return true;
                }
            }
        }
        return false;
    }
}
