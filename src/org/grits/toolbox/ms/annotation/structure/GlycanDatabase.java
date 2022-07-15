package org.grits.toolbox.ms.annotation.structure;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Storage object for a glycan database. This class corresponds to the XML file
 * that contains the database and can be (de)serialized from/to the XML file
 * using JAXB. The class represents the &lt;database&gt; main tag.
 *
 * @author rene
 *
 */
@XmlRootElement(name = "database")
public class GlycanDatabase extends AnalyteDatabase
{
    /**
     * List of glycans that are stored in the database, Each glycan is
     * represented by an individual &lt;glycan&gt; tag.
     */
    @XmlElement(name = "glycan")
    private List<GlycanStructure> m_structures = new ArrayList<>();    
    /** Version of the database */
    private String m_version = "1.1";

    @XmlTransient
    public List<? extends AnalyteStructure> getStructures()
    {
        return m_structures;
    }
    public void setStructures(List<? extends AnalyteStructure> a_structures)
    {
        m_structures = (List<GlycanStructure> ) a_structures;
    }

    public boolean addStructure(AnalyteStructure a_structure)
    {
        return m_structures.add((GlycanStructure) a_structure);
    }

    public String getVersion()
    {
        return m_version;
    }

    @XmlAttribute
    public void setVersion(String a_version)
    {
        this.m_version = a_version;
    }
}
