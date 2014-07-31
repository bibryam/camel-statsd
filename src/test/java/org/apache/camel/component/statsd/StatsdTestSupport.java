/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.statsd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;

public class StatsdTestSupport extends CamelTestSupport {
    public static final int STATSD_PORT = 12345;
    private TestStatsDServer testServer;

    @Before
    @Override
    public void setUp() throws Exception {
        testServer = new TestStatsDServer(STATSD_PORT);
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        testServer.stop();
    }

    private static final class TestStatsDServer {
        private final List<String> messages = new ArrayList<String>();
        private final DatagramSocket socket;

        public TestStatsDServer(int port) throws Exception {
            socket = new DatagramSocket(port);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            final DatagramPacket packet = new DatagramPacket(new byte[256], 256);
                            socket.receive(packet);
                            messages.add(new String(packet.getData(), "UTF-8").trim());
                        }
                    } catch (Exception e) {
                    }
                }
            }).start();
        }

        public void stop() {
            socket.close();
        }

        public void waitForMessage() {
            while (messages.isEmpty()) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public List<String> getReceivedMessages() {
        testServer.waitForMessage();
        return new ArrayList<String>(testServer.messages);
    }
}