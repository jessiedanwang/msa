package msa;

public class MetroStatArea {
	private String metro;
	private String state;
	
	public MetroStatArea(String m, String s){
		this.metro = m;
		this.state = s;
	}
	
	public String getMetro(){
		return metro;
	}
	
	public String getState() {
		return state;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime*result + metro.hashCode() + state.hashCode();
		return result;
	}
	
	public boolean equals(Object obj){
		if (obj instanceof MetroStatArea)
		{
			MetroStatArea msa = (MetroStatArea)obj;
			return this.metro.toLowerCase().contains(msa.metro.toLowerCase()) && 
					this.state.toLowerCase().contains(msa.state.toLowerCase());	
		}
		else
			return false;
	}
	

}
