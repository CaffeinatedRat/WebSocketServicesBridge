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

import java.net.Socket;
import java.text.MessageFormat;
import java.util.List;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Frames.Frame;

/**
 * Handles writing to the client from the proxy.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class ProxyFrameWriter {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    Socket socket = null;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    /**
     * Constructor
     * @param socket the socket to write frames to.
     */
    public ProxyFrameWriter(Socket socket) {
        
        if (socket == null) {
            throw new IllegalArgumentException("The socket is invalid (null).");
        }
        
        this.socket= socket;
        
    }
    
    // ----------------------------------------------
    // Public Methods
    // ----------------------------------------------
    
    /**
     * Writes a collection of frames to an endpoint.
     * @param frames the frames to write to an endpoint.
     * @param serverName the servername to include in the frame wrapper.
     * @throws InvalidFrameException if the frame is invalid.
     */
    public synchronized void Write(List<Frame> frames, String serverName) throws InvalidFrameException {
        
        if (frames == null) {
            return;
        }
        
        if(frames.size() > 0) {
            
            Frame.OPCODE opCode = frames.get(0).getOpCode();
            
            if (opCode == Frame.OPCODE.TEXT_DATA_FRAME) {
                
                Frame header = new Frame(this.socket);
                header.clearFinalFragment();
                header.setOpCode(opCode);
                //TODO: Version number in the WSSB.
                header.setPayload(MessageFormat.format("'{'\"WSSB\": 1, \"serverName\":\"{0}\",\"serverInfo\":", serverName));
                header.write();
                
            }
            //END OF if (opCode == Frame.OPCODE.TEXT_DATA_FRAME) {...
            
            for(Frame frame : frames) {

                frame.clearFinalFragment();
                frame.setOpCode(Frame.OPCODE.CONTINUATION_DATA_FRAME);
                
                frame.write(this.socket);
            }
            
            if (opCode == Frame.OPCODE.TEXT_DATA_FRAME) {
                
                Frame footer = new Frame(this.socket);
                footer.setFinalFragment();
                footer.setOpCode(Frame.OPCODE.CONTINUATION_DATA_FRAME);
                footer.setPayload("}");
                footer.write();
                
            }
            //END OF if (opCode == Frame.OPCODE.TEXT_DATA_FRAME) {...
        }
        //END OF if(frames.size() > 0) {...
        
    }
    
}
