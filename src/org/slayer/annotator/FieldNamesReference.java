package org.slayer.annotator;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by slayer on 03.11.14.
 */
public class FieldNamesReference extends PsiReferenceContributor {

    private static final ElementPattern<PsiElement> IN_METHOD_CALL =
            PlatformPatterns.psiElement().withParent(PsiLiteralExpression.class).withSuperParent(3, PsiMethodCallExpression.class);

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider( IN_METHOD_CALL, new FieldNamesReferenceProvider() );
    }


    public static class FieldNamesReferenceProvider extends PsiReferenceProvider
    {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new PsiReference[0];
        }
    }

}
