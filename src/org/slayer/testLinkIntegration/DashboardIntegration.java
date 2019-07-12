package org.slayer.testLinkIntegration;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.sun.tools.corba.se.idl.ExceptionEntry;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.slayer.testLinkIntegration.UI.FILTER;
import org.slayer.testLinkIntegration.UI.SettingsDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by slayer on 24.09.14.
 */
public class DashboardIntegration extends Source {


    private String sessionID = "";
    private static final String filterCasesUrl = "/tree/TestCases/open_node?method=openNode&data%5Bid%5D=1&data%5Bdocid%5D=1&data%5Btype%5D=root&data%5Bproject%5D=_project_id_&data%5Bquick_search%5D=&data%5Bquick_search_entity%5D=testcase_name&data%5Bshow_deleted%5D=&data%5Bfilt%5D%5Bchildren%5D%5B0%5D%5Bid%5D=1459345677232_923&data%5Bfilt%5D%5Bchildren%5D%5B0%5D%5Bsubject%5D%5Bname%5D=testcase&data%5Bfilt%5D%5Bchildren%5D%5B0%5D%5Bsubject%5D%5Bproperty%5D=label&data%5Bfilt%5D%5Bchildren%5D%5B0%5D%5Bsubject%5D%5Bvalue%5D%5B%5D=_STATUS_&data%5Bfilt%5D%5Bchildren%5D%5B0%5D%5Bexpression%5D=contains&data%5Bfilt%5D%5Bchildren%5D%5B0%5D%5Bparent%5D=def-cond-group-1&data%5Bfilt%5D%5Bid%5D=def-cond-group-1&data%5Bfilt%5D%5Boperation%5D=intersect&data%5Bfilt%5D%5Btable%5D=tc_test";
    private static int projectID;
    private static JSONArray actualTestLabels;
    private String jsonString = "[{\"id\":\"1\",\"name\":\"General Web\",\"suffix\":\"GW\"},{\"id\":\"2\",\"name\":\"Express Setup\",\"suffix\":\"ES\"},{\"id\":\"3\",\"name\":\"Import\",\"suffix\":\"IP\"},{\"id\":\"4\",\"name\":\"RC-UBP\",\"suffix\":\"UBP\"},{\"id\":\"5\",\"name\":\"Modano\",\"suffix\":\"CHE\"},{\"id\":\"6\",\"name\":\"ROI Calculator 1.0\",\"suffix\":\"CNVR\"},{\"id\":\"7\",\"name\":\"Performance Measurement\",\"suffix\":\"PM\"},{\"id\":\"8\",\"name\":\"Telephony (black box)\",\"suffix\":\"TBB\"},{\"id\":\"9\",\"name\":\"RC UBP ALL\",\"suffix\":\"UBP\"},{\"id\":\"10\",\"name\":\"Android_SMS\",\"suffix\":\"AASMS\"}]";
    //    private final String dashboardUrl = "http://192.168.66.27";
    private final String getTestCaseUrl = "/testcases/ajax/get?id=%s&method=get&object=Testcase";
    private final String getProjectIdsUrl = "/get_com_projects";
    private String dashboardUrlBackUp = "";
    private JSONArray actualLabels = null;
    private JSONArray projectIDs = null;

    List<String> projects = new ArrayList<>();

    public List<String> getAllProjectNames() {

        if ( !projects.isEmpty() )
              return projects;

        try {

            JSONArray jsonArray = sendGet(SettingsStorage.loadData("dashboard.url") + getProjectIdsUrl, "");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject project = jsonArray.getJSONObject(i);
                projects.add(project.getString("suffix"));
            }
        } catch (JSONException ex) {
            return Collections.emptyList();
        }

        return projects;
    }

    public List<TestEntity> getTestList(String pattern, boolean checkBoxState) {
        return getTestList(pattern, checkBoxState, getRawList(pattern));
    }

    private List<TestEntity> getTestList(String pattern, boolean checkBoxState, JSONArray jsonArray) {

        List<TestEntity> result = new ArrayList<>();

        if (pattern.matches("\\w+-\\d+")) {

            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String id = object.getString("text");
                        String desc = object.getString("text");
                        String dbID = object.getString("id");
                        String icon = object.getString("icon");
                        String parent = object.getString("parent");


                        boolean testFilter = desc.startsWith("<span class=project>");

                        if (checkBoxState)
                            testFilter &= !icon.contains("automated");

                        if (testFilter) {
                            desc = desc.substring(desc.indexOf("</span>") + 7);
                            id = id.substring(id.indexOf(">") + 1, id.indexOf("</")).replace("&ndash;", "-");
                            TestEntity testEntity = new TestEntity(id, desc);
                            testEntity.setDataBaseId(dbID);
                            testEntity.setParentID(parent);
                            testEntity.setIcon(icon);
                            result.add(testEntity);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!result.isEmpty())
            System.out.println();

        return result;
    }

    @Override
    public List<StepEntity> getStepsList(String dataBaseId) {
        dataBaseId = dataBaseId.replace("doc_", "");
        String url = SettingsStorage.loadData("dashboard.url") + "/testcases/ajax/get?id=" + dataBaseId + "&method=get&object=Testcase";
        JSONArray jsonSteps = sendGet(url, "steps");

        List<StepEntity> steps = new ArrayList<StepEntity>();
        for (int i = 0; i < jsonSteps.length(); i++) {
            try {
                JSONObject jsonStep = jsonSteps.getJSONObject(i);
                String description = jsonStep.getString("description");
                description = description.replaceAll("\\<.*?>", "").
                        replaceAll("\n", ".").
                        replace("&#92;", "\\").
                        replaceAll("&quot;", "\\\\\"").
                        replaceAll("&amp;", "&").
                        replaceAll("&nbsp;", " ").
                        replaceAll("&lsquo;", "\\\\\'").
                        replaceAll("&rsquo;", "\\\\\'").
                        replaceAll("\'", "\\\\\'").
                        replace("&ldquo;", "\"").
                        replace("&rdquo;", "\"");

                StepEntity step = new StepEntity(description);

                String verifyStep = Jsoup.parse(jsonStep.getString("expected")).text().
                        replace("\\", "\\\\").
                        replace("\"", "\\\"").
                        replaceAll("\\<.*?>", "").
                        replace("&#92;", "\\").
                        replaceAll("&.*?;", "").
                        replaceAll("&quot;", "\\\\\"").
                        replaceAll("&amp;", "&").
                        replaceAll("&nbsp;", " ").
                        replaceAll("&lsquo;", "\\\\\'").
                        replaceAll("&rsquo;", "\\\\\'").
                        replaceAll("\'", "\\\\\'");

                String[] verifySteps = verifyStep.split("\n");

                for (String verify : verifySteps)
                    step.addVerify(verify);

                steps.add(step);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return steps;
    }

    private JSONArray sendGet(String rawUrl, String array) {


        StringBuilder response = new StringBuilder();
        JSONArray resultedArray = null;
        try {

            URL url = new URL(rawUrl + getSessionId(rawUrl.endsWith(getProjectIdsUrl)));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout( 10000 );
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }


        } catch (Exception e )
        {
            resolveConnectionError( rawUrl, e );
            String dashboardUrl = SettingsStorage.loadData("dashboard.url");
            if ( !rawUrl.startsWith( dashboardUrl ) && !rawUrl.startsWith("http://"))
                  rawUrl = dashboardUrl + rawUrl;

            return sendGet( rawUrl.replaceAll("http://\\d+\\.\\d+\\.\\d+\\.\\d+", dashboardUrl ), array );
        }

        try {
            String resp = response.toString();
            if (array.isEmpty()) {


                if (!resp.startsWith("["))
                    resp = "[" + resp + "]";

                resultedArray = new JSONArray(resp);
            } else
                resultedArray = new JSONObject(resp).getJSONArray(array);
        } catch (Exception ex) { /*ignore*/ }

        return resultedArray;
    }

    private String sendGetString(String rawUrl, String name) {


        StringBuilder response = new StringBuilder();
        String result = null;
        try {

            URL url = new URL(rawUrl + getSessionId());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            result = new JSONObject(response.toString()).getString(name);
        } catch (Exception ex) { /*ignore*/ }

        return result;
    }


    private JSONArray getRawList(String pattern, int projectID) {
        pattern = pattern.substring(pattern.indexOf("-") + 1);
        String rawUrl = SettingsStorage.loadData("dashboard.url") + "/tree/TestCases/open_node?method=openNode&id=1&docid=1&type=root&project=" + projectID + "&hideemptynode=1";
        return sendGet(rawUrl, "");
    }

    private int getProjectID(String projectName) {

        try {

            if ( projectIDs == null )
                 projectIDs = sendGet(SettingsStorage.loadData("dashboard.url") + getProjectIdsUrl, "");

            for (int i = 0; i < projectIDs.length(); i++) {
                JSONObject project = projectIDs.getJSONObject(i);
                String suffix = project.getString("suffix");
                if (suffix.equals(projectName))
                    return project.getInt("id");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private String getSessionId() {
        return getSessionId(false);
    }


    private void logout()
    {
        try {
            URL url = new URL(SettingsStorage.loadData("dashboard.url") + "/auth/logout");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.disconnect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    private String getSessionId(boolean forProjects) {

//        if ( sessionID == null || sessionID.isEmpty() ) {

            logout();
            StringBuilder response = new StringBuilder();

            String user = SettingsStorage.loadData("user");
            String pass = SettingsStorage.loadData("pass");
            String sessionUrl = SettingsStorage.loadData("dashboard.url") + "/login?get_session_id=true&user=" + user + "&pass=" + pass;

            if (user.isEmpty() || pass.isEmpty())
                throw new EmptyCredentials("Check credentials, user or pass is empty! User folder: " + System.getProperty("user.home"));

            try {

                URL url = new URL(sessionUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                int responceCode = con.getResponseCode();
                if (responceCode == HttpURLConnection.HTTP_OK) {
                    String inputLine;
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    String resp = response.toString();
                    if (resp.contains("session_id"))
                        resp = resp.substring(resp.indexOf("{\"session_id"));
                    else
                        return "";

                    sessionID = new JSONObject(resp).getString("session_id");

                    con.disconnect();
                } else if (responceCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new WrongCredentialsException("Cannot login to dashboard. Check credentials " + user + ":" + pass);
                } else {
                    resolveConnectionError(sessionUrl, null);
                    return getSessionId(forProjects);
                }

            } catch (Exception e) {
                resolveConnectionError(sessionUrl, e);
                return getSessionId(forProjects);
            }

            if (!dashboardUrlBackUp.equals(SettingsStorage.loadData("dashboard.url"))) {

                dashboardUrlBackUp = SettingsStorage.loadData("dashboard.url");
            }

//        }
        if (forProjects)
            return "?session_id=" + sessionID;
        else
            return "&session_id=" + sessionID;
    }

    public String getTestPreconditions(String dataBaseId) {

        dataBaseId = dataBaseId.replace("doc_", "");
        String url = SettingsStorage.loadData("dashboard.url") + String.format(getTestCaseUrl, dataBaseId);
        String preconditions = sendGetString(url, "preconditions");
        String[] preconditionsArray = preconditions.split("\n");
        StringBuilder resultedPreconditions = new StringBuilder();

        for (String p : preconditionsArray)
            resultedPreconditions.append(Jsoup.parse(p).text()).append("\n");

        return resultedPreconditions.toString();

    }

    public String getTestTitle(String dataBaseId) {

        dataBaseId = dataBaseId.replace("doc_", "");
        String url = SettingsStorage.loadData("dashboard.url") + String.format(getTestCaseUrl, dataBaseId);
        String preconditions = sendGetString(url, "title");
        return Jsoup.parse(preconditions).text();

    }

    public String getTestLabels(String dataBaseId) {
        String resultedLabels = "";

        try {

            dataBaseId = dataBaseId.replace("doc_", "");
            String url = SettingsStorage.loadData("dashboard.url") + String.format(getTestCaseUrl, dataBaseId);
            JSONArray labels = sendGet(url, "label");

            for (int i = 0; i < labels.length(); i++)
                for (int j = 0; j < actualLabels.length(); j++)
                    if (actualLabels.getJSONObject(j).get("id").equals(labels.getString(i)))
                        resultedLabels += "\n" + actualLabels.getJSONObject(j).getString("label");


        } catch (Exception ex) { /* ignore */ }
        return resultedLabels;
    }


    public List<TestFolder> getAllTestsHierarchy(String pattern, FILTER filter) {
        JSONArray rawList = getRawList(pattern + "-0");
        List<TestFolder> folders = new ArrayList<>();

        try {
            if (rawList != null) {
                for (int i = 0; i < rawList.length(); i++) {

                    JSONObject object = rawList.getJSONObject(i);
                    String desc = object.getString("text");
                    String dbID = object.getString("id");
                    String icon = object.getString("icon");
                    String parent = object.getString("parent");

                    TestFolder folder = new TestFolder(dbID, desc, parent);

                    folder.setIsFolder(icon.contains("icon folder"));
                    folder.setIsTest(!folder.isFolder());

                    if (folder.isTest() && desc.startsWith("<span class=project>")) {
                        folder.setTestLinkID(object.getString("text"));
                        folder.setIcon(icon);
//                             getTestDetails( folder.getId() );
                    }


                    if (filter == FILTER.Automated && (icon.contains(filter.toString().toLowerCase()) || icon.contains("folder"))) {
                        folders.add(folder);
                    } else if (filter == FILTER.Manual && (!icon.contains(FILTER.Automated.toString().toLowerCase()) || icon.contains("folder"))) {
                        folders.add(folder);
                    } else if (filter == FILTER.All) {
                        folders.add(folder);
                    }

                }
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }

        return folders;
    }

    private JSONArray getRawList(String pattern) {
        String projectPrefix = pattern.substring(0, pattern.indexOf("-"));

        projectID = getProjectID(projectPrefix);
        actualLabels = sendGet(SettingsStorage.loadData("dashboard.url") + "/testcases/ajax/getLabel?projectId%5B%5D=" + projectID + "&method=getLabel", "");

        if ( actualLabels == null )
             actualLabels = sendGet(SettingsStorage.loadData("dashboard.url") + "/testcases/ajax/getLabel?projectId%5B%5D=" + projectID + "&method=getLabel", "");

        return getRawList(pattern, projectID);
    }

    private void resolveConnectionError()
    {
        resolveConnectionError( SettingsStorage.loadData("dashboard.url"), null );
    }

    private void resolveConnectionError( String url, Exception e )
    {
        Messages.showErrorDialog( project, "Cannot connect to " + url + ( e != null ? e.getMessage() : ""), "Connection Error" );
            SettingsDialog n = new SettingsDialog(project, true );
            n.show();
            if (n.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                n.saveData();
            }
    }

    public List<String> getTestIdsForUpdate()
    {
        return filterTests( TEST_STATUS.NEED_UPDATE );
    }

    public List<String> getDisabledTestIds()
    {
        return filterTests(TEST_STATUS.DISABLED);
    }

    public List<String> getDeprecatedTestIds()
    {
        return filterTests(TEST_STATUS.DEPRECATED);
    }

    public List<String> filterTests( TEST_STATUS status )
    {
        projectID = getProjectID( SettingsStorage.loadData("projectPrefix").replace("-", "") );
        JSONArray list = sendGet(SettingsStorage.loadData("dashboard.url") +
                filterCasesUrl.replace( "_project_id_", String.valueOf(projectID) ).replace( "_STATUS_", status.toString() ), "");

        List<String> result = new ArrayList<>();
        try {
            for (int i = 0; i < list.length(); i++) {
                JSONObject obj = (JSONObject) list.get(i);
                result.add( obj.getString("id") );
            }
        }
        catch (Exception e)
        {
            /***/
        }

        return result;
    }

    public void clearCache()
    {

    }

}
