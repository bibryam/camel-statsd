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

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import static org.hamcrest.Matchers.hasItem;

public class StatsdEventNotifierTest extends StatsdTestSupport {

    @Test
    public void testNagiosEventNotifierOk() throws Exception {

        getMockEndpoint("mock:ok").expectedMessageCount(1);

        template.sendBody("direct:ok", "Hello World");

        assertMockEndpointsSatisfied();

        context.stop();

        assertThat(getReceivedMessages(), hasItem("my.host.success:1|c"));
    }

    @Test
    public void testNagiosEventNotifierError() throws Exception {
        try {
            template.sendBody("direct:fail", "Bye World");
            fail("Should have thrown an exception");
        } catch (Exception e) {
            // ignore
        }

        context.stop();

        Thread.sleep(2000);
        assertThat(getReceivedMessages(), hasItem("my.host.failure:1|c"));
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        StatsdEventNotifier notifier = new StatsdEventNotifier();
        notifier.setHost("localhost");
        notifier.setPort(STATSD_PORT);
        notifier.setPrefix("my.host");

        CamelContext context = super.createCamelContext();
        context.getManagementStrategy().addEventNotifier(notifier);
        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:ok").to("mock:ok");

                from("direct:fail").throwException(new IllegalArgumentException("Damn"));
            }
        };
    }
}
