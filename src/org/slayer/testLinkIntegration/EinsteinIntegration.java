package org.slayer.testLinkIntegration;


import com.google.gson.*;
import ods.einstein.api.EinsteinClient;
import ods.einstein.api.auth.AuthImpl;

import org.jsoup.Jsoup;
import org.slayer.testLinkIntegration.UI.FILTER;

import java.util.*;
import java.util.stream.Collectors;

public class EinsteinIntegration extends Source {

    EinsteinClient client;
    Gson gson = new GsonBuilder().create();
    Project[] projects = new Project[0];
    HashMap<String, String> cache = new HashMap<>();
    private String user;
    private String pass;

    private EinsteinClient getClient()
    {
        boolean reload = false;
        if ( user == null || !user.equals( SettingsStorage.loadData("user") )) {
             user = SettingsStorage.loadData("user");
             reload = true;
        }

        if ( pass == null || pass.equals( SettingsStorage.loadData("pass") )) {
             pass = SettingsStorage.loadData("pass");
             reload = true;
        }

        if ( client == null || reload )

             client = EinsteinClient.setup()
                .host( SettingsStorage.loadData("dashboard.url") )
                .authorizeBy( new AuthImpl() )
                .user( user )
                .password( pass ).build();

        return client;
    }

    @Override
    public List<TestEntity> getTestList(String pattern, boolean checkBoxState) {

        return new ArrayList<>();
    }

    @Override
    public List<StepEntity> getStepsList(String dataBaseId) {



        List<StepEntity> steps = new ArrayList<>();

        JsonArray rawSteps = (JsonArray) getTestAttribute( dataBaseId, "children");
        for ( JsonElement el : rawSteps )
        {

            JsonObject obj = el.getAsJsonObject();
            StepEntity stepEntity = new StepEntity( Jsoup.parse( obj.get("name").getAsString() ).text() );

            JsonElement jsonResult = obj.get("expectedResult");
            String expectedResult = jsonResult.isJsonNull() ? "" : Jsoup.parse( jsonResult.getAsString() ).text();
            stepEntity.addVerify( expectedResult );
            stepEntity.order = obj.get("order").getAsInt();
            steps.add( stepEntity );
        }

        Collections.sort( steps, (it, it2) -> it.order - it2.order );
        return steps;
    }

    private Object getTestAttribute( String dataBaseId, String attr )
    {
        String rawData;
        if ( cache.containsKey( dataBaseId ) )
            rawData = cache.get( dataBaseId );
        else {
            rawData = getClient().getTestCaseInfo(dataBaseId);
            cache.put( dataBaseId, rawData );
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse( rawData );
        JsonElement object = element.getAsJsonObject().getAsJsonObject("item").get( attr );
        if ( object.isJsonArray() )
             return object.getAsJsonArray();

        return Jsoup.parse( object.getAsString() ).text();
    }

    @Override
    public String getTestPreconditions(String dataBaseId) {

       return getTestAttribute( dataBaseId, "preconditions").toString();
    }

    @Override
    public String getTestTitle(String dataBaseId) {
        return getTestAttribute( dataBaseId, "name").toString();
    }

    @Override
    public String getTestLabels(String dataBaseId) {
        return "";
    }

    @Override
    public List<TestFolder> getAllTestsHierarchy( String pattern, FILTER filter) {

        Project[] projects = getProjects();
        String id = Arrays.stream( projects ).filter( it -> it.prefix.equals( pattern ))
                .findAny().orElse( projects[0] ).id;
        String rawData = getClient().getSuites( Integer.parseInt( id ) );

        List<TestFolder> folders = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse( rawData );
        JsonObject object = element.getAsJsonObject();
        JsonArray folderItems = object.getAsJsonArray("children");

        for ( JsonElement el : folderItems )
              extractItems( el.getAsJsonObject(), folders );

        return folders;
    }

    private void extractItems( JsonObject object, List<TestFolder> list )
    {
        extractItems( object, list, object.get( "itemId" ).getAsString() );
    }

    private void extractItems( JsonObject object, List<TestFolder> list, String folderID )
    {
        JsonElement parentId = object.get("parentId");
        String pid;
        if ( parentId == null )
             if ( object.get("itemId").getAsString().equals( folderID  ) )
                  pid = "0";
             else
                  pid = folderID;
        else
             pid = parentId.getAsString();

        TestFolder folder = new TestFolder( object.get("itemId").getAsString(), object.get("name").getAsString(), pid );
        folder.setIsFolder( object.get("type").getAsString().equalsIgnoreCase("suite") );
        folder.setIsTest( object.get("type").getAsString().equalsIgnoreCase("case") );
        JsonElement externalId = object.get("externalId");
        if ( externalId != null )
             folder.setTestLinkID( externalId.getAsString() );

        list.add( folder );

        JsonArray children = object.getAsJsonArray("children");
        if ( children != null ) {
             for ( JsonElement element : children )
                   extractItems( element.getAsJsonObject(), list, folder.getId() );
        }
    }

    @Override
    public List<String> getAllProjectNames() {

        return Arrays.stream( getProjects() ).map( it -> it.prefix ).collect(Collectors.toList());
    }

    @Override
    public List<String> getTestIdsForUpdate() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getDisabledTestIds() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getDeprecatedTestIds() {
        return new ArrayList<>();
    }

    private class Project
    {
        public String prefix;
        public String id;
    }

    private Project[] getProjects()
    {
        if ( projects.length == 0 )
             projects = gson.fromJson( getClient().getProjects(), Project[].class );

        return projects;
    }

    public void clearCache()
    {
        cache.clear();
    }
}
