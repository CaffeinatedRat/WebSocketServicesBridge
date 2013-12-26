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

package com.caffeinatedrat.WebSocketServicesBridge.Server;

import java.net.Socket;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Frames.Frame;
import com.caffeinatedrat.SimpleWebSockets.Frames.FrameReader;
import com.caffeinatedrat.SimpleWebSockets.Payload.*;

public class ProxyFrameReader extends FrameReader {

    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Read-Only property that returns the type of the frame.
     * @return the type of the frame.
     */
    public Frame.OPCODE getFrameType() {
        return this.frameType;
    }
    
    /**
     * Not supported for this class.
     */
    @Override
    public Payload getPayload() {
        
        throw new UnsupportedOperationException("The method getPayload() is not supported for this class.");
        
    }
    
    /**
     * Not supported for this class.
     */
    @Override
    public TextPayload getTextPayload() {
        
        throw new UnsupportedOperationException("The method getTextPayload() is not supported for this class.");
        
    }    
    
    public Frame getFrame() {
        
        return this.frame;
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ProxyFrameReader(Socket socket) throws InvalidFrameException {
        
        this(socket, 1000);
        
    }
    
    public ProxyFrameReader(Socket socket, int timeout) throws InvalidFrameException {
    
        super(socket, null, timeout, 2);
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------

    /**
     * Reads a single frame.
     * @return true if a frame has been read or false if nothing was available.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    @Override
    public boolean read()
            throws InvalidFrameException {
        
        // --- CR (7/20/13) --- Start blocking only if a frame is available or this is the initial request, which requires blocking.
        // --- CR (11/18/13) --- The FrameReader will remain in blocking mode indefinitely unless told to stop by the method stopBlocking.
        if (isAvailable() || this.initialBlocking || this.blocking) {
            
            //If we enter this block, we are either:
            //1) Blocking until we receive a frame during the initial opening of the connection.
            //2) Blocking to receive a new frame.
            this.frame.read();
            this.frameType = this.frame.getOpCode();
            
            //Blocking has ended unless something is available for future reads.
            this.initialBlocking = false;
           
            //The initial read has been completed.
            return true;
            
        }
        else {
            
            //Nothing is available to read...
            return false;
            
        }
        //END OF if (isAvailable() || this.initialBlocking || this.blocking) {...
        
    }
    
}
