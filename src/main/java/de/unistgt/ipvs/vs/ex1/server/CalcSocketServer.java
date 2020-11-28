package de.unistgt.ipvs.vs.ex1.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Extend the run-method of this class as necessary to complete the assignment.
 * You may also add some fields, methods, or further classes.
 */
public class CalcSocketServer extends Thread {
	private ServerSocket srvSocket;
	private int port;
	private Map<Socket, CalculationSession> map = new HashMap<>();

	public CalcSocketServer(int port) {
		this.srvSocket = null;
		
		this.port = port;
	}
	
	@Override
	public void interrupt() {
		try {
			if (srvSocket != null) srvSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
           
		if (port <= 0) {
			System.err.println("Wrong number of arguments.\nUsage: SocketServer <listenPort>\n");
			System.exit(-1);
		}
		
		// Start listening server socket ..
		try {
			srvSocket = new ServerSocket(port);
			while (true) {
				new Thread(new CalculationSession(srvSocket.accept())).start();
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
    
	
	
    public void waitUnitlRunnig(){
        while(this.srvSocket == null){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
    }
}