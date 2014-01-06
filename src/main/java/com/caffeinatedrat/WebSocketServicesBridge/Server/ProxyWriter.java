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
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * Handles writing to the client from the proxy.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class ProxyWriter {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    Socket socket = null;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ProxyWriter(Socket socket) {
        
        if (socket == null) {
            throw new IllegalArgumentException("The socket is invalid (null).");
        }
        
        this.socket= socket;
        
    }
    
    // ----------------------------------------------
    // Public Methods
    // ----------------------------------------------
    public synchronized void Write(List<Frame> frames) throws InvalidFrameException {
        
        for(Frame frame : frames) {
            
            String string = frame.getPayloadAsString();
            Logger.verboseDebug(MessageFormat.format("Writing frame: {0}", string));
            
            Frame responseFrame = new Frame(frame, this.socket);
            responseFrame.write();
        }

    }
    
}
