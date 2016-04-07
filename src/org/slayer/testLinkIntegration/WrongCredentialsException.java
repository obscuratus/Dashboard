package org.slayer.testLinkIntegration;

/**
 * Created by slayer on 07.10.14.
 */
public class WrongCredentialsException extends RuntimeException {

    public WrongCredentialsException( String message )
    {
        super( message );
    }
}
