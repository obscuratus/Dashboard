package org.slayer.testLinkIntegration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Created by slayer on 26.09.14.
 */
public class SettingsStorage {

    private static String settingsFilePath = "/resources";
    private static File settingsFile ;

    public static void storeData( String key, String value )
    {
        try {
        String path = System.getProperty("user.home");
        settingsFile = new File( path + "/settings.ini" );

        Properties properties = new Properties();

        if ( !settingsFile.exists() )
        {
            settingsFile.createNewFile();
        }
        else
        {
            properties.load( new FileInputStream( settingsFile ) );
        }

        properties.put( key, value );
        properties.store( new FileOutputStream( settingsFile ), "" );

        } catch (Exception e) {
            throw new RuntimeException( e + " file: " + settingsFile.getPath() );
        }


    }

    public static String loadData( String key )
    {

        Properties properties = new Properties();

            try {

                if ( settingsFile == null )
                {
                    String path = System.getProperty("user.home");
                    settingsFile = new File( path + "/settings.ini" );
                }

                if ( settingsFile.exists() )
                     properties.load( new FileInputStream( settingsFile ));

            } catch (Exception e) {

                return "";
            }

        return properties.containsKey( key ) ? properties.getProperty( key ) : "";
    }

}
