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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

/**
 * Connect to the server programmatically and print the response.
 * For a Javascript based client, see index.html.
 * @author nishant
 *
 */
public class TestNettosphereClient {

    private static final int port = 9080;
    private static final String wsUrl = "ws://127.0.0.1:" + port;

    public static void main(String[] args) throws Exception {

        final CountDownLatch l = new CountDownLatch(1);

        final AtomicReference<String> response = new AtomicReference<String>();
        AsyncHttpClient c = new AsyncHttpClient();
        WebSocket webSocket = c.prepareGet(wsUrl + "/test")
                .execute(new WebSocketUpgradeHandler.Builder().build()).get();

        webSocket.addWebSocketListener(new WebSocketTextListener() {

            @Override
            public void onMessage(String message) {
                System.out.println("onMessage");
                response.set(message);
                l.countDown();
            }

            @Override
            public void onFragment(String fragment, boolean last) {
                System.out.println("onFragment");
            }

            @Override
            public void onOpen(WebSocket websocket) {
                System.out.println("onOpen");
            }

            @Override
            public void onClose(WebSocket websocket) {
                System.out.println("onClose");
                l.countDown();
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError");
                l.countDown();
            }
        }).sendTextMessage("Ping");

        l.await(5, TimeUnit.SECONDS);

        webSocket.close();
        String resp = response.get();
        System.out.println("Response: " + resp);
    }
}
