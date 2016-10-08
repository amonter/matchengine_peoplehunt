import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import json.ProfileUpdate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;



public class HuntServerTest{
	public static void main(String[] args) throws Exception, IOException {
		
		// All the pairs here match one another 
		
		
		
		final List<String> requests = new ArrayList<String>();
		requests.add("http://dev.peoplehunt.me/rest/gethelp2");
	//	requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=44232&bundleId=157&selectedtags=9551");

		//requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=61929&bundleId=159&selectedtags=9580");
		//requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=33381&bundleId=159&selectedtags=9581");

		//requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=61929&bundleId=159&selectedtags=9577");       
        //requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=57302&bundleId=159&selectedtags=9581");
		
		//requests.add("http://prod.crowdscanner.com/addusertobundle/?profileid=1588&action=1&bundleid=162");
		
        //requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=16451&bundleId=159&selectedtags=9584");       
        //requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=33381&bundleId=159&selectedtags=10280");
		//47577
		//requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=47577&bundleId=159&selectedtags=9577");

		
		//requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=17");
		//requests.add("http://prod.crowdscanner.com/rest/retrievehunterprofile/?profileid=18052&bundleid=162");
		
		
	//	requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=44406&bundleId=148&selectedtags=7632");
	//	requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=47490&bundleId=148&selectedtags=7484");//pair one match		
	//	requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=50786&bundleId=148&selectedtags=7482");
	//	requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=32630&bundleId=148&selectedtags=7482");//pair two
	//	requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=19003&bundleId=148&selectedtags=7483");
	//	requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=79962&bundleId=148&selectedtags=7483");//pair three
		
		
/*		//Real server URL 	running live as a daemon in linux		
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=44406&bundleId=148&selectedtags=7632");	
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=47490&bundleId=148&selectedtags=7484");//pair one	
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=50786&bundleId=148&selectedtags=7482");
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=32630&bundleId=148&selectedtags=7482");//pair two
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=19003&bundleId=148&selectedtags=7483");
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=79962&bundleId=148&selectedtags=7483");//pair three
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=74989&bundleId=153&selectedtags=8106");
		requests.add("http://prod.crowdscanner.com/rest/pairinghuntmatching/?myhuntid=31312&bundleId=153&selectedtags=8106");//pair four
*/	
	
		  //addUserToRoom("adrian");
		  //addUserToRoom("ellen");
			
		  //sendMessage("adrian", "ellen");
	
		 
		
		
		
		for (final String theRequest : requests) {	
		
				new Thread(new Runnable(){
					@Override
					public void run() {
						
						try {
							
							URL address = new URL(theRequest);
							InputStream incoming = new GZIPInputStream( address.openStream() );
							BufferedReader decoder = new BufferedReader( new InputStreamReader(incoming, "UTF-8"));
							final Type collectionType = new TypeToken<Collection<Map<String, Object>>>() {}.getType();
							final Gson gson = new Gson();					          
					          
							final Collection<Map<String, Object>> theData = gson.fromJson(decoder, collectionType);
							
							MongoClient mongoClient = new MongoClient( "50.19.45.37" , 27017 );
							DB db = mongoClient.getDB( "peoplehunt" );	
							DBCollection col = db.getCollection("offers");									
							DBCursor cursor = col.find();
							List<Map<String, Object>> mapList = new ArrayList<Map<String,Object>>();
							try {
							   while(cursor.hasNext()) {
								   DBObject theObj = cursor.next();
								   String description = (String) theObj.get("description");
								   double theId = (Double) theObj.get("id");		
								   Map<String, Object> aMap = new HashMap<String, Object>();
								   aMap.put("id", (int)theId);
								   aMap.put("description", description);
								   mapList.add(aMap);
							       
							   }
							} finally {
							   cursor.close();
							}					
					         
							System.out.println(mapList);
							
							
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}				
						

				}				
			}).start();			
				
		}
	}

	private static void addUserToRoom(String userId) throws UnknownHostException, IOException {
		
		String sentence;
		  String modifiedSentence;
		  //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
		  Socket clientSocket = new Socket("localhost", 1044);
		  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		  //sentence = inFromUser.readLine();
		  outToServer.writeBytes("1:"+userId+ '\n');			  
		  modifiedSentence = inFromServer.readLine();
		  System.out.println("FROM SERVER: " + modifiedSentence);
		  clientSocket.close();
	}	
	
	
	private static void sendMessage(String userId, String receiverId) throws UnknownHostException, IOException {
		
		 String fromServer;
		  //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
		  Socket clientSocket = new Socket("localhost", 1044);
		  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		  //sentence = inFromUser.readLine();
		  outToServer.writeBytes("2:"+ userId+":"+receiverId+ '\n');	    
		  fromServer = inFromServer.readLine();
		  while ((fromServer = inFromServer.readLine()) != null) {
		              System.out.println("Server: " + fromServer);
		              if (fromServer.equals("Terminate"))
		                  break;
		  }
		 
		clientSocket.close();
	}	
	
	
}
	
	
	
	
	

