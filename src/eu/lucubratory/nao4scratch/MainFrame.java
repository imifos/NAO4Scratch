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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * Handles the Swing GUI.
 */
public class MainFrame extends JFrame {

    private final JTextField   naoIpEdit=new JTextField("parsec.local");  //nao.local");
    private final JTextField   naoPortEdit=new JTextField("8080");
    private final JLabel       naoMessage=new JLabel(" Not connected!");
    private final JButton      naoConnectButton=new JButton("Connect");
    private final JButton      naoDisconnectButton=new JButton("Disconnect");
    private final ActivityPane scratchTxRxProgressPanel=new ActivityPane();
    private final JLabel       scratchMessage=new JLabel("");

    private long previousMessageReceptionCounter=-1;
    
    
    /**
     * Handles the Swing GUI.
     */
    public MainFrame() {
        
        super("NAO for Scratch 2 Extension Server");
        
        setLayout(new BorderLayout(0,0));
        
        // NAO Panel
        // ---------
        JPanel naoPanel=new JPanel(new GridLayout(5,1,3,3));

        JLabel naoTitle = new JLabel("NAO Robot:");
        naoTitle.setFont(new Font("serif", Font.BOLD, 18));
        naoPanel.add(naoTitle);
        
        JPanel naoAddressPanel=new JPanel(new GridLayout(1,2));
        naoAddressPanel.add(naoIpEdit);
        naoAddressPanel.add(naoPortEdit);
        naoPanel.add(naoAddressPanel);
        
        naoMessage.setFont(new Font("serif", Font.PLAIN, 11));
        naoPanel.add(naoMessage);
        
        JPanel naoButtonPanel=new JPanel(new GridLayout(1,2));
        naoButtonPanel.add(naoConnectButton);
        naoButtonPanel.add(naoDisconnectButton);
        naoDisconnectButton.setEnabled(false);
        naoPanel.add(naoButtonPanel);
                
        add(naoPanel,BorderLayout.NORTH);
        
        // SCRATCH Panel
        // -------------
        JPanel scratchPanel=new JPanel(new GridLayout(4,1,0,0));

        JLabel scratchTitle=new JLabel("Scratch 2:");
        scratchTitle.setFont(new Font("serif", Font.BOLD, 18));
        scratchPanel.add(scratchTitle);
        
        //JPanel naoTxRxPanel=new JPanel(new GridLayout(1,2));
        // not used for the moment
        //scratchPanel.add(naoTxRxPanel);
                        
        scratchPanel.add(scratchTxRxProgressPanel);
        
        scratchMessage.setFont(new Font("serif", Font.PLAIN, 11));
        scratchPanel.add(scratchMessage);
        
        add(scratchPanel,BorderLayout.SOUTH);
        
        // More stuff...
        // -------------
        
        naoConnectButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent ae) {
                connectToNAO();
            }
        });
        
        naoDisconnectButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent ae) {
                disconnectFromNAO();
            }
        });
        
        Timer t=new Timer(200, new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                updateScratchMessageReceptionDisplay();
            }
        });
        t.start();
                
        // Finalise GUI
        setSize(270,320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    
    /**
     * Displays a message related to the scratch side of the connection bridge.
     */
    public void setScratchMessage(String msg) {
        scratchMessage.setText(msg);
    }
    
    
    /**
     * Displays a message related to the NAO side of the connection bridge.
     */
    public void setNaoMessage(String msg) {
        naoMessage.setText(msg);
    }
    
    
    /**
     * 
     */
    public void setConnectionButtons(boolean enableConnect,boolean enableDisconnect) {
        naoConnectButton.setEnabled(enableConnect);
        naoDisconnectButton.setEnabled(enableDisconnect);
    }
    
    
    /**
     * Updates the display of a scratch message reception.
     */
    public void updateScratchMessageReceptionDisplay() {
        if (previousMessageReceptionCounter!=CommandHandler.getInstance().getScratchMessageReceptionCounter()) {
            // We are getting messages from Scratch
            scratchTxRxProgressPanel.signal();
            previousMessageReceptionCounter=CommandHandler.getInstance().getScratchMessageReceptionCounter();
        }
        else {
            // Not connected to Scratch
            scratchTxRxProgressPanel.reset();
        }
    }
    
    
    /**
     * 
     */
    private void connectToNAO() {
        
        String addr=naoIpEdit.getText();
        
        int port;
        try {
            port=Integer.parseInt(naoPortEdit.getText());
        }
        catch(NumberFormatException e) {
            naoMessage.setText("Port not a number!");
            return;
        }
            
        setConnectionButtons(false,false);
        setNaoMessage(" Connecting...");
        
        // Start a network thread that connects to NAO and handles sending and receiving
        NaoClient.getInstance().connect(addr,port);
    }
        
    
    /**
     * 
     */
    private void disconnectFromNAO() {
        NaoClient.getInstance().disconnect();
    }
    
}
