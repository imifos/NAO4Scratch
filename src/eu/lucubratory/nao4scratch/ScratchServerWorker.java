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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingWorker;



/**
 * SwingWorker that scans for incoming Scratch network commands and forwards
 * them to the handler.
 * 
 * NB. SwingWorkers are one shot classes. They must be recreated for each run.
 */
public class ScratchServerWorker extends SwingWorker<Void,String> {

    static private final int PORT=5999; // constant to avoid too much technical stuff to handle by the user

    private InputStream sockIn;
    private OutputStream sockOut;
    
    private CommandHandler commandHandler;
    
    
    /**
     * Initialising constructor.
     */
    public ScratchServerWorker(CommandHandler commandHandler) {
        this.commandHandler=commandHandler;
    }
    
    
    
    /**
     * Worker body running as thread outside the event handling thread.
     */
    @Override
    protected Void doInBackground() throws Exception {

        //InetAddress addr=InetAddress.getLocalHost();
        
        Socket sock=null;
        ServerSocket serverSock=new ServerSocket(PORT);
        try {
            while (true) {
                sock=serverSock.accept();

                sockIn=sock.getInputStream();
                sockOut=sock.getOutputStream();

                byte[] buf=new byte[4096];
                int rd=sockIn.read(buf,0,buf.length);

                if (rd > 0) {
                    Command scratchCommand=new Command();
                    scratchCommand.scratchRaw=new String(Arrays.copyOf(buf, rd));

                    if (scratchCommand.scratchRaw.toLowerCase().contains("crossdomain.xml")) {
                        sendPolicyFile();
                    }
                    else {
                        commandHandler.signalScratchMessageReception(); // Indirect GUI update
                        
                        if (scratchCommand.scratchRaw.contains("/poll")) {
                            // Poll command to check against status of blocking commands status map
                            // -> not implemented: commandHandler.checkBlockingCommandStatus(scratchCommand);
                            // Will return a status object with status value and potential error message to Scratch
                            sendResponse("OK");
                        }
                        else {
                            // Operative command - forward to NAO client for handling
                            sendResponse("OK");
                            if (NaoClient.getInstance().isConnected()) {
                                commandHandler.queueQ(scratchCommand);
                            }
                        }
                        
                        /* Using SwingWorker's "publish(scratchCommand.scratchRaw)" would be
                        the most clean solution. However, changing into the EDT scope
                        takes so much time that we cannot keep up with Scratch's 30 
                        commands per second. For this reason we use basic inter-
                        thread communication while staying in the worker thread's 
                        context. */
                    }
                }
                try { sock.close(); } catch(Exception e) {}
            } // while(true)
        }
        catch(Exception ex) {
            ex.printStackTrace(System.err);
        }
        finally {
            try { sock.close(); } catch(Exception e) {}
            try { serverSock.close(); } catch(Exception e) {}
        }
        return null;
    }

    
    /**
     * Sends a Flash/AIR null-terminated cross-domain policy file.
     */
    private void sendPolicyFile() {
        System.out.println("Flash/AIR null-terminated cross-domain policy file");
        String policyFile
                = "<cross-domain-policy>\n"
                + " <allow-access-from domain=\"*\" to-ports=\"" + PORT + "\"/>\n"
                + "</cross-domain-policy>\n\0";
        sendResponse(policyFile);
    }

    
    /**
     * Sends a HTTP response
     */
    private void sendResponse(String s) {
        
        String httpResponse = "HTTP/1.1 200 OK\r\n";
        httpResponse+="Content-Type: text/html; charset=ISO-8859-1\r\n";
        httpResponse+="Access-Control-Allow-Origin: *\r\n\r\n" ;
        httpResponse+=s;
        httpResponse+="\r\n";
        
        try {
            byte[] outBuf=httpResponse.getBytes();
            sockOut.write(outBuf,0,outBuf.length);
        } 
        catch (Exception ex) {
        }
    }
    
    
    
    /**
     * Invoked at publish(). Can safely update the GUI from this method.
     */
    @Override
    protected void process(List<String> chunks) {
        // Not used, since far too slow. See comment above.
    }
};
