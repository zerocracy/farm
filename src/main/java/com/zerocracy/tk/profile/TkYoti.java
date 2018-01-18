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
package com.zerocracy.tk.profile;

import com.yoti.api.client.FileKeyPairSource;
import com.yoti.api.client.HumanProfile;
import com.yoti.api.client.ProfileException;
import com.yoti.api.client.YotiClientBuilder;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.takes.Response;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHref;

/**
 * Yoti callback page.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
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
            "%s %s %d-%d-%d",
            profile.getGivenNames(), profile.getFamilyName(),
            profile.getDateOfBirth().getDay(),
            profile.getDateOfBirth().getMonth(),
            profile.getDateOfBirth().getYear()
        );
        final String user = new RqSecureLogin(new Pmo(this.farm), req).value();
        new People(this.farm).bootstrap().details(user, name);
        return new RsForward(
            new RsFlash(
                new Par(
                    "@%s have been successfully identified as %s"
                ).say(user, name)
            ),
            String.format("/u/%s", user)
        );
    }

}
