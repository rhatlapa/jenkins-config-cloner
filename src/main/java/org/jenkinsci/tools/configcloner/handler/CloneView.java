/*
 * The MIT License
 *
 * Copyright (c) 2013 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.tools.configcloner.handler;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;
import org.jenkinsci.tools.configcloner.UrlParser;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone configuration of views in view from <SRC> to <DST>")
public class CloneView extends Handler {

    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> views;

    @Parameter(names = {"--force", "-f"}, description = "Overwrite target view if already exists.")
    private boolean force = false;

    @Parameter(names = {"--recursive", "-r"}, description = "Transfer contained job and views.")
    private boolean recursive = false;

    public CloneView(final ConfigTransfer config) {

        super(config);
    }

    @Override
    public void validate() {

        if (views == null || views.size() < 2) throw new ParameterException(
                "Expecting 2 or more positional arguments"
        );

        // Instantiates all destinations
        destinations();
    }

    /*package*/ ConfigDestination source() {

        return PARSER.destination(views.get(0));
    }

    /*package*/ List<ConfigDestination> destinations() {

        return PARSER.pair(source(), views.subList(1, views.size()));
    }

    @Override
    public CommandResponse run(final CommandResponse response) {

        response.out().println("Fetching " + source());
        final CommandResponse.Accumulator xml = config.execute(
                source(), "", "get-view", source().entity()
        );

        if (!xml.succeeded()) return response.merge(xml);

        for (final ConfigDestination dest: destinations()) {

            response.out().println("Sending " + dest);
            send(dest, response, xml);
        }

        return response;
    }

    private CommandResponse send(
            final ConfigDestination destination,
            final CommandResponse response,
            final CommandResponse.Accumulator xml
    ) {

        final String destJob = destination.entity();

        if (force) {

            final CommandResponse.Accumulator rsp = config.execute(
                    destination, xml.stdout(), "update-view", destJob
            );

            if (rsp.succeeded()) return response.returnCode(0);
        }

        return response.merge(
                config.execute(destination, xml.stdout(), "create-view", destJob)
        );
    }

    private static final UrlParser PARSER = new UrlParser() {

        @Override
        protected ConfigDestination parseDestination(final URL url) {

            final Matcher urlMatcher = Pattern
                    .compile("^(.*?/)view/([^/]+(?:/view/[^/]+)*).*")
                    .matcher(url.toString())
            ;

            if (!urlMatcher.matches()) return new ConfigDestination(url, "");

            final String jenkins = urlMatcher.group(1);
            String entity = urlMatcher.group(2).replaceAll("/view/", "/");

            return new ConfigDestination(jenkins, entity);
        }
    };
}