package org.grits.toolbox.ms.annotation.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.Glyde.SugarImporterGlydeII;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine.SearchEngine;
import org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine.SearchEngineException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

import org.grits.toolbox.ms.annotation.structure.GlycanStructure;

@XmlType(name="filter")
public class Filter implements IFilter{
	@XmlTransient
	private static final Logger logger = Logger.getLogger(Filter.class);

	private String filterName;
	private String filterGlyde;
	@XmlTransient
	private boolean included;
	@XmlTransient
	private boolean enabled;

	@XmlTransient
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getFilterName() {
		return filterName;
	}
	@XmlAttribute(name="filterName")
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}
	public String getFilterGlyde() {
		return filterGlyde;
	}
	@XmlAttribute(name="filterGlyde")
	public void setFilterGlyde(String filterGlyde) {
		this.filterGlyde = filterGlyde;
	}
	@XmlTransient
	public boolean isIncluded() {
		return included;
	}
	public void setIncluded(boolean included) {
		this.included = included;
	}

	public boolean isMatch(Sugar _candidateSugar, GlycanStructure glycan ) {
		try{
			// parse sub structure and structure into object model
			// prepare the search engine
			SearchEngine search = new SearchEngine ();
			SugarImporterGlydeII t_importer = new SugarImporterGlydeII(); 
			Sugar t_subStructure = t_importer.parse(filterGlyde);
			search.setQueryStructure(t_subStructure);
			// test for each structure
			search.setQueriedStructure(_candidateSugar);
			search.match();
			if (search.isExactMatch())
			{
				//if the user is interested in only the glycans that contains this motif 
				if(included)
					return true;
				else
					return false;
			}
			else
			{
				//if the user is interested in only the glycans that contains this motif 
				if(included)
					return false;
				else
					return true;
			}
		}catch(SugarImporterException e){
			logger.error("Incorrect Glyde: \n" + glycan.getId() + " \n" + glycan.getSequence(), e);
			return false;		
		} catch(GlycoVisitorException e){
			logger.error(e.getMessage(), e);
			return false;
		} catch(SearchEngineException e){
			logger.error(e.getMessage(), e);
			return false;
		} catch(GlycoconjugateException e){
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean isMatch(GlycanStructure glycan) {
		try{
			// parse sub structure and structure into object model
			SugarImporterGlydeII t_importer = new SugarImporterGlydeII(); 
			Sugar t_structure = t_importer.parse(glycan.getSequence());
			return isMatch(t_structure, glycan);
		}catch(SugarImporterException e){
			logger.error("Incorrect Glyde: \n" + glycan.getId() + " \n" + glycan.getSequence(), e);
			return false;
		}
	}



}
