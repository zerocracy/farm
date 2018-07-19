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

import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.cactoos.text.JoinedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link Agenda}.
 * @since 1.0
 * @checkstyle AvoidDuplicateLiterals (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle JavadocVariableCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class AgendaTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void addsAndRemovesAgenda() throws Exception {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Agenda agenda = new Agenda(farm, "yegor").bootstrap();
        final String first = "gh:test/test#1";
        agenda.add(project, first, "REV");
        final String second = "gh:test/test#2";
        agenda.add(project, second, "QA");
        agenda.remove(first);
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasItem(second));
    }

    /**
     * Agenda can remove all the orders.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void removesAllOrders() throws Exception {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Agenda agenda = new Agenda(farm, "mihai").bootstrap();
        agenda.add(project, "gh:test2/test#1", "REV");
        agenda.add(project, "gh:test2/test#2", "QA");
        agenda.add(project, "gh:test2/test#3", "DEV");
        // @checkstyle MagicNumber (1 line)
        MatcherAssert.assertThat(agenda.jobs(), Matchers.not(3));
        agenda.removeAll();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.emptyIterable());
    }

    /**
     * Agenda can remove the only order.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void removesSoleOrder() throws Exception {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Agenda agenda = new Agenda(farm, "john").bootstrap();
        agenda.add(project, "gh:test3/test#1", "ARC");
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasSize(1));
        agenda.removeAll();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.emptyIterable());
    }

    /**
     * Agenda can "remove" orders if it's empty.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void removesOrdersFromEmptyAgenda() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Agenda agenda = new Agenda(farm, "jane").bootstrap();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.emptyIterable());
        agenda.removeAll();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.emptyIterable());
    }

    @Test
    public void returnsJobsForProject() throws Exception {
        final Project first = new FkProject("FAKEPRJC1");
        final Project second = new FkProject("FAKEPRJC2");
        final Agenda agenda = new Agenda(new FkFarm(), "g4s8").bootstrap();
        agenda.add(first, "gh:test4/test#1", "DEV");
        agenda.add(second, "gh:test4/test#2", "DEV");
        agenda.add(first, "gh:test4/test#3", "DEV");
        agenda.add(first, "gh:test4/test#4", "DEV");
        MatcherAssert.assertThat(
            agenda.jobs(first),
            Matchers.hasSize(Tv.THREE)
        );
    }

    @Test
    public void addTitleFailsIfNoJob() throws Exception {
        this.exception.expect(SoftException.class);
        this.exception.expectMessage(
            Matchers.allOf(
                Matchers.containsString("is not in the agenda of @user[/z]"),
                Matchers.containsString("can't set title")
            )
        );
        final Path tmp = this.folder.newFolder().toPath();
        final FkProject project = new FkProject(tmp, "AGENDPRJ1");
        final FkFarm farm = new FkFarm(project);
        final Agenda agenda = new Agenda(farm, "user").bootstrap();
        agenda.title("gh:test5/test#5", "Test issue");
    }

    @Test
    public void addTitleChangesXml() throws Exception {
        final Path tmp = this.folder.newFolder().toPath();
        final FkProject project = new FkProject(tmp, "AGENDPRJ2");
        final Agenda agenda = new Agenda(
            new FkFarm(project),
            "user"
        ).bootstrap();
        final String job = "gh:test6/test#6";
        agenda.add(project, job, "DEV");
        final String title = "Title of the GitHub issue";
        agenda.title(job, title);
        MatcherAssert.assertThat(
            new Xocument(tmp.resolve("agenda/user.xml"))
                .xpath("agenda/order/title/text()"),
            Matchers.contains(title)
        );
    }

    @Test
    public void addInspector() throws Exception {
        final Path tmp = this.folder.newFolder().toPath();
        final FkProject project = new FkProject(tmp);
        final String performer = "user42";
        final Agenda agenda = new Agenda(new FkFarm(project), performer)
            .bootstrap();
        final String job = "gh:test/test#666";
        agenda.add(project, job, "DEV");
        final String inspector = "qauser";
        agenda.inspector(job, inspector);
        MatcherAssert.assertThat(
            new Xocument(
                tmp.resolve(String.format("agenda/%s.xml", performer))
            ),
            XhtmlMatchers.hasXPath(
                new JoinedText(
                    "",
                    "/agenda/order",
                    String.format("[@job = '%s']", job),
                    "/inspector",
                    String.format("[text() = '%s']", inspector)
                ).asString()
            )
        );
    }

    @Test
    public void returnsAddedTime() throws Exception {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Instant time = Instant.now();
        final Agenda agenda = new Agenda(
            farm, "yegor", Clock.fixed(time, ZoneOffset.UTC)
        ).bootstrap();
        final String first = "gh:test/test#1";
        agenda.add(project, first, "REV");
        MatcherAssert.assertThat(
            agenda.added(first), Matchers.is(time)
        );
    }
}
