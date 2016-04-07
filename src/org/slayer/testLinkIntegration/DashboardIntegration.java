package org.slayer.testLinkIntegration;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
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

    private static int projectID;
    private static JSONArray actualTestLabels;
    private String jsonString = "[{\"id\":\"1\",\"name\":\"General Web\",\"suffix\":\"GW\"},{\"id\":\"2\",\"name\":\"Express Setup\",\"suffix\":\"ES\"},{\"id\":\"3\",\"name\":\"Import\",\"suffix\":\"IP\"},{\"id\":\"4\",\"name\":\"RC-UBP\",\"suffix\":\"UBP\"},{\"id\":\"5\",\"name\":\"Modano\",\"suffix\":\"CHE\"},{\"id\":\"6\",\"name\":\"ROI Calculator 1.0\",\"suffix\":\"CNVR\"},{\"id\":\"7\",\"name\":\"Performance Measurement\",\"suffix\":\"PM\"},{\"id\":\"8\",\"name\":\"Telephony (black box)\",\"suffix\":\"TBB\"},{\"id\":\"9\",\"name\":\"RC UBP ALL\",\"suffix\":\"UBP\"},{\"id\":\"10\",\"name\":\"Android_SMS\",\"suffix\":\"AASMS\"}]";
    //    private final String dashboardUrl = "http://192.168.66.27";
    private final String getTestCaseUrl = "/testcases/ajax/get?id=%s&method=get&object=Testcase";
    private final String getProjectIdsUrl = "/get_com_projects";
    private String dashboardUrlBackUp = "";

    public List<String> getAllProjectNames() {
        List<String> projects = new ArrayList<>();
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
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }


        } catch (Exception e )
        {
            resolveConnectionError();
            return sendGet( rawUrl.replaceAll("http://\\d+\\.\\d+\\.\\d+\\.\\d+", SettingsStorage.loadData("dashboard.url")), array );
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
        String rawUrl = SettingsStorage.loadData("dashboard.url") + "/tree/TestCases/open_node?method=openNode&data%5Bid%5D=1&data%5Bdocid%5D=1&data%5Btype%5D=root&data%5Bproject%5D=" + projectID + "&data%5Bdel_date%5D=&data%5Bfilter%5D=showFilter%3Dfiltered%26nameFilter%3D%26testname%3D%26testid%3D" + pattern + "%26hideemptynode%3D1";
        return sendGet(rawUrl, "");
    }

    private int getProjectID(String projectName) {

        try {

            JSONArray jsonArray = sendGet(SettingsStorage.loadData("dashboard.url") + getProjectIdsUrl, "");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject project = jsonArray.getJSONObject(i);
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

    private String getSessionId(boolean forProjects) {
        StringBuilder response = new StringBuilder();
        String id = "";
        try {
            String user = SettingsStorage.loadData("user");
            String pass = SettingsStorage.loadData("pass");

            if (user.isEmpty() || pass.isEmpty())
                throw new EmptyCredentials("Check credentials, user or pass is empty! User folder: " + System.getProperty("user.home"));

            String sessionUrl = SettingsStorage.loadData("dashboard.url") + "/ged?user=" + user + "&pass=" + pass;
            URL url = new URL(sessionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responceCode = con.getResponseCode();
            if (responceCode == HttpURLConnection.HTTP_OK) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                id = new JSONObject(response.toString()).getString("session_id");
            } else if (responceCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new WrongCredentialsException("Cannot login to dashboard. Check credentials " + user + ":" + pass);
            } else
            {
                resolveConnectionError();
                return getSessionId( forProjects );
            }

        } catch ( Exception e) {
            resolveConnectionError();
            return getSessionId( forProjects );
        }

        if (!dashboardUrlBackUp.equals(SettingsStorage.loadData("dashboard.url"))) {

            dashboardUrlBackUp = SettingsStorage.loadData("dashboard.url");
        }

        if (forProjects)
            return "?session_id=" + id;
        else
            return "&session_id=" + id;
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
            JSONArray actualLabels = sendGet(SettingsStorage.loadData("dashboard.url") + "/testcases/ajax/getLabel?projectId%5B%5D=" + projectID + "&method=getLabel", "");

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
        SettingsStorage.storeData("projectPrefix", projectPrefix + "-");
        return getRawList(pattern, projectID);
    }

    private void resolveConnectionError()
    {
        Messages.showErrorDialog( project, "Cannot connect to dashboard " + SettingsStorage.loadData("dashboard.url"), "Connection Error" );
            SettingsDialog n = new SettingsDialog(project, true);
            n.show();
            if (n.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                n.saveData();
            }
    }

}
