package org.slayer.annotator;

import com.intellij.codeInsight.ExpectedTypeInfo;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.JavaSmartCompletionContributor;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by slayer on 30.10.14.
 */
public class AccountSuggestor extends CompletionContributor {

    private static final ElementPattern<PsiElement> IN_ANNOTATION_INITIALIZER =
            PlatformPatterns.psiElement().withParent(PsiLiteralExpression.class).withSuperParent(2, PsiNameValuePair.class).withSuperParent(3, PsiAnnotationParameterList.class).withSuperParent(4, PsiAnnotation.class);

    private static final ElementPattern<PsiElement> IN_ANNOTATION_ARRAY_INITIALIZER =
            PlatformPatterns.psiElement().withParent(PsiLiteralExpression.class).withSuperParent(2, PsiArrayInitializerMemberValue.class).withSuperParent(3, PsiNameValuePair.class).withSuperParent(4, PsiAnnotationParameterList.class).withSuperParent(5, PsiAnnotation.class);

    @Override
    public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {

        PsiElement pos = parameters.getPosition();
        PsiNameValuePair pair = null;
        PsiAnnotationMemberValue value = null;

        if ( IN_ANNOTATION_INITIALIZER.accepts(pos) ) {
              pair = (PsiNameValuePair)pos.getParent().getParent();
              value = pair.getValue();
        }

        if ( IN_ANNOTATION_ARRAY_INITIALIZER.accepts(pos) ) {
            pair = (PsiNameValuePair) pos.getParent().getParent().getParent();
            PsiAnnotationMemberValue[] values = ((PsiArrayInitializerMemberValue) pos.getParent().getParent()).getInitializers();

            for ( PsiAnnotationMemberValue val : values )
                  if ( val.getText().replace("\"","").endsWith("IntellijIdeaRulezzz ") )
                       value = val;
        }

        if (!(value instanceof PsiExpression)) return;
        PsiReference ref = pair.getReference();
        if (ref == null) return;
        PsiMethod method = (PsiMethod)ref.resolve();
        if (method == null) return;
        if ( method.getName().equals("accounts") )
        {
            List<String> accountsForSuggestion = AccountListProvider.getAccountStartedWith( value.getText().replace("IntellijIdeaRulezzz ","").replace("\"","").toLowerCase() );

            for ( String account : accountsForSuggestion )
                result.caseInsensitive().addElement(JavaSmartCompletionContributor.decorate(LookupElementBuilder.create(value, account), Arrays.asList(ExpectedTypeInfo.EMPTY_ARRAY)));
        }

    }
}
