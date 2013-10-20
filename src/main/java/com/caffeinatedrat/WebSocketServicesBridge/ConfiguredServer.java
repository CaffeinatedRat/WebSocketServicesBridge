/**
* Copyright (c) 2013, Ken Anderson <caffeinatedrat at gmail dot com>
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

package com.caffeinatedrat.WebSocketServicesBridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import com.caffeinatedrat.SimpleWebSockets.Handshake;
import com.caffeinatedrat.WebSocketServicesBridge.Exception.InvalidConfiguredServerInfoException;
//import com.caffeinatedrat.WebSocketServicesBridge.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

public class ConfiguredServer {

    public enum StateInfo {
        UNINITIALIZED,
        INITIALIZED,
        CONNECTING,
        CONNECTED,
        CLOSED,
        BADSTATE
    }
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    private String serverName;
    private String hostAddress;
    private int port;
    private Socket socket = null;
    private StateInfo state = StateInfo.UNINITIALIZED;
    private Handshake handshake = null;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Returns the name of the server.
     * @return returns the name of the server.
     */
    public String getServerName() {
        return this.serverName;
    }
    
    /**
     * Returns the host or IP address.
     * @return returns the host or IP address.
     */
    public String getAddress() {
        return this.hostAddress;
    }
    
    /**
     * Returns the port.
     * @return returns the port.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * Returns the socket.
     * @return the socket.
     */
    public Socket getSocket() {
        return this.socket;
    }
    
    /**
     * Returns the state of the Configured Server.
     * @return the state of the Configured Server.
     */
    public StateInfo getState() {
        return this.state;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ConfiguredServer(String serverName, String hostAddress, int port) {
        this.serverName = serverName;
        this.hostAddress = hostAddress;
        this.port = port;
        
        this.state = StateInfo.INITIALIZED;
    }
    
    public ConfiguredServer(String serverName, String address) 
            throws InvalidConfiguredServerInfoException {
        
        this.serverName = serverName;
        String[] tokens = address.split(":", 2);
        if (tokens.length == 2) {
            this.hostAddress = tokens[0];
            try {
                this.port =  Integer.parseInt(tokens[1]);
                
                this.state = StateInfo.INITIALIZED;
            }
            catch (NumberFormatException nfe) {
                throw new InvalidConfiguredServerInfoException(MessageFormat.format("The port {0} for the ConfiguredServerInfo class is invalid.", tokens[1]));
            }
        }
        else {
            throw new InvalidConfiguredServerInfoException(MessageFormat.format("The address {0} for the ConfiguredServerInfo class is invalid.  It should contain a host(ip) and port.", address));
        }
    }
    
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    @Override
    public boolean equals(Object argument) {

        return (this.serverName.equalsIgnoreCase(((ConfiguredServer)argument).serverName));
        
    }
    
    @Override
    public int hashCode() {

        return this.serverName.hashCode();
        
    }
    
    /**
     * Attempts to open a connection to the configured server.
     * @return true if the connection was successful.
     */
    public boolean open() {
        
        //We are already connected or connecting, ignore this.
        if (this.state == StateInfo.CONNECTED || this.state == StateInfo.CONNECTING) {
            return true;
        }
        
        if (this.state == StateInfo.INITIALIZED || this.state == StateInfo.CLOSED) {
        
            try {
                
                //If a socket exists, see if we can reuse it if it is closed.
                if (this.socket != null) {
                
                    if ( this.socket.isClosed() ) {
                    
                        this.socket = new Socket(getAddress(), getPort());
                    }
                    
                }
                else {
                    
                    this.socket = new Socket(getAddress(), getPort());
                    
                }
                
                Logger.verboseDebug(MessageFormat.format("A connection to the configured server {0} has been opened.", this.serverName));
                this.state = StateInfo.CONNECTING;
                return true;
            }
            catch (UnknownHostException e) {
    
                this.state = StateInfo.CLOSED;
                Logger.info(MessageFormat.format("The host {0} is invalid or could not be resolved.", getAddress()));
                Logger.verboseDebug(MessageFormat.format("Details: {0}", e.getMessage()));
            }
            catch (IOException e) {
                
                this.state = StateInfo.CLOSED;
                Logger.verboseDebug(MessageFormat.format("Unknown IOException.\r\n Details: {0}", e.getMessage()));
                
            }
        }
        else {
            
            Logger.verboseDebug(MessageFormat.format("The connection cannot be opened during this state {0}", this.state.toString()));
            
        }
        
        return false;
    }
    
    /**
     * Attempts to close a connection to the configured server.
     */
    public void close() {
        if (this.socket != null) {
            try {
                
                Logger.verboseDebug(MessageFormat.format("The connection to the configured server {0} has been closed.", this.serverName));
                
                this.state = StateInfo.CLOSED;
                socket.close();
                
            } catch (IOException e) {
                //Do nothing...
            }
        }
    }
    
    /**
     * Attempt to handshake with the configured server.
     * @param handshake An already established handshake from the original client.
     * @return true if the handshake was successful
     */
    public boolean handshake(Handshake handshake) {
        
        //We are already connected ignore this.
        if (this.state == StateInfo.CONNECTED) {
            return true;
        }
        
        if (this.state == StateInfo.CONNECTING) {
            
            this.handshake = handshake.cloneHandshake(this.socket);
            this.handshake.forwardRequest();
            if(this.handshake.negotiateResponse()) {
                
                this.state = StateInfo.CONNECTED;
                return true;
            }
            else {
                Logger.verboseDebug("The handshake has failed.");
            }
            
        }
        else {
            
            Logger.verboseDebug(MessageFormat.format("The handshake cannot be performed during this state {0}", this.state.toString()));
            
        }
        
        //Close the socket.
        close();
        return false;
    }
    
    /**
     * Attempt to write everything in the inputstream to the configured server.
     * @param inputStream The stream to write to the configured server.
     * @return true if the handshake was successful
     */    
    public void write(byte[] buffer, int size) {
        
        //We can only write if the connection is still opened.
        if (this.state == StateInfo.CONNECTED) {
        
            try {
                
                OutputStream outputStream = this.socket.getOutputStream();
            
                outputStream.write(buffer, 0, size);
                outputStream.flush();
            
            }
            catch (IOException e) {
                
                Logger.verboseDebug(MessageFormat.format("The write cannot be performed due to some unexpected exception: {0}", e.getMessage()));
                close();
                
            }
        }
        else {
            Logger.verboseDebug(MessageFormat.format("The write cannot be performed during this state {0}", this.state.toString()));
        }
    }
    
}
