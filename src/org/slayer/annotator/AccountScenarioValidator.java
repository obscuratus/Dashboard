package org.slayer.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.java.PsiNameValuePairImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slayer on 28.10.14.
 */
public class AccountScenarioValidator implements Annotator {



    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {



        if ( element instanceof PsiNameValuePairImpl )
        {
            TextRange range = new TextRange(element.getTextRange().getStartOffset(),
                    element.getTextRange().getEndOffset());

            String name = ((PsiNameValuePairImpl) element).getName();
            PsiAnnotationMemberValue value = ((PsiNameValuePairImpl) element).getValue();
            if ( name != null && value != null )
            {
//                List<TestEntity> testEntities = Source.getSource().getTestList( value.getText().replace("\"","") , false );
//                if ( name.equals("ids") ) {
//                    if ( testEntities.isEmpty() )
//                         holder.createErrorAnnotation(range, "Wrong test id");
//                }

                List<String> values = new ArrayList<String>();
                if ( name.equals("accounts") )
                    if ( value.getText().startsWith("{") )
                         values = Arrays.asList( value.getText().replace("{","").replace("}","").split(",") ) ;
                    else
                         values.add( value.getText() );

                for ( String ac : values )
                    if ( !AccountListProvider.getAccountsList().contains( ac.replace("\"","").trim().toLowerCase() ) )
                          holder.createWarningAnnotation(range, "Account scenario " + ac + " is not found in pool");
                }
            }
        }







}
