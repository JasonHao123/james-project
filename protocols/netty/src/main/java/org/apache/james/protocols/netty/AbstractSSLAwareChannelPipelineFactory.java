/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.protocols.netty;


import javax.net.ssl.SSLEngine;

import org.apache.james.protocols.api.Encryption;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.HashedWheelTimer;

/**
 * Abstract base class for {@link ChannelPipeline} implementations which use TLS
 */
public abstract class AbstractSSLAwareChannelPipelineFactory extends AbstractChannelPipelineFactory {

    private Encryption secure;

    public AbstractSSLAwareChannelPipelineFactory(int timeout,
                                                  int maxConnections, int maxConnectsPerIp, ChannelGroup group, ExecutionHandler eHandler,
                                                  ChannelHandlerFactory frameHandlerFactory, HashedWheelTimer hashedWheelTimer) {
        super(timeout, maxConnections, maxConnectsPerIp, group, eHandler, frameHandlerFactory, hashedWheelTimer);
    }

    public AbstractSSLAwareChannelPipelineFactory(int timeout,
            int maxConnections, int maxConnectsPerIp, ChannelGroup group, Encryption secure,
            ExecutionHandler eHandler, ChannelHandlerFactory frameHandlerFactory, HashedWheelTimer hashedWheelTimer) {
        this(timeout, maxConnections, maxConnectsPerIp, group, eHandler, frameHandlerFactory, hashedWheelTimer);

        this.secure = secure;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline =  super.getPipeline();

        if (isSSLSocket()) {
            // We need to set clientMode to false.
            // See https://issues.apache.org/jira/browse/JAMES-1025
            SSLEngine engine = secure.createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addFirst(HandlerConstants.SSL_HANDLER, new SslHandler(engine));
        }
        return pipeline;
    }

    /**
     * Return if the socket is using SSL/TLS
     */
    protected boolean isSSLSocket() {
        return secure != null && secure.getContext() != null && !secure.isStartTLS();
    }
}
