package org.slayer.testLinkIntegration;

import com.intellij.ide.projectView.impl.nodes.PackageUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.containers.ContainerUtil;

import java.util.*;

/**
 * Created by slayer on 10/23/15.
 */
public class MethodRunnerImpl {

    private com.intellij.psi.PsiClass finalClazzCur;
    private com.intellij.psi.PsiMethod psiMethod;
    private com.intellij.psi.PsiElementFactory psiElementFactory;
    private String testId;
    private String testTitle;
    private String testLabels;
    private String preconditions;
    private boolean classExists;
    private Project project;

    public void setup(PsiClass finalClazzCur,
                            PsiMethod psiMethod,
                            PsiElementFactory psiElementFactory,
                            String testId, String testTitle, String testLabels,
                            String preconditions, boolean classExists, Project project) {
        this.finalClazzCur = finalClazzCur;
        this.psiMethod = psiMethod;
        this.psiElementFactory = psiElementFactory;
        this.testId = testId;
        this.testTitle = testTitle;
        this.testLabels = testLabels;
        this.preconditions = preconditions;
        this.classExists = classExists;
        this.project = project;
    }

    public void startRunner()
    {
        {

            PsiMethod[] existingMethods = finalClazzCur.getMethods();

//                Collection<PsiClass> classes = AllClassesSearch.search( GlobalSearchScope.projectScope( project ), project ).findAll();
            PsiMethod method = null;
            boolean exists = false;

//                for ( PsiClass clazz : classes ) {
//                    PsiMethod[] existingMethods = clazz.getMethods();


            boolean methodMatch = false;
            for (PsiMethod existingMethod : existingMethods) {

                for (PsiElement p : existingMethod.getModifierList().getChildren())
                    if (p instanceof PsiAnnotation)
                        if (((PsiAnnotation) p).getQualifiedName().contains("Manual"))
                            for (PsiNameValuePair keyValue : ((PsiAnnotation) p).getParameterList().getAttributes())
                                if (keyValue.getName().equals("ids"))
                                    methodMatch = keyValue.getValue().getText().equals("\"" + testId + "\"");


//                    existingMethod.getName().equals( psiMethod.getName() );
                exists |= methodMatch;

                if (methodMatch)
                {
//                            foundClazz = clazz;
                    method = existingMethod;
                }
            }
//                }

            String commentBefore = "/* Title: " + testTitle + "\nPreconditions: " + preconditions + "\nLabels:" + testLabels + "*/";
            psiMethod.addBefore( psiElementFactory.createCommentFromText( commentBefore, null ), psiMethod.getFirstChild() );

            if ( exists ) {
                commentBefore = "/* FIXME Duplicated test";
                String commentAfter = "//*/";

                method.addBefore(psiElementFactory.createCommentFromText(commentBefore, null), method.getModifierList());
                method.addAfter(psiElementFactory.createCommentFromText(commentAfter, null), method.getLastChild());
                method.addAfter(psiMethod, method.getLastChild());
//                    Notifications.Bus.notify( new Notification( "Test Log Generator","Found test duplicate " + testId, "IDE will be navigated to class with duplicated test!", NotificationType.WARNING) );

//                    OpenSourceUtil.navigate( true, );

            }
            else
            {
                finalClazzCur.add(psiMethod);

            }



            if (!classExists)
            {

                String testClassName = "modano.definitions.base.ModanoTest";
                String savedTestClass = SettingsStorage.loadData( "testClassName" );
                String predefinedClass = savedTestClass == null ? testClassName : savedTestClass;
                testClassName = Messages.showInputDialog(project,
                        "Enter tests parent class name with package",
                        "Tests Parent Class",
                        Messages.getQuestionIcon(),
                        predefinedClass,
                        new NonEmptyInputValidator());

                savedTestClass = testClassName == null ? "" : testClassName;
                SettingsStorage.storeData("testClassName", savedTestClass);

                final PsiClass testClass = findClass( testClassName , project );


                if ( testClass == null )
                    Messages.showWarningDialog("Cannot find test class: " + testClassName, "Check Project Path!");
                else
                {
                    PsiClassType psiType = psiElementFactory.createType(testClass);
                    final PsiJavaCodeReferenceElement referenceElement = psiElementFactory.createReferenceElementByType(psiType);
                    finalClazzCur.getExtendsList().add(referenceElement);
                }
            }

            JavaCodeStyleManager.getInstance(project).shortenClassReferences( finalClazzCur );

        };
    }

    private PsiClass findClass(String classNameWithPackage, Project project)
    {
        String[] classNameParts = classNameWithPackage.split("\\.");
        String className = classNameParts[ classNameParts.length - 1 ];
        String[] packageParts = Arrays.copyOfRange(classNameParts, 0, classNameParts.length - 1);

        List<PsiPackage> allPackages = getPackages(project);
        for ( int i = 0; i < packageParts.length; i++ )
        {
            allPackages = searchPackage( allPackages, packageParts[i], i == packageParts.length - 1 );
        }

        if ( allPackages == null )
            return null;

        for ( PsiClass psiClass : allPackages.get( 0 ).getClasses() )
            if ( psiClass.getName().equals( className ) )
                return psiClass;

        return null;
    }

    private static List<PsiPackage> getPackages( Project myProject ) {

        final List<VirtualFile> sourceRoots = new ArrayList<VirtualFile>();
        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(myProject);
        ContainerUtil.addAll(sourceRoots, projectRootManager.getContentSourceRoots());

        final PsiManager psiManager = PsiManager.getInstance(myProject);
        final Set<PsiPackage> topLevelPackages = new HashSet<PsiPackage>();

        for (final VirtualFile root : sourceRoots) {
            final PsiDirectory directory = psiManager.findDirectory(root);
            if (directory == null) {
                continue;
            }
            final PsiPackage directoryPackage = JavaDirectoryService.getInstance().getPackage(directory);
            if (directoryPackage == null || PackageUtil.isPackageDefault(directoryPackage)) {
                // add subpackages
                final PsiDirectory[] subdirectories = directory.getSubdirectories();
                for (PsiDirectory subdirectory : subdirectories) {
                    final PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(subdirectory);
                    if (aPackage != null && !PackageUtil.isPackageDefault(aPackage)) {
                        topLevelPackages.add(aPackage);
                    }
                }
            } else {
                // this is the case when a source root has package prefix assigned
                topLevelPackages.add(directoryPackage);
            }
        }

        return new ArrayList<PsiPackage>(topLevelPackages);
    }



    private static List<PsiPackage> searchPackage( List<PsiPackage> packages, String packageName, boolean lastPart )
    {
        if ( packages != null )
            for ( PsiPackage psiPackage : packages )
                if ( psiPackage.getName().equals( packageName ))
                    return lastPart ? Arrays.asList( psiPackage ) : Arrays.asList( psiPackage.getSubPackages() );

        return null;
    }

}
