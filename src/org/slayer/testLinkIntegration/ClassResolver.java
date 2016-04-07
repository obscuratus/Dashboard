package org.slayer.testLinkIntegration;

import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;
import java.util.Objects;

/**
 * Created by slayer on 24.09.14.
 */
public class ClassResolver {
//    private static Logger logger = Logger.getGlobal();
    public static MethodRunnerImpl addMethodRunner = new MethodRunnerImpl();

    private static PsiClass psiCurrentClass;

    public static void setCurrentClass( PsiClass psiClass )
    {
        psiCurrentClass = psiClass;
    }

    public static void resolveClass( final Project project, final TestEntity testEntity )
    {
        PsiClass psiClass = null;
        PsiPackage psiPackage = null;
        String className = "";

        if ( psiCurrentClass == null ) {

            int exitCode = Messages.showYesNoDialog(project, "Create new class?", "Test Class Generator", "Create", "Use existing", Messages.getQuestionIcon());

            if (exitCode == Messages.OK) {
                psiPackage = showChooserDialog(project);

                if (psiPackage == null)
                    return;

                className = Messages.showInputDialog("Enter new class name", "Class Name", Messages.getQuestionIcon());
                if (className == null)
                    return;
            } else if (exitCode == Messages.NO) {
                TreeClassChooser treeClassChooser = TreeClassChooserFactory.getInstance(project).createWithInnerClassesScopeChooser("Select class", GlobalSearchScope.allScope(project), ClassFilter.ALL, null);
                treeClassChooser.showDialog();
                psiClass = treeClassChooser.getSelected();
                if (psiClass == null)
                    return;
            }


        }
        else
        {
            psiClass = psiCurrentClass;
            psiCurrentClass = null;
        }

        generateTestClass( project, testEntity, psiPackage, className, psiClass );
    }

    private static PsiStatement[] generateMethodBody( PsiElementFactory psiElementFactory, TestEntity testEntity ) {

        List<String> log = testEntity.getLog();
        PsiStatement[] psiStatements = new PsiStatement[ log.size() ];

        for ( int i = 0; i < log.size(); i++ ) {
            psiStatements[i] = psiElementFactory.createStatementFromText(log.get(i), null);
        }

        return psiStatements;
    }



    private static PsiPackage showChooserDialog( Project project )
    {
        PackageChooserDialog packageChooserDialog = new PackageChooserDialog("Select package", project);
        packageChooserDialog.setResizable( false );
        packageChooserDialog.show();

        if (packageChooserDialog.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
            Messages.showInfoMessage("Test log generation canceled", "Test Log Generator");
            return null;
        }

        return packageChooserDialog.getSelectedPackage();
    }

    private static void generateTestClass ( final Project project, TestEntity testEntity, PsiPackage psiPackage, String className, PsiClass clazzCur )
    {
        final String preconditions = testEntity.getPreconditions();
        final String testTitle = testEntity.getTitle();
        final String testLabels = testEntity.getLabels();
        final boolean classExists = clazzCur != null;

        String description = testEntity.getDescription().replaceAll("\\\\", " ").replaceAll("\"","\\\\\"");


        String methodName = WordUtils.capitalizeFully( description.replaceAll("[^\\w]", " ").replaceAll("\\d","") ).replace(" ", "");
        char firstChar = methodName.toCharArray()[0];
        methodName = Character.toLowerCase(firstChar) + methodName.substring(1);


        final String testId = testEntity.getId();

        if ( clazzCur == null ) {

            final PsiDirectory psiDir = psiPackage.getDirectories()[0];
            clazzCur = JavaDirectoryService.getInstance().createClass(psiDir, className);
        }

        clazzCur.navigate(true);

        final PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
        final PsiMethod psiMethod = psiElementFactory.createMethod(methodName, PsiType.VOID);
        psiMethod.getModifierList().setModifierProperty("public", true);
        PsiStatement[] methodBody = generateMethodBody( psiElementFactory, testEntity );


        PsiCodeBlock codeBlock = psiMethod.getBody();

        Objects.requireNonNull( codeBlock, () -> "PsiCodeBlock is null!" );
        for (PsiStatement m : methodBody)
            codeBlock.add(m);


        AllClassesSearch.search( GlobalSearchScope.projectScope( project ), project ).findAll();
        psiMethod.getModifierList().add(psiElementFactory.createAnnotationFromText( "@org.testng.annotations.Test( description = \"" + description + "\")" + "\n", null) );
        psiMethod.getModifierList().add(psiElementFactory.createAnnotationFromText( "@kernel.core.logger.annotation.Manual( ids = \"" + testId + "\", accounts = \"\"  )" + "\n", null) );

        addMethodRunner.setup( clazzCur, psiMethod, psiElementFactory, testId, testTitle, testLabels, preconditions, classExists, project );


        ApplicationManager.getApplication().invokeAndWait(
                () -> CommandProcessor
                        .getInstance()
                        .executeCommand(project,
                                () -> ApplicationManager.getApplication().runWriteAction(addMethodRunner::startRunner), "DiskWrite", null), ModalityState.NON_MODAL);



    }
}
