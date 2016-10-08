package notifications;

import java.util.ArrayList;

import model.Profile;

public class NotificationMatch {
	public Profile profile;
	public String common;
	ArrayList<String> list;
	public NotificationMatch(Profile p, String s){
		list = new ArrayList<String>();
		profile = p;
		common = s;
		list.add(s);
	}
	
	public void addCommon(String s){
		if(!list.contains(s)){
			common +=", "+s;
			list.add(s);
		}
	}
}
