package org.slayer.testLinkIntegration.UI;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.refactoring.ui.InfoDialog;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.slayer.testLinkIntegration.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;

//import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by slayer on 3/2/16.
 */
public class DashboardIntegrationToolFactory implements ToolWindowFactory {

    public static class UIForm {

        private JPanel mainPanel;
        private JTree testsTree;
        private JButton refreshButton;
        private JButton expandBtn;
        private JButton collapseBtn;
        private JTextField searchField;
        private JLabel foundCount;
        private JComboBox<Enum> comboBox1;
        private JComboBox<String> projectPrefix;
        private JButton settings;
        private DefaultMutableTreeNode root;
        private DefaultTreeModel model;

        private JPopupMenu popupMenu;
        private List<TreePath> foundPaths = new ArrayList<>();
        private List<String> projects;
    }

    private HashMap<Project, UIForm> uiMap = new HashMap<>();
    private UIForm currentForm = null;
    private Project project;


    enum MODE
    {
        Collapsed,
        Expanded
    }



    public DashboardIntegrationToolFactory()
    {


    }

    private void setupForm()
    {
        checkSettings();
        currentForm.projects = Source.getSource().getAllProjectNames();



        createEmptyTree();
        ActionFunc refreshAction = (e) -> { createEmptyTree(); updateTestTree(e); };
        currentForm.refreshButton.addActionListener(refreshAction);
        currentForm.foundCount.setVisible( false );

        currentForm.settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                initSettingsPopup( false );

            }
        });
//        initContextMenuItems();

        currentForm.testsTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                if ( e.getClickCount() == 2 ) {
//                    onSelectTreeNode();
//                }
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
//                    new InfoDialog("Right mouse clicked on: " + (selectedNode.getUserObject() ).toString(), project ).show();
                    currentForm.popupMenu.show(currentForm.testsTree, e.getX(), e.getY());

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        currentForm.searchField.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchTestInTree();

            }
        });

        currentForm.comboBox1.addItem(FILTER.Automated);
        currentForm.comboBox1.addItem(FILTER.Manual);
        currentForm.comboBox1.addItem(FILTER.All);
        currentForm.comboBox1.setSelectedItem(FILTER.All);


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

        while ( currentForm.projects.isEmpty() )
        {
            Messages.showErrorDialog(project, "Cannot connect to dashboard " + SettingsStorage.loadData("dashboard.url"), "Connection Error");
            initSettingsPopup(true, false);
            currentForm.projects = Source.getSource().getAllProjectNames();

        }
    }

    private void checkSettings() {
        String user = SettingsStorage.loadData("user");
        String pass = SettingsStorage.loadData("pass");
        String url = SettingsStorage.loadData("dashboard.url");

        if ( user.isEmpty() || pass.isEmpty() || url.isEmpty() ) {
             initSettingsPopup(true);
        }
    }

    public void morphTree(JTree tree, MODE mode) {
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

                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentForm.testsTree.getLastSelectedPathComponent();

                if ( selectedNode.getUserObject() instanceof TestEntity) {
                     ClassResolver.resolveClass(project, (TestEntity) selectedNode.getUserObject());
                }
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
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentForm.testsTree.getLastSelectedPathComponent();
        Object obj = selectedNode.getUserObject();

        if ( currentForm.testsTree.getSelectionPaths().length > 1 )
        {

            Document document = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();

            PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile( document );
            PsiClass psiClass = ((PsiJavaFile) file).getClasses()[0];

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

                Document document = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();

                PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile( document );
                PsiClass psiClass = ((PsiJavaFile) file).getClasses()[0];
                ClassResolver.setCurrentClass( psiClass );
                ClassResolver.resolveClass(project, (TestEntity) selectedNode.getUserObject());
            }
        else
            if ( obj instanceof TestFolder) {
                Document document = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();

                PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile( document );
                PsiClass psiClass = ((PsiJavaFile) file).getClasses()[0];

                Enumeration children = selectedNode.children();
                while ( children.hasMoreElements() ) {
                    DefaultMutableTreeNode testNode = (DefaultMutableTreeNode) children.nextElement();

                    if ( testNode.getUserObject() instanceof TestEntity ) {
                        TestEntity test = (TestEntity) testNode.getUserObject();
                        ClassResolver.setCurrentClass(psiClass);
                        ClassResolver.resolveClass( project, test );
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
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)value;
                TreeNode t = nodo.getParent();
                URL automatedIcon = this.getClass().getClassLoader().getResource("resources/automated.png");
                if(t!=null && nodo.getUserObject() instanceof TestEntity && automatedIcon != null ){
                    TestEntity test = (TestEntity) nodo.getUserObject();
                    boolean testAutomated =  test.getIcon().contains("automated");
                    Icon icon =  testAutomated ? new ImageIcon( automatedIcon ) :  getDefaultLeafIcon();

                        if ( currentForm.comboBox1.getSelectedItem().equals( FILTER.Automated ) && !testAutomated )
                             setVisible( false );
                    else
                        if ( currentForm.comboBox1.getSelectedItem().equals( FILTER.Manual ) && testAutomated )
                             setVisible(false);
                    else
                        if ( currentForm.comboBox1.getSelectedItem().equals( FILTER.All ) )
                             setVisible( true );

                    setIcon( icon );
                }
                return this;
            }
        });

    }



    private void searchTestInTree() {


        for ( int i = 0; i < currentForm.testsTree.getRowCount() - 1; i++)
            currentForm.testsTree.collapseRow( i );



        if ( currentForm.searchField.getText().isEmpty() ) {
             currentForm.testsTree.setSelectionPaths( new TreePath[0]);
             currentForm.foundCount.setVisible(false);
             return;
        }

        currentForm.foundPaths.clear();



        Enumeration<DefaultMutableTreeNode> e = currentForm.root.depthFirstEnumeration();
        while ( e.hasMoreElements() )
        {
            DefaultMutableTreeNode node = e.nextElement();
            if ( node.getUserObject() instanceof TestEntity )
                 if (  ((TestEntity) node.getUserObject()).getId().contains( currentForm.searchField.getText() ))
                 {
                     currentForm.foundPaths.add( new TreePath( node.getPath() ) );
                 }
        }


        TreeModel model = currentForm.testsTree.getModel();
        currentForm.testsTree.setModel( null );
        currentForm.testsTree.setModel( model );
        currentForm.testsTree.setSelectionPaths( currentForm.foundPaths.toArray( new TreePath[ currentForm.foundPaths.size() ] ) );

              if ( !currentForm.foundPaths.isEmpty() )
                  currentForm.testsTree.scrollPathToVisible( currentForm.foundPaths.get( 0 ) );

              currentForm.foundCount.setText(currentForm.foundPaths.isEmpty() ? "No test found" : "Found tests: " + currentForm.foundPaths.size());
              currentForm.foundCount.setVisible( true );

    }


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        if ( !uiMap.containsKey( project ) )
              uiMap.put( project, new UIForm() );

        currentForm = uiMap.get( project );


        setupForm();
        currentForm.projects.forEach(currentForm.projectPrefix::addItem);
        currentForm.projectPrefix.setSelectedItem( SettingsStorage.loadData("projectPrefix").replace("-", "") );

        currentForm.projectPrefix.addItemListener(e -> {
            if ( e.getStateChange() == ItemEvent.SELECTED )
            {
                currentForm.searchField.setText("");
                createEmptyTree();
                addFoldersToTree( FILTER.valueOf(currentForm.comboBox1.getSelectedItem().toString()) );
                SettingsStorage.storeData( "projectPrefix", currentForm.projectPrefix.getSelectedItem().toString() + "-");
            }
        });

        this.project = project;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent( currentForm.mainPanel, "", false );
        toolWindow.getContentManager().addContent( content );

        updateTestTree( null );

    }

    private void updateTestTree( ActionEvent e )
    {

        addFoldersToTree( FILTER.valueOf( currentForm.comboBox1.getSelectedItem().toString() ) );

    }

    private void addFoldersToTree( FILTER filter )
    {
        List<TestFolder> folders = Source.getSource().getAllTestsHierarchy(currentForm.projectPrefix.getSelectedItem().toString(), filter);

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

                    if ( parent.toString().startsWith("Manual ") )
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

        currentForm.model.reload(currentForm.root);

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
                    else if ( node.getUserObject() instanceof TestFolder )
                       node.removeFromParent();

            }
        }
    }


    private void viewSteps()
    {

            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentForm.testsTree.getLastSelectedPathComponent();

            if ( selectedNode.getUserObject() instanceof TestEntity) {
                TestEntity t = (TestEntity) selectedNode.getUserObject();
                List<StepEntity> steps = Source.getSource().getStepsList(t.getDataBaseId());

                String[][] data = new String[steps.size()][2];

                for ( int i = 0; i < steps.size(); i++ )
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder
                            .append( i )
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


               /* for ( StepEntity step : steps )
                {



                    if ( !verifySteps.isEmpty() ) {
                          stringBuilder.append( "----------------------------------------------------------------------------" );
                          stringBuilder.append("\n\t\tExpected:");
                    }
                    for ( String verify : verifySteps )
                          stringBuilder.append("\n\t").append(verify);

                    stringBuilder.append("\n");
                    stringBuilder.append( "----------------------------------------------------------------------------" );
                }*/
            //    Messages.showMessageDialog( project, "Test: " + t.getId() + "\nSteps:\n" + stringBuilder.toString(), "Test Steps", new ImageIcon());

                ViewTestDialog viewTestDialog = new ViewTestDialog( project, data, t.getPreconditions() );
                viewTestDialog.show();
                if ( viewTestDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE )
                     addTestsToClass();
            }

    }

    private void initSettingsPopup( boolean cancelDisabled )
    {
        initSettingsPopup( cancelDisabled, true );
    }

    private void initSettingsPopup( boolean cancelDisabled, boolean updateTree )
    {
        SettingsDialog n = new SettingsDialog( project, cancelDisabled );
        n.show();
        if ( n.getExitCode() == DialogWrapper.OK_EXIT_CODE ) {
            n.saveData();
            if ( updateTree ) {
                createEmptyTree();
                addFoldersToTree(FILTER.valueOf(currentForm.comboBox1.getSelectedItem().toString()));
            }
        }
    }

}
