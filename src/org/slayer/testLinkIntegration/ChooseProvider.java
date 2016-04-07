package org.slayer.testLinkIntegration;

import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slayer on 23.09.14.
 */
public class ChooseProvider implements ChooseByNameItemProvider {
    @NotNull
    @Override
    public List<String> filterNames(@NotNull ChooseByNameBase base, @NotNull String[] names, @NotNull String pattern) {

//        Object[] filteredIDs = base.getModel().getElementsByName( "", false, pattern );

        return Arrays.asList("asd", "asd2");
    }

    @Override
    public boolean filterElements(@NotNull ChooseByNameBase base, @NotNull String pattern, boolean everywhere, @NotNull ProgressIndicator cancelled, @NotNull Processor<Object> consumer) {


        Object[] filteredIDs = base.getModel().getElementsByName( "", everywhere, pattern );
        for ( Object id : filteredIDs )
              consumer.process( id );

        return true;
    }
}
