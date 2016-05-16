package org.slayer.testLinkIntegration.UI;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.ActionButton;

import java.awt.*;

/**
 * Created by slayer on 4/6/16.
 */
public class NextTestButton extends ActionButton {

    public NextTestButton() {
        this(new NextTestButton.Action(), new Presentation("Next test"), "Refresh", new Dimension(24, 24));
    }

    private NextTestButton(AnAction anAction, Presentation presentation, String s, Dimension dimension) {
        super(anAction, presentation, s, dimension);
        presentation.setIcon( AllIcons.Actions.NextOccurence );
        presentation.setEnabledAndVisible(true);

    }

    private static class Action extends AnAction
    {

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {

        }
    }

}
