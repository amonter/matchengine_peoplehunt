/*
 * This class obfuscates the details of the notifications that are dependent on what device the end-user uses, ie. iOS push notifications vs. SMS
 * This will allow you to issue notification without having to worry about what device the user operates.
 * Just create an instance of this class(empty constructor) and call method passing in the parameters that suit you,
 * i.e. individual 'people' objects or a list of them to iterate over.
 *  
 * */

package notifications;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Profile;


public class Notifier {
	
	
	//variables
	private SmsSender smsSender;
	private Logger logger;
	
	//constructor
	public Notifier(){
		smsSender = new SmsSender();
		logger = LoggerFactory.getLogger(Notifier.class);
	}

	/*
	public void issueNotifications(Profile profile, String text){			//take input as a list of 'Person's, depending on which device they have call according notification methods
		try{
			iOSPusher iOS = new iOSPusher();
			//see if it's iPhone/iPod
			if(profile.getIphoneUDID()!=null){
				//do the iPhone magic
				String theUDID = profile.getIphoneUDID();//.replaceAll("(<|>)", "").replace(" ", "");
				//logger.info(theUDID);
				iOS.send(theUDID, text);
			}
			else if(profile.getNumber() != null){ //alternative is to use SMS...for now
				//logger.info(profile.getNumber());
				smsSender.send(profile.getNumber(), text);		
			}
			else{
				// the user didn't allow for push/sms notifications
			}
		}catch(Exception e){
			logger.info(e.getMessage());
		}
	}
	*/
	
	//do not use without editing
	public void issueNotifications(List<String> ios_dest, List<String> ios_msg, List<String> sms_dest, List<String> sms_msg){	//handle Lists of people
		try{
			(new Thread(new NotificationDispatcherThread(ios_dest, ios_msg, sms_dest, sms_msg))).start();
		}catch(Exception e){
			logger.info(e.getMessage());
		}
	}

}