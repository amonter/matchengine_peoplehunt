package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import json.Location;
import json.MatchesSnapshot;

import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.sun.corba.se.impl.encoding.CodeSetConversion.BTCConverter;

/**
 * 
 * @author adriano
 * This class stores all the users and data 
 */
public class Population {
	private Logger logger = LoggerFactory.getLogger(Population.class);
	private Map<Integer, Profile> usersByID;
	private Map<Integer, Map<Integer, Profile>> locations;
	private final int initialSize = 500; //initial size, keep this reasonable
	DB mongoDatabase;
	
	public Population(){
		usersByID = new ConcurrentHashMap<Integer, Profile>(initialSize);
		locations = new ConcurrentHashMap<Integer, Map<Integer, Profile>>(20); //number of locations to start with
		
		try {
			//27017
			MongoClient mongoClient = new MongoClient( "50.19.45.37" , 3307 );
			mongoDatabase = mongoClient.getDB( "peoplehunt" );			
			
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		
		//usersByID = new HashMap<Integer,Profile>();
		//we might want other kind of tables, by location would be a good one
		
		startUp();
	}
	
	public Profile add(int id){ //resolves the users profile
		Profile p = null;
		if(!usersByID.containsKey(id)){
			p = new Profile(id, mongoDatabase);	//profile creation is expensive do that in the client thread space if this method is synchronized
			usersByID.put(p.id, p);
			//we want to build up location tables
			addToLocations(p);
		}
		else{
			//refresh the 'active' timestamp 
			p = usersByID.get(id);
		}
		return p;
	}
	
	public Profile getUser(int id){
		if( usersByID.containsKey(id) ){
			return usersByID.get(id);
		}
		return null;
	}
	
	public void removeFromLocations(Profile p){
		// we don't actually remove the user from the runtime though
		for(Location x : p.getInfo().locations){
			if(locations.containsKey(x.id)){
				if(locations.get(x.id).containsKey(p.id)){
					locations.get(x.id).remove(p.id);
				}
			}
		}
	}
	
	public void addToLocations(Profile p){
		
		for(Location x : p.getInfo().locations){
			if( locations.containsKey(x.id) ){
				//add				
				locations.get(x.id).put(p.id, p);
				//logger.info("LOCATION ++++ "+x.location+" "+p.getInfo().name);
			}
			else{
				//create new hashmap for that location and add
				Map<Integer, Profile> loc = new ConcurrentHashMap<Integer, Profile>(initialSize);
				loc.put(p.id, p);
				//logger.info("LOCATION ++++ "+x.location+" "+p.getInfo().name);
				locations.put(x.id, loc);
			}
		}
	}
	
	public boolean sendMessageToUser(String msg, int userID){
		if(usersByID.containsKey(userID)){
			return usersByID.get(userID).sendMessage(msg);
		}
		return false;
	}
	
	public List<Match> hubMatching(Profile p){
		
		Map<Integer,Match> matches = new HashMap<Integer,Match>(); //profileID as keys		
		//all the users...
		match(p, usersByID, matches, false, true);		

		//sorting by rank
		ArrayList<Match> newMatches = new ArrayList<Match>(matches.values());
		Collections.sort(newMatches);
		
		return newMatches;
	}
	
	
	public MatchesSnapshot getMatches(Profile p){
		Map<Integer,Match> matches = new HashMap<Integer,Match>(); //profileID as keys
		
		//first off check does the user have any messages from users who are not yet a connection - we want to make them such then
		//list if people with interactions time..
		//forced match doesn't matter if there is anything in common. they just have an interaction
		Match forcedMatch = null;
		Profile somebody = null;
		synchronized(p.interactionTime){
			for(int id : p.interactionTime.keySet()){
				logger.info("previous matches "+p.id+" previous "+matches);
				if( !p.previousMatches.containsKey(id) ){
					//means that user sent this one a message, so we make a match - help them find each other
					//this RELIES on the previusMatches being loaded consistently accross restarts of the server
					if(usersByID.containsKey(id)){
						somebody = usersByID.get(id);						
						forcedMatch = p.matchAndRank(somebody, false, 100);
						if(forcedMatch == null){
							forcedMatch = new Match(p, somebody, 100);
						}
						matches.put(id, forcedMatch);
					}
				}
			}
		}
		//location second - use the match() for each hashmap
		//synchronized(p){
			//Location is a hashMap with user and he is matching people with the same location and the user who is requestion
			logger.info("Profile Info "+ p.getInfo().locations);
			for(Location x : p.getInfo().locations){
				if(locations.containsKey(x.id)){
					logger.info("Contains "+ x.id);
					match(p, locations.get(x.id), matches, false, false);
				}
			}
		//}
		
		//Do bot logic
			if (matches.isEmpty()){
							
				for(int x : p.getInfo().interested.keySet()){
					int botId = 1139;
					if (x == 2) botId = 1142; 		
					System.out.println("EMPTY EMPYYYYYY "+x);	
					Profile botProfile = usersByID.get(botId);
					Match botMatch = new Match(p, botProfile, p.getInfo().interested, x, "help", 1);
					Location loc = p.getInfo().locations.get(0);
					Map<Integer, String> locMap = new HashMap<Integer, String>();
					locMap.put(loc.id, loc.location);
					botMatch.addToMatch(locMap, loc.id, "locations");
					matches.put(botId, botMatch);
				}
			}
			
		//not enough matches, do on whole population (iusers with an id)
		//if(matches.size()<=2){
			//all the users...
			//logger.info("more matches "+ matches);
			//match(p, usersByID, matches, false, false);
		//}

		//sorting by rank
		ArrayList<Match> newMatches = new ArrayList<Match>(matches.values());
		//Collections.sort(newMatches);
		
		//Old Matches
		forcedMatch = null;
		somebody = null;
		ArrayList<Match> oldMatches = new ArrayList<Match>();
		if(!p.hasNoPreviousMatches()){
			//first check for people with the interaction time which this user has not 'clicked' on
			for(int x : p.interactionTime.keySet()){
				if( !p.previousMatches.containsKey(x) ){
					somebody = usersByID.get(x);
					forcedMatch = p.matchAndRank(somebody, true, 100); //should be something really high for initial rank to always land at top
					if(forcedMatch != null){
						oldMatches.add(forcedMatch);
					}
				}
			}
			//now the rest
			for(int x : p.previousMatches.keySet()){
				//currently the status is unchanging, so no need to check
				somebody = usersByID.get(x);
				forcedMatch = p.matchAndRank(somebody, true, 1);
				if(forcedMatch == null){
					forcedMatch = new OldMatch(p, somebody, 1);
				}
				oldMatches.add(forcedMatch);
			}
		}
		Collections.sort(oldMatches);
		Collections.reverse(oldMatches); //FIXME remove it after Adrian fixes order on the app side, this is insanely expensive operation

		logger.info(" newMatches.size(): " + newMatches.size());
		//System.out.println("before slice, oldMatches.size(): " + oldMatches.size());
		
		return new MatchesSnapshot(newMatches, oldMatches, p);
	}
	
	private void match(Profile user, Map<Integer,Profile> members, Map<Integer, Match> matches, boolean secondRound, boolean disableConnections){ //second round means we're going through the list of ALL users
		Match tmp = null;

		//System.out.println("This location has " + members.size() + " users.");
		//this method take all the users in the runtime and sorting them by rank
		try{
			for(Profile somebody : members.values()){
				//logger.info("ID: user "+somebody.id);			
				if (disableConnections){
					doMatchLogic(user, matches, secondRound, somebody);
					
				} else {				
					if(!user.previousMatches.containsKey(somebody.id) && (somebody.id != user.id)){
						//now the logic for the match
						//System.out.println("comparing #" + user.id + " with #" + somebody.id);
						doMatchLogic(user, matches, secondRound, somebody);					
					}		
				}
			}
		}catch(Exception e){
			System.out.println("Error with matching: " + e.getLocalizedMessage() );
		}
	}

	private void doMatchLogic(Profile user, Map<Integer, Match> matches,
			boolean secondRound, Profile somebody) {
		Match tmp;
		if(matches.containsKey(somebody.id) && !secondRound){
			//already matched, increase rank
			//System.out.print(" rank++.");
			matches.get(somebody.id).incrementRank(); //now do we want to increment by 'rank' or just 1 like it is now?
		}
		else{
			//fresh user
			//second round has to with checking the sorting of new matches and old matches and the sorting
			if(secondRound){
			
				tmp = user.matchAndRank(somebody, false, 1); //1 because it's non-location based 
			}
			else{
				
				tmp = user.matchAndRank(somebody, false, 2); //false to get back Match not OldMatch
			}
			if(tmp != null){
				logger.info(" fresh match.");
				matches.put(somebody.id, tmp);
				//System.out.print("matches.size(): "+ matches.size());
			}
		}
	}
	
	//runs once
	//this method adds all the user to all the data structures 
	public void startUp(){
		String updateUrl = "http://50.19.45.37:8080/rest/retrieveallprofileids";
		Gson gson = new Gson();
		
		Collection<Integer> ids = null;
		try{
			
			URL address = new URL(updateUrl);
			InputStream incoming = new GZIPInputStream( address.openStream() );
			BufferedReader decoder = new BufferedReader( new InputStreamReader(incoming, "UTF-8"));
			
			Type typ = new TypeToken<Collection<Integer>>(){}.getType();
			ids = gson.fromJson(decoder, typ);
			
		}catch(Exception e){
			logger.info("Error in retrieving all profile IDs: "+e.getLocalizedMessage());
		}
		
		//now for each ID create a profile and update it
		//System.out.println("Loading information on " + ids.size() + " users.");
		
		for(int i : ids){
			add( i );
		}
		
		//System.out.println("Profiles loaded.");
	}
	
}
