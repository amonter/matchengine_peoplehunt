package notifications;

import java.util.Map;
import java.util.HashMap;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Sms;

public class SmsSender {
	//variables
	private Account account;
	private TwilioRestClient client;
	private SmsFactory smsFactory;
	private final String accSID = "AC942c230380184d03b9a7c21ac4edb701";
	private final String authToken = "3d8e058a2eb34c98c071b45bf0b3f01c";
	public final String From = "+16464033045";	//this is our Twilio number that we bought
	
	public SmsSender(){
		client = new TwilioRestClient(accSID, authToken, null);	//for Adrians account
		account = client.getAccount();
		smsFactory = account.getSmsFactory();
	}
	
	@SuppressWarnings("unused")
	public void send(String to, String text){	//uses the Twilio API default for sending sms messages
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("To", to);
		parameters.put("From", From);		//self-explanatory
		parameters.put("Body", text);
		
		//try to send the sms
		try{
			Sms sms = smsFactory.create(parameters);	//this sends a POST request
		}catch(TwilioRestException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}//end
	
}