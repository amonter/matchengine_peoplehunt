package json;

import java.util.List;
import java.util.Map;

public class ProfileInfo{
	public UserConnection[] connections;
	public Map<Integer,String> help;
	public String imageURL;
	public Map<Integer,String> interested;
	public List<Location> locations; //this is bad
	public String name;
	public int profileId;
	public String username;
	public String bio;
	public Map<Integer, String> lastMessages;
	public Map<Integer, Integer> proficiency;
	public List<Map<String,Object>> personalInterests;
	public Map<String,Object> paymentType;
	public Map<String,Object> ratings;
	
	
	
	public ProfileInfo(){}
	
	public void update(ProfileUpdate u){
		locations = u.locations;
		interested = u.interested;
		help = u.help;
		proficiency = u.proficiency;
	}
}