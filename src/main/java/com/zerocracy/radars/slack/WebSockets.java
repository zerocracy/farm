/*
 * Copyright (c) 2016-2019 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.radars.slack;

import com.ullink.slack.simpleslackapi.WebSocketContainerProvider;
import javax.websocket.WebSocketContainer;
import org.cactoos.Scalar;
import org.cactoos.scalar.SolidScalar;
import org.cactoos.scalar.UncheckedScalar;
import org.glassfish.tyrus.client.ClientManager;

/**
 * Slack session containers.
 *
 * @since 1.0
 */
final class WebSockets implements WebSocketContainerProvider {

    /**
     * Single container for all sessions.
     */
    private static final Scalar<WebSocketContainer> CONTAINER =
        new SolidScalar<>(ClientManager::createClient);

    @Override
    public WebSocketContainer getWebSocketContainer() {
        return new UncheckedScalar<>(WebSockets.CONTAINER).value();
    }
}
