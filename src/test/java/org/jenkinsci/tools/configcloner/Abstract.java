package org.jenkinsci.tools.configcloner;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.Main;
import org.jenkinsci.tools.configcloner.handler.Handler;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;

public class Abstract extends PowerMockTestCase {

    private ByteArrayOutputStream bout = new ByteArrayOutputStream();
    private ByteArrayOutputStream berr = new ByteArrayOutputStream();
    private PrintStream out = new PrintStream(bout);
    private PrintStream err = new PrintStream(berr);
    protected Main main;
    protected CommandResponse inResponse;

    @BeforeMethod
    public void setUp() {

        bout = new ByteArrayOutputStream();
        berr = new ByteArrayOutputStream();
        out = new PrintStream(bout);
        err = new PrintStream(berr);

        inResponse = new CommandResponse(out, err);
        main = new Main(inResponse);
    }

    protected Handler dispatch(final String... args) {

        return main.getHandler(args);
    }

    protected String stderr() {

        try {

            return berr.toString("UTF-8");
        } catch (final UnsupportedEncodingException e) {

            e.printStackTrace();
            fail(e.getMessage());
        }

        return null;
    }

    protected String stdout() {

        try {

            return bout.toString("UTF-8");
        } catch (final UnsupportedEncodingException e) {

            e.printStackTrace();
            fail(e.getMessage());
        }

        return null;
    }

}