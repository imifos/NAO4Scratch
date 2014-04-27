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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingWorker;


/**
 * SwingWorker that keeps a connection to NAO open and sends and "process()"ess 
 * commands coming from the robot.
 * 
 * NB. SwingWorkers are one shot classes. They must be recreated for each run.
 */
public class NaoClientRxWorker extends SwingWorker<Void, String> {

    private InputStream sockIn;
    private CommandHandler commandHandler;
    
    
    /**
     * Initialising constructor.
     */
    public NaoClientRxWorker(InputStream sockIn,CommandHandler commandHandler) {
        this.sockIn=sockIn;
        this.commandHandler=commandHandler;
    }

    
    /**
     * Worker body running as thread outside the event dispatching thread.
     */
    @Override
    protected Void doInBackground() throws Exception {

        // Loop on incoming messages
        try {
            while(true) {

                byte[] buf=new byte[1024];
                int rd=sockIn.read(buf,0,buf.length);

                if (rd > 0) {
                    String msg=new String(Arrays.copyOf(buf, rd));
                    publish(msg);
                }

                Thread.sleep(50);
            }
        }
        catch(Exception e) {
            // We cannot do anything. User must restart connection.
        }
        return null;
    }
            
            
    
    /**
     * Invoked at publish(). Executed in EDT. Can safely update the GUI from this method.
     * Indicates a received message.
     */
    @Override
    protected void process(List<String> chunks) {
        
        StringBuilder sb=new StringBuilder();
        Iterator<String> i=chunks.iterator();
        while(i.hasNext())
            sb.append(i.next());
        
        String msg=sb.toString();
        
        if (msg.startsWith("~Hello")) {
            MainFrame mainFrame = NAO4Scratch.getMainFrame();
            mainFrame.setNaoMessage(" Connected to NAO");
        }
        else if (msg.startsWith("~")) {
            // ignore
            System.out.println("RECEIVED FROM NAO: "+msg);
        }
        else {
            // We received a blocking command status update
            // This is not yet implemented
            // see CommandHandler class
            System.out.println("RECEIVED END-OF-BLOCKING-COMMAND FROM NAO: "+msg);
        }
    }    
    
};
