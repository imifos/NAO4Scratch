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

import javax.swing.SwingUtilities;

/**
 * Entry point.
 */
public class NAO4Scratch {

    static private MainFrame mainFrame;
    
    /**
     * 
     */
    public static MainFrame getMainFrame() {
        return mainFrame;
    }
    
    
    /**
     * 
     */
    public static void main(String[] args) {
        
        System.out.println("Starting NAO 4 Scratch Server...");
        
        /* Set the Nimbus look and feel */
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         * Metal,Nimbus,CDE/Motif,Windows,Windows Classic
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Trouble setting the SWING look and feel! Stop here!");
            System.out.flush();
            ex.printStackTrace();
            return;
        } 
                
        System.out.println("And here we go...");
        
        // Start-up Swing GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NAO4Scratch.mainFrame=new MainFrame();
            }
        });
        
        // Start the Scratch receiver loop
        (new ScratchServerWorker(CommandHandler.getInstance())).execute();
    }
}
