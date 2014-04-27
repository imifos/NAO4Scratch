/*****************************************************************************
* NAO4Scratch is free software: you can redistribute it and/or modify 
* it under the terms of the GNU General Public License as published by 
* the Free Software Foundation, either version 3 of the License, or 
* (at your option) any later version. 
* 
* NAO4Scratch is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
* GNU General Public License for more details. 
* 
* A copy of the GNU General Public License can be found here:
* http://www.gnu.org/licenses/
*****************************************************************************/


package eu.lucubratory.nao4scratch;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Handles the communication between the two side, NAO and Scratch, including
 * synchronisation, message transfer, message transformation, blocking command
 * status management.
 */
public class CommandHandler {
    
    private static final CommandHandler commandHandler=new CommandHandler();
    
    private long tempCnt=0;
    
    /** Thread-safe Queue allowing communication between the Scratch and the NAO threads. */
    private final LinkedBlockingQueue<Command> queue=new LinkedBlockingQueue<>();
    
    /** Holds the NAO ID/Status map for blocking commands. @see checkBlockingCommandStatus*/
    private final ConcurrentHashMap<Long,Long> blockingStatusMap=new ConcurrentHashMap<>();
    
    private long scratchMessageCounter=0;
    
    
    
    /** 
     * Returns the single and unique instance of the CommandHandler class.
     */
    public static synchronized CommandHandler getInstance() {
        return commandHandler;
    }
    
    
    /**
     * Checks the polled NAO Command ID again the status of waiting commands to
     * verify if the command is still executing or if concluded. 
     * Returns a status object holding the status and a potential (error) message 
     * to Scratch.
     */
    public void checkBlockingCommandStatus(Command cmd) {
        
        // Not yet implemented due to bug in Scratch 2 beta:
        // http://scratch.mit.edu/discuss/topic/36630/
    }
    
    
    
    /**
     * Converts the (scratchRaw) Scratch Command to a NAO that can be send 
     * to the robot. Does not handle POLL requests (see "checkBlockingCommandStatus()").
     * <p/>
     * The Scratch raw command is a class GET request and the NAO command format 
     * is the following String "uid#command#param1#param2#...#paramN$"
     */
    public void ScratchToNao(Command cmd) {
        
        cmd.naoCommand=null; // reset NAO part
        
        // Extract the GET command
        int getI=cmd.scratchRaw.indexOf("GET");
        int httpI=cmd.scratchRaw.indexOf("HTTP");
        if (getI<0 || httpI<0 || httpI-getI<2) {
            // Not a correct GET request
            System.err.println("Ignore incorrect Scratch request: ["+cmd.scratchRaw+"]");
            return;
        }
        String req;
        try {
            req = URLDecoder.decode(cmd.scratchRaw.substring(getI+3,httpI).trim(),"UTF-8");
        } 
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace(System.err); // BUG
            return;
        }
  
        // Dissect the GET command and initialise the NAO command object
        String part[]=req.split("/");
        
        if (part.length==0) {
            System.err.println("Ignore corrupt Scratch request: ["+cmd.scratchRaw+"]");
            return;
        }
        
        // Leading / -> empty part[0]
        String naoCommand=part[1];
        String naoParam=(part.length>2) ? naoParam=part[2] :  null;
        
        // Note: blocking commands are not yet supported (bug in Scratch v404).
        // However, unfortunately, the ID added by scratch takes the place of
        // the parameter in case of blocking/waiting commands. For this reason,
        // we need to know the blocking commands here to decide the meaning of
        // the parameter. This is a design flaw, but currently we don't have a 
        // choice. We will address this as soon as we implement the blocking 
        // command handling.
        
        //if (part.length>2) naoBlockingId=part[3];
        tempCnt++;
        
        StringBuilder sb=new StringBuilder();
        sb.append(tempCnt);
        sb.append("#"); // UID will be implemented with the arrival of blocking commands
        sb.append(naoCommand);
        if (naoParam!=null) sb.append("#").append(naoParam);
        sb.append("$");
        
        cmd.naoCommand=sb.toString();
        
        System.out.println("["+cmd.naoCommand+"]"); 
    }
    
    
    
    
    
    
    
    /**
     * @see java.util.concurrent.LinkedBlockingQueue#clear() 
     */
    public void clearQ() {
        queue.clear();
    }
    
    /**
     * @see java.util.concurrent.LinkedBlockingQueue#poll() 
     */
    public Command pollQ() {
        return queue.poll();
    }
    
    /**
     * @see java.util.concurrent.LinkedBlockingQueue#offer(java.lang.Object) 
     */
    public void queueQ(Command c) {
        queue.offer(c);
    }
    
    /**
     * Signals the reception of a message from Scratch in order to update the GUI.
     * Since the value is just read, and precision is not an issue, we don't
     * need to synchronise.
     */
    public void signalScratchMessageReception() {
        scratchMessageCounter++;
    }
    
    /**
     * 
     */
    public long getScratchMessageReceptionCounter() {
        return scratchMessageCounter;
    }
}


/*
Sample scratchRaw SCRATCH requests:

GET /reset_all HTTP/1.1
Referer: app:/ScratchOffline.swf
Accept: text/xml, application/xml, application/xhtml+xml, text/html;q=0.9, text/plain;q=0.8, text/css, image/png, image/jpeg, image/gif;q=0.8, application/x-shockwave-flash, video/mp4;q=0.9, flv-application/octet-stream;q=0.8, video/x-flv;q=0.7, audio/mp4, application/futuresplash, **;q=0.5
x-flash-version: 12,0,0,38
Accept-Encoding: gzip,deflate
User-Agent: Mozilla/5.0 (Windows; U; en-US) AppleWebKit/533.19.4 (KHTML, like Gecko) AdobeAIR/4.0
Host: 127.0.0.1:5999
Connection: Keep-Alive
Cookie: s_pers=%20s_fid%3D7B857B41D9AF1640-1A8D3E8EEEC4A554%7C1460218673721%3B%20s_vs%3D1%7C1397062073723%3B%20s_nr%3D1397060273729-Repeat%7C1428596273729%3B


GET /say/hello%21 HTTP/1.1
Referer: app:/ScratchOffline.swf
Accept: text/xml, application/xml, application/xhtml+xml, text/html;q=0.9, text/plain;q=0.8, text/css, image/png, image/jpeg, image/gif;q=0.8, application/x-shockwave-flash, video/mp4;q=0.9, flv-application/octet-stream;q=0.8, video/x-flv;q=0.7, audio/mp4, application/futuresplash, **;q=0.5
x-flash-version: 12,0,0,38
Accept-Encoding: gzip,deflate
User-Agent: Mozilla/5.0 (Windows; U; en-US) AppleWebKit/533.19.4 (KHTML, like Gecko) AdobeAIR/4.0
Host: 127.0.0.1:5999
Connection: Keep-Alive
Cookie: s_pers=%20s_fid%3D7B857B41D9AF1640-1A8D3E8EEEC4A554%7C1460218673721%3B%20s_vs%3D1%7C1397062073723%3B%20s_nr%3D1397060273729-Repeat%7C1428596273729%3B
*/