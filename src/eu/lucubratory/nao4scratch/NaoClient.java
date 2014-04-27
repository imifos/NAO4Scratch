/*****************************************************************************
* NAO4Scratch is free software: you can redistribute it and/or modify 
* it under the terms of the GNU Lesser General Public License as published by 
* the Free Software Foundation, either version 3 of the License, or 
* (at your option) any later version. 
* 
* NAO4Scratch is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
* GNU Lesser General Public License for more details. 
* 
* A copy of the GNU Lesser General Public License can be found here:
* http://www.gnu.org/licenses/
*****************************************************************************/

package eu.lucubratory.nao4scratch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Manages the connection to NAO and the two Rx and Tx threads.
 */
public class NaoClient {

    static private NaoClient naoClient = new NaoClient();

    private InputStream sockIn;
    private OutputStream sockOut;
    private Socket sock;

    private NaoClientRxWorker naoClientWorker;
    
    private boolean connected=false;
    

    /**
     * Returns single-tone instance.
     */
    synchronized static public NaoClient getInstance() {
        return naoClient;
    }

    
    /**
     * Connects to NAO robot and starts required threads.
     */
    public void connect(String addr, int port) {

        MainFrame mainFrame = NAO4Scratch.getMainFrame();
        
        connected=false;
        
        try {
            sock = new Socket(addr, port);
            sockOut = sock.getOutputStream();
            sockIn = sock.getInputStream();
        } 
        catch (UnknownHostException e) {
            System.err.println("UnknownHostException: " + addr);
            try {
                sock.close();
            } catch (Exception ex) {
            }
            mainFrame.setNaoMessage("Hostname error:" + e.getMessage());
            mainFrame.setConnectionButtons(true, false);
            return;
        } 
        catch (IOException e) {
            System.err.println("IOException: " + addr + "/" + port);
            try {
                sock.close();
            } catch (Exception ex) {
            }
            mainFrame.setNaoMessage("Network Error:" + e.getMessage());
            mainFrame.setConnectionButtons(true, false);
            return;
        }

        mainFrame.setConnectionButtons(false, true);
        connected=true;
        
        // Starts a swing worker thread on the reception of feedback from NAO
        naoClientWorker = new NaoClientRxWorker(sockIn, CommandHandler.getInstance());
        naoClientWorker.execute();

        // Clears potentially existing commands from command Q
        CommandHandler.getInstance().clearQ();
        
        // Starts a local thread that forwards incoming commands to NAO.
        // Stops when disconnected.
        (new Thread(new NaoClientTxThread(sockOut))).start();
    }
    

    /**
     * Disconnects from NAO robot.
     */
    public void disconnect() {

        try {
            sock.close();
        } 
        catch (Exception ex) {
        }

        connected=false;
        
        MainFrame mainFrame = NAO4Scratch.getMainFrame();
        mainFrame.setConnectionButtons(true, false);
        mainFrame.setNaoMessage(" Disconnected from NAO");
    }

    
    /**
     * 
     */
    public boolean isConnected() {
        return connected;
    }
}
