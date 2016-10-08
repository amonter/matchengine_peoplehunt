package threads;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import model.Profile;
import model.Runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

/*
 * This is like a low-level wrap of the functionality, making underlying runtime more efficient.
 */

public class UserThread extends Thread{
	private Logger logger = LoggerFactory.getLogger(UserThread.class);
	private Runtime runtime;
	private Socket conn;
	private BufferedReader in;
	private OutputStreamWriter out;
	//private BufferedWriter out;
	private Profile user;
	private String name;
	private int timeout;
	
	public UserThread(Socket s, Runtime rt){
		runtime = rt;
		conn = s;
		timeout = 1200000;
	}
	
	@Override
	public void run(){
		String msg = null;
		try{
			conn.setKeepAlive(true);
			conn.setSoTimeout(timeout);
			in = new BufferedReader( new InputStreamReader(conn.getInputStream(), "UTF-8") );
			//out = new BufferedWriter( new OutputStreamWriter(conn.getOutputStream()) );
			out = new OutputStreamWriter( conn.getOutputStream(), Charset.forName("UTF-8").newEncoder() );
			//not sure how this actually worked for Adrian, will have to have a look on the client-side
			while((msg = in.readLine()) != null){
				logger.info("coming in to server "+msg);
				if(processProtocol(msg)){
					break;
				}
			}	
		}
		catch(SocketTimeoutException e){
			//no active input for too long, exit gracefully
			userGoneOffline();
		}
		catch(Exception e){
			logger.info("execeptio level "+e.getMessage());
			
		}
		
		try{//clean up
			in.close();
			out.close();
			conn.close();
		}catch(Exception e){
			logger.info("exception level two "+e.getMessage());
		}
	}
	
	private boolean processProtocol(String msg){
		boolean rtn = false;	//true indicates we want to break the socket connection
		String[] protocol = msg.split(":");
		int targetID;
		String info;
		switch(Integer.valueOf(protocol[0])){
			case 0:
				//echo back
				writeResponse("0:echo");
				break;
			case 1:
				//add user to runtime, notify [matched] active users (s)he's online
				name = protocol[2];
				user = runtime.addUser( Integer.valueOf(protocol[1]) );
				user.recordSocket(this);
				writeResponse("1:echo");

				break;
			case 7:
				//add user interests
				Gson gson = new Gson();
				final Map<String, Object> map = new HashMap<String, Object>();
				int indexStatus0 = nthOccurrence(msg, ':', 1);
				final String jsonStatus0 = msg.substring(indexStatus0 + 1);
				runtime.updateUserInterests(user, jsonStatus0);
				
			
				try {
					
					MongoClient mongoClient = new MongoClient( "50.19.45.37" , 27017 );
					DB db = mongoClient.getDB( "peoplehunt" );	
					DBCollection col = db.getCollection("interests");			
					
					BasicDBObject query = new BasicDBObject("profile_id", user.id);
					DBCursor cursor = col.find(query);
					boolean hasValue = false;
					if (cursor.hasNext()) hasValue = true;
					
					if(!hasValue) {				
						Map<String, Object> interestData = gson.fromJson(URLDecoder.decode(jsonStatus0, "UTF-8"), map.getClass());
						Map<String, Object> profileInterests = new HashMap<String, Object>();
						profileInterests.put("profile_id", user.id);
						profileInterests.put("interests", interestData);
						
						BasicDBObject theObj = new BasicDBObject(profileInterests);
						col.insert(theObj);		
					}
					cursor.close();
					
					
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					logger.info(e1.getMessage());  
					e1.printStackTrace();
				} catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					logger.info(e.getMessage());  
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					logger.info(e.getMessage());  
					e.printStackTrace();
				}
				
				break;
			case 20:
				//can't meet, find the user to send the message to
				targetID = Integer.valueOf(protocol[1]);
				info = protocol[2]; //location clue
				runtime.sendMessageToUser(String.format("20:%1$s %2$s", name, info), targetID);
				break;
			case 30:
				//need more time
				targetID = Integer.valueOf(protocol[1]);
				info = protocol[2]; //location clue
				runtime.sendMessageToUser(String.format("30:%1$s needs more time to get to %2$s. Can you wait?", name, info), targetID);
				break;
			case 31:
				//waiting
				targetID = Integer.valueOf(protocol[1]);
				info = protocol[2]; //location clue
				runtime.sendMessageToUser(String.format("31:%1$s is waiting for you %2$s", name, info), targetID);
				break;
			case 32:
				//can't wait any more
				targetID = Integer.valueOf(protocol[1]);
				runtime.sendMessageToUser(String.format("32:Sorry, %1$s can't wait any longer for you. You can try again later", name), targetID);
				break;
			case 40:
				//chat - either send the message to the recipient or back to sender
				//final int recipientID = Integer.valueOf(protocol[1]);
				final String allRecipients = protocol[1];
				String[] individualRecipients = allRecipients.split(",");				
				final String msgContent = protocol[2]; //msg content
				String msgNumber = protocol[3];	
				List<Map<String, Object>> theMessages = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < individualRecipients.length; i++) {
					messageAction(Integer.valueOf(individualRecipients[i]), msgContent, msgNumber);
					//user.previousMatches.put(Integer.valueOf(individualRecipients[i]), 1);
					runtime.updateStatus(user, Integer.valueOf(individualRecipients[i]), -1);
					Map<String, Object> connection = new HashMap<String, Object>();
					connection.put("profile_id", individualRecipients[i]);
					connection.put("status", 0);
					theMessages.add(connection);					
				}			
				
				Gson gson2 = new Gson();				
				final String jsonData = gson2.toJson(theMessages);					
				try{
				new Thread(new Runnable(){ //store messages sent
					@Override
					public void run() {
						try{
							URL address = new URL("http://50.19.45.37:8080/rest/changeconnectionstatus/"+user.id);										 						 
							URLConnection conn = address.openConnection();	
							conn.setDoOutput(true);
							conn.setRequestProperty("Accept-Charset", "ISO-8859-1,UTF-8;q=0.7,*;q=0.7");				
							conn.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,q=0.8");
							OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());							
							writer.write(URLEncoder.encode(jsonData, "UTF-8"));
							writer.flush();

							BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));							
					
							String line;
							while ((line = rd.readLine()) != null) {
								System.out.println(line);
							}	
							rd.close();
						}catch(Exception e){
							logger.info(e.getMessage());  
						}	
					}
				}).start();	
				}catch(Exception e){
					logger.info(e.getMessage());  
				}
				
				
				break;				
			case 50:
				//share location
				targetID = Integer.valueOf(protocol[1]);
				info = protocol[2]; //location
				runtime.sendMessageToUser(String.format("50:%1$s:%2$s:%3$d", name, info, user.id), targetID);
				break;
				
			case 55:
				//get hub Locations based on Interest
				int indexProfileHub = nthOccurrence(msg, ':', 1);
				final String jsonProfileInfoHub = msg.substring(indexProfileHub + 1);
				runtime.getHubLocations(user, jsonProfileInfoHub);	
				break;
				
			case 88:
				//matches				
				int indexProfile = nthOccurrence(msg, ':', 1);				
				final String jsonProfileInfo = msg.substring(indexProfile + 1); 
				logger.info("json profile "+jsonProfileInfo);				
				//Runtime Object abstraction and proxy for all other calls intermediate the data and user thread like a house keeper
				//get marches update profileIndo data 
				logger.info("before "+user);
				logger.info("id "+user);
				runtime.getMatches(user, jsonProfileInfo);				
				try{
					new Thread(new Runnable(){ //store messages sent
						@Override
						public void run() {
							try{
								URL address = new URL("http://50.19.45.37:8080/rest/addallfeelers/"+user.id);										 						 
								URLConnection conn = address.openConnection();	
								conn.setDoOutput(true);
								conn.setRequestProperty("Accept-Charset", "ISO-8859-1,UTF-8;q=0.7,*;q=0.7");				
								conn.setRequestProperty("Accept", "texxt/plain,text/html,application/xhtml+xml,application/xml;q=0.9,q=0.8");
								OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());							
								writer.write(URLEncoder.encode(jsonProfileInfo, "UTF-8"));
								writer.flush();

								BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));							
						
								String line;
								while ((line = rd.readLine()) != null) {
									System.out.println(line);
								}	
								rd.close();
							}catch(Exception e){
								logger.info("EX "+e.getMessage());  
							}	
						}
					}).start();	
				}catch(Exception e){
					logger.info("EX "+e.getMessage());  
				}
				break;
			case 89:
				//update user info + proxy info to Adrians backend
				//int indexStatus = nthOccurrence(msg, ':', 1);				
				Integer otherUserId = Integer.valueOf(protocol[1]);
				//logger.info("json status "+jsonStatus);
				Profile otherProfile = runtime.users.getUser((int)otherUserId);
				otherProfile.previousMatches.put(user.id, 1);							
				/*try{
					new Thread(new Runnable(){ //store messages sent
						@Override
						public void run() {
							try{
								URL address = new URL("http://50.19.45.37:8080/rest/changeconnectionstatus/"+user.id);										 						 
								URLConnection conn = address.openConnection();	
								conn.setDoOutput(true);
								conn.setRequestProperty("Accept-Charset", "ISO-8859-1,UTF-8;q=0.7,*;q=0.7");				
								conn.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,q=0.8");
								OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());							
								writer.write(URLEncoder.encode(jsonStatus, "UTF-8"));
								writer.flush();

								BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));							
						
								String line;
								while ((line = rd.readLine()) != null) {
									System.out.println(line);
								}	
								rd.close();
							}catch(Exception e){
								logger.info(e.getMessage());  
							}	
						}
					}).start();	
				}catch(Exception e){
					logger.info(e.getMessage());  
				}*/				
				break;
			case 101:
				//message confirmation
				targetID = Integer.valueOf(protocol[1]);				
				info = protocol[2]; //location clue
				runtime.sendMessageToUser(String.format("101:%1$s", info), targetID);
				break;				
			case 113://get all messages
				targetID = Integer.valueOf(protocol[1]);
				final int profileId = Integer.valueOf(protocol[1]);
				//copy-paste, mostly - will move if I find the time
				try{
					new Thread(new Runnable(){ //store messages sent
						@Override
						public void run() {
							String updateUrl = "http://50.19.45.37:8080/rest/sentmessages?profileid="+profileId;
							Gson gson = new Gson();							
							Collection<Map<String, Object>> ids = null;
							
							try{
								
								URL address = new URL(updateUrl);
								InputStream incoming = new GZIPInputStream( address.openStream() );
								BufferedReader decoder = new BufferedReader( new InputStreamReader(incoming, "UTF-8"));
								
								Type typ = new TypeToken<Collection<Map<String, Object>>>(){}.getType();
								ids = gson.fromJson(decoder, typ);				
								for (Map<String, Object> individualMessage : ids) {
									double otherProfileId = (Double)individualMessage.get("profile_id");
									Profile otherProfile = runtime.users.getUser((int)otherProfileId);
									if (otherProfile.previousMatches.containsKey(profileId)){
										Integer interactionFlag = otherProfile.previousMatches.get(profileId);
										if (interactionFlag == -1){
											individualMessage.put("new_message", true);
											logger.info("NEW MESSAGE "+otherProfileId+" my profile "+profileId);
										}
									}
								}								
								
								writeResponse("113:"+gson.toJson(ids));
								//logger.info("RES MESSAGES "+ids);
								
							} catch(Exception e){
								logger.info("Error in retrieving all profile IDs: "+e.getLocalizedMessage());
							}
						}
					}).start();
				}catch(Exception e){
					System.out.println(e.getMessage());
					logger.info(e.getMessage());  
				}
				
				break;
			default:
				//exception so to speak - we should close the socket//remove user from runtime
				rtn = true;
				break;
		}
		user.updateTimestamp();
		return rtn;
	}

	private void messageAction(final int recipientID, final String msgContent,
			String msgNumber) {
		if( !runtime.sendMessageToUser(String.format("40:%1$d:%2$s:%3$s", user.id, msgContent, msgNumber), recipientID) ){
			//we didn't succeed in sending the message to the user, notify this user
			user.sendMessage( String.format("40:%1$d:%2$d",  recipientID, user.id) ); //need to check the App side of things to make sense of this
		}
		else{
			
		}
		
		user.updateLastMessage(recipientID, msgContent); //msg was sent
		//interaction has to always be recorded independently if the other user is online or not
		//user.interactedWithAnotherUser(recipientID);
		runtime.usersInteracted(user, recipientID); //records both ways
		
		//copy-paste, mostly - will move if I find the time
		try{
			new Thread(new Runnable(){ //store messages sent
				@Override
				public void run() {
					try{
						URL address = new URL("http://50.19.45.37:8080/rest/postnewmessage/"+user.id+"/"+recipientID);										 						 
						URLConnection conn = address.openConnection();	
						conn.setDoOutput(true);
						conn.setRequestProperty("Accept-Charset", "ISO-8859-1,UTF-8;q=0.7,*;q=0.7");				
						conn.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,q=0.8");
						OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());							
						writer.write(URLEncoder.encode(msgContent, "UTF-8"));
						writer.flush();

						BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));							
				
						String line;
						while ((line = rd.readLine()) != null) {
							System.out.println(line);
						}	
						rd.close();
					}catch(Exception e){
						logger.info(e.getMessage());  
					}	
				}
			}).start();
		}catch(Exception e){
			logger.info(e.getMessage());  
		}
	}
	
	
	public int nthOccurrence(String str, char c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}
	
	//add synchronize in this method if I wanr to call it directly.
	public boolean writeResponse(String msg){ //synchronization is up to the higher level objects
		boolean success = false;
		try{

			logger.info("user 2"+user.id+" message " +msg);
			out.write( msg +"\r\n");
			out.flush();
			success = true;
		}catch(Exception e){
			logger.info(e.getLocalizedMessage());
			success = false;
		}
		
		//debug
		//System.out.println(msg);
		
		return success;
	}
	
	public void userGoneOffline(){
		if(user != null){
			user.goneOffline(this);
		}
	}
}
