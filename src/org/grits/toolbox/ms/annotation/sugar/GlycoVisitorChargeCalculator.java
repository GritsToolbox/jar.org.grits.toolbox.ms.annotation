package org.grits.toolbox.ms.annotation.sugar;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
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
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserSimple;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class GlycoVisitorChargeCalculator implements GlycoVisitor 
{
    /**
     * Charge calculator for Glycans. Repeat Units calcultated once.
     * Acidic functions: -1
     * Phoshate (in chain): -1
     * Phospate (terminal): -2
     * Sulfate	(in chain): 0
     * Sulfate 	(terminal): -1
     * Pyruvate: -1
     * N-succinate:	-1
     * n-sulfate: -1
     * Amino (free)		  :	+1
     * N-ala : +1
     **/

    private Integer m_charge = 0;

    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException 
    {
        for (Modification t_modification : a_objMonosaccharid.getModification())
        {
            if ( t_modification.getModificationType().equals(ModificationType.ACID) )
            {
                this.m_charge--;
            }
        }
    }

    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("NonMonosaccharide is not supported.");        
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

    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.SULFATE))
        {
            if ( a_objSubstituent.getChildEdges().size() == 0 )
            {
                this.m_charge--;
            }
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.PHOSPHATE))
        {
            this.m_charge--;
            if ( a_objSubstituent.getChildEdges().size() == 0 )
            {
                this.m_charge--;
            }
        }

        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.PYROPHOSPHATE))
        {
            this.m_charge--;
            this.m_charge--;
            if ( a_objSubstituent.getChildEdges().size() == 0 )
            {
                this.m_charge--;
            }
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.TRIPHOSPHATE))
        {
            this.m_charge--;
            this.m_charge--;
            this.m_charge--;
            if ( a_objSubstituent.getChildEdges().size() == 0 )
            {
                this.m_charge--;
            }
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.AMINO))
        {
            m_charge++;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.AMIDINO))
        {
            m_charge++;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.ETHANOLAMINE))
        {
            m_charge++;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.IMINO))
        {
            m_charge++;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.N_ALANINE))
        {
            m_charge++;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.N_SULFATE))
        {
            m_charge--;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.R_CARBOXYETHYL))
        {
            m_charge--;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.S_CARBOXYETHYL))
        {
            m_charge--;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.R_CARBOXYMETHYL))
        {
            m_charge--;	    		   
        }
        if (a_objSubstituent.getSubstituentType().equals(SubstituentType.S_CARBOXYMETHYL))
        {
            m_charge--;	    		   
        }
        if (	a_objSubstituent.getSubstituentType().equals(SubstituentType.R_PYRUVATE)||
                a_objSubstituent.getSubstituentType().equals(SubstituentType.S_PYRUVATE)||
                a_objSubstituent.getSubstituentType().equals(SubstituentType.PYRUVATE))
        {
            m_charge--;	    		   
        }
    }

    public Integer getCharge()
    {
        return this.m_charge;
    }

    public void setCharge(Integer a_charge)
    {
        this.m_charge = a_charge;
    }

    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {}

    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("SugarUnitAlternative is not supported.");      
    }

    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode is not supported.");      
    }

    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {}

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

    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserSimple(a_objVisitor);
    }

    public void clear() 
    {
        this.m_charge=0;
    }    
}