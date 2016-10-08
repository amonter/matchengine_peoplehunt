package json;

import java.util.List;
import java.util.Map;

import model.Match;
import model.Profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMatch {
	private Logger logger = LoggerFactory.getLogger(UserMatch.class);
	public int profile_id;
	public int status;
	public Map<String, Map<Integer,String>> match_criteria;
	//The format is:
	//{locations:{"12":"Galway,Ireland", "18":"Eindhoven, Netherlands"}, help:{"23":"Startup Brasil" ,"34" : "Native Spanish"}, interested:{}}
	public int shares;
	public String name;
	public String image_url;
	public String bio;
	public String lastMessage;
	public Boolean isNewMessage;
	public Map<Integer, Integer> proficiency;
	public List<Map<String, Object>> interests;
	public Map<String, Object> paymentType;
	public Map<String, Object> ratings;	
	public int live_now;
	
	public UserMatch(){}
	
	public UserMatch(Match m){
		Profile p = null;
		p = m.them;
		synchronized(p){
			ProfileInfo info = p.getInfo();
			profile_id = info.profileId;
			bio = info.bio;
			status = m.getStatus();
			shares = m.getShares();
			proficiency = info.proficiency;
			name = info.name;
			image_url = info.imageURL;
			live_now = p.isOnline() ? 1 : 0;
			interests = info.personalInterests;
			paymentType = info.paymentType;
			ratings = info.ratings;
			
			List<Integer> theNewMessages =  m.them.newMessages;
			isNewMessage = new Boolean(false);
			for (Integer userIsNew : theNewMessages) {
				logger.info("user new"+userIsNew);
				if (userIsNew.intValue() == m.us.id){
					isNewMessage = new Boolean(true);
					break;
				}
			}
			
			lastMessage = m.them.getLastMessage(m.us.id);
			match_criteria = m.matchCriteria;
			Map<Integer,String> ptr = match_criteria.get("locations");
			for(Location x : m.us.getLocationsInCommon(p)){
				ptr.put(x.id, x.location);
			}
			Integer indexObj = new Integer(m.us.id);
			m.them.newMessages.remove(indexObj);
		}
	}
}
