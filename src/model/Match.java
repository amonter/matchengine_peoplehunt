package model;

import java.util.HashMap;
import java.util.Map;


public class Match implements Comparable<Match> {
	public int rank;
	public Profile us;
	public Profile them;
	public int shared; //# of things shared in common
	public Map<String,Map<Integer,String>> matchCriteria;
	
	public Match(Profile u, Profile t, Map<Integer,String> what, int key, String which, int baseRank){
		matchCriteria = new HashMap<String,Map<Integer,String>>();
		matchCriteria.put("locations", new HashMap<Integer,String>());
		matchCriteria.put("help", new HashMap<Integer,String>());
		matchCriteria.put("interested", new HashMap<Integer,String>());
		Map<Integer,String> meh = matchCriteria.get(which);
		meh.put(key, what.get(key));
		
		shared = 1;
		rank = baseRank;
		us = u;
		them = t;
	}

	//used to create a match where there might not be things in common
	public Match(Profile u, Profile t, int baseRank){
		matchCriteria = new HashMap<String,Map<Integer,String>>();
		matchCriteria.put("locations", new HashMap<Integer,String>());
		matchCriteria.put("help", new HashMap<Integer,String>());
		matchCriteria.put("interested", new HashMap<Integer,String>());
		
		shared = 0;
		rank = baseRank;
		us = u;
		them = t;
	}
	
	public void addToMatch(Map<Integer,String> what, int key, String which){
		rank++;
		shared++;
		matchCriteria.get(which).put(key, what.get(key));
	}
	
	public void incrementRank(){
		rank++;
	}
	
	public int getShares(){
		return shared;
	}
	
	public int getStatus(){
		return 0; //FIXME
	}

	@Override
	public int compareTo(Match o) {
		if(o.rank == rank){ //first by rank
			return (them.getLastOnline() >= o.them.getLastOnline()) ? 1 : -1; //then by time active
		}
		return (o.rank<rank) ? -1 : 1; //this is now REVERSED. #FIXME
	}

}
