package notifications;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationDispatcherThread implements Runnable {
	
	private List<String> ios_dest;
	private List<String> ios_msg;
	private List<String> sms_dest;
	private List<String> sms_msg;
	private Logger logger;
	
	public NotificationDispatcherThread(List<String> ios_dest, List<String> ios_msg, List<String> sms_dest, List<String> sms_msg){
		this.ios_dest = ios_dest;
		this.ios_msg = ios_msg;
		this.sms_dest = sms_dest;
		this.sms_msg = sms_msg;
		logger = LoggerFactory.getLogger(NotificationDispatcherThread.class);
	}

	@Override
	public void run() {
		try{
			//iOS
			iOSPusher iOS = new iOSPusher();
			
			iOS.sendMultiple(ios_dest, ios_msg);
			
			//sms
			SmsSender smsSender = new SmsSender();
			int size = sms_dest.size();
			for(int i=0; i<size; ++i){
				String theMessage =  String.format("Your %1$s peoplehunt match is available right now! Tap PLAY again to catch them http://bit.ly/wbB8pk" , sms_msg.get(i));
				smsSender.send(sms_dest.get(i), theMessage);
			}
			
		}catch(Exception e){
			logger.info(e.getMessage());
		}
	}

	
}
