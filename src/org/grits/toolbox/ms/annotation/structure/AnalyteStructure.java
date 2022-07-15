package org.grits.toolbox.ms.annotation.structure;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Generic class corresponding to the Analyte object in the object model.
 */
@XmlType
public class AnalyteStructure
{
    private String m_id = null;  
    private String glytoucan_id = null;
    private String m_sequence = null;
    private String m_sequenceFormat = null;
    
    public void setGlytoucanId(String glytoucan_id) {
		this.glytoucan_id = glytoucan_id;
	}
    
    @XmlAttribute(name="GlytoucanId")
    public String getGlytoucanId() {
		return glytoucan_id;
	}
    
    public String getId()
    {
        return m_id;
    }
    
    @XmlAttribute(name="id")
    public void setId(String a_id)
    {
        m_id = a_id;
    }
    
    public String getSequenceFormat()
    {
        return m_sequenceFormat;
    }
    
    @XmlAttribute(name="sequenceFormat")
    public void setSequenceFormat(String a_sequenceFormat)
    {
    	m_sequenceFormat = a_sequenceFormat;
    }
    
    public String getSequence()
    {
        return m_sequence;
    }
    
    @XmlAttribute(name="sequence")
    public void setSequence(String a_sequence)
    {
        m_sequence = a_sequence;
    }
	
	@Override
	public boolean equals(Object obj) {
		if( ! (obj instanceof AnalyteStructure) )
			return false;
		
		AnalyteStructure other = (AnalyteStructure) obj;
		if( (m_id != null && other.getId() == null) ||
			(m_id == null && other.getId() != null) ||
			! m_id.equals(other.getId()) ) 
			return false;
		
		if( (m_sequence != null && other.getSequence() == null) ||
				(m_sequence == null && other.getSequence() != null) ||
				! m_sequence.equals(other.getSequence()) ) 
				return false;

		return true;
	}
	
}
