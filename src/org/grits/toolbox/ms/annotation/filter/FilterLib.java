package org.grits.toolbox.ms.annotation.filter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="filtersLibrary")
public class FilterLib {
	@XmlElement(name="filter")
	private List<Filter> filters = new ArrayList<Filter>();
    
	@XmlTransient
	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	

}
