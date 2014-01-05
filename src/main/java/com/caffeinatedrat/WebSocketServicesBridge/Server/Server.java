/**
* Copyright (c) 2012-2013, Ken Anderson <caffeinatedrat at gmail dot com>
* All rights reserved.
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE AUTHOR AND CONTRIBUTORS BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.caffeinatedrat.WebSocketServicesBridge.Server;

import java.net.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.caffeinatedrat.WebSocketServicesBridge.Globals;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * The actual WebSocketServicesBridge server.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Server extends Thread {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private ServerSocket serverSocket;
    
    private boolean isServerRunning;
    
    //Properties
    private int port;
    private int maximumThreads;
    private int handshakeTimeOutInMilliseconds;
    private int maximumNumberOfSupportedFragmentedFrames = 20;
    private boolean checkOrigin;
    private boolean pingable;
    private HashSet<String> whitelist = null;
    private Set<ConfiguredServer> configuredServers = null;
    
    //Keep track of all threads.
    private LinkedList<Connection> threads = null;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------    
    
    /**
     * Returns the port the server is listening on.
     * @return The port the server is listening on.
     */
    public int getPort() {
        
        return this.port;
        
    }
    
    /**
     * Returns the maximum number of threads the server will support concurrently.
     * @return The maximum number of threads the server will support concurrently.
     */
    public int getMaximumThreads() {
        return this.maximumThreads;
    }

    /**
     * Returns the handshake timeout in milliseconds.
     * @return The handshake timeout in milliseconds.
     */
    public int getHandshakeTimeout() {
        return handshakeTimeOutInMilliseconds;
    }
        
    /**
     * Sets the handshake timeout in milliseconds.
     * @param timeout The timeout handshake in milliseconds.
     */
    public void setHandshakeTimeout(int timeout) {
        this.handshakeTimeOutInMilliseconds = timeout;
    }
    
    /**
     * Determines if the origin is checked during the handshake.
     * @return If the origin is checked during handshaking.
     */
    public boolean isOriginChecked() {
        return this.checkOrigin;
    }
    
    /**
     * Enables or disables origin checking.
     * @param checkOrigin Enables or disables origin checking.
     */
    public void setOriginCheck(boolean checkOrigin) {
        this.checkOrigin = checkOrigin;
    }    
    
    /**
     * Determines if the server is pingable via websockets.
     * @return If the server is pingable via websockets.
     */
    public boolean isPingable() {
        return this.pingable;
    }
    
    /**
     * Enables or disables the ability to ping the server via websockets.
     * @param isPingable Enables or disables ability to ping the server via websockets.
     */
    public void setPingable(boolean isPingable) {
        this.pingable = isPingable;
    }
    
    /**
     * Returns the maximum number of supported fragmented frames for this server.
     * @return the maximum number of supported fragmented frames for this server.
     */    
    public int getMaximumNumberOfSupportedFragmentedFrames() {
        return this.maximumNumberOfSupportedFragmentedFrames;
    }
    
    /**
     * Sets the maximum number of supported fragmented frames for this server.
     * @param maximumNumberOfSupportedFragmentedFrames the maximum number of supported fragmented frames for this server.
     */    
    public void setMaximumNumberOfSupportedFragmentedFrames(int maximumNumberOfSupportedFragmentedFrames) {
        this.maximumNumberOfSupportedFragmentedFrames = maximumNumberOfSupportedFragmentedFrames;
    }     
    
    /**
     * Determines if the websockets server is publicly available or by white-list only.
     * @return the type of accessibility.
     */
    public boolean isWhiteListed() {
        
        return (this.whitelist != null);
        
    }
    
    public HashSet<String> getWhiteList() {
        
        return this.whitelist;
        
    }
    
    /**
     * Returns a collection of servers to communicate with the proxy server.
     * @return the type of accessibility.
     */
    public Set<ConfiguredServer> getServers() {
    
        return this.configuredServers;
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
     
    public Server(int port, Set<ConfiguredServer> configuredServers, boolean isWhiteListed, int maximumThreads) {
        
        if (configuredServers == null) {
            
            throw new IllegalArgumentException("The configuredServers argument is invalid (null).");
            
        }
        
        this.isServerRunning = true;
        this.threads = new LinkedList<Connection>();
        
        //Properties
        this.port = port;
        this.configuredServers = configuredServers;
        this.maximumThreads = maximumThreads;
        this.handshakeTimeOutInMilliseconds = 1000;
        this.checkOrigin = true;
        this.pingable = true;
        
        if(isWhiteListed)
        {
            this.whitelist = new HashSet<String>();
            if(!loadWhiteList())
            {
                //TO-DO: Create the white-list.txt and bail.
                Logger.severe("The white-list was not found...");
            }
        }
        
        //Let the user know that they have not configured any servers in the config.yml.
        if (this.configuredServers.size() == 0) {
            
            Logger.info("Warning: Either no servers have been configured in the config.yml to communicate with the bridge or all servers are set to restricted access.");
            
        }
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Begin running the websocket server.
     */
    @Override
    public void run() {
        
        try {
            
            Logger.info(MessageFormat.format("WebSocketServerBridge listening on port {0}...", this.port));
            
            serverSocket = new ServerSocket(this.port);
            
            //Waiting for the server socket to close or until the server is shutdown manually.
            while ( (this.isServerRunning) && (!serverSocket.isClosed()) ) {

                //Wait for incoming connections.
                Socket socket = serverSocket.accept();

                //Try to reclaim any threads if we are exceeding our maximum.
                //TimeComplexity: O(n) -- Where n is the number of threads valid or invalid.
                //NOTE: Minimal unit testing has been done here...more testing is required.
                if (threads.size() + 1 > this.getMaximumThreads()) {
                    for (int i = 0; i < threads.size(); i ++) {
                        if (!threads.get(i).isAlive()) {
                            threads.remove(i);
                        }
                    }
                }

                //Make sure we have enough threads before accepting.
                //NOTE: Minimal unit testing has been done here...more testing is required.
                if ( (threads.size() + 1) <= this.getMaximumThreads()) {
                    
                    Connection t = new Connection(socket, this);
                    t.start();
                    threads.add(t);
                    
                }
                else {
                    Logger.debug("The server has reached its thread maximum...");
                }

            }
            //END OF while ( (this.isServerRunning) && (!serverSocket.isClosed()) )...
            
            Logger.info("WebSocket server stopping...");
            
        }
        catch (IOException ioException) {
            
            // --- CR (8/10/13) --- Only log the event if the server is still running.  This should prevent an error message from appearing when the server is restarted.
            if (this.isServerRunning) {
                
                Logger.info(MessageFormat.format("The port {0} could not be opened for WebSockets.", this.port));
                Logger.debug(ioException.getMessage());
                
            }
            
            //Close all threads.
            //TimeComplexity: O(n) -- Where n is the number of threads valid or invalid.
            for (Connection t : threads) {
                if (t.isAlive()) {
                    t.close();
                }
            }
            
        }
    }
    
    /**
     * Begins shutting down the server.
     */
    public void Shutdown() {
        
        this.isServerRunning = false;
        try {
            this.serverSocket.close();
        }
        catch(IOException io) {
            //Do nothing...
        }
        
    }
    
    /**
     * Attempts to load the white-list.
     */
    private boolean loadWhiteList() {
        
        File whitelistFile = new File(Globals.PLUGIN_FOLDER + "/" + Globals.WHITE_LIST_FILENAME);
        
        if (whitelistFile.exists()) {
            BufferedReader br = null;
            try {
                
                // --- CR (10-14/12) --- Removed the try-with-resources block to support backwards compatibility.
                //try (BufferedReader br = new BufferedReader(new FileReader(whitelistFile)))
                br = new BufferedReader(new FileReader(whitelistFile));
                if(br != null)
                {
                    String domain;
                    while ((domain = br.readLine()) != null) {
                        if (domain != "") {
                            this.whitelist.add(domain.toUpperCase());
                        }
                    }
                }

                br.close();

                return true;
            }
            catch(FileNotFoundException fnfe){}
            catch(IOException io) {}
            finally {
                try {
                    br.close();
                }
                catch (IOException io) {
                    
                }
            }
        }
        else {
            //The white-list was not found so create it.
            try {
                whitelistFile.createNewFile();
            }
            catch(IOException io) {
                Logger.debug(MessageFormat.format("Cannot create \"{0}/{1}\".", Globals.PLUGIN_FOLDER, Globals.WHITE_LIST_FILENAME));
            }
        }
        //END OF if(whitelistFile.exists())...
        
        return false;
        
    }
    
}
