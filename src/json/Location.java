package json;



public class Location {
	//public long creationDate;
	public int id;
	public String location;
	
	public Location(){}
	
	@Override
	public boolean equals(Object obj) {		
		
		Location theLoc = (Location) obj;
		boolean isTrue = false;
		if (theLoc.id == this.id) {
			isTrue = true;			
		}				
		return isTrue;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 98664213;
	}
	
	@Override
	public String toString() {
		return "id: "+id+" location "+location;
	}
}