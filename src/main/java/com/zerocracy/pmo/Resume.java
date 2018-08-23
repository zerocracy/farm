/*
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
package com.zerocracy.pmo;

import java.time.Instant;

/**
 * Resume.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface Resume {

    /**
     * Time of resume submission.
     * @return Time of resume submission
     */
    Instant submitted();

    /**
     * Login of user which sent the resume.
     * @return Login of user
     */
    String login();

    /**
     * Text of the resume.
     * @return Text of the resume
     */
    String text();

    /**
     * Personality type of the resume sender.
     * @return Personality type as a {@link String}
     */
    String personality();

    /**
     * Stack overflow id of the user.
     * @return Stack overflow id
     */
    long soid();

    /**
     * Telegram login of the user.
     * @return Telegram login
     */
    String telegram();

    /**
     * Fake implementation for {@link Resume}.
     */
    class Fake implements Resume {

        /**
        * Resume submission date time.
        */
        private final Instant time;

        /**
        * Resume user.
        */
        private final String user;

        /**
        * Resume text.
        */
        private final String txt;

        /**
        * Resume personality.
        */
        private final String person;

        /**
        * Stack overflow id.
        */
        private final long id;

        /**
        * Telegram login.
        */
        private final String tele;

        /**
         * Constructor.
         *
         * @param time Time of resume submission
         * @param user Login of resume user
         * @param txt Text of resume
         * @param person Personality of resume
         * @param id Stackoverflow id of resume user
         * @param tele Telegram login of resume user
         * @checkstyle ParameterNumberCheck (10 lines)
         */
        public Fake(final Instant time, final String user,
            final String txt, final String person, final long id,
            final String tele) {
            this.time = time;
            this.user = user;
            this.txt = txt;
            this.person = person;
            this.id = id;
            this.tele = tele;
        }

        @Override
        public Instant submitted() {
            return this.time;
        }

        @Override
        public String login() {
            return this.user;
        }

        @Override
        public String text() {
            return this.txt;
        }

        @Override
        public String personality() {
            return this.person;
        }

        @Override
        public long soid() {
            return this.id;
        }

        @Override
        public String telegram() {
            return this.tele;
        }
    }
}
