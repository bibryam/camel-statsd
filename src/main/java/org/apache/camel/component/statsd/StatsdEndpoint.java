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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
/**
 * Represents a Statsd endpoint.
 */
public class StatsdEndpoint extends DefaultEndpoint {
    private StatsDClient statsd;
    private URI url;
    private String operation;
    private String key;
    private Integer value;

    public StatsdEndpoint(String uri, StatsdComponent component) throws URISyntaxException {
        super(uri, component);
    }

    public Producer createProducer() throws Exception {
        return new StatsdProducer(this, statsd);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Consumer not supported for Statsd endpoint");
    }

    public boolean isSingleton() {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        url = new URI(getEndpointUri());
        String path = url.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }
        statsd = new NonBlockingStatsDClient(path, url.getHost(), url.getPort());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        statsd.stop();
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
