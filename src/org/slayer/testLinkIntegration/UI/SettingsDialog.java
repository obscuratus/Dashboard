package org.slayer.testLinkIntegration.UI;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import org.slayer.testLinkIntegration.SettingsStorage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class SettingsDialog extends DialogWrapper{

    JTextField tfText1 = new JTextField( );
    JPasswordField tfText2  = new JPasswordField( );
    JTextField tfText3 = new JTextField( );

    public SettingsDialog( Project project, boolean cancelDisabled ) {
        super( project );
        init();

        setTitle("Dashboard Settings");
        setOKButtonText("Save");
        setCancelButtonText("Discard");
        setResizable( false );
        tfText1.setText( SettingsStorage.loadData("user"));
        tfText2.setText( SettingsStorage.loadData("pass"));
        tfText3.setText( SettingsStorage.loadData("dashboard.url"));

        tfText1.setPreferredSize(new Dimension(200, 23));
        tfText2.setPreferredSize(new Dimension(200, 23));
        tfText3.setPreferredSize(new Dimension(200, 23));

        tfText1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });
        tfText2.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });
        tfText3.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });

        changed();

        if ( cancelDisabled )
             getCancelAction().setEnabled( false );


    }

    private void changed()
    {
        if ( tfText1.getText().isEmpty() || tfText2.getPassword().length == 0 || tfText3.getText().isEmpty())
             getOKAction().setEnabled( false );
        else
             getOKAction().setEnabled( true );
    }

    public void saveData() {

        SettingsStorage.storeData("user", tfText1.getText());
        SettingsStorage.storeData("pass", new String(tfText2.getPassword()));
        SettingsStorage.storeData("dashboard.url", tfText3.getText());
        dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        JPanel pnPanel0 = new JPanel();
        GridBagLayout gbPanel0 = new GridBagLayout();
        GridBagConstraints gbcPanel0 = new GridBagConstraints();
        pnPanel0.setLayout( gbPanel0 );

        JLabel lbLabel0 = new JLabel( "LDAP User:"  );
        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 2;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( lbLabel0, gbcPanel0 );
        pnPanel0.add( lbLabel0 );


        JLabel lbLabel1 = new JLabel( "LDAP Password: "  );
        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 3;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 0;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( lbLabel1, gbcPanel0 );
        pnPanel0.add( lbLabel1 );



        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 2;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( tfText1, gbcPanel0 );
        pnPanel0.add( tfText1 );


        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 3;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 0;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( tfText2, gbcPanel0 );
        pnPanel0.add( tfText2 );

        JLabel lbLabel2 = new JLabel( "Dashboard URL:"  );
        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 4;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( lbLabel2, gbcPanel0 );
        pnPanel0.add( lbLabel2 );


        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 4;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 0;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( tfText3, gbcPanel0 );
        pnPanel0.add( tfText3 );

        return pnPanel0;
    }

}
