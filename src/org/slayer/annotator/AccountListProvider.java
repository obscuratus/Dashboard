package org.slayer.annotator;

import org.reficio.ws.client.core.SoapClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slayer on 31.10.14.
 */
public class AccountListProvider {

    static List<String> cachedAccountList = new ArrayList<String>();

    static boolean connectionIsUp = true;

    public static List<String> getAccountsList( ) {

        String url = getAgsHost();
        if ( cachedAccountList == null || cachedAccountList.isEmpty() ) {

            if (!url.startsWith("http://"))
                url = "http://" + url;


            if ( connectionIsUp ) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
                    conn.setConnectTimeout(2000);
                    connectionIsUp = conn.getResponseCode() == 200;


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!url.endsWith("/ags/ws?wsdl"))
                url += "/ags/ws?wsdl";


            if ( connectionIsUp ) {
                SoapClient client = SoapClient.builder().endpointUri(url).build();
                String response = client.post("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ags=\"http://ringcentral.com/ags\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <ags:getScenarioList/>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>");

                response = response.substring(response.indexOf("<scenarioList>"));
                response = response.replace("<scenarioList>", "");
                response = response.substring(0, response.indexOf("</scenarioList>"));
                response = response.replace("<scenario>", "");
                cachedAccountList = Arrays.asList(response.split("</scenario>"));
            }
        }

        return cachedAccountList;

    }

    private static String getAgsHost() {
/*
        String agsHost = "";

        PsiJavaFile javaFile = ((PsiJavaFile) element.getContainingFile());
        PsiClass superClass = javaFile.getClasses()[0].getSuperClass();

        PsiJavaFile superClassFile = (PsiJavaFile) superClass.getContainingFile();
        PsiPackage pkg = JavaPsiFacade.getInstance(element.getProject()).findPackage(superClassFile.getPackageName());
        PsiPackage parentPackage = pkg.getParentPackage().getParentPackage();
        PsiPackage envPackage = JavaPsiFacade.getInstance(element.getProject()).findPackage(parentPackage.getName() + ".data.env");
        Collection<VirtualFile> envFiles = FileBasedIndex.getInstance().getContainingFiles(FilenameIndex.NAME, "MOD_ENV.csv", PackageScope.allScope(element.getProject()));

        String envFileUrl = "";
        for (VirtualFile envFile : envFiles) {
            if (envFile.getPath().contains(envPackage.getQualifiedName().replace(".", "/")))
                envFileUrl = envFile.getUrl();


            CSVParser parser = CSVParser.parse(new URL(envFileUrl), Charset.defaultCharset(), CSVFormat.DEFAULT);

            for (CSVRecord record : parser)
                if (record.get(0).equals("ags_host"))
                    agsHost = record.get(1);


        }*/

        return "http://mod01-t01-jws03.lab.nordigy.ru/";
    }

    public static List<String> getAccountStartedWith( String pattern )
    {
        List<String> result = new ArrayList<String>();
        if ( cachedAccountList == null )
             getAccountsList();

        for ( String account : cachedAccountList )
        {
            if ( account.startsWith( pattern ) )
                 result.add( account );
        }

        return result;
    }
}
