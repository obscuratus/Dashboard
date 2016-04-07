package org.slayer.testLinkIntegration.UI;

import org.slayer.testLinkIntegration.TestEntity;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.net.URL;

/**
 * Created by slayer on 3/4/16.
 */
public class CustomCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)value;
        TreeNode t = nodo.getParent();
        URL automatedIcon = this.getClass().getClassLoader().getResource("resources/automated.png");
        if(t!=null && nodo.getUserObject() instanceof TestEntity && automatedIcon != null ){
                TestEntity test = (TestEntity) nodo.getUserObject();
                Icon icon = test.getIcon().contains("automated") ? new ImageIcon( automatedIcon ) :  getDefaultLeafIcon();


                setIcon( icon );
        }
        return this;
    }
}
