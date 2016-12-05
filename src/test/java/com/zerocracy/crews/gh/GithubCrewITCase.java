/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 jare.io
 *
 * Permission is hereby granted, free of charge,  to any person obtaining
 * a copy  of  this  software  and  associated  documentation files  (the
 * "Software"),  to deal in the Software  without restriction,  including
 * without limitation the rights to use,  copy,  modify,  merge, publish,
 * distribute,  sublicense,  and/or sell  copies of the Software,  and to
 * permit persons to whom the Software is furnished to do so,  subject to
 * the  following  conditions:   the  above  copyright  notice  and  this
 * permission notice  shall  be  included  in  all copies or  substantial
 * portions of the Software.  The software is provided  "as is",  without
 * warranty of any kind, express or implied, including but not limited to
 * the warranties  of merchantability,  fitness for  a particular purpose
 * and non-infringement.  In  no  event shall  the  authors  or copyright
 * holders be liable for any claim,  damages or other liability,  whether
 * in an action of contract,  tort or otherwise,  arising from, out of or
 * in connection with the software or  the  use  or other dealings in the
 * software.
 */
package com.zerocracy.crews.gh;

import com.jcabi.github.RtGithub;
import com.zerocracy.farm.SimpleFarm;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration case for {@link GithubCrew}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class GithubCrewITCase {

    /**
     * Fetches notifications from Github.
     * @throws Exception If some problem inside
     */
    @Test
    @Ignore
    public void unzipsAndLogs() throws Exception {
        new GithubCrew(
            new RtGithub("0crat", "R83mSikl")
        ).deploy(new SimpleFarm());
    }

}
