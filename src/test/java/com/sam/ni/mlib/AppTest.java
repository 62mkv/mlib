package com.sam.ni.mlib;

import com.redprairie.moca.server.MocaServerMain;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        try {
        System.out.println("now start test");
        System.setProperty("com.redprairie.moca.config","D:\\MFC\\mlib\\src\\main\\resource\\82.registry");
        System.setProperty("LESDIR","D:\\MFC\\mlib\\src\\main\\resource");
        String lesdir = System.getenv("LESDIR");
        System.out.println(lesdir);
        MocaServerMain.main(null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
