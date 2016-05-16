package org.slayer.testLinkIntegration.UI;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.refactoring.ui.InfoDialog;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.slayer.testLinkIntegration.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.*;
import java.util.List;

//import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by slayer on 3/2/16.
 */
public class DashboardIntegrationToolFactory implements ToolWindowFactory {

    private Font defaultFont = null;
    private Font strikedFont = null;
    private ListIterator<TreePath> foundTestIter = null;
    private boolean searchFailed = false;
    private final Key<UIForm> FORM_KEY = new Key<>("UIForm") ;

    public static class UIForm {

        private JPanel mainPanel;
        private JTree testsTree;
        private RefreshButton refreshButton;
        private ExpandButton expandBtn;
        private CollapseButton collapseBtn;
        private SearchField searchField;
        private JComboBox<Enum> comboBox1;
        private SettingsButton settings;
        private NextTestButton nextTestBtn;
        private DefaultMutableTreeNode root;
        private DefaultTreeModel model;

        private JPopupMenu popupMenu;
        private List<TreePath> foundPaths = new ArrayList<>();

    }

    private UIForm currentForm = null;
    private Project project;
    private List<String> filteredIdsForUpdate = new ArrayList<>();
    private List<String> disabledTests = new ArrayList<>();
    private List<String> deprecatedTests = new ArrayList<>();

    private enum MODE
    {
        Collapsed,
        Expanded
    }

    private ImageIcon automatedIcon;
    private ImageIcon warningIcon;
    private ImageIcon disabledIcon;

    public DashboardIntegrationToolFactory()
    {
        if ( SettingsStorage.loadData("log.class").isEmpty() )
             SettingsStorage.storeData("log.class", "kernel.core.logger.Log");

        ClassLoader classLoader = this.getClass().getClassLoader();
        automatedIcon = new ImageIcon( classLoader.getResource("resources/automated.png") );
        warningIcon = new ImageIcon( classLoader.getResource("resources/warning-icon.png") );
        disabledIcon = new ImageIcon( classLoader.getResource("resources/Erase.png") );
    }

    private void setupForm()
    {

        ComboBoxModel<Enum> comboBoxModel = new DefaultComboBoxModel<>( new FILTER[]{ FILTER.All, FILTER.Automated, FILTER.Manual } );

        currentForm.comboBox1.setModel( comboBoxModel );
        currentForm.comboBox1.setSelectedItem(FILTER.All);
        currentForm.nextTestBtn.setVisible( false );
        checkSettings();

        filteredIdsForUpdate = Source.getSource().getTestIdsForUpdate();
        disabledTests = Source.getSource().getDisabledTestIds();
        deprecatedTests = Source.getSource().getDeprecatedTestIds();
        createEmptyTree();

        currentForm.refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                createEmptyTree();
                updateTestTree( null );
                Messages.showInfoMessage("Refreshing is done", "Tree Update");
            }
        });

        currentForm.settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                initSettingsPopup( false );

            }
        });

        currentForm.testsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ( e.getClickCount() == 2 && Boolean.valueOf( SettingsStorage.loadData("doubleClickViewSteps") )) {
                     int row = currentForm.testsTree.getClosestRowForLocation(e.getX(), e.getY());
                     currentForm.testsTree.setSelectionRow(row);
                     viewSteps();
                }

                initContextMenuItems();

                if (SwingUtilities.isRightMouseButton(e)) {

                    if (currentForm.testsTree.getSelectionPaths() == null || currentForm.testsTree.getSelectionPaths().length == 1) {

                        int row = currentForm.testsTree.getClosestRowForLocation(e.getX(), e.getY());
                        currentForm.testsTree.setSelectionRow(row);
                        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentForm.testsTree.getLastSelectedPathComponent();
                        if (selectedNode.getUserObject() instanceof TestEntity) {
                            JMenuItem viewItem = new JMenuItem(new AbstractAction("View steps") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    viewSteps();
                                }
                            });

                            currentForm.popupMenu.add(viewItem);
                        }
                    }
                    currentForm.popupMenu.show(currentForm.testsTree, e.getX(), e.getY());

                }
            }

        });

        currentForm.searchField.addKeyboardListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {

                if ( e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if ( currentForm.searchField.isCriteriaChanged() ) {
                         currentForm.searchField.addCurrentTextToHistory();
                         searchTestInTree();

                         if ( !searchFailed )
                               currentForm.searchField.setCriteriaChanged( false );
                    }
                    else
                        nextTest();
                }
            }
        });

        currentForm.searchField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {
                currentForm.searchField.setCriteriaChanged( true );
            }
        });

        currentForm.comboBox1.addItemListener(e -> {
            if ( e.getStateChange() == ItemEvent.SELECTED )
            {

                currentForm.searchField.setText("");
                createEmptyTree();
                addFoldersToTree( FILTER.valueOf(currentForm.comboBox1.getSelectedItem().toString()) );
            }
        });



        currentForm.expandBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                super.mouseClicked(ev);
                morphTree(currentForm.testsTree, MODE.Expanded);
            }
        });

        currentForm.collapseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                super.mouseClicked(ev);
                morphTree(currentForm.testsTree, MODE.Collapsed);
            }
        });


        currentForm.nextTestBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                nextTest();
            }
        });
    }

    private void nextTest()
    {
        if ( !foundTestIter.hasNext() ) {

             foundTestIter = currentForm.foundPaths.listIterator();
             Messages.showInfoMessage(project, "Cannot find more tests for search criteria.", "Search");
        }

        TreePath nextPath = foundTestIter.next();
        currentForm.testsTree.setSelectionPath( nextPath );
        currentForm.testsTree.scrollPathToVisible( nextPath );


    }


    private void checkSettings() {
        String user = SettingsStorage.loadData("user");
        String pass = SettingsStorage.loadData("pass");
        String url = SettingsStorage.loadData("dashboard.url");

        if ( user.isEmpty() || pass.isEmpty() || url.isEmpty() ) {
             initSettingsPopup(true);
        }
    }

    private void morphTree(JTree tree, MODE mode) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        morphTree(tree, new TreePath(root), mode);
    }

    private void morphTree(JTree tree, TreePath parent, MODE mode) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                morphTree(tree, path, mode);
            }
        }
        if ( mode == MODE.Expanded )
             tree.expandPath(parent);
        else
            if ( mode == MODE.Collapsed )
                 tree.collapsePath(parent);
    }

    private void initContextMenuItems()
    {
        currentForm.popupMenu = new JPopupMenu();


        JMenuItem importItem = new JMenuItem(new AbstractAction("Import test(s)...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTestsToClass( null );
            }
        });

        JMenuItem importToCurrentItem = new JMenuItem(new AbstractAction("Import test(s) to current opened class") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTestsToClass();
            }
        });

        currentForm.popupMenu.add( importItem );
        currentForm.popupMenu.add( importToCurrentItem );


    }

    private void addTestsToClass()
    {
        Document document = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();

        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile( document );
        addTestsToClass( ((PsiJavaFile) file).getClasses()[0] );
    }

    private void addTestsToClass( PsiClass psiClass )
    {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentForm.testsTree.getLastSelectedPathComponent();
        Object obj = selectedNode.getUserObject();

        if ( currentForm.testsTree.getSelectionPaths().length > 1 )
        {

            for ( TreePath path : currentForm.testsTree.getSelectionPaths() )
            {
                DefaultMutableTreeNode t = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = t.getUserObject();

                if ( userObject instanceof TestEntity ) {
                     ClassResolver.setCurrentClass(psiClass);
                     ClassResolver.resolveClass(project, (TestEntity) userObject);
                }
                else {
                    new InfoDialog("Folder multi-selection is not supported yet", project).show();
                    break;
                }
            }

        }
        else
            if ( obj instanceof TestEntity) {


                ClassResolver.setCurrentClass( psiClass );
                ClassResolver.resolveClass(project, (TestEntity) selectedNode.getUserObject());
            }
        else
            if ( obj instanceof TestFolder) {

                Enumeration children = selectedNode.children();
                while ( children.hasMoreElements() ) {
                    DefaultMutableTreeNode testNode = (DefaultMutableTreeNode) children.nextElement();

                    if ( testNode.getUserObject() instanceof TestEntity ) {
                        TestEntity test = (TestEntity) testNode.getUserObject();
                        ClassResolver.setCurrentClass( psiClass );
                        ClassResolver.resolveClass( project, test );

                        if ( psiClass == null ) {
                             Document document = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
                             PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
                             psiClass = ((PsiJavaFile) file).getClasses()[0];
                        }
                    }


                }
            }


    }

    private void createEmptyTree()
    {
        currentForm.model = (DefaultTreeModel) currentForm.testsTree.getModel();
        currentForm.root = (DefaultMutableTreeNode) currentForm.model.getRoot();

        currentForm.root.removeAllChildren();
        currentForm.root.setUserObject("Tests");

        currentForm.testsTree.setCellRenderer( new DefaultTreeCellRenderer()
        {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
                StringBuilder sb = new StringBuilder();

                if ( defaultFont == null ) {
                     defaultFont = getFont();
                     Font font = new Font("helvetica", Font.PLAIN, 12);
                     Map  attributes = font.getAttributes();
                     attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                     strikedFont = new Font(attributes);
                }

                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)value;
                TreeNode t = nodo.getParent();

                if(t!=null && nodo.getUserObject() instanceof TestEntity && automatedIcon != null ){
                    TestEntity test = (TestEntity) nodo.getUserObject();
                    boolean testAutomated =  test.getIcon().contains("automated");
                    Icon icon = testAutomated ? automatedIcon : getDefaultLeafIcon();

                        if ( currentForm.comboBox1.getSelectedItem().equals( FILTER.Automated ) && !testAutomated )
                             setVisible( false );
                    else
                        if ( currentForm.comboBox1.getSelectedItem().equals( FILTER.Manual ) && testAutomated )
                             setVisible(false);
                    else
                        if ( currentForm.comboBox1.getSelectedItem().equals( FILTER.All ) )
                             setVisible( true );

                    List<Icon> icons = new ArrayList<>();
                    icons.add( icon );

                    if ( disabledTests.contains( test.getDataBaseId() ))
                    {
                        icons.add( disabledIcon );
                        sb.append("Disabled");
                    }

                    if ( filteredIdsForUpdate.contains( test.getDataBaseId() ) )
                    {
                        icons.add( warningIcon );

                        if ( !sb.toString().isEmpty() )
                            sb.append("/");

                        sb.append("Needs update");
                    }

                        CompoundIcon compoundIcon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, icons.toArray( new Icon[ icons.size() ]  ) );
                        setIcon( compoundIcon );

                    if ( deprecatedTests.contains( test.getDataBaseId() )) {

                         if ( !sb.toString().isEmpty() )
                               sb.append("/");

                         sb.append("Deprecated");

                         setFont( strikedFont );
                    }
                    else {
                         setFont( defaultFont );
                    }




                }
                else
                    setFont( defaultFont );

                setToolTipText( sb.toString() );
                return this;
            }
        });


    }



    private void searchTestInTree() {

        if ( currentForm.searchField.getText().isEmpty() ) {
             currentForm.nextTestBtn.setVisible( false );
             currentForm.testsTree.setSelectionPaths( new TreePath[0] );
             return;
        }

        currentForm.foundPaths.clear();

        Enumeration<DefaultMutableTreeNode> e = currentForm.root.depthFirstEnumeration();
        while ( e.hasMoreElements() )
        {
            DefaultMutableTreeNode node = e.nextElement();
            if ( node.getUserObject() instanceof TestEntity ) {

                 TestEntity test = ((TestEntity) node.getUserObject());
                 String searchCriteria = currentForm.searchField.getText();
                 if ( test.getId().contains( searchCriteria ) || test.getDescription().toLowerCase().contains( searchCriteria.toLowerCase() ) )
                 {
                    currentForm.foundPaths.add(new TreePath(node.getPath()));

                 }
            }
        }

        if ( currentForm.foundPaths.isEmpty() ) {
             currentForm.nextTestBtn.setVisible( false );
             currentForm.searchField.markAsInvalid();
             searchFailed = true;
             return;
        }
        else {
            currentForm.nextTestBtn.setVisible( true );
            searchFailed = false;
            currentForm.searchField.restoreDefaultColor();
        }


        foundTestIter = currentForm.foundPaths.listIterator();

        currentForm.testsTree.setSelectionPath( foundTestIter.next() );

              if ( !currentForm.foundPaths.isEmpty() )
                    currentForm.testsTree.scrollPathToVisible( currentForm.foundPaths.get( 0 ) );
    }


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {


        if ( project.getUserData( FORM_KEY ) == null )
             project.putUserData( FORM_KEY, new UIForm() );

        currentForm = project.getUserData( FORM_KEY );
        setupForm();

        this.project = project;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent( currentForm.mainPanel, "", false );
        toolWindow.getContentManager().addContent( content );
        updateTestTree(null);

    }



    private void updateTestTree( ActionEvent e )
    {
        addFoldersToTree( FILTER.valueOf( currentForm.comboBox1.getSelectedItem().toString() ) );
    }

    private void addFoldersToTree( FILTER filter )
    {
        List<TestFolder> folders = Source.getSource().getAllTestsHierarchy(SettingsStorage.loadData("projectPrefix").replace("-", ""), filter);

        HashMap<String, DefaultMutableTreeNode> nodesMap = new HashMap<>();


        for ( TestFolder folder : folders ) {
              DefaultMutableTreeNode node = new DefaultMutableTreeNode( folder );

              if ( folder.isFolder() )
                   nodesMap.put( folder.getId(), node );
        }

        for ( TestFolder folder : folders )
        {
            if ( nodesMap.containsKey(folder.getParentID())) {
                if (folder.isFolder())
                    nodesMap.get(folder.getParentID()).add(nodesMap.get(folder.getId()));
                else if (!folder.getTestLinkID().isEmpty()) {
                    DefaultMutableTreeNode testNode = new DefaultMutableTreeNode();


                    TestEntity test = new TestEntity(folder.getTestLinkID(), folder.getDescription());
                    test.setDataBaseId(folder.getId());
                    test.setIcon(folder.getIcon());


                    testNode.setUserObject(test);
                    nodesMap.get(folder.getParentID()).add(testNode);
                }


            }



            if ( folder.getId().equals("3777") || folder.getDescription().equals("Manual ")  )
                currentForm.root.add( nodesMap.get( folder.getId() ));
        }




        folders.stream().filter(folder -> nodesMap.containsKey(folder.getId())).forEach(folder -> {

            DefaultMutableTreeNode child = nodesMap.get(folder.getId());


            if (child.getChildCount() == 0) {

                child.removeFromParent();
                String parentID = folder.getParentID();
                DefaultMutableTreeNode p = new DefaultMutableTreeNode();
                DefaultMutableTreeNode parent = new DefaultMutableTreeNode();
                while (/*parentID.equals("3777")*/  nodesMap.containsKey(parentID)) {
                    parent = nodesMap.get(parentID);

                    removeAllParentChildren(parent);

                    if (parent.toString().startsWith("Manual "))
                        break;

                    p = (DefaultMutableTreeNode) parent.getParent();
                    if (p == null)
                        break;

                    parentID = ((TestFolder) p.getUserObject()).getParentID();
                }

                nodesMap.remove(folder.getId());
            }


        });

        for (Map.Entry<String, DefaultMutableTreeNode> entry : nodesMap.entrySet() )
        {


            DefaultMutableTreeNode node = entry.getValue();

            TestFolder folder = ((TestFolder) node.getUserObject());
            folder.setChildrenCount(node.getChildCount() > 0 ? node.getLeafCount() : node.getChildCount());

        }

        if ( currentForm.root.getChildCount() == 0 )
             currentForm.root.setUserObject("No data...");
        else
             currentForm.root.setUserObject("Tests");

//        saveExpansionState();

        currentForm.model.reload(currentForm.root);
//        setExpansionState();

    }

    private void removeAllParentChildren( DefaultMutableTreeNode parent )
    {
        if ( parent.getChildCount() > 0 )
        {
            Enumeration children = parent.children();
            while ( children.hasMoreElements() ) {
                    DefaultMutableTreeNode node = ((DefaultMutableTreeNode) children.nextElement());
                    if ( node.getChildCount() > 0 )
                         removeAllParentChildren( node );
                    else
                         if ( node.getUserObject() instanceof TestFolder ) {
                              node.removeFromParent();
                         }

            }
        }
    }


    private void viewSteps()
    {

            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentForm.testsTree.getLastSelectedPathComponent();

            if ( selectedNode.getUserObject() instanceof TestEntity) {
                TestEntity t = (TestEntity) selectedNode.getUserObject();
                t.gatherKeywords();
                List<StepEntity> steps = Source.getSource().getStepsList(t.getDataBaseId());

                String[][] data = new String[steps.size()][2];

                for ( int i = 0; i < steps.size(); i++ )
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder
                            .append( i + 1 )
                            .append(" ")
                            .append(steps.get(i).toString().trim());
                    String step = stringBuilder.toString();

                    List<String> verifySteps = steps.get(i).getVerifySteps();
                    stringBuilder = new StringBuilder();
                    for ( String verify : verifySteps ) {
                          stringBuilder.append(verify.trim());
                        if ( verifySteps.size() > 1 && verifySteps.indexOf( verify ) != verifySteps.size() - 1 )
                             stringBuilder.append("\n");

                    }


                    data[i][0] = step;
                    data[i][1] = stringBuilder.toString();
                }

                ViewTestDialog viewTestDialog = new ViewTestDialog( project, data, t.getPreconditions(), t.getKeywords() );
                viewTestDialog.show();

                if ( viewTestDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE )
                     addTestsToClass();
            }

    }


    private void initSettingsPopup( boolean cancelDisabled)
    {
        SettingsDialog n = new SettingsDialog( project, cancelDisabled );
        n.show();
        if ( n.getExitCode() == DialogWrapper.OK_EXIT_CODE ) {

            if ( n.saveData() ) {
                createEmptyTree();
                addFoldersToTree(FILTER.valueOf(currentForm.comboBox1.getSelectedItem().toString()));
                currentForm.searchField.setCriteriaChanged( true );
            }
        }
    }

}
