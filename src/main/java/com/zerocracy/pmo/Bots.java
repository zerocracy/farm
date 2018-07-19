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

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.json.JsonObject;
import org.cactoos.iterable.Mapped;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Slack bots.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Bots {

    /**
     * Project.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public Bots(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public Bots(final Pmo pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Bots bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/bots");
        }
        return this;
    }

    /**
     * Register new bot.
     * @param json JSON from Slack OAuth
     * @return Team name
     * @throws IOException If fails
     */
    public String register(final JsonObject json)
        throws IOException {
        final JsonObject bot = json.getJsonObject("bot");
        if (bot == null) {
            throw new IllegalArgumentException(
                new Par("Can't find bot ID in: %s").say(json)
            );
        }
        final String bid = bot.getString("bot_user_id");
        final String team = json.getString("team_id");
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/bots[not(bot[@id='%s'])]",
                            bid
                        )
                    )
                    .add("bot")
                    .attr("id", bid)
                    .xpath(
                        String.format(
                            "/bots/bot[@id='%s']",
                            bid
                        )
                    )
                    .strict(1)
                    .addIf("access_token")
                    .set(json.getString("access_token")).up()
                    .addIf("team_name")
                    .set(json.getString("team_name")).up()
                    .addIf("team_id")
                    .set(team).up()
                    .addIf("bot_access_token")
                    .set(bot.getString("bot_access_token")).up()
                    .addIf("created")
                    .set(new DateAsText().asString())
            );
        }
        return team;
    }

    /**
     * Get team name by ID.
     * @param bot Bot ID
     * @return Team name
     * @throws IOException If fails
     */
    public String name(final String bot) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/bots/bot[@id='%s']/team_name/text()", bot
                )
            ).get(0);
        }
    }

    /**
     * Get all bot access tokens.
     * @return Tokens
     * @throws IOException If fails
     */
    public Iterable<Map.Entry<String, String>> tokens() throws IOException {
        try (final Item item = this.item()) {
            return new Mapped<>(
                node -> new HashMap.SimpleEntry<>(
                    node.xpath("@id").get(0),
                    node.xpath("bot_access_token/text()").get(0)
                ),
                new Xocument(item.path()).nodes(
                    "/bots/bot"
                )
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("bots.xml");
    }

}
