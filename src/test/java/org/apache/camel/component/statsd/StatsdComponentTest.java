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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import static org.hamcrest.Matchers.hasItem;

public class StatsdComponentTest extends StatsdTestSupport {

    @Test
    public void countsForDefaultKeys() throws Exception {
        template.sendBody("direct:start", null);
        assertThat(getReceivedMessages(), hasItem("my.host.test.key:1|c"));
    }

    @Test
    public void countsWithDifferentKeyAndValue() throws Exception {
        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(StatsdConstants.KEY, "another.key");
                exchange.getIn().setHeader(StatsdConstants.VALUE, "2");
                exchange.getIn().setHeader(StatsdConstants.OPERATION, StatsdConstants.COUNT);
            }
        });

        assertThat(getReceivedMessages(), hasItem("my.host.another.key:2|c"));
    }

    @Test
    public void decrementsWithDefaultKey() throws Exception {
        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(StatsdConstants.OPERATION, StatsdConstants.DECREMENT);
            }
        });

        assertThat(getReceivedMessages(), hasItem("my.host.test.key:-1|c"));
    }

    @Test
    public void incrementsWithDefaultKey() throws Exception {
        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(StatsdConstants.OPERATION, StatsdConstants.INCREMENT);
                exchange.getIn().setHeader(StatsdConstants.VALUE, "3");
            }
        });

        assertThat(getReceivedMessages(), hasItem("my.host.test.key:1|c"));
    }

    @Test
    public void gaugesWithDefaultKey() throws Exception {
        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(StatsdConstants.OPERATION, StatsdConstants.GAUGE);
                exchange.getIn().setHeader(StatsdConstants.VALUE, "3");
            }
        });

        assertThat(getReceivedMessages(), hasItem("my.host.test.key:3|g"));
    }

    @Test
    public void timingWithDefaultKey() throws Exception {
        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(StatsdConstants.OPERATION, StatsdConstants.TIME);
                exchange.getIn().setHeader(StatsdConstants.VALUE, "4");
            }
        });

        assertThat(getReceivedMessages(), hasItem("my.host.test.key:4|ms"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {

                from("direct:start")
                        .to("statsd:localhost:12345/my.host?key=test.key");
            }
        };
    }
}
