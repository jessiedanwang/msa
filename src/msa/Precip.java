package msa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* Precip class 
 * calculation monthly precipitation
 */
public class Precip {
	// map from wban to monthly precipitation excluding 12pm to 7am
	private static Map<String, Float> monthlyWetness = new HashMap<String, Float>();
	// map from wban to monthly precipitation
	private static Map<String, Float> monthlyPrecip = new HashMap<String, Float>();
	// map from wban to daily precipitation map 
	// daily precipitation map from day to precipitation 
	private static Map<String, Map<String, String>> dailyPrecip = new HashMap<String, Map<String, String>>();
	private static final int MaxRowsToRead = 500000;
	private static final int NTHREADS = 2;
	
	Precip(){
		calcPrecip();
	}
	
	public Map<String, Float> getPrecip(){
		return monthlyWetness;
	}
	
	/* 
	 * use multiple threads to work on hourly precipitation files
	 */
	private void calcPrecip() {
		String dir = "C:\\Jessie\\Evariant\\";
		String monthlyPrecipFile = dir +"201505monthly";
		String monthlyPrecipCol = "TotalMonthlyPrecip";
		processMonthlyPrecip(monthlyPrecipFile, monthlyPrecipCol);
		String dailyPrecipFile = dir +"201505daily";
		String dailyPrecipCol = "PrecipTotal";
		processDailyPrecip(dailyPrecipFile, dailyPrecipCol);
		String hourlyPrecipFile = dir + "201505precip";
		String hourlyPrecipCol = "Precipitation";
		int nFiles = splitHourlyPrecip(hourlyPrecipFile, hourlyPrecipCol);
		
		ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);	
		List<Future<Map<String, Float>>> list = new ArrayList<Future<Map<String, Float>>>();
		for (int i=0; i<nFiles ; i++)
		{
			Callable<Map<String,Float>> worker = new processHourlyPrecip(hourlyPrecipFile + "_part" + i + ".txt");
			Future<Map<String,Float>> sum = exec.submit(worker);
			list.add(sum);
		}
		for (Future<Map<String,Float>> future: list){
			try {
				Map<String,Float> minusHourlyPrecip = future.get();
				
				for (String key: monthlyPrecip.keySet()){
					float minus = 0;
					if (minusHourlyPrecip!=null) {
						if (minusHourlyPrecip.containsKey(key))
							minusHourlyPrecip.get(key);
					}
					float monthly = monthlyPrecip.get(key);
					monthlyWetness.put(key,  monthly-minus );
				}
			} catch (InterruptedException e){
				e.printStackTrace();
			} catch(ExecutionException e) {
				e.printStackTrace();
			}
		}
		exec.shutdown();
	}
	
	/*
	 * split 201505precip.txt into chunks based on MaxRowsToRead and store as separate files
	 * only keep data between 01 and 07
	 */
	private static int splitHourlyPrecip(String fileName, String colName)
	{
		List<String> rows = new ArrayList<String>();
		int numFiles = 0;
		try
		{
			FileReader fr = new FileReader(fileName +".txt");
			BufferedReader br = new BufferedReader(fr);
			String[] header = br.readLine().split(",");
			int idx = 0;
			for (String s: header) {
				if (s.compareTo(colName)==0)
					break;
				idx++;
			}			
			String[] row = header;
			while (row != null)
			{
				for (int i=0; i<MaxRowsToRead; i++)
				{
					String line = br.readLine();
					if (line == null) {
						row = null;
						break;
					}
					row = line.split(",");
					String wban_id = row[0];
					String day = row[1];
					String time = row[2];
					String precip = row[idx].trim();
					if (dailyPrecip.containsKey(wban_id))
					{			
						if(dailyPrecip.get(wban_id).containsKey(day))
						{
							if (precip.isEmpty() || precip.compareTo("0")==0 || precip.contains("M")|| precip.contains("T") )
								continue;
							//keep data between 01 and 07
							int hour = Integer.parseInt(time);
							if (hour>=1 && hour <=7)
								rows.add(wban_id +","+day+","+time+","+precip);
						}
					}
				}
				
				FileWriter fw = new FileWriter(fileName + "_part" + numFiles + ".txt");
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("WBAN,Date,Time,Precip\n");
				for (int i=0; i<rows.size(); i++)
					bw.write(rows.get(i)+"\n");
				bw.close();
				fw.close();
				numFiles++;
				rows.clear();
			
			}
			br.close();
			fr.close();	
			return numFiles;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}
	
	
	private static void processDailyPrecip(String fileName, String colName)
	{
		Map<String, String> dailyPrecipMap = new HashMap<String, String>();
		try
		{
			FileReader fr = new FileReader(fileName+".txt");
			BufferedReader br = new BufferedReader(fr);
			String[] header = br.readLine().split(",");
			int idx = 0;
			for (String s: header) {
				if (s.compareTo(colName)==0)
					break;
				idx++;
			}
				
			String[] row = header;
			String preWban = "0";
			while (row != null)
			{
				String line = br.readLine();
				if (line == null)
					break;
				row = line.split(",");
				String wban_id = row[0];
				String day = row[1];
				String precip = row[idx].trim();
				// filter out row if precip is 0, T or M
				if (precip.isEmpty() || precip.compareTo("0")==0 || precip.contains("M") || precip.contains("T") )
					continue;
				if (wban_id.compareTo(preWban)!=0)
				{
					preWban = wban_id;
					if (dailyPrecipMap.size()>0)
						dailyPrecip.put(wban_id, dailyPrecipMap);
					dailyPrecipMap.clear();					
				}
				dailyPrecipMap.put(day,precip);		
			}
			br.close();
			fr.close();			
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void processMonthlyPrecip(String fileName, String colName)
	{
		try
		{
			FileReader fr = new FileReader(fileName+".txt");
			BufferedReader br = new BufferedReader(fr);
			String[] header = br.readLine().split(",");
			int idx = 0;
			for (String s: header) {
				if (s.compareTo(colName)==0)
					break;
				idx++;
			}
			 	
			String[] row = header;

			while (row != null)
			{
				String line = br.readLine();
				if (line == null)
					break;
				row = line.split(",");
				
				String wban_id = row[0];
				String precip = row[idx].trim();
				
				//System.out.println(wban_id + " " + precip);
				
				// filter out row if precip is 0, T or M
				if (precip.isEmpty() || precip.compareTo("0")==0 || precip.contains("M")|| precip.contains("T") )
					continue;
				monthlyPrecip.put(wban_id, Float.parseFloat(precip));

			}
			br.close();
			fr.close();		
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}	
}
