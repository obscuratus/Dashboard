package org.slayer.testLinkIntegration;

import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

/**
 * Created by slayer on 30.10.14.
 */
public class TestLinkIntegrationEditorPopup extends AnAction {
    public TestLinkIntegrationEditorPopup()
    {
        super("Generate _Test Log");
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);

            PsiFile psiFile = anActionEvent.getData(PlatformDataKeys.PSI_FILE);
            if ( psiFile != null )
            {
                PsiClass psiClass = ((PsiJavaFile) psiFile).getClasses()[0];
                ClassResolver.setCurrentClass( psiClass );
            }

        String predefinedPrefix = SettingsStorage.loadData( "projectPrefix" );
        final ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project, new TestCaseModel(), new ChooseProvider(), predefinedPrefix);
        popup.invoke(new ChooseByNamePopupComponent.Callback() {
            @Override
            public void elementChosen(Object element) {

                popup.close( false );
                ClassResolver.resolveClass( project, (TestEntity) element );

            }
        }, ModalityState.defaultModalityState(), false);


    }
}
