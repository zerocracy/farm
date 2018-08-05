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
package com.zerocracy.farm.strict;

import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

/**
 * PMO project.
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class StrictProject implements Project {

    /**
     * Files that are allowed in PMO.
     */
    private static final Pattern PMO = Pattern.compile(
        String.join(
            "|",
            "(_[a-z]+\\.xml)",
            "(test\\.txt)",
            "(claims\\.xml)",
            "(roles\\.xml)",
            "(catalog\\.xml)",
            "(bots\\.xml)",
            "(people\\.xml)",
            "(rfps\\.xml)",
            "(vacancies\\.xml)",
            "(debts\\.xml)",
            "(awards/[a-zA-Z0-9-]+\\.xml)",
            "(agenda/[a-zA-Z0-9-]+\\.xml)",
            "(projects/[a-zA-Z0-9-]+\\.xml)",
            "(blanks/[a-zA-Z0-9-]+\\.xml)",
            "(speed/[a-zA-Z0-9-]+\\.xml)",
            "(options/[a-zA-Z0-9-]+\\.xml)",
            "(verbosity/[a-zA-Z0-9-]+\\.xml)",
            "(resumes\\.xml)",
            "(test)",
            "(test/heap)",
            "(test/bucket)"
        )
    );

    /**
     * Files that are allowed in a regular project.
     */
    private static final Pattern PROJECT = Pattern.compile(
        String.join(
            "|",
            "(_[a-z]+\\.xml)",
            "(guts\\.xml)",
            "(claims\\.xml)",
            "(roles\\.xml)",
            "(reviews\\.xml)",
            "(ledger\\.xml)",
            "(rates\\.xml)",
            "(vesting\\.xml)",
            "(equity\\.xml)",
            "(elections\\.xml)",
            "(estimates\\.xml)",
            "(budget\\.xml)",
            "(wbs\\.xml)",
            "(orders\\.xml)",
            "(bans\\.xml)",
            "(test\\.txt)",
            "(precedences\\.xml)",
            "(milestones\\.xml)",
            "(impediments\\.xml)",
            "(boosts\\.xml)",
            "(reminders\\.xml)",
            "(releases\\.xml)"
        )
    );

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Ctor.
     * @param pkt Project
     */
    StrictProject(final Project pkt) {
        this.origin = pkt;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final boolean pmo = "PMO".equals(this.origin.pid());
        if (pmo && !StrictProject.PMO.matcher(file).matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "File \"%s\" is not accessible in \"%s\"",
                    file, this.origin.pid()
                )
            );
        }
        if (!pmo && !StrictProject.PROJECT.matcher(file).matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "File \"%s\" is not allowed in project \"%s\"",
                    file, this.origin.pid()
                )
            );
        }
        return this.origin.acq(file);
    }

}
