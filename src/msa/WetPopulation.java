package msa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 * class WetPopulation
 * one MSA can have more than one wban
 */
public class WetPopulation {
	
	private MetroStatArea msaName;
	private List<String> wbans = new ArrayList<String>();
	private List<Float> wetPopulation = new ArrayList<Float>();
	
	public WetPopulation(MetroStatArea msa, String wban, float wetPopu) {
		this.msaName = msa;
		this.wbans.add(wban);
		this.wetPopulation.add(wetPopu);
	}
	
	public void addStationData(String wban, float webPopu){
		wbans.add(wban);
		wetPopulation.add(webPopu);
	}
	
	/*
	 * get average wet population
	 */
	public float getWetPop(){
		int i=0;
		float sum = 0;
		for (String w : wbans){
			sum += wetPopulation.get(i);
			i++;
		}
		return (float)(sum/i);
	}
	
	public MetroStatArea getMSA() {
		return msaName;
	}
	
	
	public static Comparator<WetPopulation> WetPopuComp = new Comparator<WetPopulation>() {
		public int compare(WetPopulation wp1, WetPopulation wp2){
			return (int)(wp2.getWetPop() - wp1.getWetPop());
		}
	};
	
	//map from state to metro population map
	//metro population map from metro to population
	private static Map<String, Map<String,Integer>> populationMap = new HashMap<String, Map<String, Integer>>();
	//map from wban to MetroStatArea
	private static Map<String, MetroStatArea> stationMap = new HashMap<String, MetroStatArea>();

	public static void main(String[] args) throws IOException {
		String dir = "C:\\Jessie\\Evariant\\";
		String outputFile =  dir + "output.csv";
		String populationFile = dir +"CPH-T-5.txt";
		processPopulation(populationFile);
		/*
		FileWriter fw1 = new FileWriter(dir + "msa_population" + ".txt");
		BufferedWriter bw1 = new BufferedWriter(fw1);
		bw1.write("MSA,Population\n");
		for (String state : populationMap.keySet())
			for (String metro : populationMap.get(state).keySet())
				bw1.write(state + "," + metro + "," + populationMap.get(state).get(metro)+"\n");
		bw1.close();
		fw1.close();
		*/
		String stationFile = dir + "201505station.txt";
		processStation(stationFile);
		/*
		FileWriter fw2 = new FileWriter(dir + "wban_msa" + ".txt");
		BufferedWriter bw2 = new BufferedWriter(fw2);
		bw2.write("WBAN,MSA\n");
		for (String key : stationMap.keySet())
			bw2.write(key + "," + stationMap.get(key).getMetro() + "," + stationMap.get(key).getState()+"\n");
		bw2.close();
		fw2.close();
		*/
		Precip precip = new Precip();
		Map<String, Float> monthlyWetness = precip.getPrecip();
		List<WetPopulation> wplist = new ArrayList<WetPopulation>();
		Map<MetroStatArea, WetPopulation> wpMap = new HashMap<MetroStatArea, WetPopulation>();
		// go through monthlyWetness map via wban
		for (String key: monthlyWetness.keySet()){
			float wetness = monthlyWetness.get(key);
			MetroStatArea msa = stationMap.get(key);
			
			int popu = 0;
			String state = msa.getState();
			String metro = msa.getMetro().toUpperCase();
			
			if (populationMap.containsKey(state)) {
				// need to match metro with 2nd key in populationMap
				if (populationMap.get(state).containsKey(metro)) {
					popu = populationMap.get(state).get(metro);
					float wetPop = popu*wetness;
					WetPopulation wp;
					if (wpMap.containsKey(msa)){
						wp = wpMap.get(msa);
						wp.addStationData(key, wetPop);
					}
					else {
						wp = new WetPopulation(msa, key, wetPop);
						wpMap.put(msa, wp);	
						wplist.add(wp);
					}
				}
				//else
					//System.out.println("could not find state " + state + ", metro " +metro + " in populationMap");
			}
			//else {
				//System.out.println("could not find " + state + " in populationMap");
			//}	
		}	
		Collections.sort(wplist, WetPopuComp);
		try {
			FileWriter fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("rank|msa|WetPopulation\n");
			int i = 1;
			for (WetPopulation w: wplist){
				bw.write(i + "|" + w.getMSA().getMetro() + "," + w.getMSA().getState() + "|" + w.getWetPop() +"\n" );	
				i++;
			}
			bw.close();
			fw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private static int string2int(String s){
		String[] str = s.split(",");
		int sum = 0;
		for (int i=0; i<str.length; i++){
			sum = 1000*sum;
			sum += Integer.parseInt(str[i]);
		}	
		return sum;
	}
	
	private static void processStation(String fileName)
	{
		try
		{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String[] header = br.readLine().split(",");
 	
			String[] row = header;

			while (row != null)
			{
				String line = br.readLine();
				if (line == null)
					break;
				
				row = line.split("\\|");
				String wban = row[0].trim();
				String name = row[6].trim();
				if (name.contains("/"))
					name = name.split("/")[0];
				String state = row[7].trim();
				if (wban.length()==0 || name.length()==0 || state.length()==0)
					continue;
				//System.out.println(wban + " " +row[1]+" "+row[2]+" "+row[3]+" "+row[4]+" "+row[5]+" "+ name + " " + state);
				stationMap.put(wban, new MetroStatArea(name,state));	
			
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
	
	private static void processPopulation(String fileName)
	{
		try
		{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String[] header = br.readLine().split(",");
 	
			String[] row = header;
			boolean start = false;
			boolean end = false;
			while (row != null)
			{
				String line = br.readLine();
				if (line == null )
					break;		
				if (line.contains("Metropolitan statistical area")) {
					start = true;
					continue;
				}
				if (!start)
					continue;
				if (line.contains("Footnote"))
					end = true;
				if (end)
					continue;
				
				row = line.split("\\t");	
				if (row.length != 5)
					continue;

				String msaRaw = row[0].trim();
				if (msaRaw.isEmpty())
					continue;
				String msaTmp = msaRaw.substring(1, msaRaw.length()-1);
				String[] msa = msaTmp.toUpperCase().split(",");
				if (msa.length < 2)
					continue;
				String metro = msa[0].trim();
				String[] metros = null;
				if (metro.contains("-"))
				{
					metros = metro.split("-");
				}
				
				String state = msa[1].trim();
				if (state.contains("-")) {
					state = state.split("-")[0];
				}
				

				String prePop = row[1].trim();
				String deltaPop = row[3].trim();
				if (prePop.isEmpty() ||deltaPop.isEmpty())
					continue;
				String prePopulation = prePop.substring(1, prePop.length()-1);
				String deltaPopulation = deltaPop;
				if (deltaPopulation.contains("\"") )
						deltaPopulation = deltaPop.substring(1, deltaPop.length()-1);
				//System.out.println(msaTmp+" "+prePopulation+" "+deltaPopulation);
				int iPrePop = string2int(prePopulation);
				int iDeltaPop = string2int(deltaPopulation);
				int pop201505 = (120+61)*iDeltaPop/120 + iPrePop;
				if (populationMap.containsKey(state)) {
					if (metros!=null) {
						for (String m : metros)
							populationMap.get(state).put(m, pop201505);
					}
					else {
						populationMap.get(state).put(metro, pop201505);	
					}
				}
				else {
					Map<String, Integer> metroPop = new HashMap<String, Integer>();
					if (metros!=null) {
						for (String m: metros)
							metroPop.put(m, pop201505);
					}
					else {
						metroPop.put(metro, pop201505);
					}
					populationMap.put(state, metroPop);
				}
						
				

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