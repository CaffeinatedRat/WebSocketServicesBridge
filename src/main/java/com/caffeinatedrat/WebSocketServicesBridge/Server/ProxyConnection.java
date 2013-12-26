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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.caffeinatedrat.SimpleWebSockets.Handshake;
import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Frames.Frame;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * Handles a single threaded connection to a targeted server.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class ProxyConnection {

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
    private ConfiguredServer configuredServer = null;
    
    private Socket socket = null;
    private StateInfo state = StateInfo.UNINITIALIZED;
    
    //This member variable has two states due to the fact we are going to recycle it.
    // 1) Before the connection to the server is opened, this holds the original handshake request from the client.
    // 2) After the connection to the server is opened, this holds the handshake to the proxy connection (to the server endpoint).
    private Handshake handshake = null;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
   
    /**
     * Returns true if the connection is closed.
     * @return true if the connection is closed.
     */
    public boolean isClosed() {
        
        if (this.socket != null) {
            return this.socket.isClosed();
        }
        else {
            return true;
        }
    }
    
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ProxyConnection(ConfiguredServer configuredServer) {
        
        if (configuredServer == null) {
            throw new IllegalArgumentException("The configuredServer is invalid (null).");
        }
        
        this.configuredServer = configuredServer;
        this.state = StateInfo.INITIALIZED;
        this.handshake = null;
        
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
        
        if (this.state == StateInfo.INITIALIZED ) {
        
            String address = this.configuredServer.getAddress();
            
            try {
                
                //If a socket exists, see if we can reuse it if it is closed.
                if (this.socket != null) {
                
                    if ( this.socket.isClosed() ) {
                    
                        this.socket = new Socket(address, this.configuredServer.getPort());
                    }
                    
                }
                else {
                    
                    this.socket = new Socket(address, this.configuredServer.getPort());
                    
                }
                
                Logger.verboseDebug(MessageFormat.format("A connection to the configured server {0} has been opened.", this.configuredServer.getServerName()));
                this.state = StateInfo.CONNECTING;
                return true;
            }
            catch (UnknownHostException e) {
    
                this.state = StateInfo.CLOSED;
                Logger.info(MessageFormat.format("The host {0} is invalid or could not be resolved.", address));
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
                
                Logger.verboseDebug(MessageFormat.format("The connection to the configured server {0} has been closed.", this.configuredServer.getServerName()));
                
                this.state = StateInfo.CLOSED;
                socket.close();
                
            } catch (IOException e) {
                //Do nothing...
            }
            
        }
        
    }
    
    /**
     * Attempt to handshake with the configured server.
     * @param handshakeRequest An already established handshake from the original client.
     * @return true if the handshake was successful
     */
    public boolean performHandshake(Handshake handshakeRequest) {
        
        //We are already connected ignore this.
        if (this.state == StateInfo.CONNECTED) {
            return true;
        }
        
        if (this.state == StateInfo.CONNECTING) {
            
            this.handshake = handshakeRequest.cloneHandshake(this.socket);
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
     * @param buffer The buffer to write to the configured server.
     * @param size The size of the buffer to write to the configured server.
     * @return true if the handshake was successful
     */
    /*
    private void write(byte[] buffer, int size) {
        
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
    */
    
    /**
     * Attempt to write a read frame to the configured server.
     * @param frame The stream to write to the configured server.
     * @return true if the handshake was successful
     */    
    public void write(Frame frame) throws InvalidFrameException {
        
        //For now, do nothing on null frames.
        if (frame == null) {
            return;
        }
        
        //We can only write if the connection is still opened.
        if (this.state == StateInfo.CONNECTED) {
        
            try {
                
                Frame responseFrame = new Frame(frame, this.socket);
                Logger.verboseDebug(MessageFormat.format("INPUT: {0}", responseFrame.getPayloadAsString()));
                responseFrame.write();
            
            } catch (InvalidFrameException e) {
                 
                Logger.verboseDebug(MessageFormat.format("An invalid frame was supplied: {0}", e.getMessage()));
                close();
                throw e;

            }
        }
        else {
            
            Logger.verboseDebug(MessageFormat.format("The write cannot be performed during this state {0}", this.state.toString()));
            
        }
    }
    
    public List<Frame> read() throws InvalidFrameException  {
        
        List<Frame> frames = new ArrayList<Frame>();
        
        try {
            
            ProxyFrameReader reader = new ProxyFrameReader(this.socket, 15000);
            while(reader.read())
            {
                Frame frame = reader.getFrame();
                frames.add(new Frame(frame));
                
                if (frame.getOpCode() == Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME) {
                    break;
                }
            }
            
        } catch (InvalidFrameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        return frames;
    }
    
}
