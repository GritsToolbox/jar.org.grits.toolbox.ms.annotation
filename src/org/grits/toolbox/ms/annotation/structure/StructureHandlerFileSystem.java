package org.grits.toolbox.ms.annotation.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.ms.om.data.GlycanFilter;

public class StructureHandlerFileSystem implements IStructureHandler
{
	private static final Logger logger = Logger.getLogger(StructureHandlerFileSystem.class);

	/**
	 * gets structures from the given database. Also updates the passed argument (GlycanFilter)
	 * with the version information from the database
	 * @param a_filter GlycanFilter that contains the filename/path for the database
	 */
    @Override
    public List<AnalyteStructure> getStructures(GlycanFilter a_filter) throws StructureHandlerException
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
            GlycanDatabase db = new GlycanDatabase();
            File f = new File(a_filter.getDatabase());

            if (f.exists())
            {
                jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                db = (GlycanDatabase) jaxbUnmarshaller.unmarshal(f);
                a_filter.setVersion(db.getVersion());
                return (List<AnalyteStructure>) db.getStructures();
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
        	logger.warn("Invalid database!", e);
            return null;
        }
    }

    @Override
    public boolean addStructure(AnalyteStructure a_structure, String a_database) throws StructureHandlerException
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            GlycanDatabase db = new GlycanDatabase();

            File f = new File(a_database);

            if (f.exists())
            {
                jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                db = (GlycanDatabase) jaxbUnmarshaller.unmarshal(f);
                ((List<AnalyteStructure>) db.getStructures()).add(a_structure);
                jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                jaxbMarshaller.marshal(db, f);
                jaxbMarshaller.marshal(db, System.out);

            }
            else
            {
                GlycanDatabase newDb = new GlycanDatabase();
                List<AnalyteStructure> glycans = new ArrayList<AnalyteStructure>();
                glycans.add(a_structure);
                newDb.setName(a_database);
                newDb.setStructures(glycans);
                jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
                jaxbMarshaller = jaxbContext.createMarshaller();

                // output pretty printed
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                jaxbMarshaller.marshal(newDb, f);
                jaxbMarshaller.marshal(newDb, System.out);

            }
            return true;
        }
        catch (Exception e)
        {
        	logger.warn("Cannot add structure to the database", e);
            return false;
        }
    }

    @Override
    public boolean addStructures(List<AnalyteStructure> a_structures, String a_database) throws StructureHandlerException
    {
        try        
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            GlycanDatabase db = new GlycanDatabase();

            File f = new File(a_database);

            if (f.exists())
            {
                jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                db = (GlycanDatabase) jaxbUnmarshaller.unmarshal(f);
                ((List<AnalyteStructure>) db.getStructures()).addAll(a_structures);
                jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                jaxbMarshaller.marshal(db, f);
                jaxbMarshaller.marshal(db, System.out);

            }
            else
            {
                GlycanDatabase newDb = new GlycanDatabase();
                newDb.setName(a_database);
                newDb.setStructures(a_structures);
                jaxbContext = JAXBContext.newInstance(GlycanDatabase.class);
                jaxbMarshaller = jaxbContext.createMarshaller();

                // output pretty printed
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                jaxbMarshaller.marshal(newDb, f);
                jaxbMarshaller.marshal(newDb, System.out);

            }
            return true;
        }
        catch (Exception e)
        {
        	logger.warn("Cannot add structures to the database", e);
            return false;
        }
    }
}
