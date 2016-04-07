package org.slayer.testLinkIntegration.UI;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by slayer on 3/17/16.
 */
public class TableCustomCellRenderer  extends JTextArea implements TableCellRenderer {


        public TableCustomCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        public int getNeededHeight( Object o, int columnWidth )
        {
            int length = o.toString().length();
            int charsPerLine = columnWidth / getColumnWidth();
            charsPerLine += 1;
            int ret = length / charsPerLine;
            if((length % charsPerLine) > 0)
                ret += 1;
            ret *= getRowHeight();

            return ret;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String data = (String) value.toString();
            int lineWidth = this.getFontMetrics(this.getFont()).stringWidth(data);
            int lineHeight = this.getFontMetrics(this.getFont()).getHeight();
            int rowWidth = table.getCellRect(row, column, true).width;

            int newRowHeight = (int) ((lineWidth / rowWidth) * (lineHeight)) + lineHeight * 2;
            if (table.getRowHeight(row) != newRowHeight) {
                table.setRowHeight(row, newRowHeight);
            }
            this.setText(data);
            return this;
        }
}
