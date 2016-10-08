import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class NotificationsTest {
	
	public static void main(String[] args){
		
		// All the pairs here match one another 
		final List<String> requests = new ArrayList<String>();
		
		//these will just match
		//requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=54108&bundleId=162&selectedtags=10039");
		//requests.add("http://prod.crowdscanner.com/addusertobundle/?bundleid=174&profileid=68789&action=2");
		
		requests.add("http://dev.crowdscanner.com/pairinghuntmatching/?myhuntid=15");//pair one match
		
		
		//requests.add("http://prod.crowdscanner.com/addusertobundle/?bundleid=178&profileid=15338&action=0");
		//requests.add("http://prod.crowdscanner.com/addusertobundle/?bundleid=178&profileid=91964&action=0");
		//requests.add("http://prod.crowdscanner.com/addusertobundle/?bundleid=178&profileid=90023&action=0");
		
		//requests.add("http://127.0.0.1:7000/rest/pairinghuntmatching/?myhuntid=5995&bundleId=162&selectedtags=10041");//pair one match	
		
		for (final String theRequest : requests) {	
		

			 new Thread(new Runnable() {
	             public void run() {
	                 try {
	                	 System.out.println("RUNE");
	                     final URL address = new URL("http://dev.crowdscanner.com/rest/postnewmessage/21/10");
	                     final URLConnection conn = address.openConnection();
	                     conn.setDoOutput(true);
	                     conn.setRequestProperty("Accept-Charset", "UTF-8;q=0.7,*;q=0.7");
	                     conn.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,q=0.8");
	                     final OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
	                     writer.write("aloha there now here sper");
	                     writer.flush();
	                     final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	                     String line;
	                     while ((line = rd.readLine()) != null) {
	                         System.out.println(line);
	                     }
	                     rd.close();
	                     
	                 }
	                 catch (Exception e) {
	                     e.printStackTrace();
	                 }
	             }
	         }).start();
				
		}	
	}
}
