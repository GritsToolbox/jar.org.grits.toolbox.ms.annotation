package org.grits.toolbox.ms.annotation.structure;

import java.util.List;

import org.grits.toolbox.ms.om.data.GlycanFilter;

/**
 * Interface for the necessary methods for database of structures.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * 
 */
public interface IStructureHandler 
{
    public List<AnalyteStructure> getStructures(GlycanFilter a_filter) throws StructureHandlerException;
    public boolean addStructure(AnalyteStructure a_structure, String a_database) throws StructureHandlerException;
    public boolean addStructures(List<AnalyteStructure> a_structures, String a_database) throws StructureHandlerException;
}
