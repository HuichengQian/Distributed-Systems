package de.unistgt.ipvs.vs.ex1.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Implement the connectTo-, disconnect-, and calculate-method of this class
 * as necessary to complete the assignment. You may also add some fields or methods.
 */
public class CalcSocketClient {
	private Socket cliSocket;
	private Socket sliSocket;
	private int    rcvdOKs;		// --> Number of valid message contents
	private int    rcvdErs;		// --> Number of invalid message contents
	private int    calcRes;		// --> Calculation result (cf.  'RES')
    private DataOutputStream dos;
    private DataInputStream dis;
	
	public CalcSocketClient() {
		this.cliSocket = null;
		this.rcvdOKs   = 0;
		this.rcvdErs   = 0;
		this.calcRes   = 0;
	}
	
	public int getRcvdOKs() {
		return rcvdOKs;
	}

	public int getRcvdErs() {
		return rcvdErs;
	}

	public int getCalcRes() {
		return calcRes;
	}

	public boolean connectTo(String srvIP, int srvPort) {
               
		//Solution here
		try {
			cliSocket = new Socket(srvIP, srvPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean disconnect() {
               
	    //Solution here
        try {
	        dis.close();
			dos.close();
			cliSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean calculate(String request) {
               
		if (cliSocket == null) {
			System.err.println("Client not connected!");
			return false;
		}
		
		//Solution here
		//open an input stream and output stream to the corresponding client socket
		try {
			dos = new DataOutputStream(cliSocket.getOutputStream());
			dis = new DataInputStream(cliSocket.getInputStream());
	        //write the calculation request to the output stream
	        dos.writeUTF(request);
	        System.out.println("[CLI]request: " + request);
	        while(true){
	            if (dis.available() > 0) {
	                //read the response from the input stream
	                String response =  dis.readUTF();
	                System.out.println("[CLI]server response:" + response);
	                //process the server response to extract valid message contents
	                String[] msg = response.replaceAll("<|:|>", " ").trim().split(" +");
	                if (msg[1].equals("OK")){
	                    rcvdOKs++;
	                    System.out.println("[CLI]received number of OKs: " + rcvdOKs);
	                    if (msg.length > 2){
	                        if (msg[2].equals("RES")){
	                            calcRes = Integer.parseInt(msg[3]);
	                            System.out.println("[CLI]received calculation result: " + calcRes);
	                        }
	                    }
	                }else if (msg[1].equals("ERR")){
	                    rcvdErs++;
	                    System.out.println("[CLI]received number of errors: " + rcvdErs);
	                }else if (msg[1].equals("FIN")){
	                    break;
	                }
	            }
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
}
