package org.slayer.testLinkIntegration.UI;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.OnOffButton;
import org.jetbrains.annotations.Nullable;
import org.slayer.testLinkIntegration.SettingsStorage;
import org.slayer.testLinkIntegration.Source;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;

public class SettingsDialog extends DialogWrapper{

    private JTextField tfText1 = new JTextField( );
    private JPasswordField tfText2  = new JPasswordField( );
    private JTextField tfText3 = new JTextField( );
    private JBLabel doubleClickActionLabel = new JBLabel( "View test steps by double click:" );
    private OnOffButton doubleClickAction = new OnOffButton( );

    private JBLabel addIdToDescriptionLabel = new JBLabel( "Add test id to description:" );
    private OnOffButton addIdToDescriptionCheckbox = new OnOffButton( );

    private JBLabel addTestIdToMethodName = new JBLabel( "Add test id to generated method name:" );
    private OnOffButton addTestIdToMethodNameCheckbox = new OnOffButton( );

    private JBLabel projectBoxLabel = new JBLabel( "Project:" );
    private ComboBox projectsBox;

    private JBLabel logClassNameLabel = new JBLabel( "Full path to log impl(e.g. 'kernel.core.logger.Log'): " );
    private JTextField logClassName = new JTextField();


    private static java.util.List<String> projectNames = new ArrayList<>();

    private boolean doubleClickActionSelected = Boolean.valueOf( SettingsStorage.loadData("doubleClickViewSteps") );

    public SettingsDialog( Project project, boolean cancelDisabled ) {

        super( project );

        if ( !SettingsStorage.loadData("projectPrefix").isEmpty() )
              projectNames = Source.getSource().getAllProjectNames();

        if ( !projectNames.isEmpty() ) {
              ComboBoxModel<String> boxModel = new DefaultComboBoxModel<>( projectNames.toArray( new String[ projectNames.size() ] ) );
              projectsBox = new ComboBox( boxModel );
              projectsBox.setSelectedItem( SettingsStorage.loadData("projectPrefix") );
        }
        else
             projectsBox = new ComboBox();

        init();


        setTitle("Plugin Settings");
        setOKButtonText("Save");
        setCancelButtonText("Discard");
        setResizable( false );
        tfText1.setText( SettingsStorage.loadData("user"));
        tfText2.setText( SettingsStorage.loadData("pass"));
        tfText3.setText( SettingsStorage.loadData("dashboard.url"));
        logClassName.setText( SettingsStorage.loadData("log.class") );

        tfText1.setPreferredSize(new Dimension(200, 23));
        tfText2.setPreferredSize(new Dimension(200, 23));
        tfText3.setPreferredSize(new Dimension(200, 23));

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInputs();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInputs();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInputs();
            }
        };

        tfText1.getDocument().addDocumentListener( documentListener );
        tfText2.getDocument().addDocumentListener( documentListener );
        tfText3.getDocument().addDocumentListener( documentListener );
        logClassName.getDocument().addDocumentListener( documentListener );



        ItemListener itemListener = e -> validateInputs();
        doubleClickAction.setSelected( doubleClickActionSelected );
        addTestIdToMethodNameCheckbox.setSelected( Boolean.valueOf( SettingsStorage.loadData("addTestIdToMethodName") ) );
        addIdToDescriptionCheckbox.setSelected( Boolean.valueOf( SettingsStorage.loadData("addIdToDescription") ) );

        projectsBox.addItemListener( itemListener );
        doubleClickAction.addItemListener( itemListener );
        addTestIdToMethodNameCheckbox.addItemListener( itemListener );
        addIdToDescriptionCheckbox.addItemListener( itemListener );

        validateInputs();

        if ( cancelDisabled )
             getCancelAction().setEnabled( false );


    }


    private boolean validateInputs()
    {
        boolean check = tfText1.getText().isEmpty() || tfText2.getPassword().length == 0 || tfText3.getText().isEmpty();
        boolean validate =  tfText1.getText().equals( SettingsStorage.loadData("user") )
                            && new String( tfText2.getPassword() ).equals( SettingsStorage.loadData("pass"))
                            && tfText3.getText().equals( SettingsStorage.loadData("dashboard.url"))
                            && doubleClickAction.isSelected() == Boolean.valueOf( SettingsStorage.loadData("doubleClickViewSteps") )
                            && addIdToDescriptionCheckbox.isSelected() == Boolean.valueOf( SettingsStorage.loadData("addIdToDescription") )
                            && addTestIdToMethodNameCheckbox.isSelected() == Boolean.valueOf( SettingsStorage.loadData("addTestIdToMethodName") )
                            && logClassName.getText().equals( SettingsStorage.loadData("log.class") );

        if ( !projectNames.isEmpty() )
              validate &= projectsBox.getSelectedItem().toString().equals( SettingsStorage.loadData( "projectPrefix" ) );

             getOKAction().setEnabled( !check && !validate );

        return !check;
    }

    public boolean saveData() {

        boolean check = !tfText1.getText().equals( SettingsStorage.loadData("user") )
                || !new String( tfText2.getPassword() ).equals( SettingsStorage.loadData("pass"))
                || !tfText3.getText().equals( SettingsStorage.loadData("dashboard.url"));


        SettingsStorage.storeData("user", tfText1.getText());
        SettingsStorage.storeData("pass", new String(tfText2.getPassword()));

        String dashUrl = tfText3.getText();
        if ( !dashUrl.startsWith("http://") && !dashUrl.startsWith("https://") )
              dashUrl = "http://" + dashUrl;

        SettingsStorage.storeData("dashboard.url", dashUrl);

        SettingsStorage.storeData("doubleClickViewSteps", String.valueOf( doubleClickAction.isSelected() ) );
        SettingsStorage.storeData("addIdToDescription", String.valueOf( addIdToDescriptionCheckbox.isSelected() ) );
        SettingsStorage.storeData("addTestIdToMethodName", String.valueOf( addTestIdToMethodNameCheckbox.isSelected() ) );
        SettingsStorage.storeData( "log.class", logClassName.getText() );

        if ( projectsBox.getModel().getSize() == 0 ) {
             projectNames = Source.getSource().getAllProjectNames();
             projectsBox.setModel(new DefaultComboBoxModel<>(projectNames.toArray(new String[projectNames.size()])));
             projectsBox.setSelectedIndex( 0 );
        }

        check |= !projectsBox.getSelectedItem().toString().equals( SettingsStorage.loadData( "projectPrefix" ) );

        SettingsStorage.storeData("projectPrefix", projectsBox.getSelectedItem().toString() );

        dispose();

        return check;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        JPanel pnPanel0 = new JPanel();
        GridBagLayout gbPanel0 = new GridBagLayout();
        GridBagConstraints gbcPanel0 = new GridBagConstraints();
        pnPanel0.setLayout( gbPanel0 );

        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 1;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( projectBoxLabel, gbcPanel0 );
        pnPanel0.add( projectBoxLabel );

        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 1;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( projectsBox , gbcPanel0 );
        pnPanel0.add( projectsBox );


        if ( projectsBox.getModel().getSize() > 0 && SettingsStorage.loadData("projectPrefix") != null )
             projectsBox.setSelectedItem(SettingsStorage.loadData("projectPrefix").replace("-", ""));


        JBLabel lbLabel0 = new JBLabel( "LDAP User:"  );
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


        JBLabel lbLabel1 = new JBLabel( "LDAP Password: "  );
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

        JBLabel lbLabel2 = new JBLabel( "Source URL:"  );
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


        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 5;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( doubleClickActionLabel, gbcPanel0 );
        pnPanel0.add( doubleClickActionLabel );

        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 5;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.WEST;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( doubleClickAction, gbcPanel0 );
        pnPanel0.add( doubleClickAction );

        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 6;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( addIdToDescriptionLabel, gbcPanel0 );
        pnPanel0.add(addIdToDescriptionLabel);

        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 6;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.WEST;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( addIdToDescriptionCheckbox, gbcPanel0 );
        pnPanel0.add( addIdToDescriptionCheckbox );

        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 7;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( addTestIdToMethodName, gbcPanel0 );
        pnPanel0.add( addTestIdToMethodName );

        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 7;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.WEST;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( addTestIdToMethodNameCheckbox , gbcPanel0 );
        pnPanel0.add( addTestIdToMethodNameCheckbox );

        gbcPanel0.gridx = 1;
        gbcPanel0.gridy = 8;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.WEST;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( logClassNameLabel , gbcPanel0 );
        pnPanel0.add( logClassNameLabel );

        gbcPanel0.gridx = 6;
        gbcPanel0.gridy = 8;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 1;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( logClassName, gbcPanel0 );
        pnPanel0.add( logClassName );

        return pnPanel0;
    }

}
