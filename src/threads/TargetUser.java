//package threads;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.lang.reflect.Modifier;
//import java.net.Socket;
//import java.net.URL;
//import java.net.URLConnection;
//import java.net.URLEncoder;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import model.Bundle;
//import model.Profile;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//
//
//public class TargetUser extends Thread {
//
//	private Socket socket = null;
//	public BufferedReader in = null;
//	//private DataOutputStream out;
//	//public PrintWriter out = null;
//	private OutputStreamWriter out = null;
//	private List<TargetUser> targets;
//	private Integer targetId;
//	private String name;	
//	private Bundle bundle;
//	private static Logger logger = LoggerFactory.getLogger(TargetUser.class);
//	
//	
//	
//	
//	public TargetUser(Socket client, List<TargetUser> targetList, Bundle theBundle){
//		targets = targetList;
//		socket = client;		
//		bundle = theBundle;
//		try {
//			client.setKeepAlive(true);
//            /* obtain an input stream to this client ... */
//            in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
//            /* ... and an output stream to the same client */           
//            out = new OutputStreamWriter(client.getOutputStream(),  Charset.forName("UTF-8").newEncoder());
//        } catch (IOException e) {
//            System.err.println(e);
//            return;
//        }	
//	}
//	
//	@Override
//	public void run() {		
//		
//		String msgReceived;		
//		String res = null;
//		for (TargetUser theTarget : targets) {
//			logger.info("added target "+theTarget.targetId);
//		}			
//		try {	
//			
//			while ((msgReceived = in.readLine()) != null) {
//				logger.info("coming in to server "+msgReceived);		        
//		        res = processProtocol(msgReceived);	    		
//		        logger.info("sent "+res);
//		        if (!res.equals("block")){
//		        		System.out.println("sending");
//		    			//out.println(res+"\r\n");
//		        }
//		    		if (res.equals("done") || res.equals("remove") ){
//		    			break;	
//		    		}		        	       
//			}			
//		
//			
//			for (Iterator<TargetUser> it = targets.iterator(); it.hasNext();) {				
//				if (targetId.equals(it.next().targetId)){
//					it.remove();
//					logger.info("remove "+targetId);
//					bundle.quickRemove(targetId);
//				}				
//			}			
//			
//			out.close();
//			socket.close();
//	        
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//              
//	}
//	
//
//
//	private String processProtocol(String msgReceived) {
//		
//		String[] protocolArray = msgReceived.split(":");
//		String result = "error";
//		Profile theProfile = null;
//		//get the action of the protocol
//		switch (Integer.valueOf(protocolArray[0])) {
//		case 0:
//			//just echo back
//			sendResponseActionOne("0:echo");
//			break;
//		case 1:	//add user to the system			
//			targetId = Integer.valueOf(protocolArray[1]);
//			logger.info("adding "+targetId);
//			TargetUser receiver2 = retrievetarget(Integer.valueOf(targetId));
//			if (receiver2 != null){
//				//doRemoveAction("remove");
//			}			
//			name = protocolArray[2];
//			for (Iterator<TargetUser> it = targets.iterator(); it.hasNext();) {
//				TargetUser theTarget = it.next();
//				if (theTarget.targetId.intValue() == targetId.intValue()){
//					it.remove();
//				}
//			}			
//			
//			targets.add(this);			
//			theProfile = new Profile(targetId, 3033);
//			bundle.add(theProfile);			
//			//notify my matching connections I'm alive
//			List<Profile> profiles = retrieveMatchedActiveUsers(targetId, true);	
//			//addLiveProfiles(profiles);
//			for (Profile otherProfile : profiles) {
//				notifyOnline(theProfile, otherProfile);
//			}	
//			result = "1:added";			
//			
//			break;
//		case 2:	//remove user from the system
//			result = doRemoveAction(result);
//			
//			break;
//		case 20: //can't meet protocol	
//			result = "error";
//			String receiverId = protocolArray[1];
//			String clueLocation = protocolArray[2]; 
//			TargetUser receiver = retrievetarget(Integer.valueOf(receiverId));
//			if (receiver != null){
//				 String msg = String.format("20:%1$s %2$s", name, clueLocation);
//				 receiver.sendResponseActionOne(msg);
//				 result = "sent";
//			}			
//			break;
//		case 30:	// need more time protocol
//			result = "error";
//			String receiverId2 = protocolArray[1];
//			String clueLocation2 = protocolArray[2]; 
//			TargetUser receiver3 = retrievetarget(Integer.valueOf(receiverId2));
//			if (receiver3 != null){
//				 String msg2 = String.format("30:%1$s needs more time to get to %2$s. Can you wait?", name, clueLocation2);
//				 receiver3.sendResponseActionOne(msg2);
//				 result = "sent";
//			}			
//			break;
//		case 31: //wait protocol
//			result = "error";
//			String receiverId3 = protocolArray[1];
//			String clueLocation3 = protocolArray[2]; 
//			TargetUser receiver4 = retrievetarget(Integer.valueOf(receiverId3));
//			if (receiver4 != null){
//				 String msg3 = String.format("31:%1$s is waiting for you %2$s", name, clueLocation3);
//				 receiver4.sendResponseActionOne(msg3);
//				 result = "sent";
//			}			
//			break;
//		case 32:
//			result = "error";
//			String receiverId4 = protocolArray[1];
//			//String clueLocation4 = protocolArray[2]; 
//			TargetUser receiver6 = retrievetarget(Integer.valueOf(receiverId4));
//			if (receiver6 != null){
//				 String msg4 = String.format("32:Sorry, %1$s can't wait any longer for you. You can try again later", name);
//				 receiver6.sendResponseActionOne(msg4);
//				 result = "sent";
//			}			
//			break;		
//		case 40://open chat
//			result = "error";
//			final Integer receiverId5 = Integer.valueOf(protocolArray[1]);			
//			final String content = protocolArray[2];	
//			String number = protocolArray[3];
//			System.out.println(receiverId5);
//			TargetUser receiver5 = retrievetarget(receiverId5);			
//			if (receiver5 != null){
//				 String msg5 = String.format("40:%1$d:%2$s:%3$s", targetId, content, number);
//				 receiver5.sendResponseActionOne(msg5);								 
//			} else {
//				sendResponseActionOne(String.format("40:%1$d:%2$d",  receiverId5, targetId));
//				//sendResponseActionOne(String.format("40:%1$d:error", targetId, content));
//			}	
//			
//			try{//store messages sent
//				new Thread(new Runnable() {								
//					@Override
//					public void run() {
//						try{
//							
//							URL address = new URL("http://dev.crowdscanner.com/rest/postnewmessage/"+targetId+"/"+receiverId5);										 						 
//							URLConnection conn = address.openConnection();	
//							conn.setDoOutput(true);
//							conn.setRequestProperty("Accept-Charset", "ISO-8859-1,UTF-8;q=0.7,*;q=0.7");				
//							conn.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,q=0.8");
//							OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());							
//							writer.write(URLEncoder.encode(content, "UTF-8"));
//							writer.flush();
//
//							BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));							
//					
//							String line;
//							while ((line = rd.readLine()) != null) {
//								System.out.println(line);
//							}	
//							rd.close();
//							}catch(Exception e){
//								logger.info(e.getMessage());  
//							}	
//						}
//					}).start();
//				}catch(Exception e){
//					logger.info(e.getMessage());  
//				}
//				result = "sent";
//			
//			break;
//			
//		case 50://share location
//			result = "error";
//			Integer receiverId6 = Integer.valueOf(protocolArray[1]);			
//			String content2 = protocolArray[2];
//			TargetUser receiver8 = retrievetarget(receiverId6);
//			if (receiverId6 != null){
//				 String msg7 = String.format("50:%1$s:%2$s:%3$d", name, content2, targetId);
//				 receiver8.sendResponseActionOne(msg7);
//				 result = "sent";
//			}
//			break;
//		case 80://refresh retrieve active users			
//			bundle.quickRemove(targetId);
//			Profile profile = new Profile(targetId, 3033);
//			bundle.quickAdd(profile);
//			final List<Profile> allProfiles = retrieveMatchedActiveUsers(targetId, true);
//			sentMatchingConnections(allProfiles);	
//			final Profile aProfile = theProfile;
//			new Thread( new Runnable() {				
//				@Override
//				public void run() {	
//					for (Profile otherProfile : allProfiles) {
//						notifyOnline(aProfile, otherProfile);
//					}						
//				}
//			}).start();									
//			result = "block";
//			break;
//		case 101://message confirmation
//			String receiverId7 = protocolArray[1];		
//			String content7 = protocolArray[2];	
//			TargetUser receiver7 = retrievetarget(Integer.valueOf(receiverId7));
//			String msg7 = String.format("101:%1$s",  content7);
//			receiver7.sendResponseActionOne(msg7);	
//			break;
//		default:
//			break;
//		}
//		
//		return result;
//	}
//
//	private void addLiveProfiles(List<Profile> profiles) {
//		Collection<Profile> allLiveProfiles = bundle.getMembers().values();
//		final List<Map<String, Object>> notifications = new ArrayList<Map<String,Object>>();
//		for (Profile liveProfile : allLiveProfiles) {
//			if (!profiles.contains(liveProfile) && liveProfile.getId() != targetId.intValue()){
//				liveProfile.setMatchItem("\r\n");
//				profiles.add(liveProfile);
//				createNotificationObj(liveProfile, notifications);
//			}
//		}		
//		
//		if (notifications.size() > 0){
//			try{
//				new Thread(new Runnable() {								
//					@Override
//					public void run() {
//						try{
//							Gson gson = new Gson();
//							URL address = new URL("http://dev.crowdscanner.com/rest/postnotification");							 						 
//							URLConnection conn = address.openConnection();	
//							conn.setDoOutput(true);
//							OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
//							String json =  gson.toJson(notifications);		
//							logger.info("live notifications "+ json);
//							writer.write(String.format("jsondata=%1$s", json));
//							writer.flush();
//	
//							BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//	
//							String line;
//							 while ((line = rd.readLine()) != null) {
//								 System.out.println(line);
//							  }	
//							  rd.close();
//						}catch(Exception e){
//							logger.info(e.getMessage());  
//						}
//					}
//				}).start();
//			}catch(Exception e){
//				logger.info(e.getMessage());  
//			}
//		}
//	}
//
//	private void createNotificationObj(Profile profile, List<Map<String, Object>> notifications) {
//		Map<String, Object> theMap = new HashMap<String, Object>();
//		theMap.put("senderid", targetId);
//		theMap.put("profileid", profile.getId());
//		theMap.put("matchtext","\r\n");
//		theMap.put("shares", false);	
//		System.out.println("adding to notify "+theMap.get("profileid")+" "+theMap.get("senderid"));
//		notifications.add(theMap);
//
//
//	}
//	
//	private String doRemoveAction(String result) {
//		for (Iterator<TargetUser> it = targets.iterator(); it.hasNext();) {				
//			if (targetId.equals(it.next().targetId)){
//				result = "remove";
//				it.remove();
//			}				
//		}
//		return result;
//	}
//
//	private void notifyOnline(Profile theProfile, Profile otherProfile) {
//		TargetUser theTarget = retrievetarget(otherProfile.id);			
//		Gson gson2 = new GsonBuilder()
//		 .excludeFieldsWithModifiers(Modifier.PRIVATE)
//		 .create();		
//		theProfile.setMatchItem(otherProfile.getMatchItem());		
//	
//		String msg10 = String.format("110:%1$s",  gson2.toJson(theProfile));		
//		theTarget.sendResponseActionOne(msg10);
//	}
//
//	private void sentMatchingConnections(List<Profile> profiles) {			
//		//sendAliveNowResponse
//		 Gson gson2 = new GsonBuilder()
//		 .excludeFieldsWithModifiers(Modifier.PRIVATE)
//		 .create();		
//		String msg9 = String.format("80:%1$s",  gson2.toJson(profiles));
//		sendResponseActionOne(msg9);		
//	}
//
//	
//	public List<Profile> retrieveMatchedActiveUsers(Integer profileId, boolean isReverse) {
//		List<Profile> profiles = bundle.match(profileId, isReverse);		
//		return profiles;		
//	}
//	
//	private TargetUser retrievetarget(int retrieveTarget) {
//		for (TargetUser theTarget : targets) {
//			if (retrieveTarget == theTarget.targetId.intValue()){							
//				return theTarget;		
//			}
//		}
//		return null;
//	}
//	
//	
//	public void sendResponseActionOne (String message) {				
//		
//		try {
//			logger.info("send 2: "+targetId+" "+message);	
//			out.write(message+"\r\n");
//			out.flush();
//			/*PrintWriter writer = new PrintWriter(out);
//			if (!writer.checkError()) {
//				writer.println(message+"\r\n");		
//				writer.flush();
//			} else {
//				logger.info("oops connection is lost for: "+targetId);
//				//doRemoveAction("remove");
//			}*/
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//	}
//	
//	
//	@Override
//	public boolean equals(Object obj) {		
//		TargetUser otherTargetUser = (TargetUser) obj;
//		boolean isTrue = false;
//		if (this.targetId.equals(otherTargetUser.targetId)) {
//			isTrue = true;			
//		}				
//		return isTrue;
//	}
//	
//	@Override
//	public int hashCode() {
//		// TODO Auto-generated method stub
//		return 286623455;
//	}
//	
//}
