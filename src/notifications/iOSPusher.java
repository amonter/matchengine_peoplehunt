package notifications;


import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javapns.Push;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.devices.implementations.basic.BasicDevice;
//import javapns.notification.PushedNotification;
//import javapns.notification.ResponsePacket;


//import org.apache.log4j.*;

public class iOSPusher {
	
	private Logger logger;
	
	public iOSPusher(){
		java.security.Security.addProvider(new BouncyCastleProvider());
		logger = LoggerFactory.getLogger(iOSPusher.class);
		/*
		try {
	        BasicConfigurator.configure();
		}catch(Exception e){
			logger.info(e.getMessage());
		}
		*/
	}
	
	public void send(String appleDeviceID, String Text){
		try{
			
			//Push.alert(Text, "/usr/local/tomcat/peoplepush2.p12", "espana19", true, appleDeviceID);	
			Push.combined(Text, 0, "default", "/usr/local/tomcat/peoplepush2.p12", "espana19", true, appleDeviceID);
		/*	List<PushedNotification> result = Push.alert(Text, "peoplepush2.p12", "espana19", true, appleDeviceID);
			for (PushedNotification pushedNotification : result) {
				
				if (!pushedNotification.isSuccessful()) {
					Exception theProblem = pushedNotification.getException();
					logger.info("error ios "+theProblem.getMessage());

				
					ResponsePacket theErrorResponse = pushedNotification.getResponse();
					if (theErrorResponse != null) {
						logger.info(" data packet "+theErrorResponse.getMessage());
					}
					
                 } else {
                	 	
                	 	logger.info("Success send to"+appleDeviceID+" "+Text);
                 }
			}
		*/	
			
		}catch(Exception e){
			//e.printStackTrace();
			logger.info(e.getMessage());
		}
	}
	
	public void sendMultiple(List<String> appleDeviceIDs, List<String> messages){
		try{
			List<PayloadPerDevice> pairs = new Vector<PayloadPerDevice>();
			
			int size = appleDeviceIDs.size();
			for(int i=0; i<size; ++i){
				String theMessage =  String.format("%1$s peoplehunt match is available right now. Open the app to catch them!" , messages.get(i));				
				pairs.add(new PayloadPerDevice(PushNotificationPayload.combined(theMessage, 0, "default"), new BasicDevice(appleDeviceIDs.get(i)) ));
			}
			
			Push.payloads("/usr/local/tomcat/peoplepush2.p12", "espana19", true, pairs);			
		/*	List<PushedNotification> result =  Push.payloads("peoplepush2.p12", "espana19", true, pairs);
			for (PushedNotification pushedNotification : result) {
				
				if (!pushedNotification.isSuccessful()) {
					Exception theProblem = pushedNotification.getException();
					logger.info("error ios "+theProblem.getMessage());

				
					ResponsePacket theErrorResponse = pushedNotification.getResponse();
					if (theErrorResponse != null) {
						logger.info(" data packet "+theErrorResponse.getMessage());
					}
					
                 } else {
                	 	
                	 	logger.info("Successfully sent the notifications.");
                 }
			}
		*/
		}catch(Exception e){
			e.printStackTrace();
			//logger.info(e.getMessage());
		}
	}
	
}
