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
package com.zerocracy.tk.profile;

import com.yoti.api.client.FileKeyPairSource;
import com.yoti.api.client.HumanProfile;
import com.yoti.api.client.ProfileException;
import com.yoti.api.client.YotiClientBuilder;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pmo.People;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHref;

/**
 * Yoti callback page.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkYoti implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkYoti(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Props props = new Props(this.farm);
        final String token = new RqHref.Smart(req).single("token");
        final Path pem = Files.createTempFile("", ".pem");
        new LengthOf(
            new TeeInput(props.get("//yoti/pem").trim(), pem)
        ).intValue();
        final HumanProfile profile;
        try {
            profile = YotiClientBuilder.newInstance()
                .forApplication(props.get("//yoti/sdk_id"))
                .withKeyPair(FileKeyPairSource.fromFile(pem.toFile()))
                .build()
                .getActivityDetails(token)
                .getUserProfile();
        } catch (final ProfileException ex) {
            throw new IOException(ex);
        }
        final String name = String.format(
            "%s %s %d-%d-%d @Yoti",
            profile.getGivenNames(), profile.getFamilyName(),
            profile.getDateOfBirth().getDay(),
            profile.getDateOfBirth().getMonth(),
            profile.getDateOfBirth().getYear()
        );
        final String user = new RqUser(this.farm, req).value();
        new People(this.farm).bootstrap().details(user, name);
        new ClaimOut()
            .type("User identified")
            .param("login", user)
            .param("details", name)
            .param("system", "yoti")
            .postTo(new ClaimsOf(this.farm));
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "We just identified @%s as \"%s\" via Yoti"
            ).say(user, name)
        ).postTo(new ClaimsOf(this.farm));
        return new RsForward(
            new RsParFlash(
                new Par(
                    "@%s have been successfully identified as %s"
                ).say(user, name),
                Level.INFO
            ),
            String.format("/u/%s", user)
        );
    }

}
