package org.slayer.testLinkIntegration.UI;

import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;

/**
 * Created by slayer on 4/6/16.
 */
public class SearchField extends SearchTextField {

    public boolean criteriaChanged = false;
    public final JBColor defaultColor;

    public SearchField()
    {
        super( true );
        defaultColor = new JBColor( this::getForeground );
    }

    public void restoreDefaultColor()
    {
        getTextEditor().setForeground( defaultColor );
    }

    public void markAsInvalid() {
        getTextEditor().setForeground( JBColor.RED );
    }
}
