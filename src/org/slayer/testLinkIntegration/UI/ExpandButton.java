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
public class ExpandButton extends ActionButton {

        public ExpandButton() {
            this(new ExpandButton.Action(), new Presentation("Expand all"), "Refresh", new Dimension(24, 24));
        }

        public ExpandButton(AnAction anAction, Presentation presentation, String s, Dimension dimension) {
            super(anAction, presentation, s, dimension);
            presentation.setIcon( AllIcons.Actions.Expandall );
            presentation.setEnabledAndVisible(true);

        }

        private static class Action extends AnAction
        {

            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {

            }
        }

}
