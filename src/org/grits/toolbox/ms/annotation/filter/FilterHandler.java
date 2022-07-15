package org.grits.toolbox.ms.annotation.filter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.grits.toolbox.ms.annotation.structure.GlycanDatabase;
import org.grits.toolbox.ms.annotation.structure.GlycanStructure;
import org.grits.toolbox.ms.annotation.structure.StructureHandlerException;
import org.grits.toolbox.ms.om.data.GlycanFilter;

public class FilterHandler {
	
	public boolean serializeFilters(FilterLib newFilters,String fileName){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(FilterLib.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            FilterLib filters = new FilterLib();

            File f = new File(fileName + ".xml");

            if (f.exists()) {
                jaxbContext = JAXBContext.newInstance(Filter.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                filters = (FilterLib) jaxbUnmarshaller.unmarshal(f);
                for(Filter fil : newFilters.getFilters()){
                	filters.getFilters().add(fil);
                }
                jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                jaxbMarshaller.marshal(filters, f);
                jaxbMarshaller.marshal(filters, System.out);

            } else {
               
                jaxbContext = JAXBContext.newInstance(FilterLib.class);
                jaxbMarshaller = jaxbContext.createMarshaller();

                // output pretty printed
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                jaxbMarshaller.marshal(newFilters, f);
                jaxbMarshaller.marshal(newFilters, System.out);

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
	
	 public List<Filter> getFilters(String path){
	        try{
	            JAXBContext jaxbContext = JAXBContext.newInstance(FilterLib.class);
	            FilterLib lib = new FilterLib();
	            //File temp = new File(FilterHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	            //File f = new File(temp.getParent()+"/filters.xml");
	            File f = new File(path);
	            System.out.println(f.getAbsolutePath());

	            if(f.exists()){
	                jaxbContext = JAXBContext.newInstance(FilterLib.class);
	                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	                lib = (FilterLib) jaxbUnmarshaller.unmarshal(f);
	                return lib.getFilters();  
	            }
	            else{
	                return null;
	            }
	        }catch(Exception e){
	            e.printStackTrace();
	            return null;
	        }
	    }
	 
	
	
	public static void main(String[] args){
//		Filter f = new Filter();
//		f.setFilterName("GalGal");
//		f.setFilterGlyde("<?xml version=\"1.0\" encoding=\"UTF-8\"?><GlydeII><molecule subtype=\"glycan\" id=\"From_GlycoCT_Translation\"><residue subtype=\"base_type\" partid=\"1\" ref=\"http://www.monosaccharideDB.org/GLYDE-II.jsp?G=x-dgal-HEX-1:5\" /><residue subtype=\"base_type\" partid=\"2\" ref=\"http://www.monosaccharideDB.org/GLYDE-II.jsp?G=a-dgal-HEX-1:5\" /><residue_link from=\"2\" to=\"1\"><atom_link from=\"C1\" to=\"O3\" to_replaces=\"O1\" bond_order=\"1\" /></residue_link></molecule></GlydeII>");
//		FilterLib lib = new FilterLib();
//		lib.getFilters().add(f);
//		FilterHandler fh = new FilterHandler();
//		fh.serializeFilters(lib, "filters");
		
		FilterHandler fh = new FilterHandler();
		//System.out.println(fh.getFilters().size());
		
		
		
	}


}
