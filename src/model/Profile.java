package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import json.Location;
import json.ProfileInfo;
import json.ProfileUpdate;
import json.UserConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import threads.UserThread;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class Profile {
	private Logger logger = LoggerFactory.getLogger(Profile.class);
	DB mongoDatabase;
	public int id;
	private UserThread connection;
	private ProfileInfo info;
	public Map<Integer, Integer> previousMatches; //profileId & interaction flag
	public Map<Integer, Long> interactionTime; //when we sent the message to the user last
	private long lastActive;
	public List<Integer> newMessages;
 
	public Profile(int id, DB mongoDB){
		mongoDatabase = mongoDB;
		interactionTime = new HashMap<Integer,Long>();
		previousMatches = new HashMap<Integer,Integer>();
		newMessages = new ArrayList<Integer>();
		this.id = id;
		update();
		updateTimestamp();
	}
	
	/* Methods */
	
	public Match matchAndRank(Profile p, boolean oldMatch, int baseRank){ //baseRank to give location based matches higher score
		//calculate and return the rank of this potential connection
		Match m = null;
		synchronized(this){
			synchronized(p){
				//figure out how strongly they match, send that rank back
				//System.out.println( "user's interest:" );
				//this loop is for the interaction time
				for(int x : this.info.interested.keySet()){
					//System.out.print( "\t"+x+": " );
					if(p.info.help.containsKey(x)){
						//System.out.println( "yes" );
						if(m==null){
							if(oldMatch){
								m = new OldMatch(this, p, p.info.help, x, "help", baseRank);
							}
							else{
								m = new Match(this, p, p.info.help, x, "help", baseRank);
							}
							//System.out.println( "Match created" );
						}
						else{
							m.addToMatch(p.info.help, x, "help");
						}
						
					}
					//else{
						//System.out.println( "no" );
					//}
				}
				//System.out.println( "user's help with:" );
				//
				if (this.info.help != null){
					for(int x : this.info.help.keySet()){
						//System.out.print( "\t"+x+": " );
						if(p.info.interested.containsKey(x)){
							//System.out.println( "yes" );
							if(m==null){
								if(oldMatch){
									m = new OldMatch(this, p, p.info.interested, x, "interested", baseRank);
								}
								else{
									m = new Match(this, p, p.info.interested, x, "interested", baseRank);
								}
								//System.out.println( "Match created" );
							}
							else{
								m.addToMatch(p.info.interested, x, "interested");
							}
						}
						//else{
							//System.out.println( "no" );
						//}
					}
				}
			}
		}
		
		return m;
	}
	
	public void updateFromApp(String json){
		Gson gson = new Gson();
		try{
			
			Type typ = new TypeToken<ProfileUpdate>(){}.getType();
			ProfileUpdate update = gson.fromJson(json, typ);
			info.update(update);
			
		}catch(Exception e){
			logger.info("Updating Profile Exception "+e.getLocalizedMessage() +" profileid "+id);
		}
	}
	
	public synchronized void update(){
		String updateUrl = "http://50.19.45.37:8080/rest/retrievehunterprofile/?profileid="+id;
		Gson gson = new Gson();
		
		try{
			
			URL address = new URL(updateUrl);
			InputStream incoming = new GZIPInputStream( address.openStream() );
			BufferedReader decoder = new BufferedReader( new InputStreamReader(incoming, "UTF-8"));
			
			Type typ = new TypeToken<ProfileInfo>(){}.getType();
			info = gson.fromJson(decoder, typ);
			//update interests of exists from mongoDB
				
			DBCollection col = mongoDatabase.getCollection("interests");	
			//logger.info("asking mongo "+id);
			BasicDBObject query = new BasicDBObject("profile_id", id);
			DBCursor cursor = col.find(query);
			DBObject dbObj = null;
			try {
			   while(cursor.hasNext()) {
				   dbObj = cursor.next();					   
			   }
			} finally {
			   cursor.close();
			}
			
			//check if interests
			if (dbObj != null && dbObj.get("interests") != null) {				
				BasicDBObject interestsObj = (BasicDBObject) dbObj.get("interests");				
				BasicDBObject skillsObj = (BasicDBObject) interestsObj.get("skills");
				
				if (skillsObj != null){
					List<Map<String, Object>> theMapList = new ArrayList<Map<String,Object>>();
					BasicDBList valuesObj = (BasicDBList) skillsObj.get("values");
					for (int i = 0; i < valuesObj.size(); i++) {
						BasicDBObject valueObj = (BasicDBObject) valuesObj.get(i);
						BasicDBObject theSkillObj = (BasicDBObject) valueObj.get("skill");
						theMapList.add(theSkillObj);
					}			
					info.personalInterests = theMapList;					
				}
				
				BasicDBObject likesObj = (BasicDBObject) interestsObj.get("likes");
				if (likesObj != null){
					List<Map<String, Object>> theMapList = new ArrayList<Map<String,Object>>();
					BasicDBList likesList = (BasicDBList) likesObj.get("data");
					for (int i = 0; i < likesList.size(); i++) {
						BasicDBObject valueObj = (BasicDBObject) likesList.get(i);						
						theMapList.add(valueObj.toMap());
					}
					info.personalInterests = theMapList;
				}				
			}		
			
			
			//now use the connection in info to pupulate previous matches
			int len = info.connections.length;
			UserConnection user = null;
			for(int i=0; i<len; i++){
				user = info.connections[i];
				if( !previousMatches.containsKey(user.id) ){
					previousMatches.put(user.id, user.status);
				}
			}
			
		}catch(Exception e){
			logger.info("Updating Profile Exception "+e.getLocalizedMessage() +" profileid "+id);
		}
	}
	
	public ProfileInfo getInfo(){
		return info;
	}
	
	
	
	public synchronized void updateInterests(List<Map<String,Object>> theInterests) {
		logger.info("update interest "+theInterests);
		info.personalInterests = theInterests;
	}

	public synchronized void updateTimestamp(){
		Calendar c = Calendar.getInstance();
		lastActive = c.getTimeInMillis();
	}
	
	public synchronized long getLastOnline(){
		return lastActive;
	}
	
	public synchronized void recordSocket(UserThread t){
		connection = t;
		/*
		if(connection == null){
			connection = t;
		}
		else{ 
			//this actually implies we have two of the same clients connecting - or abuse
		}
		*/
	}
	
	public synchronized void interactedWithAnotherUser(int id){
		Calendar c = Calendar.getInstance();
		interactionTime.put(id, c.getTimeInMillis());
	}
	
	public synchronized long getInteractionTime(int id){
		if(interactionTime.containsKey(id)){
			return interactionTime.get(id);
		}
		return 0; //no interaction time
	}
	
	public boolean isOnline(){ //access to this HAS to be synchronized
		return (connection!=null);
	}
	
	public synchronized void goneOffline(UserThread invoker){
		if(connection!=null && invoker.equals(connection)){
			connection = null;
		}
	}
	
	public synchronized boolean sendMessage(String msg){
		if(connection != null){
			return connection.writeResponse(msg);
		}
		return false;
	}
	
	public boolean hasNoPreviousMatches(){
		return previousMatches.isEmpty();
	}
	
	public synchronized String getLastMessage(int userID){
		if(info.lastMessages.containsKey(userID)){
			//System.out.println(userID+" "+info.lastMessages);
			return info.lastMessages.get(userID);
		}
		return "";
	}
	
	public synchronized void updateLastMessage(int userID, String msg){
		newMessages.add(userID);
		info.lastMessages.put(userID, msg);
	}
	
	public synchronized List<Location> getLocationsInCommon(Profile them){
		HashMap<Integer,Boolean> theirLocations = new HashMap<Integer,Boolean>();
		for(Location x : them.info.locations){
			theirLocations.put(x.id, true);
		}
		List<Location> rtn = new ArrayList<Location>();
		for(Location x : info.locations){
			if(theirLocations.containsKey(x.id)){
				rtn.add(x);
			}
		}
		return rtn;
	}
}