package com.sam.ni.mlib;

import com.sam.moca.server.MocaServerMain;
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
        System.setProperty("com.sam.moca.config","D:\\MFC\\mlib\\src\\main\\resource\\82.registry");
        System.setProperty("LESDIR","D:\\MFC\\mlib");
        String lesdir = System.getenv("LESDIR");
        System.out.println(lesdir);
        String args[] = null;
        args = new String[3];
        args[0] = "-R";
        args[1] = "-t*";
        args[2] ="-TJ";
        MocaServerMain.main(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
