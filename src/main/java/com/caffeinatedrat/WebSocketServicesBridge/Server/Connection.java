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

import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import com.caffeinatedrat.SimpleWebSockets.Handshake;
import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Frames.Frame;
import com.caffeinatedrat.SimpleWebSockets.Frames.FrameWriter;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * Handles a single connection to the WebSocketServicesBridge server.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Connection extends Thread {
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Socket socket;
    private Server server;
    private String id;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /*
     * Returns the simple proxy server.
     */
    public Server getServer() {
        return this.server;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Connection(Socket socket, com.caffeinatedrat.WebSocketServicesBridge.Server.Server server) {
        
        if (server == null) {
            throw new IllegalArgumentException("The proxyServer is invalid (null).");
        }
        
        this.socket = socket;
        this.server = server;
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    
    /**
     * Begins managing an individual connection.
     */
    @Override
    public void run() {
        
        Logger.verboseDebug(MessageFormat.format("A new thread {0} has spun up...", this.getName()));
        Logger.debug(MessageFormat.format("Connection {0} established.", this.id));
        
        try {
            
            try {
                
                final Handshake handshake = new Handshake(this.socket, getServer().getHandshakeTimeout(), getServer().isOriginChecked(), getServer().getWhiteList());
                
                //Negotiate with the client (webbrowser)
                if (handshake.negotiateRequest()) {
                    
                    Logger.debug("Handshaking successful.");
                    
                    try {
                        
                        final Set<ConfiguredServer> configuredServers = this.server.getServers();
                        
                        //Instantiate and prepare our collection of servers for this connection.
                        int i = 0;
                        ProxyConnection[] connections = new ProxyConnection[configuredServers.size()];
                        for(ConfiguredServer configuration : configuredServers) {
                            
                            connections[i++] = new ProxyConnection(configuration);
                            
                        }

                        //Begin reading from this connection.
                        ProxyFrameReader reader = new ProxyFrameReader(this.socket, 15000);

                        int numberOfOpenConnections = connections.length;
                        while ( (numberOfOpenConnections > 0) ) {
                        
                            while(reader.read())
                            {
                                Frame readFrame = reader.getFrame();
                                
                                for(ProxyConnection connection : connections) {
                                    
                                    if (connection.open()) {
                                        
                                        if (connection.performHandshake(handshake)) {
                                            
                                            connection.write(readFrame);
                                            
                                        }
                                    }
                                    //END OF if (connection.open()) {...
                                }
                                //END OF for(ProxyConnection connection : connections) {...
                                
                                //There is nothing to read at this point, stop blocking.
                                reader.stopBlocking();
                                
                            }
                            
                            //Respond...test...
                            for(i = 0; i < connections.length; i++) {
                                
                                ProxyConnection connection = connections[i];
                                
                                List<Frame> frames = connection.read();
                                
                                for(Frame frame : frames) {
                                    
                                    if (frame.getOpCode() != Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME) {
                                    
                                        Frame writerFrame = new Frame(frame, this.socket);
                                        writerFrame.write();
                                        
                                    }
                                    else {
                                        
                                        numberOfOpenConnections--;
                                        break;
                                        
                                    }
                                    
                                }
                            }
                            
                        }
                        //END OF while ( (numberOfOpenConnections > 0) && (this.socket.isConnected() && !this.socket.isClosed()) ) {...
                        
                        for(ProxyConnection connection : connections) {
                            connection.close();
                        }
                        
                    }
                    catch (Exception e) {
                        
                        Logger.severe(e.getMessage());
                        
                    }
                    
                }
                else {
                    
                    Logger.debug("Handshaking failure.");
                    FrameWriter.writeClose(this.socket, "The handshaking has failed.");
                }
                //END OF if(handshake.processRequest())...
            }
            catch (InvalidFrameException invalidFrame) {
                
                Logger.debug("The frame is invalid.");
                
                try {
                    
                    FrameWriter.writeClose(this.socket, "The frame was invalid.");
                    
                }
                catch(InvalidFrameException ife) {
                    //Do nothing...
                }
                
            }

            Logger.debug(MessageFormat.format("Connection {0} terminated.", this.id));
            Logger.verboseDebug(MessageFormat.format("Thread {0} has spun down...", this.getName()));
        }
        finally {

            //NOTE: Do not close the connection!
            close();
        }        
    }
    
    /**
     * Close the connection.
     */
    public void close() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch(IOException io) {
            //Do nothing...
        }
    }
    
}
