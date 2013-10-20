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
import java.util.Iterator;
import java.util.Set;

import com.caffeinatedrat.SimpleWebSockets.Frame;
import com.caffeinatedrat.SimpleWebSockets.Handshake;
import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Frame.OPCODE;
import com.caffeinatedrat.WebSocketServicesBridge.ConfiguredServer;
//import com.caffeinatedrat.WebSocketServicesBridge.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * Handles a single websocket connection with the server.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class ProxyConnection extends Thread {
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Socket socket;
    private ProxyServer server;
    private String id;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /*
     * Returns the simple proxy server.
     */
    public ProxyServer getServer() {
        return this.server;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ProxyConnection(Socket socket, com.caffeinatedrat.WebSocketServicesBridge.Server.ProxyServer server) {
        
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
                
                Handshake handshake = new Handshake(this.socket, getServer().getHandshakeTimeout(), getServer().isOriginChecked(), getServer().getWhiteList());
                
                //Negotiate with the client (webbrowser)
                if (handshake.negotiateRequest()) {
                    
                    Logger.debug("Handshaking successful.");
                    
                    try {
                        
                        final Set<ConfiguredServer> configuredServers = this.server.getServers();
                        
                        //Begin the handshaking process for all our configured servers.
                        //A configured server that fails a handshake will have its connection closed and will be ignored by the proxy.
                        Iterator<ConfiguredServer> iterator = configuredServers.iterator();
                        while (iterator.hasNext()) {
                            
                            final ConfiguredServer configuredServerInfo = iterator.next();
                            if (configuredServerInfo.open()) {
                                
                                configuredServerInfo.handshake(handshake);
                                
                            }
                        }
                        //END OF while (iterator.hasNext()) {...

                        //From the proxy back to the client (webbrowser)
                        //final OutputStream proxyOut = this.socket.getOutputStream();
                        
                        //From the client (webbrowser) to the configured server.
                        final InputStream clientIn = this.socket.getInputStream();
                        
                        // Define and create a thread to transmit bytes from client to server
                        Thread c2s = new Thread() {
                          public void run() {
                          
                              try {
                              
                                  final byte[] buffer = new byte[4048];
                                  int bytes_read;
                            
                                  while((bytes_read = clientIn.read(buffer)) != -1) {
    
                                      Iterator<ConfiguredServer> iterator = configuredServers.iterator();
                                      while (iterator.hasNext()) {
                                          
                                          final ConfiguredServer configuredServerInfo = iterator.next();
                                          configuredServerInfo.write(buffer, bytes_read);
                                          
                                      }
                                      //END OF while (iterator.hasNext()) {...
    
                                  }
                              }
                              catch(IOException exception) {
                                  
                              }
                              
                          }
                        };
                        
                        
                        c2s.start();
                        
                        /*
                            
                            /*
                            // Define and create a thread to transmit bytes from client to server
                            Thread c2s = new Thread() {
                              public void run() {
                              
                                  try {
                                      final Socket bukkitServer = new Socket(configuredServerInfo.getAddress(), configuredServerInfo.getPort());
                                      final OutputStream to_out = bukkitServer.getOutputStream();
                                      final InputStream to_in = bukkitServer.getInputStream();
                                  
                                      byte[] buffer = new byte[2048];
                                      int bytes_read;
                                
                                      while((bytes_read = this_in.read(buffer)) != -1) {
                                          to_out.write(buffer, 0, bytes_read);
                                          to_out.flush();
                                      }
                                  
                                      bukkitServer.close();
                                  }
                                  catch (IOException e) {}
                              }
                            };
                            */
                            
                            //c2s.start();
                    
                    }
                    catch (Exception e) {
                        
                        Logger.severe("Blah");
                        
                    }
                    //catch (IOException e) {}

                    
                    //Get the each server.
                    //Map<String, ServerInfo> serverCollection = this.server.getBridge().getProxy().getServers();

                    
                    /*
                    Iterator it = serverCollection.entrySet().iterator();
                    while (it.hasNext()) {
                        
                        Map.Entry pairs = (Map.Entry)it.next();
                        
                        InetSocketAddress address = ((ServerInfo)pairs.getValue()).getAddress(); 
                        
                        try {

                            Socket bukkitServer = new Socket(address.getHostName(), address.getPort());
                            OutputStream out = bukkitServer.getOutputStream();
                            InputStream in = this.socket.getInputStream();
                            
                            byte[] buffer = new byte[2048];
                            int bytes_read;
                            try {
                              while((bytes_read = in.read(buffer)) != -1) {
                                  out.write(buffer, 0, bytes_read);
                                  out.flush();
                              }
                            }
                            catch (IOException e) {}

                            try { out.close(); } catch (IOException e) {}
                            
                        } catch (IOException e) {
                            
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            
                        }
                        
                        it.remove(); // avoids a ConcurrentModificationException
                        
                    }
                    */
                    
                    
                }
                else {
                    
                    Logger.debug("Handshaking failure.");
                    writeCloseFrame("The handshaking has failed.");
                }
                //END OF if(handshake.processRequest())...
            }
            catch (InvalidFrameException invalidFrame) {
                
                Logger.debug("The frame is invalid.");
                
                try {
                    
                    writeCloseFrame("The frame was invalid.");
                }
                catch(InvalidFrameException ife) {
                    //Do nothing...
                }
                
            }

            Logger.debug(MessageFormat.format("Connection {0} terminated.", this.id));
            Logger.verboseDebug(MessageFormat.format("Thread {0} has spun down...", this.getName()));
        }
        finally {
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
    
    /**
     * Close the connection.
     * @param message The message to include in the close frame as to why the frame is closing.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    private void writeCloseFrame(String message) throws InvalidFrameException {
        
        if (this.socket != null) {
        
            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
            Frame closeFrame = new Frame(this.socket);
            closeFrame.setFinalFragment();
            closeFrame.setOpCode(OPCODE.CONNECTION_CLOSE_CONTROL_FRAME);
            closeFrame.setPayload(message);
            closeFrame.write();
            
        }
        
    }    
    
}
