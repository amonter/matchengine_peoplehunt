package threads;
/*
 * A multi-threaded server for handling the requests and launching other threads (Maestro for the runtime...)
 * Operates on Thread-per-request basis (no theoretical limit on the number of requests).
 * 
 * Future Work:
 * Add config file
 * Add remote administration of the server 
 * 
 */

import java.net.ServerSocket;
import java.net.UnknownHostException;

import model.Runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

//Main file hunt requests
public class PeopleHuntServer {
	/*
	private static boolean running = true; //very important that it's static, as static and non-static mutex locks are different
	private static Object runningMutex = new Object(); //synchronized statements should work with static fields the same way methods do (on Class level)
	*/
	private static Logger logger = LoggerFactory.getLogger(PeopleHuntServer.class);
	
	
	public static void main(String[] args){//setup
		//Settings...use a config file?
		//int port = 7000;
		
		//System.out.println("Starting server..");
		
		//start server`
		//runServer(port);
		
		//start new socket server
		PeopleHuntServer huntServer = new PeopleHuntServer();
		huntServer.runProtocolServer();		
		//connect mongoDB	
		
	}//end of main()


	private void runProtocolServer() {
		
		ServerSocket server = null;
		//List<TargetUser> targets = new ArrayList<TargetUser>();
		//Bundle bundle = new Bundle();
		Runtime rt = new Runtime();
		
		try{
			server = new ServerSocket(1045);			
			logger.info("Running server");
		}catch(Exception e){
			//e.printStackTrace();
			logger.info(e.getMessage());
		}
		
		while(true){
			try{
				//(new Thread( new TargetUser(server.accept(), targets, bundle))).start();
				(new Thread( new UserThread(server.accept(), rt))).start();
			}catch(Exception e){
				e.printStackTrace(); //temp code - FIXME
				//logger.info(e.getMessage());
				break;
			}
		}
		
		try{ //close socket after the server stopped/crashed
			server.close();
		}catch(Exception e){
			//e.printStackTrace();
			logger.info(e.getMessage());
		}
		
	}
	
	/*
	private static boolean isRunning(){ // for future remote management etc
		synchronized(runningMutex){	//elegant
			return running;
		}
	}
	*/
	
}