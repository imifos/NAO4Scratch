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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Background thread reading the incoming Scratch commands, converts them to NAO 
 * commands using the CommandHandler and sends them to NAO.
 * /Poll requests are excluded, they are handled on the other side of the Q.
 */
public class NaoClientTxThread implements Runnable  {

    private OutputStream sockOut;
    
    /**
     * 
     */
    public NaoClientTxThread(OutputStream sockOut) {
        this.sockOut=sockOut;
    }


    /**
     * 
     */
    @Override
    public void run() {
        
        while(NaoClient.getInstance().isConnected()) {
            
            Command c=CommandHandler.getInstance().pollQ();
            if (c != null) {
                CommandHandler.getInstance().ScratchToNao(c);
                try {
                    if (c.hasNaoCommand()) 
                        sockOut.write(c.naoCommand.getBytes());
                }
                catch(IOException e) {
                    System.err.println("Unable to send data to NAO:"+e.getMessage());
                }
            }
            
            try { Thread.sleep(20); } catch (InterruptedException ex) {}
        }
    }
}
