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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.MsgPriority;
import com.zerocracy.db.ExtDataSource;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.zold.ZldCallbacks;
import com.zerocracy.zold.Zold;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHref;
import org.takes.rs.RsText;

/**
 * Zold callback.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkZoldCallback implements Take {

    /**
     * Zents in ZLD.
     * @checkstyle MagicNumberCheck (2 lines)
     */
    private static final BigDecimal ZENTS = BigDecimal.valueOf(1L << 32);

    /**
     * Amount format.
     */
    private static final DecimalFormat FMT = new DecimalFormat();

    static {
        TkZoldCallback.FMT.setMaximumFractionDigits(2);
        TkZoldCallback.FMT.setMinimumFractionDigits(2);
        TkZoldCallback.FMT.setGroupingUsed(false);
    }

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public TkZoldCallback(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final RqHref.Smart href = new RqHref.Smart(req);
        final String cid = href.single("callback");
        final String code = href.single("details");
        final String txn = href.single("id");
        final String prefix = href.single("prefix");
        final String wallet = href.single("source");
        final String amount = href.single("amount");
        final String secret = href.single("token");
        final Project pkt = new ZldCallbacks(
            new ExtDataSource(this.farm).value(), this.farm
        ).take(cid, code, secret, prefix);
        final Cash.S cash = new Cash.S(
            String.format(
                "USD %s",
                TkZoldCallback.FMT.format(
                    new BigDecimal(amount)
                        .divide(TkZoldCallback.ZENTS, RoundingMode.FLOOR)
                        .multiply(new Zold(this.farm).rate())
                        .setScale(2, RoundingMode.FLOOR)
                )
            )
        );
        new ClaimOut()
            .type("Funded by Zold")
            .param("callback", cid)
            .param("txn", txn)
            .param("wallet", wallet)
            .param("amount", cash)
            .param("priority", MsgPriority.HIGH)
            .postTo(new ClaimsOf(this.farm, pkt));
        new ClaimOut()
            .type("Notify PMO")
            .param(
                "message",
                new Par(
                    this.farm,
                    "Project %s was funded for %s (%s) with ZLD",
                    "to invoice with prefix %s",
                    "from wallet %s",
                    "transaction id is %s",
                    "callabck id is %s"
                ).say(pkt.pid(), amount, cash, prefix, wallet, txn, cid)
            ).postTo(new ClaimsOf(this.farm));
        return new RsText("OK");
    }
}
