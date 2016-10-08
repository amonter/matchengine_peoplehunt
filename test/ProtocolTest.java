import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class ProtocolTest {

	
	public static void main(String[] args) throws Exception, IOException {	
		
		  addUserToRoom("ellen");	
	
	}

	private static void addUserToRoom(String userId) throws UnknownHostException, IOException {
		
		String sentence = "1:"+userId+ '\n';
		  String fromServer;
		  String fromUser;
		  //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
		  Socket clientSocket = new Socket("50.19.45.37", 1045);
		 // DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		  PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		  BufferedReader feed = new BufferedReader(new InputStreamReader(System.in));
		  
		  
		  //sentence = inFromUser.readLine();
		  //outToServer.writeBytes("1:"+userId+ '\n');		  
		  out.print(sentence);
		  //out.flush();
		  
		  while ((fromServer = inFromServer.readLine()) != null) {
              System.out.println("Server: " + fromServer);             
             
              if ((fromUser = feed.readLine()) != null) {                
                  out.println(fromUser);
                  //out.flush();
              }
		  }
		  
		  //while (!inFromServer.ready()) {
			 // System.out.println("aloha");
		  //}
	       //System.out.println(inFromServer.readLine()); // Read one line and output it

		  
		  //clientSocket.close();
		  System.out.println("closing");
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
		              //if (fromServer.equals("Terminate"))
		                 // break;
		  }
		 
		clientSocket.close();
	}	
	
}
