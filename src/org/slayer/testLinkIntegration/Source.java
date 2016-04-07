package org.slayer.testLinkIntegration;

import com.intellij.openapi.project.Project;
import org.slayer.testLinkIntegration.UI.FILTER;

import java.util.List;

/**
 * Created by slayer on 24.09.14.
 */
public abstract class Source {

    Project project;

    public void setProject( Project project )
    {
        this.project = project;
    }


    private static Source sourceInstance;

    public static Source getSource()
    {
        if ( sourceInstance == null )
             sourceInstance = new DashboardIntegration();

        return sourceInstance;
    }

    public abstract List<TestEntity> getTestList( String pattern, boolean checkBoxState );


    public abstract List<StepEntity> getStepsList(String dataBaseId);

    public abstract String getTestPreconditions(String dataBaseId);

    public abstract String getTestTitle(String dataBaseId);

    public abstract String getTestLabels(String dataBaseId);

    public abstract List<TestFolder> getAllTestsHierarchy( String pattern, FILTER filter  );

    public abstract List<String> getAllProjectNames();

//    public abstract List<TestFolder> getAllTestsHierarchy( String pattern, String parentID );

}
