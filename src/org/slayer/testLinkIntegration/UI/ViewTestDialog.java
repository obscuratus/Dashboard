package org.slayer.testLinkIntegration.UI;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by slayer on 3/14/16.
 */
public class ViewTestDialog extends DialogWrapper {


    private final String[][] data;
    private final String preconditions;
    private String keywords;
    private static Dimension savedSize = null;
    private final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    private Dimension defaultSize = new Dimension( screen.width/3, screen.height/3 );
    JPanel pnPanel0 = new JPanel();

    protected ViewTestDialog(@Nullable Project project, String[][] data, String preconditions, String keywords ) {
        super(project);
        this.data = data;
        this.preconditions = preconditions;
        this.keywords = keywords;
        init();
        setOKButtonText("Import to current class");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {


        GridBagLayout gbPanel0 = new GridBagLayout();
        GridBagConstraints gbcPanel0 = new GridBagConstraints();

        pnPanel0.setLayout( gbPanel0 );

        String []colsTable1 = new String[] { "Step", "Expected result" };
        NonEditTableModel model = new NonEditTableModel( data, colsTable1 );

        JBTable tbTable1 = new JBTable( model );
        JBScrollPane pane0 = new JBScrollPane(tbTable1);
        tbTable1.getColumn( "Step" ).setCellRenderer( new TableCustomCellRenderer() );
        tbTable1.getColumn( "Expected result" ).setCellRenderer( new TableCustomCellRenderer() );
        tbTable1.setShowColumns( true );

        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 2;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 2;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.SOUTH;

        gbPanel0.setConstraints( pane0, gbcPanel0 );
        tbTable1.setBorder( BorderFactory.createLineBorder( JBColor.BLACK ));
        pnPanel0.add( pane0 );

        String p = preconditions.trim() + ( keywords.isEmpty() ? "" : "\n\n\n\nKeywords:" + keywords );
        if ( !p.replace("\n", "").replace(" ", "").isEmpty() ) {
            JTextArea lbLabel1 = new JTextArea(p + " \n\n\n", 10, 10);
            JBScrollPane pane = new JBScrollPane(lbLabel1);
            lbLabel1.setEditable(false);
            lbLabel1.setLineWrap(true);
            gbcPanel0.gridx = 1;
            gbcPanel0.gridy = 1;
            gbcPanel0.gridwidth = 1;
            gbcPanel0.gridheight = 1;
            gbcPanel0.fill = GridBagConstraints.BOTH;
            gbcPanel0.weightx = 1;
            gbcPanel0.weighty = 1;
            gbcPanel0.anchor = GridBagConstraints.NORTH;
            gbPanel0.setConstraints(pane, gbcPanel0);
            pnPanel0.add(pane);
        }


        setResizable( true );

        return pnPanel0;

    }

    public void show()
    {
        if ( savedSize == null )
            pnPanel0.setPreferredSize( defaultSize );
        else
            pnPanel0.setPreferredSize( savedSize );

        super.show();

        if ( !getSize().equals( savedSize ) )
              savedSize = getSize();
    }

}
