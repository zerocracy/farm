/**
 * Copyright (c) 2016-2018 Zerocracy
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
package com.zerocracy.radars.viber;

/**
 * Viber bot.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @since 0.22
 * @todo #939:30min Implement Viber bot. We should be able to send messages via
 *  the Viber REST API. There are other operations but I think that is the only
 *  operation we need (I might be wrong). See:
 *  https://developers.viber.com/docs/api/rest-bot-api/#authentication-token
 *  https://developers.viber.com/docs/api/rest-bot-api/#send-message
 */
final class VbBot {

    /**
     * Send a message to a user.
     * @param id User
     * @param text Message text
     */
    public void sendMessage(final String id, final String text) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
