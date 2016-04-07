package org.slayer.testLinkIntegration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slayer on 3/2/16.
 */
public class TestFolder{

    private List<TestFolder> subFolders = new ArrayList<>();
    private List<TestEntity> rootTests = new ArrayList<>();
    private String id;
    private String desc;
    private String parentID;
    private boolean isFolder;
    private boolean isTest;
    private String icon;
    private String testLinkID = "";
    private int childrenCount = 0;

    public TestFolder( String id, String desc, String parentID )
    {
        this.id = id;
        this.desc = desc.replaceAll("<span class=\"counttest\">","").replaceAll("\\d+</span>","").replace("</span>", "").replaceAll("\\(\\d+\\)", "");
        this.parentID = parentID;
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    public String getDescription() {
        return desc;
    }

    public String toString()
    {
        return testLinkID.isEmpty() ? desc + "(" + childrenCount + ")" : testLinkID + " : " + desc;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }

    public String getId() {
        return id;
    }

    public String getParentID() {
        return parentID;
    }

    public void setTestLinkID( String testLinkID )
    {
        this.testLinkID = testLinkID.substring(testLinkID.indexOf(">") + 1, testLinkID.indexOf("</")).replace("&ndash;", "-");
        String prefix = this.testLinkID.substring(0, this.testLinkID.indexOf("-"));
        desc = desc.replace("<span class=project>" + prefix + "&ndash;", "");
    }

    public String getTestLinkID() {
        return testLinkID;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }
}
