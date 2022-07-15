package org.grits.toolbox.ms.annotation.input;

import com.csvreader.CsvReader;

import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;

public class CSVReader {
	
	public Scan readCSV(String fileName){
		try{
			Scan scan = new Scan();
			scan.setMsLevel(1);
			scan.setParentScan(-1);
			CsvReader reader = new CsvReader(fileName);
			int index = 1;
			while(reader.readRecord()){
				Peak peak = new Peak();
				peak.setId(index++);
				//peak.setCharge(Integer.parseInt(reader.get(3)));
				peak.setMz(Double.parseDouble(reader.get(0)));
				peak.setIntensity(Double.parseDouble(reader.get(1)));
				scan.getPeaklist().add(peak);
			}
			return scan;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
