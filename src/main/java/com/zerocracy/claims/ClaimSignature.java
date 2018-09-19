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
package com.zerocracy.claims;

import com.jcabi.xml.XML;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import org.cactoos.Text;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Sorted;

/**
 * Signature of claim.
 * <p>
 * Computed as SHA-256 of claim's type, author, token and params, then
 * converted to base64 string.
 *
 * @since 1.0
 */
public final class ClaimSignature implements Text {

    /**
     * Claim.
     */
    private final XML xml;

    /**
     * Ctor.
     *
     * @param claim Claim
     */
    public ClaimSignature(final XML claim) {
        this.xml = claim;
    }

    @Override
    public String asString() {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException err) {
            throw new IllegalStateException("SHA-256 algorithm required", err);
        }
        final Charset charset = StandardCharsets.UTF_8;
        final ClaimIn claim = new ClaimIn(this.xml);
        if (claim.isUnique()) {
            digest.update(claim.unique().getBytes(charset));
        } else {
            digest.update(claim.type().getBytes(charset));
            if (claim.hasToken()) {
                digest.update(claim.token().getBytes(charset));
            }
            if (claim.hasAuthor()) {
                digest.update(claim.author().getBytes(charset));
            }
            final Iterable<String> params = new Mapped<>(
                entry -> String.format(
                    "%s=%s", entry.getKey(), entry.getValue()
                ),
                new Sorted<>(
                    Comparator.comparing(Map.Entry::getKey),
                    claim.params().entrySet()
                )
            );
            for (final String item : params) {
                digest.update(item.getBytes(charset));
            }
        }
        return Base64.getEncoder().encodeToString(digest.digest());
    }
}
