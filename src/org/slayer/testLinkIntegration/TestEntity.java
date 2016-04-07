package org.slayer.testLinkIntegration;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by slayer on 23.09.14.
 */
public class TestEntity implements Comparable<TestEntity>, Transferable{

    private String className = "TestClass";
    private String description;
    private String id ;
    private String dataBaseId;
    List<StepEntity> stepEntities;
    private String parentID;
    private String icon;
    private String keywords = "";

    public TestEntity( String id, String description )
    {
        this.id = id;
        this.description = description;
        stepEntities = new ArrayList<StepEntity>();

    }

    public String getClassName()
    {
        return className;
    }

    public String getDescription()
    {
        return description;
    }

    public List<String> getLog()
    {
        gatherSteps();
        List<String> log = new ArrayList<String>();

        int i = 1;
        for ( StepEntity step : stepEntities )
        {
            if ( !step.step.trim().isEmpty() )
                log.add( "kernel.core.logger.Log.step(\"" + i + ". " + step.step + "\");" + "\n" );

            log.addAll(
                    step.verifySteps.stream()
                            .filter(verify -> !verify.trim().isEmpty())
                            .map(verify -> "kernel.core.logger.Log.verify(\"" + verify.trim() + "\", false);").
                            collect(Collectors.toList()));

            i++;
        }

        return log;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return id;
    }

    private void gatherSteps()
    {
        stepEntities = Source.getSource().getStepsList( dataBaseId );
    }

    public String toString()
    {
        String spaces = "";
        for ( int i = 0; i <= 20 - id.length(); i++ )
              spaces += " ";

        return id + spaces + description.trim();
    }


    public String getDataBaseId() {
        return dataBaseId;
    }

    public void setDataBaseId(String dataBaseId) {
        this.dataBaseId = dataBaseId;
    }

    public void gatherKeywords()
    {
        keywords = getLabels();
    }

    public void setParentID( String parentID )
    {
        this.parentID = parentID;
    }


    @Override
    public int compareTo(TestEntity testEntity) {

        int clearId = Integer.parseInt( id.replaceFirst("\\w+-", "") );
        int clearId2 = Integer.parseInt( testEntity.getId().replaceFirst("\\w+-", "") );
        return clearId - clearId2;
    }

    public String getPreconditions()
    {
        return Source.getSource().getTestPreconditions( dataBaseId );
    }

    public String getTitle() { return Source.getSource().getTestTitle( dataBaseId ); }

    private String getLabels() { return Source.getSource().getTestLabels( dataBaseId ); }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getKeywords()
    {
        return keywords;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { new DataFlavor( this.getClass(), "tree-node") };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return "asd";
    }


}
