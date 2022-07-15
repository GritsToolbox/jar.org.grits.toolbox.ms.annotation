package org.grits.toolbox.ms.annotation.structure;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Generic storage object for an analyte database. This class corresponds to the XML file
 * that contains the database and can be (de)serialized from/to the XML file
 * using JAXB. The class represents the &lt;database&gt; main tag.
 *
 * @author rene
 *
 */
@XmlRootElement(name = "analyteDatabase")
abstract class AnalyteDatabase
{
    /** Name of the database */
    private String m_name = null;
    /** Description text of the database */
    private String m_description = null;
    /**
     * Number of structures in the database, this should be equal to
     * m_structures.size().
     */
    private Integer m_structureCount = null;
    /** Version of the database */
    private String m_version = "1.1";

    public abstract List<? extends AnalyteStructure> getStructures();
    public abstract void setStructures(List<? extends AnalyteStructure> a_structures);
    public abstract boolean addStructure(AnalyteStructure a_structure);
    
    public String getName()
    {
        return m_name;
    }

    @XmlAttribute
    public void setName(String a_name)
    {
        m_name = a_name;
    }

    public String getDescription()
    {
        return m_description;
    }

    @XmlAttribute
    public void setDescription(String a_description)
    {
        m_description = a_description;
    }

    public Integer getStructureCount()
    {
        return m_structureCount;
    }

    @XmlAttribute
    public void setStructureCount(Integer a_structureCount)
    {
        m_structureCount = a_structureCount;
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
