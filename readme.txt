Readme for Evariant MSA by wet population

Precip class
It used calculate monthly precipitation - precipitation from 12am to 7am. 
Go through monthly precipiation file and daily precipitation file, then split hourly precipitation file into chunks 
and uses worker processHourlyPrecip to process hourly procipitation in parallel.

MetroStatArea class
MetroStatArea object has state and metro as member, also implemented hasCode() and equals().

WetPopulation class
WetPopulation object consists of one MetroStatArea object and one or more wban/station and one or more precipitation data.
getWetPop() method return average wet population in the MetroStatArea.
processPopulation() method go through CPH-T-5.txt (which is saved from CPH-T-5 excel file as text with tab delimitor) and 
produce hash map from state to hash map (from metro to population). Population is calculated based on 4/2000 and 4/2010 population.
processStation() method go through 201505station.txt, and produce hash map from wban_id to station/MetroStatArea.
WetPopulation object is inserted into list, then sort based on average wet population, results are written to output.csv file.




