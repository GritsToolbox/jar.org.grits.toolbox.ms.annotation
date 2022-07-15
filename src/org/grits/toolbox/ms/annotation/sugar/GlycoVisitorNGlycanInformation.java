package org.grits.toolbox.ms.annotation.sugar;

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
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public class GlycoVisitorNGlycanInformation implements GlycoVisitor
{
    public static String UNKNOWN = "unknown";
    public static String HYBRID = "hybrid";
    public static String HIGH_MANNOSE = "high mannose";
    public static String COMPLEX = "complex";

    private Monosaccharide m_G1 = null;
    private Monosaccharide m_G2 = null;
    private Monosaccharide m_M1 = null;
    private Monosaccharide m_M3 = null;
    private Monosaccharide m_M6 = null;
    private boolean m_bisection = false;
    private int m_m3GlcNac = 0;
    private int m_m3Man = 0;
    private int m_m6Man = 0;
    private int m_m6GlcNac = 0;

    @Override
    public void clear()
    {
        this.m_G1 = null;
        this.m_G2 = null;
        this.m_M1 = null;
        this.m_M6 = null;
        this.m_M3 = null;
        this.m_m3GlcNac = 0;
        this.m_m3Man = 0;
        this.m_m6Man = 0;
        this.m_m6GlcNac = 0;
        this.m_bisection = false;
    }

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor a_visitor) throws GlycoVisitorException
    {
        return new GlycoTraverserTreeSingle(a_visitor);
    }

    @Override
    public void start(Sugar a_sugar) throws GlycoVisitorException
    {
        this.clear();
        GlycoTraverser t_traverser = this.getTraverser(this);
        t_traverser.traverseGraph(a_sugar);
    }

    @Override
    public void visit(NonMonosaccharide a_nonMs) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("NonMonosaccharide is not supported.");
    }

    @Override
    public void visit(SugarUnitRepeat a_repeat) throws GlycoVisitorException
    {
        // nothing to do
    }

    @Override
    public void visit(Substituent a_subst) throws GlycoVisitorException
    {
        // nothing to do
    }

    @Override
    public void visit(SugarUnitCyclic a_cyclic) throws GlycoVisitorException
    {
        // nothing to do
    }

    @Override
    public void visit(SugarUnitAlternative a_alternative) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("SugarUnitAlternative is not supported.");
    }

    @Override
    public void visit(UnvalidatedGlycoNode a_unvalidated) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode is not supported.");
    }

    @Override
    public void visit(GlycoEdge a_edge) throws GlycoVisitorException
    {
        // nothing to do
    }

    @Override
    public void visit(Monosaccharide a_ms) throws GlycoVisitorException
    {
        if (a_ms.getSuperclass().equals(Superclass.HEX))
        {
            for (Modification t_modi : a_ms.getModification())
            {
                if (!t_modi.getModificationType().equals(ModificationType.ALDI))
                {
                    return;
                }
            }
            if (this.isGlucose(a_ms))
            {
                for (GlycoEdge t_edge : a_ms.getChildEdges())
                {
                    if (this.isPosition(2, t_edge) && this.isSubst(SubstituentType.N_ACETYL, t_edge.getChild())
                            && this.isLinkageType(LinkageType.DEOXY, t_edge))
                    {
                        GlycoNode t_node = a_ms.getParentNode();
                        // so its a glcnac
                        if (t_node == null)
                        {
                            this.m_G1 = a_ms;
                            return;
                        }
                        if (t_node == this.m_G1)
                        {
                            this.m_G2 = a_ms;
                            return;
                        }
                        if (t_node == this.m_M1)
                        {
                            this.setBisection(true);
                            return;
                        }
                        if (t_node == this.m_M3)
                        {
                            this.m_m3GlcNac++;
                            return;
                        }
                        if (t_node == this.m_M6)
                        {
                            this.m_m6GlcNac++;
                            return;
                        }
                    }
                }
                return;
            }
            if (this.isMannose(a_ms))
            {
                GlycoNode t_node = a_ms.getParentNode();
                // so its a glcnac
                if (t_node == null)
                {
                    return;
                }
                if (t_node == this.m_G2)
                {
                    this.m_M1 = a_ms;
                }
                if (t_node == this.m_M1)
                {
                    if (this.isPosition(3, a_ms.getParentEdge()))
                    {
                        if (this.m_M3 != null)
                        {
                            // probably set from the unknown linkage => copy to
                            // M6 if possible
                            if (this.m_M6 == null)
                            {
                                this.m_M6 = this.m_M3;
                            }
                        }
                        this.m_M3 = a_ms;
                    }
                    if (this.isPosition(6, a_ms.getParentEdge()))
                    {
                        if (this.m_M6 != null)
                        {
                            // probably set from the unknown linkage => copy to
                            // M3 if possible
                            if (this.m_M3 == null)
                            {
                                this.m_M3 = this.m_M6;
                            }
                        }
                        this.m_M6 = a_ms;
                    }
                    if (this.isPosition(Linkage.UNKNOWN_POSITION, a_ms.getParentEdge()))
                    {
                        if (this.m_M3 == null)
                        {
                            this.m_M3 = a_ms;
                        }
                        else if (this.m_M6 == null)
                        {
                            this.m_M6 = a_ms;
                        }
                    }
                }
                if (t_node == this.m_M3)
                {
                    this.m_m3Man++;
                }
                if (t_node == this.m_M6)
                {
                    this.m_m6Man++;
                }
            }
        }
    }

    private boolean isGlucose(Monosaccharide a_ms)
    {
        List<BaseType> t_base = a_ms.getBaseType();
        if (t_base.size() != 1)
        {
            return false;
        }
        for (BaseType t_baseType : t_base)
        {
            if (t_baseType.equals(BaseType.DGLC) || t_baseType.equals(BaseType.LGLC)
                    || t_baseType.equals(BaseType.XGLC))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isMannose(Monosaccharide a_ms)
    {
        List<BaseType> t_base = a_ms.getBaseType();
        if (t_base.size() != 1)
        {
            return false;
        }
        for (BaseType t_baseType : t_base)
        {
            if (t_baseType.equals(BaseType.DMAN) || t_baseType.equals(BaseType.LMAN)
                    || t_baseType.equals(BaseType.XMAN))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isPosition(int a_iSollPosition, GlycoEdge a_edge)
    {
        if (a_edge.getGlycosidicLinkages().size() != 1)
        {
            return false;
        }
        for (Linkage t_linkage : a_edge.getGlycosidicLinkages())
        {
            if (t_linkage.getParentLinkages().size() != 1)
            {
                return false;
            }
            for (Integer t_iPos : t_linkage.getParentLinkages())
            {
                if (t_iPos.equals(a_iSollPosition))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSubst(SubstituentType a_soll, GlycoNode a_nodeChild) throws GlycoVisitorException
    {
        GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
        Substituent t_subst = t_visNodeType.getSubstituent(a_nodeChild);
        if (t_subst == null)
        {
            return false;
        }
        if (t_subst.getSubstituentType().equals(a_soll))
        {
            return true;
        }
        return false;
    }

    private boolean isLinkageType(LinkageType a_soll, GlycoEdge a_edge)
    {
        if (a_edge.getGlycosidicLinkages().size() != 1)
        {
            return false;
        }
        for (Linkage t_linkage : a_edge.getGlycosidicLinkages())
        {
            if (t_linkage.getParentLinkageType().equals(a_soll))
            {
                return true;
            }
        }
        return false;
    }

    public void setBisection(boolean bisection)
    {
        this.m_bisection = bisection;
    }

    public boolean isNGlycan()
    {
        if (this.m_M3 == null || this.m_M6 == null)
        {
            return false;
        }
        return true;
    }

    public boolean getBisection()
    {
        return m_bisection;
    }

    public int getNGlycanBranches()
    {
        int t_branches = this.m_m3GlcNac + this.m_m3Man + this.m_m6GlcNac + this.m_m6Man;
        if (this.m_bisection)
        {
            t_branches++;
        }
        return t_branches;
    }

}