package org.grits.toolbox.ms.annotation.sugar;

import java.util.HashMap;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserNodes;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class GlycoVisitorNamespaceSimglycanToKegg implements GlycoVisitor 
{
	HashMap<String, String> m_hNames = new HashMap<String, String>();
	
	public GlycoVisitorNamespaceSimglycanToKegg()
	{
		super();
		this.m_hNames.put("NeuAc", "Neu5Ac");
		this.m_hNames.put("NeuGc", "Neu5Gc");
		this.m_hNames.put("6-dAlt", "6dAlt");
		this.m_hNames.put("IdoA", "LIdoA");
	}

	public void clear() 
	{}

	public GlycoTraverser getTraverser(GlycoVisitor arg0) throws GlycoVisitorException 
	{
		return new GlycoTraverserNodes(arg0);
	}

	public void visit(Monosaccharide arg0) throws GlycoVisitorException 
	{}

	public void visit(NonMonosaccharide arg0) throws GlycoVisitorException 
	{}

	public void visit(Substituent arg0) throws GlycoVisitorException 
	{}

	public void visit(SugarUnitCyclic arg0) throws GlycoVisitorException 
	{}

	public void visit(SugarUnitAlternative arg0) throws GlycoVisitorException 
	{}
	
	public void visit(GlycoEdge arg0) throws GlycoVisitorException 
	{}

	public void visit(SugarUnitRepeat a_repeat) throws GlycoVisitorException 
	{
		GlycoTraverser t_traverser = this.getTraverser(this);
		t_traverser.traverseGraph(a_repeat);
		for (UnderdeterminedSubTree t_tree : a_repeat.getUndeterminedSubTrees())
		{
			t_traverser.traverseGraph(t_tree);
		}
	}

	public void start(Sugar a_sugar) throws GlycoVisitorException 
	{
		GlycoTraverser t_traverser = this.getTraverser(this);
		t_traverser.traverseGraph(a_sugar);
		for (UnderdeterminedSubTree t_tree : a_sugar.getUndeterminedSubTrees())
		{
			t_traverser.traverseGraph(t_tree);
		}
	}
	
	public void visit(UnvalidatedGlycoNode a_node) throws GlycoVisitorException 
	{
		for (String t_key : this.m_hNames.keySet()) 
		{
			String t_strName = a_node.getName();
			if ( t_strName.contains(t_key) )
			{
				try 
				{
					a_node.setName(t_strName.replace(t_key, this.m_hNames.get(t_key)));
				} 
				catch (GlycoconjugateException e)
				{
					throw new GlycoVisitorException(e.getMessage(),e);
				}
				return;
			}
		}
	}
}
