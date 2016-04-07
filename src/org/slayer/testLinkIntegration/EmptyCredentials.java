package org.slayer.testLinkIntegration;

/**
 * Created by slayer on 06.10.14.
 */
public class EmptyCredentials extends RuntimeException {

    public EmptyCredentials( String message )
    {
        super( message, new Throwable("Wrong User/Pass") );
    }
}
