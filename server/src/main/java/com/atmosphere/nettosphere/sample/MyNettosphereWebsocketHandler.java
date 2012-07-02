/*
 * Copyright 2012 Nishant Chandra <nishant.chandra@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.atmosphere.nettosphere.sample;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.atmosphere.websocket.WebSocketProcessor;
import org.atmosphere.websocket.WebSocketProtocol;

/**
 * A simple server to client websocket based streaming handler. The server periodically
 * broadcasts current time to all the connected clients. The server also echoes back any message
 * the client sends to it.
 *
 * @author nishant
 *
 */
public class MyNettosphereWebsocketHandler implements WebSocketProtocol {

    private static final Logger logger = Logger.getLogger(MyNettosphereWebsocketHandler.class);

    private AtmosphereResource r;
    private final ConcurrentHashMap<String, Future<?>> futures = new ConcurrentHashMap<String, Future<?>>();

    public void configure(AtmosphereConfig config) {
        logger.info(String.format("configure(): {Config: %s}", config));
    }

    public void onOpen(WebSocket webSocket) {

        logger.info(String.format(
                "onOpen(): {IP: %s} : {Port: %s}",
                        webSocket.resource().getRequest().getRemoteAddr(),
                        webSocket.resource().getRequest().getRequest().getRemotePort()));

        // Accept the handshake by suspending the response.
        r = (AtmosphereResource) webSocket.resource();

        Broadcaster b = lookupBroadcaster(((AtmosphereRequest) r.getRequest())
                .getPathInfo());
        r.setBroadcaster(b);
        r.addEventListener(new WebSocketEventListenerAdapter());
        r.setSerializer(new MyNettosphereMessageSerializer());

        //Setup a broadcaster to periodically send current time to all connected clients.
        if (b.getAtmosphereResources().size() == 0) {
            if (!futures.containsKey(((AtmosphereRequest) r.getRequest())
                    .getPathInfo())) {

                if(logger.isDebugEnabled())
                    logger.debug("Broadcaster initialized.");

                final Future<?> future = b.scheduleFixedBroadcast(
                        new Callable<String>() {
                            public String call() throws Exception {
                                return new Date().toString();
                            }
                        }, 2, TimeUnit.SECONDS);
                futures.put(((AtmosphereRequest) r.getRequest()).getPathInfo(), future);
            }
        }

        r.suspend(-1);
    }

    public List<AtmosphereRequest> onMessage(WebSocket webSocket, String message) {

        logger.info(String.format(
                "onMessage(): {IP: %s} : {Port: %s} : {Message: %s}",
                        webSocket.resource().getRequest().getRemoteAddr(),
                        webSocket.resource().getRequest().getRequest().getRemotePort(),
                        new String(message)));

        Broadcaster b = lookupBroadcaster(((AtmosphereRequest) r.getRequest())
                .getPathInfo());

        if (message != null) {
            //Do something with the message. Here, simply echo the message back.
            b.broadcast("Server said: " + message);
        }

        // Do not dispatch to another Container like Jersey
        return null;
    }

    public List<AtmosphereRequest> onMessage(WebSocket webSocket,
            byte[] message, int offset, int length) {
        logger.info(String.format(
                "onMessage(): {IP: %s} : {Port: %s} : {Message: %s}", webSocket
                        .resource().getRequest().getRemoteAddr(), webSocket
                        .resource().getRequest().getRequest().getRemotePort(),
                new String(message)));
        return null;
    }

    private Broadcaster lookupBroadcaster(String pathInfo) {
        String[] decodedPath = pathInfo.split("/");
        Broadcaster b = BroadcasterFactory.getDefault().lookup(
                decodedPath[decodedPath.length - 1], true);
        return b;
    }

    public void onClose(WebSocket webSocket) {
        logger.info(String.format("onClose(): {IP: %s} : {Port: %s}",
                webSocket.resource().getRequest().getRemoteAddr(),
                webSocket.resource().getRequest().getRemotePort()));
        webSocket.resource().resume();
    }

    public void onError(WebSocket webSocket, WebSocketProcessor.WebSocketException ex) {
        logger.error(String.format(ex.getMessage() + " Status {%s} Message {%s}",
                webSocket.resource().getResponse().getStatus(),
                ex.response().getStatusMessage()), ex);
    }
}
