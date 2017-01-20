package msa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class processHourlyPrecip implements Callable<Map<String,Float>>{
	private final String fileName;
	
	processHourlyPrecip(String name)
	{
		this.fileName = name;
	}

	public Map<String, Float> call()
	{
		Map<String, Float> PrecipMap = new HashMap<String, Float>();
		try
		{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String[] header = br.readLine().split(",");
			 	
			String[] row = header;
			String preWban = "0";
			float sum = 0;
			while (row != null)
			{
				String line = br.readLine();
				if (line == null)
					break;
				row = line.split(",");
				String wban_id = row[0];
				String precip = row[3].trim();
				// filter out row if precip is 0, T or M
				if (precip.isEmpty() || precip.contains("M") || precip.contains("T") )
					continue;
				
				if (wban_id.compareTo(preWban)!=0)
				{
					preWban = wban_id;				
					PrecipMap.put(wban_id, sum);
					sum = 0;
				}

				sum += Float.parseFloat(precip);	
			}
			br.close();
			fr.close();
			return PrecipMap;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
