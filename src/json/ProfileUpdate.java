package json;

import java.util.List;
import java.util.Map;

public class ProfileUpdate {
	public int profileID;
	public List<Location> locations;
	public Map<Integer,String> interested;
	public Map<Integer,String> help;
	public Map<Integer,Integer> proficiency;
	
	ProfileUpdate(){}
}
