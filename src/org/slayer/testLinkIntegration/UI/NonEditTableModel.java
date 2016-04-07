package org.slayer.testLinkIntegration.UI;

import javax.swing.table.DefaultTableModel;

/**
 * Created by slayer on 3/14/16.
 */
public class NonEditTableModel extends DefaultTableModel {

    NonEditTableModel(Object[][] data, String[] columnNames) {
        super(data, columnNames);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
