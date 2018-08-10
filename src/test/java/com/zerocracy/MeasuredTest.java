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
package com.zerocracy;

import com.mongodb.MongoClient;
import com.zerocracy.entry.ExtMongo;
import com.zerocracy.farm.fake.FkStakeholder;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

/**
 * Tests for {@link Measured} class.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class MeasuredTest {

    @Test(expected = IllegalStateException.class)
    public void collectTimeTakenToExceute() throws IOException {
        final Farm farm = new PropsFarm();
        final MongoClient client = new ExtMongo(farm).value();
        final long previous = client.getDatabase(
            Measured.COLLECTION
        ).getCollection(Measured.TIME_TAKEN).countDocuments();
        new Measured(new FkStakeholder()).process(new Pmo(farm), null);
        MatcherAssert.assertThat(
            "time_taken metric not saved",
            client.getDatabase(Measured.COLLECTION).getCollection(
                Measured.TIME_TAKEN
            ).countDocuments(),
            new IsEqual<>(previous)
        );
    }
}
