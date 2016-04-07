package org.slayer.testLinkIntegration;

import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slayer on 23.09.14.
 */
public class TestCaseModel implements ChooseByNameModel {

    List<TestEntity> testEntities = new ArrayList<TestEntity>();

    @Override
    public String getPromptText() {
        return "Select test case id:" ;
    }

    @Override
    public String getNotInMessage() {
        return "asd";
    }

    @Override
    public String getNotFoundMessage() {
        return "Test Case not found";
    }

    @Nullable
    @Override
    public String getCheckBoxName() {
        return "Show only not automated tests";
    }

    @Override
    public char getCheckBoxMnemonic() {
        return 0;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return true;
    }

    @Override
    public void saveInitialCheckBoxState( boolean state ) {

    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new JBList.StripedListCellRenderer();
    }

    @NotNull
    @Override
    public String[] getNames(boolean checkBoxState) {
        return new String[]{"asd"};
    }

    @NotNull
    @Override
    public Object[] getElementsByName(String name, boolean checkBoxState, String pattern) {

        testEntities = Source.getSource().getTestList( pattern, checkBoxState );
        List<TestEntity> filteredIds = new ArrayList<TestEntity>();
        for ( TestEntity test : testEntities )
              if ( test.getId().contains( pattern/*.split("-")[1]*/ ) )
                   filteredIds.add( test );

        Object[] tests = filteredIds.toArray();
        Arrays.sort( tests );
        return tests;
    }

    @Nullable
    @Override
    public String getElementName(Object element) {
        return element.toString();
    }

    @NotNull
    @Override
    public String[] getSeparators() {
        return new String[0];
    }

    @Nullable
    @Override
    public String getFullName(Object element) {
        return "full name";
    }

    @Nullable
    @Override
    public String getHelpId() {
        return "helpid";
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }

    @Override
    public boolean useMiddleMatching() {
        return true;
    }
}
