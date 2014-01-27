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

package com.caffeinatedrat.WebSocketServicesBridge;

import com.caffeinatedrat.WebSocketServicesBridge.Server.IServiceLayer;
import com.caffeinatedrat.WebSocketServicesBridge.Server.Server;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * The bukkit entry point for WebSocketServicesBridge.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class WebSocketServicesBridge extends Plugin {
	
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Server server = null;
    private WebSocketServicesBridgeConfiguration config = null;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Returns the configuration.
     * @return Returns the configuration.
     */
    public WebSocketServicesBridgeConfiguration getConfig() {
        
        return this.config;
        
    }
    
    // ----------------------------------------------
    // Events
    // ----------------------------------------------
    
    /*
     * This is called when your plug-in is enabled
     */
    @Override
    public void onEnable() {
        
        this.config = new WebSocketServicesBridgeConfiguration(this);
        Logger.debugLevel = this.config.getDebugLevel();
        
        IServiceLayer serviceLayer = new ServiceLayer(this);
        
        //Start-up the server with all the appropriate configuration values.
        server = new Server(this.config.getPortNumber(), serviceLayer, this.config.getConfiguredServers(), this.config.getIsWhiteListed(), this.config.getMaximumConnections());
        server.setHandshakeTimeout(this.config.getHandshakeTimeOut());
        server.setFrameTimeoutTolerance(config.getFrameTimeOutTolerance());
        server.setOriginCheck(this.config.getCheckOrigin());
        server.setPingable(this.config.getIsPingable());
        server.setMaximumFragmentationSize(this.config.getMaximumFragmentationSize());
        
        server.start();

    }
    
    /*
     * This is called when your plug-in shuts down
     */
    @Override
    public void onDisable() {
        
        // save the configuration file, if there are no values, write the defaults.
        server.Shutdown();
        
        // save the configuration file, if there are no values, write the defaults.
        //this.config.saveConfig();
        
    }
    
    /*
     * This is called during a phase when all plug-ins have been loaded but not enabled.
     */
    @Override
    public void onLoad() {
        
        //Override the logger.
        Logger.logger = this.getLogger();
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
}
