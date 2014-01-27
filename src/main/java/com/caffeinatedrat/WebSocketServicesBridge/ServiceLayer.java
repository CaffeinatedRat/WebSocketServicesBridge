package com.caffeinatedrat.WebSocketServicesBridge;

import java.util.List;

import com.caffeinatedrat.SimpleWebSockets.Frames.Frame;
import com.caffeinatedrat.WebSocketServicesBridge.WebSocketServicesBridgeConfiguration.ServiceState;
import com.caffeinatedrat.WebSocketServicesBridge.Server.IServiceLayer;
import com.caffeinatedrat.WebSocketServicesBridge.Server.ProxyFrameWriter;

public class ServiceLayer implements IServiceLayer {
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    private WebSocketServicesBridge plugin;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ServiceLayer(WebSocketServicesBridge plugin) {
        
        if (plugin == null) {
            
            throw new IllegalArgumentException("The plugin argument is invalid (null).");
            
        }
        
        this.plugin = plugin;
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Allows the proxy to handle some services depending on the ServiceState.
     */
    public boolean proxyServiceHandler(List<Frame> framesFromClient, ProxyFrameWriter writer) {
        
        if (framesFromClient.size() > 0) {
            
            //Check the first frame and only the first frame per the precondition that a service name must only exist in the first frame.
            String service = framesFromClient.get(0).getPayloadAsString();
            String[] tokens = service.split(" ", 2);
            String serviceName = tokens[0].toLowerCase();
            
            ServiceState serverState = this.plugin.getConfig().getServiceState(serviceName);
            if (serverState == ServiceState.Passive) {
                return false;
            }
            else {
                
                //TODO: -- Logic to be implemented later.
                /*
                if (serviceName.equalsIgnoreCase("whitelist")) {
                    whiteList(framesFromClient, writer);
                    return true;
                }
                */
            }
            
            return false;
        }
        
        return false;
    }
    
    /*
    private void whiteList(List<Frame> framesFromClient, ProxyFrameWriter writer) {
        
    }
    */
    
}
