package json;

import java.util.ArrayList;
import java.util.List;

import model.Match;
import model.Profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchesSnapshot {
	public List<UserMatch> newOnes;
	public List<UserMatch> old;
	private Logger logger = LoggerFactory.getLogger(MatchesSnapshot.class);
	
	public MatchesSnapshot(List<Match> newMatches, List<Match> oldMatches, Profile user){
		newOnes = new ArrayList<UserMatch>();
		old = new ArrayList<UserMatch>();
		
		int newLimit = newMatches.size();
		//newLimit = newLimit<3 ? newLimit : 4; 
		int oldLimit = oldMatches.size();
		//oldLimit = oldLimit<3 ? oldLimit : 3; 
		
		logger.info("startn: " + newOnes.size());
		//TEMP
		for(int i=0; i<newLimit; i++){
		//for(int i=0; i< newMatches.size(); i++){
			newOnes.add( new UserMatch(newMatches.get(i)) );
		}
		//old
		System.out.println(user.getInfo().profileId);
		for(int i=0; i<oldLimit; i++){
			//System.out.println(oldMatches.get(i));
			old.add( new UserMatch(oldMatches.get(i)) );
		}
		
		
		logger.info("snap matches size(): " + newOnes.size());
		//System.out.println("oldMatchesJSON.size(): " + old.size());
		
	}
}
