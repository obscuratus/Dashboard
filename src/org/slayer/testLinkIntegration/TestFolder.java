package org.slayer.testLinkIntegration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slayer on 3/2/16.
 */
public class TestFolder{

    private String itemId;
    private String name;
    private String parentId;
    private boolean isFolder;
    private boolean isTest;
    private String type = "";
    private String icon = "";
    private String testLinkID = "";
    private int childrenCount = 0;

    public TestFolder( String id, String desc, String parentID )
    {
        this.itemId = id;
        this.name = desc.replaceAll("<span class=\"counttest\">","").replaceAll("\\d+</span>","").replace("</span>", "").replaceAll("\\(\\d+\\)", "");
        this.parentId = parentID;
    }

    public boolean isFolder()
    {
        return isFolder || type.equalsIgnoreCase("suite");
    }

    public String getDescription() {
        return name;
    }

    public String toString()
    {
        return testLinkID.isEmpty() ? name + "(" + childrenCount + ")" : testLinkID + " : " + name;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public boolean isTest() {
        return isTest || type.equalsIgnoreCase("case");
    }

    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }

    public String getId() {
        return itemId;
    }

    public String getParentID() {
        return parentId;
    }

    public void setTestLinkID( String testLinkID )
    {
        if ( !Source.isEinstein() ) {
              this.testLinkID = testLinkID.substring(testLinkID.indexOf(">") + 1, testLinkID.indexOf("</")).replace("&ndash;", "-");
              String prefix = this.testLinkID.substring(0, this.testLinkID.indexOf("-"));
              name = name.replace("<span class=project>" + prefix + "&ndash;", "");
        } else {
            this.testLinkID = SettingsStorage.loadData("projectPrefix") + "-" + testLinkID;

        }
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
