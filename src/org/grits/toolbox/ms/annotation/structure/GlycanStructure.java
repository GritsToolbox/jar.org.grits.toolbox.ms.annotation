package org.grits.toolbox.ms.annotation.structure;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class GlycanStructure extends AnalyteStructure
{
    private String m_GWBSequence = null;
	
    public String getGWBSequence() {
		return m_GWBSequence;
	}
	@XmlAttribute(name="GWBSequence")
	public void setGWBSequence(String GWBSequence) {
		this.m_GWBSequence = GWBSequence;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( ! (obj instanceof GlycanStructure) )
			return false;
		
		GlycanStructure other = (GlycanStructure) obj;
		if( (getId() != null && other.getId() == null) ||
			(getId() == null && other.getId() != null) ||
			! getId().equals(other.getId()) ) 
			return false;
		
		if( (getSequence() != null && other.getSequence() == null) ||
				(getSequence() == null && other.getSequence() != null) ||
				! getSequence().equals(other.getSequence()) ) 
				return false;

		if( (m_GWBSequence != null && other.getGWBSequence() == null) ||
				(m_GWBSequence == null && other.getGWBSequence() != null) ||
				! m_GWBSequence.equals(other.getGWBSequence()) ) 
				return false;

		return true;
	}
	
}
