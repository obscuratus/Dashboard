package org.slayer.testLinkIntegration;

/**
 * Created by slayer on 06.10.14.
 */
public class DashboardConnectionException extends RuntimeException {

    public DashboardConnectionException( String message )
    {
        super( message, new Throwable("Cannot connect to dashboard!"), true, true );
    }

}
