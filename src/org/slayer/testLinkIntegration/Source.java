package org.slayer.testLinkIntegration;

import com.intellij.openapi.project.Project;
import org.slayer.testLinkIntegration.UI.FILTER;

import java.util.List;

/**
 * Created by slayer on 24.09.14.
 */
public abstract class Source {



    public enum TEST_STATUS
    {
        DISABLED( "Disabled-Auto-Test" ),
        DEPRECATED( "Deprecated" ),
        NEED_UPDATE( "Autotest_needs_update");

        private String value;

        TEST_STATUS( String value )
        {
            this.value = value;
        }

        public String toString()
        {
            return value;
        }
    }


    Project project;

    public void setProject( Project project )
    {
        this.project = project;
    }


    private static Source sourceInstance;

    public static Source getSource()
    {


        if ( isEinstein() && ( sourceInstance == null || sourceInstance instanceof DashboardIntegration ) )
             sourceInstance = new EinsteinIntegration();
        else
            if ( !isEinstein() && ( sourceInstance == null || sourceInstance instanceof EinsteinIntegration ) )
                 sourceInstance = new DashboardIntegration();

        return sourceInstance;
    }

    public static boolean isEinstein()
    {
        return SettingsStorage.loadData("dashboard.url").contains("einstein");
    }

    public abstract List<TestEntity> getTestList( String pattern, boolean checkBoxState );


    public abstract List<StepEntity> getStepsList(String dataBaseId);

    public abstract String getTestPreconditions(String dataBaseId);

    public abstract String getTestTitle(String dataBaseId);

    public abstract String getTestLabels(String dataBaseId);

    public abstract List<TestFolder> getAllTestsHierarchy( String pattern, FILTER filter  );

    public abstract List<String> getAllProjectNames();

    public abstract List<String> getTestIdsForUpdate();

    public abstract List<String> getDisabledTestIds();

    public abstract List<String> getDeprecatedTestIds();

    public abstract void clearCache();
//    public abstract List<TestFolder> getAllTestsHierarchy( String pattern, String parentID );

}
