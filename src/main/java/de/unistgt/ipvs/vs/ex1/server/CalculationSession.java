package de.unistgt.ipvs.vs.ex1.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;

import de.unistgt.ipvs.vs.ex1.common.ICalculation;

/**
 * Add fields and methods to this class as necessary to fulfill the assignment.
 */
public class CalculationSession implements Runnable {
	private Socket socket = null; 
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private String operator=null;
	private int result;
	private CalculationImpl calculationImpl;
	
	public CalculationSession(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		System.out.println("[SRV]client connected"+socket.toString());
		//Open an input stream and output steam to the socket
		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			//After a client has connected to the server, the server sends "RDY" to the client
			dos.writeUTF("<08:RDY>");
			calculationImpl = new CalculationImpl();
			while(true){
				if (dis.available()>0){
					//Read the client request from the input stream
					String clientRequest = dis.readUTF();
					System.out.println("[SRV]client request: "+clientRequest);
					//For each received message, the server sends immediately a response with the content "OK"
					dos.writeUTF("<07:OK>");
					//Start to process the client request
					processRequest(clientRequest);
					//After the server has processed every message content from a received message,
					//it sends the content "FIN" to the client.
					dos.writeUTF("<08:FIN>");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }
	
	/**
	 * Extracts each content of the client request and performs the corresponding operation.
	 * @param clientRequest
	 * @throws IOException
	 */
	private void processRequest(String clientRequest){

		String pattern = ".*<\\d\\d:.*>.*";
		boolean isMatch = Pattern.matches(pattern,clientRequest);

		if (isMatch){

			String[] truncatedRequest = clientRequest.split("<|>");
			String[] messageContent =truncatedRequest[1].split(":");
			String[] truncatedContent = messageContent[1].trim().split(" +");

			try {
				for(int i=0;i<truncatedContent.length;i++){
					//If a valid content equals to the operator "ADD", "SUB", or "MUL",
					//the server-side calculation operator is changed to ADDing, SUBtracting, or MULtiplying by values.
					//Each valid content is acknowledged to the client
					//with the content "OK" followed by a single whitespace characterand the valid content.
					if (truncatedContent[i].equalsIgnoreCase("ADD")){
						operator="ADDing";
						dos.writeUTF("<11:OK ADD>");
						
					}
					else if(truncatedContent[i].equalsIgnoreCase("SUB")) {
						operator="SUBtracting";
						dos.writeUTF("<11:OK SUB>");}
					else if(truncatedContent[i].equalsIgnoreCase("MUL")){
						operator="MULtiplying";
						dos.writeUTF("<11:OK MUL>");
					}
					//If a valid content equals to the operator "RES", the current calculation result is
					//sent to the client with "OK" followed by a single whitespace character, the operator
					//"RES", another single whitespace character and the current calculation value.
					else if(truncatedContent[i].equalsIgnoreCase("RES")) {
						result = calculationImpl.getResult();//获得计算结果
						int resultLength = String.valueOf(result).length();
						int totalMsgLength = resultLength+12;
						dos.writeUTF("<"+totalMsgLength+":OK RES "+result+">");
						System.out.println("[SRV]result: "+result);
					}
					//If a valid content equals to an integer value, the value is based on the current
					//calculation operator added to, subtracted from, or multiplied with the result.
					else if(isNumeric(truncatedContent[i])){
						int value = Integer.parseInt(truncatedContent[i]);
						int valueLength = String.valueOf(value).length();
						int totalMsgLength = valueLength+8;
						dos.writeUTF("<"+totalMsgLength+":OK "+value+">");
						if (operator==null){result=0;}else {
							switch (operator) {
								case "ADDing":
									calculationImpl.add(value);
									result = calculationImpl.getResult();
									System.out.println("[SRV]Adding: "+result);
									break;
								case "SUBtracting":
									calculationImpl.subtract(value);
									result = calculationImpl.getResult();
									System.out.println("[SRV]SUBtracting: "+result);
									break;
								case "MULtiplying":
									calculationImpl.multiply(value);
									result = calculationImpl.getResult();
									System.out.println("[SRV]MULtiplying: "+result);
									break;
							}
						}
					}else{
						//If the content is invalid, it is acknowledged with "ERR" followed by a single
						//whitespace character and the invalid content. Invalid contents are not processed.
						int totalMsgLength = truncatedContent[i].length()+9;
						dos.writeUTF("<"+totalMsgLength+":ERR "+truncatedContent[i]+">");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Determines if the valid content is an integer value.
	 * @param validContent
	 * @return <code>true</code> if the valid content is an integer value;
	 * 		   <code>false</code> otherwise.
	 */
	public boolean isNumeric(String validContent){
		String pattern = "-?\\d+";
		boolean isMatch = Pattern.matches(pattern,validContent);
		if (isMatch) {
			return true;
		}
		else {
			return false;
		}
	}
}