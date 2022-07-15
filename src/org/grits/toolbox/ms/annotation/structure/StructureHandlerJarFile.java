package org.grits.toolbox.ms.annotation.structure;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.grits.toolbox.ms.om.data.GlycanFilter;

public class StructureHandlerJarFile implements IStructureHandler
{
	@Override
	public List<AnalyteStructure> getStructures(GlycanFilter a_filter) throws StructureHandlerException
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
			GlycanDatabase db = new GlycanDatabase();
			InputStream input = new FileInputStream(a_filter.getDatabase());
			if (input != null)
			{
				jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				db = (GlycanDatabase) jaxbUnmarshaller.unmarshal(input);
				return (List<AnalyteStructure>) db.getStructures();
			}
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean addStructure(AnalyteStructure a_structure, String a_database) throws StructureHandlerException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addStructures(List<AnalyteStructure> a_structures, String a_database) throws StructureHandlerException
	{
		// TODO Auto-generated method stub
		return false;
	}
}
