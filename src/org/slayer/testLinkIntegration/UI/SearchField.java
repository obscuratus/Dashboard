package org.slayer.testLinkIntegration.UI;

import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;

/**
 * Created by slayer on 4/6/16.
 */
public class SearchField extends SearchTextField {

    private boolean criteriaChanged = false;
    private final JBColor defaultColor;

    public SearchField()
    {
        super( true );
        defaultColor = new JBColor( this::getForeground );
    }

    void restoreDefaultColor()
    {
        getTextEditor().setForeground( defaultColor );
    }

    void markAsInvalid() {
        getTextEditor().setForeground( JBColor.RED );
    }

    public boolean isCriteriaChanged() {
        return criteriaChanged;
    }

    public void setCriteriaChanged(boolean criteriaChanged) {
        this.criteriaChanged = criteriaChanged;
    }
}
