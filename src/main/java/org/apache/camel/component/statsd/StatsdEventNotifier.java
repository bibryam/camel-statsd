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

import java.util.EventObject;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.apache.camel.management.event.CamelContextStartupFailureEvent;
import org.apache.camel.management.event.CamelContextStopFailureEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.management.event.ExchangeFailureHandledEvent;
import org.apache.camel.management.event.ExchangeRedeliveryEvent;
import org.apache.camel.management.event.ServiceStartupFailureEvent;
import org.apache.camel.management.event.ServiceStopFailureEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link org.apache.camel.spi.EventNotifier} which sends alters to Statsd server.
 * */
public class StatsdEventNotifier extends EventNotifierSupport {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(StatsdEventNotifier.class);

    private StatsDClient statsd;
    private String prefix;
    private String host;
    private Integer port;

    public void notify(EventObject eventObject) throws Exception {
        String key = determineKey(eventObject);
        LOGGER.trace("Statsd event: " + key);
        statsd.increment(key);
     }

    protected String determineKey(EventObject eventObject) {
        // failures is considered error
        if (eventObject instanceof ExchangeFailedEvent
                || eventObject instanceof CamelContextStartupFailureEvent
                || eventObject instanceof CamelContextStopFailureEvent
                || eventObject instanceof ServiceStartupFailureEvent
                || eventObject instanceof ServiceStopFailureEvent) {
            return "failure";
        }

        // the failure was handled so its just a warning
        // and warn when a redelivery attempt is done
        if (eventObject instanceof ExchangeFailureHandledEvent
                || eventObject instanceof ExchangeRedeliveryEvent) {
            return "warning";
        }

        return "success";
    }

    public boolean isEnabled(EventObject eventObject) {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        statsd = new NonBlockingStatsDClient(prefix, host, port);
    }

    @Override
    protected void doStop() throws Exception {
        statsd.stop();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
