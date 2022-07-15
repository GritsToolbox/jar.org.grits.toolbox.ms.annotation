package org.grits.toolbox.ms.annotation.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.csvreader.CsvWriter;

import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;

public class CSVWriter {
	
	public void writeCSV(Scan scan){
		try{
			//System.out.println("Scan num: " + scan.getScanNo() + " peaks: " + scan.getPeaklist().size());
			
			CsvWriter writer = null;
			if(scan.getMsLevel() == 1){
				File file = new File("./scans/"+scan.getScanNo()+".csv");
				
					file.createNewFile();
				
	 
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				for(Peak peak : scan.getPeaklist()){
					
						//System.out.println(""+peak.getId() + " "+peak.getMz());
						String[] peaks = new String[2];
						peaks[0] = ""+peak.getMz();
						peaks[1] = "NA";
						bw.write(peak.getMz()+","+peak.getIntensity()+"\n");				
				}
				bw.close();
			}
			else{
				writer = new CsvWriter("./scans/"+scan.getScanNo()+".csv");
				for(Peak peak : scan.getPeaklist()){
				String[] peaks = new String[2];
				peaks[0] = ""+peak.getMz();
				peaks[1] = ""+peak.getIntensity();
				writer.writeRecord(peaks);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			
		}
	}

}
