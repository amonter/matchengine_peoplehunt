package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import json.Location;
import json.MatchesSnapshot;
import json.ProfileUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/*
 * This is the unified runtime which handles all user interaction. 
 * This is essentially what Bundle used to be, but optimized for our use-case.
 * The idea is that we keep the duration of synchronized calls to the population to a minimum and do all the heavy lifting here.
 * */

public class Runtime{
	private Logger logger = LoggerFactory.getLogger(Runtime.class);
	public Population users; //this is what we'll try to stick into the cloud later
	private Gson gson;

	public Runtime(){
		users = new Population();
		gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PRIVATE).create();
	}
	
	public Profile addUser(int p){
		return users.add(p);
	}
	
	public void getHubLocations(Profile p, String json) {		
		
		Gson gson = new Gson();
		ProfileUpdate aProfile = null;
		Collection<Map<String, Object>> resLocs = null;
		try{
			
			Type typ = new TypeToken<ProfileUpdate>(){}.getType();
			aProfile = gson.fromJson(json, typ);		
			p.getInfo().interested = aProfile.interested;
			
		}catch(Exception e){
			logger.info("Updating hub Profile location Exception ");
		}
		
		//NEW ALG
		//retrieve the locations based in GPS HAcker locations and my locations,
		String updateUrl = "http://50.19.45.37:8080/rest/loclatlong";
		try{
			
			List<Location> locations = aProfile.locations;			
			URL address = new URL(updateUrl);
			URLConnection myURLConnection = address.openConnection();
			InputStream incoming = new GZIPInputStream( address.openStream() );		
			OutputStreamWriter writer = new OutputStreamWriter(myURLConnection.getOutputStream());							
			writer.write(URLEncoder.encode(gson.toJson(locations), "UTF-8"));
			writer.flush();
			BufferedReader decoder = new BufferedReader( new InputStreamReader(incoming, "UTF-8"));			
			Type typ = new TypeToken<Collection<Map<String, Object>>>(){}.getType();
			resLocs = gson.fromJson(decoder, typ);
			
		} catch (Exception ex){}
		
		//then find users who are at those locations and add them to the mix.		
		//The community manager should be included by default and and only include the people who match the Make item
		
		
		
		//What happens when people select the actual coworking space?
		//I just need to filter it out of the rest of the list and that's it	
		
		
		Map<Integer, Object> resMatch = new HashMap<Integer, Object>();
		Map<Integer, Object> resNoMatch = new HashMap<Integer, Object>();
		List<Match> matches = users.hubMatching(p);		
		System.out.println(" "+p.getInfo().interested);
		
		for (Match userMatch : matches) {//retrieve the users who match the make item
			//gets the profile id of the user that match the calling user
			int theProfileId = userMatch.them.getInfo().profileId;
			//doing this to get the actual locations of the user
			Profile theProfile = users.getUser(theProfileId);
			logger.info("HUB: user "+theProfileId+" locations "+theProfile.getInfo().locations);
			//creates hashmap to add the marching locations and maker item
			for (Map<String, Object> backLocation: resLocs) {
				Integer backLocId = (Integer) backLocation.get("id");
				for (Location aLocation : theProfile.getInfo().locations) {
					//check with actual locations from the backend
					if(backLocId.intValue() == aLocation.id){
						addLocationsMap(resMatch, aLocation);
					}
					logger.info("Loc "+aLocation.location);
				}				
			}			
			
			//locations of other users who match only in interest but not location
			List<Location> othersLocation = theProfile.getInfo().locations;
			for (Location location : othersLocation) {
				if (!resMatch.containsKey(location.id)){
					addLocationsMap(resNoMatch, location);
				}
			}			
		}
		//order the hashMap of map that matches only in make item
		List<Map<Integer, Object>> listMapNoMatch = new ArrayList<Map<Integer,Object>>();
		for (Map.Entry<Integer, Object> theMap : resNoMatch.entrySet()) {
			Map<Integer, Object> mapInsert = new HashMap<Integer, Object>();
			mapInsert.put(theMap.getKey(), theMap.getValue());
			listMapNoMatch.add(mapInsert);
		}
		
		//just sort the map based on the count of map that does not match
		Collections.sort(listMapNoMatch, new Comparator<Map<Integer, Object>>() {
			@SuppressWarnings("unchecked")
			public int compare(Map<Integer, Object> one, Map<Integer, Object> two) {
				List<Integer> keysOne = new ArrayList<Integer>(one.keySet());
				Map<String, Object> theInnerValsOne = (Map<String, Object>) one.get(keysOne.get(0));				
				Integer theCountOne = (Integer)theInnerValsOne.get("count");				
				List<Integer> keysTwo = new ArrayList<Integer>(two.keySet());
				Map<String, Object> theInnerValsTwo = (Map<String, Object>) two.get(keysTwo.get(0));				
				Integer theCountTwo = (Integer)theInnerValsTwo.get("count");
				return theCountOne.compareTo(theCountTwo);
			}
		});
		Collections.reverse(listMapNoMatch); 
		//insert the match locations to the list of the no matching locations to we get an ordered list of items and so on
		for (Map.Entry<Integer, Object> theMap : resMatch.entrySet()) {
			Map<Integer, Object> mapInsert = new HashMap<Integer, Object>();
			mapInsert.put(theMap.getKey(), theMap.getValue());
			listMapNoMatch.add(0, mapInsert);
		}
				
		logger.info("RES match "+listMapNoMatch);		
		p.sendMessage( String.format("55:%1$s", gson.toJson(listMapNoMatch) ));
	}

	private void addLocationsMap(Map<Integer, Object> res, Location aLocation) {
		if (res.containsKey(aLocation.id)){						
			@SuppressWarnings("unchecked")
			Map<String, Object> theInnerVals = (Map<String, Object>) res.get(aLocation.id);
			int theCount = (Integer)theInnerVals.get("count");
			theCount++;
			logger.info("extra count"+aLocation.id+" count "+theCount);
			theInnerVals.put("count", theCount);
			res.put(aLocation.id, theInnerVals);
			
		} else {
			Map<String, Object> innerVals = new HashMap<String, Object>();
			innerVals.put("location", aLocation);
			innerVals.put("count", 1);
			res.put(aLocation.id, innerVals);
		}
	}
	
	public void getMatches(Profile p, String json){
		//update the users profile info first
		logger.info("init getMatches");
		synchronized(p){
			//removing hashTables that match the location 
			logger.info("before loc");
			users.removeFromLocations(p);
			//update all the profile info to runtime
			logger.info("after loc");
			p.updateFromApp(json);
			//
			logger.info("before up");
			users.addToLocations(p);
			logger.info("after up");
			
			
		}
		//now do the matches
		logger.info("before getmatch");
		MatchesSnapshot snapshot = users.getMatches(p);
		logger.info("after getmatch");
		sendMatches(p, snapshot);
	}
	
	public void sendMatches(Profile user, MatchesSnapshot snapshot){
		final String matchesToSend = gson.toJson(snapshot.newOnes);	
		logger.info("sending matches "+matchesToSend);
		user.sendMessage( String.format("88:%1$s", gson.toJson(snapshot) ));
		final int userId = user.id;
		/*try{
			new Thread(new Runnable(){ //send store matches
				@Override
				public void run() {
					try{
						
						URL address = new URL("http://50.19.45.37:8080/rest/postconnections/"+userId);										 						 
						URLConnection conn = address.openConnection();	
						conn.setDoOutput(true);
						conn.setRequestProperty("Accept-Charset", "ISO-8859-1,UTF-8;q=0.7,*;q=0.7");				
						conn.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,q=0.8");
						OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());							
						writer.write(URLEncoder.encode(matchesToSend, "UTF-8"));
						writer.flush();

						BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));							
				
						String line;
						while ((line = rd.readLine()) != null) {
							System.out.println(line);
						}	
						rd.close();
					}catch(Exception e){
						logger.info(e.getMessages());  
					}	
				}
			}).start();	
		}catch(Exception e){
			logger.info(e.getMessage());  
		}*/
	}
	
	public boolean sendMessageToUser(String msg, int userID){
		return users.sendMessageToUser(msg, userID);
	}
	
	public void usersInteracted(Profile user, int otherUser){
		user.interactedWithAnotherUser(otherUser);
		Profile other = users.getUser(otherUser);
		if(other != null){
			other.interactedWithAnotherUser(user.id);
		}//else couldn't record interaction because user not there
	}
	
	
	@SuppressWarnings("unchecked")
	public void updateUserInterests(Profile p, String json){
		
		try {
			
			Gson gson = new Gson();   		
			Map<String, Object> map = new HashMap<String, Object>();		
			Map<String, Object> interestData = gson.fromJson(URLDecoder.decode(json, "UTF-8"), map.getClass());
			Map<String, Object> interests = ((Map<String, Object>) interestData.get("likes"));
			if (interests != null){
				List<Map<String, Object>> mapInterests = (List<Map<String, Object>>) interests.get("data");
				
				logger.info("JSON Interests "+ mapInterests.subList(1, mapInterests.size()));
				int subIndex = 3;
				if (mapInterests.size() < 3) subIndex = mapInterests.size();
				users.getUser(p.id).updateInterests(mapInterests.subList(1, subIndex));				
				users.addToLocations(users.getUser(p.id));
			} else {			
				Map<String, Object> skills = ((Map<String, Object>) interestData.get("skills"));
				if (skills.size() > 0){
					List<Map<String, Object>> skillValues =  (List<Map<String, Object>>) skills.get("values");
					List<Map<String, Object>> skillNames = new ArrayList<Map<String, Object>>();
					int subIndex = 4;
					if (skillValues.size() < 4) subIndex = skillValues.size();
					for (Map<String, Object> allSkills : skillValues.subList(1, subIndex)) {
						Map<String, Object> skillName = (Map<String, Object>) allSkills.get("skill");
						skillNames.add(skillName);
					}
					users.getUser(p.id).updateInterests(skillNames);
					users.addToLocations(users.getUser(p.id));
				}				
			}
			
			
			logger.info("retrieve interestes "+users.getUser(p.id).getInfo().personalInterests);
			
			//p.getInfo().personalInterests = 
			
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
	public void updateStatus(Profile p, int otherProfileId, int status){
		logger.info("update Status "+otherProfileId);
		p.previousMatches.put(otherProfileId, status);
				
	}
}
