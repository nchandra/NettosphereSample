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

import org.apache.log4j.Logger;
import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;

/**
 * Start the Netty server.
 * @author nishant
 *
 */
public class MyNettosphereServer {

    private static final Logger logger = Logger.getLogger(MyNettosphereServer.class);

    public static void main(String[] args) {

        int port = 9080;

        Nettosphere server = new Nettosphere.Builder().config(
                new Config.Builder().host("127.0.0.1").port(port)
                        .webSocketProtocol(MyNettosphereWebsocketHandler.class)
                        .build()).build();
        server.start();

        logger.info("Server started on port: " + port);
    }
}
