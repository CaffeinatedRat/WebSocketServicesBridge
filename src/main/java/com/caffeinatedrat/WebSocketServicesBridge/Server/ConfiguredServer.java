/**
* Copyright (c) 2013-2014, Ken Anderson <caffeinatedrat at gmail dot com>
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

import java.text.MessageFormat;

import com.caffeinatedrat.WebSocketServicesBridge.Exception.InvalidConfiguredServerInfoException;

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
    private int maximumNumberOfSupportedFragmentedFrames;
    
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
     * Returns the maximum number of supported fragmented frames for this server.
     * @return the maximum number of supported fragmented frames for this server.
     */    
    public int getMaximumNumberOfSupportedFragmentedFrames() {
        return this.maximumNumberOfSupportedFragmentedFrames;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ConfiguredServer(String serverName, String hostAddress, int port, int maximumNumberOfSupportedFragmentedFrames) {
        
        this.serverName = serverName;
        this.hostAddress = hostAddress;
        this.port = port;
        this.maximumNumberOfSupportedFragmentedFrames = maximumNumberOfSupportedFragmentedFrames;
        
    }
    
    public ConfiguredServer(String serverName, String address, int maximumNumberOfSupportedFragmentedFrames) 
            throws InvalidConfiguredServerInfoException {
        
        this.serverName = serverName;
        String[] tokens = address.split(":", 2);
        if (tokens.length == 2) {
            
            this.hostAddress = tokens[0];
            try {

                this.port =  Integer.parseInt(tokens[1]);
                
            }
            catch (NumberFormatException nfe) {
                
                throw new InvalidConfiguredServerInfoException(MessageFormat.format("The port {0} for the ConfiguredServerInfo class is invalid.", tokens[1]));
                
            }
            
        }
        else {
            
            throw new InvalidConfiguredServerInfoException(MessageFormat.format("The address {0} for the ConfiguredServerInfo class is invalid.  It should contain a host(ip) and port.", address));
            
        }
        
    }
    
    public ConfiguredServer(String serverName, String address) 
            throws InvalidConfiguredServerInfoException {
        
        this(serverName, address, 2);
        
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
    
}
