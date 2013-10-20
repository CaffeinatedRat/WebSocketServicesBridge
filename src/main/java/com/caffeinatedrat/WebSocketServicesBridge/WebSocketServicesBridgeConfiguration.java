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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import net.craftminecraft.bungee.bungeeyaml.bukkitapi.ConfigurationSection;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.YamlConfiguration;
import net.md_5.bungee.api.plugin.Plugin;

import com.caffeinatedrat.WebSocketServicesBridge.Exception.InvalidConfiguredServerInfoException;
//import com.caffeinatedrat.WebSocketServicesBridge.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

public class WebSocketServicesBridgeConfiguration extends YamlConfiguration {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Plugin pluginInfo;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Returns this plug-in's information.
     * @return This plug-in's information.
     */
    public Plugin getPlugInfo() {
        
        return this.pluginInfo;
        
    }
    
    /**
     * Safely returns the port number.
     * @return Safely returns the port number.
     */
    public int getPortNumber() {
        
        int portNumber = getInt("websocketservicesbridge.portNumber", 25564);
        
        //Validate the port number.
        if (portNumber < 0 || portNumber > 65535) {
        
            Logger.warning(MessageFormat.format("The port number {0} is invalid, defaulting to port 25564.", portNumber));
            return 25564;
            
        }
        
        return portNumber;
        
    }
    
    /**
     * Safely returns the maximum number of connections.
     * @return Safely returns the maximum number of connections.
     */
    public int getMaximumConnections() {
        
        int maxConnections = getInt("websocketservicesbridge.maximumConnections", 32);
        
        //Validate
        if (maxConnections < 0) {
        
            Logger.warning(MessageFormat.format("The maximum number of connections {0} is invalid, defaulting to 32.", maxConnections));
            return 32;
            
        }
        
        return maxConnections;
    }
    
    /**
     * Determines if the WebSocketServices plug-in is white-listed.
     * @return true if the WebSocketServices server is white-listed.
     */
    public boolean getIsWhiteListed() {
        
        return getBoolean("websocketservicesbridge.whitelist", false);

    }
    
    /**
     * Safely returns the logging level.
     * @return safely returns the logging level.
     */
    public int getDebugLevel() {
        int logLevel = getInt("websocketservicesbridge.logging", 0);
        
        //Validate
        if ( (logLevel < 0) || (logLevel > 2) ) {
        
            Logger.warning(MessageFormat.format("The log level {0} is invalid, defaulting to 0.", logLevel));
            return 0;
            
        }
        
        return logLevel;
        
    }
    
    /**
     * Safely returns the handshake timeout in milliseconds.
     * @return safely returns the handshake timeout in milliseconds.
     */
    public int getHandshakeTimeOut() {
        
        int timeout = getInt("websocketservicesbridge.handshakeTimeOutTolerance", 1000);
        
        //Validate
        if (timeout < 0) {
        
            Logger.warning(MessageFormat.format("The handshake timeout tolerance {0} is invalid, defaulting to 1000.", timeout));
            return 1000;
            
        }
        
        return timeout;
        
    }
    
    /**
     * Determines if the origin is checked when establishing a connection.
     * @return true if the origin is checked when establishing a connection.
     */
    public boolean getCheckOrigin() {
        return getBoolean("websocketservicesbridge.checkOrigin", false);
    }
    
    /**
     * Determines if the WebSocketServices server is pingable.
     * @return true if the WebSocketServices server is pingable.
     */
    public boolean getIsPingable() {
        return getBoolean("websocketservicesbridge.pingable", false);
    }
    
    /**
     * Returns a set of servers non-restricted servers from the config.yml
     * @return A collection of non-restricted servers.
     */
    public Set<ConfiguredServer> getConfiguredServers() {
        
        Set<ConfiguredServer> configuredServers = new HashSet<ConfiguredServer>();
        
        ConfigurationSection configSection = getConfigurationSection("websocketservers");

        if(configSection != null) {
            
            Set<String> serverKeys = configSection.getKeys(false);
            
            for (String serverKey : serverKeys) {
                
                if (!configSection.getBoolean(serverKey + ".restricted")) {
                    
                    try {
                        
                        //Add the configured server if the address information is a valid host and port.
                        ConfiguredServer configuredServerInfo = new ConfiguredServer(serverKey, configSection.getString(serverKey + ".address"));
                        
                        if(!configuredServers.contains(configuredServerInfo)) {
                            
                            configuredServers.add(configuredServerInfo);
                            
                        }
                        else {
                            
                            Logger.info(MessageFormat.format("The configured WebSocketServer {0} already exists.", serverKey));
                            
                        }
                        
                    }
                    catch(InvalidConfiguredServerInfoException ex) {
                        
                        Logger.info(ex.getMessage());
                        
                    }

                }
                
            }
            //END OF for (String serverKey : serverKeys) {...
        }
        //END OF if(configSection != null) {...
        
        return configuredServers;
        
    }
    
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public WebSocketServicesBridgeConfiguration(Plugin plugin) {
        super();
        
        this.pluginInfo = plugin;
        
        try {
            load(new File(plugin.getDataFolder(), "config.yml"));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            Logger.severe("Cannot load config.yml");
        } catch (InvalidConfigurationException e) {
            Logger.severe("Cannot load config.yml");
        }
    }
}
