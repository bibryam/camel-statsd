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

import com.timgroup.statsd.StatsDClient;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Statsd producer.
 */
public class StatsdProducer extends DefaultProducer {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(StatsdProducer.class);
    private StatsdEndpoint endpoint;
    private StatsDClient statsd;

    public StatsdProducer(StatsdEndpoint endpoint, StatsDClient statsd) {
        super(endpoint);
        this.endpoint = endpoint;
        this.statsd = statsd;
    }

    public void process(Exchange exchange) throws Exception {
        getOperation(exchange).execute(statsd, endpoint, exchange);
    }

    private Operation getOperation(Exchange exchange) {
        String operation = exchange.getIn().getHeader(StatsdConstants.OPERATION, String.class);
        if (operation == null && endpoint.getOperation() != null) {
            operation = StatsdConstants.OPERATION + endpoint.getOperation();
        }
        if (operation == null) {
            operation = StatsdConstants.COUNT;
        }
        LOGGER.trace("Operation: [{}]", operation);
        return Operation.valueOf(operation.substring(StatsdConstants.OPERATION.length()).toUpperCase());
    }

    enum Operation {
        COUNT {
            @Override
            void execute(StatsDClient statsd, StatsdEndpoint endpoint, Exchange exchange) {
                statsd.count(getKey(endpoint, exchange), getValue(endpoint, exchange));
            }
        }, GAUGE {
            @Override
            void execute(StatsDClient statsd, StatsdEndpoint endpoint, Exchange exchange) {
                statsd.gauge(getKey(endpoint, exchange), getValue(endpoint, exchange));
            }
        }, INCREMENT {
            @Override
            void execute(StatsDClient statsd, StatsdEndpoint endpoint, Exchange exchange) {
                statsd.increment(getKey(endpoint, exchange));
            }
        }, DECREMENT {
            @Override
            void execute(StatsDClient statsd, StatsdEndpoint endpoint, Exchange exchange) {
                statsd.decrement(getKey(endpoint, exchange));
            }
        }, TIME {
            @Override
            void execute(StatsDClient statsd, StatsdEndpoint endpoint, Exchange exchange) {
                statsd.time(getKey(endpoint, exchange), getValue(endpoint, exchange));
            }
        };

        String getKey(StatsdEndpoint endpoint, Exchange exchange) {
            String key = exchange.getIn().getHeader(StatsdConstants.KEY, String.class);
            if (key == null) {
                key = endpoint.getKey();
            }
            return key;
        }

        Integer getValue(StatsdEndpoint endpoint, Exchange exchange) {
            Integer value = exchange.getIn().getHeader(StatsdConstants.VALUE, Integer.class);
            if (value == null) {
                value = endpoint.getValue();
            }
            if (value == null) {
                value = Integer.valueOf(1);
            }
            return value;
        }

        abstract void execute(StatsDClient statsd, StatsdEndpoint endpoint, Exchange exchange);
    }
}
