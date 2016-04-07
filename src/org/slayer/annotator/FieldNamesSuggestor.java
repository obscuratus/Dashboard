package org.slayer.annotator;

import com.intellij.codeInsight.ExpectedTypeInfo;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.JavaSmartCompletionContributor;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.PackageScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by slayer on 03.11.14.
 */
public class FieldNamesSuggestor extends CompletionContributor{

    private static final ElementPattern<PsiElement> IN_METHOD_CALL =
            PlatformPatterns.psiElement().withParent(PsiLiteralExpression.class).withSuperParent(3, PsiMethodCallExpression.class);

    public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {

        PsiElement pos = parameters.getPosition();

        if ( IN_METHOD_CALL.accepts( pos ) )
        {
            PsiElement parent = pos.getParent().getParent().getParent();
            PsiJavaFile currentFile = (PsiJavaFile) pos.getContainingFile();

            String methodName = ((PsiMethodCallExpression) parent).getMethodExpression().getText();


            PsiExpression argument = ((PsiMethodCallExpression) parent).getArgumentList().getExpressions()[0];
            if ( methodName.equals("field") || methodName.equals("button") ||
                    methodName.endsWith(".field") )
            {
                String packageName = "";
                String currentClassName = "";
                PsiExpression calledFrom = ((PsiMethodCallExpression) parent).getMethodExpression().getQualifierExpression();

                if ( calledFrom != null ) {
                    PsiType type = calledFrom.getType();
                    if ( type != null ) {
                        PsiClass currentClass = ((PsiClassReferenceType) type).resolve();
                        assert currentClass != null;
                        packageName = ((PsiJavaFile)currentClass.getContainingFile()).getPackageName();
                        currentClassName = currentClass.getName();
                    }
                }
                else {
                    currentClassName = currentFile.getClasses()[0].getName();
                    packageName = currentFile.getPackageName();
                }


                PsiPackage psiPackage = JavaPsiFacade.getInstance( pos.getProject() ).findPackage( packageName + ".data.def");

                List<String> fieldNames = new ArrayList<String>();

                Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(FilenameIndex.NAME, currentClassName + ".def.csv", PackageScope.packageScope( psiPackage, false ));
                try {
                    CSVParser parser = CSVParser.parse(new URL(files.iterator().next().getUrl()), Charset.defaultCharset(), CSVFormat.DEFAULT);

                    for (CSVRecord record : parser) {
                        if ( !record.get(0).equals("Name") && !fieldNames.contains( record.get(0) ) )
                            fieldNames.add( record.get(0) );
                    }

                }
                catch ( Exception ex )
                {
                /**/
                }

                for (String fieldName : fieldNames)
                    result.caseInsensitive().addElement(JavaSmartCompletionContributor.decorate(LookupElementBuilder.create(argument, fieldName), Arrays.asList(ExpectedTypeInfo.EMPTY_ARRAY)));
            }

        }

    }
}
