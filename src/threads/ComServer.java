package threads;

import java.net.ServerSocket;
import model.Runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComServer extends Thread{
	private Logger logger = LoggerFactory.getLogger(ComServer.class);
	private final Object runningMutex = new Object();
	private boolean running = false;
	private int port = 6001;
	
	public ComServer(){}
	public ComServer(int p){
		port = p;
	}
	
	public static void main(String[] args){
		//check was port specified?
		ComServer server = new ComServer();
		server.start();
	}
	
	@Override
	public void run(){
		ServerSocket server = null;
		Runtime runtime = new Runtime();
		
		try{
			server = new ServerSocket(port);
			running = true;
			logger.info("Server online.");
			while(isRunning()){				
				(new Thread( new UserThread(server.accept(), runtime) )).start(); //launch new thread to handle the request	
			}
			server.close();
		}catch(Exception e){//
			logger.info(e.getLocalizedMessage());
		}
		logger.info("Server offline.");
		running = false;
	}

	private boolean isRunning(){
		synchronized(runningMutex){
			return running;
		}
	}
}
