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
public class SettingsButton extends ActionButton {

        public SettingsButton() {
            this(new SettingsButton.Action(), new Presentation("Settings"), "Refresh", new Dimension(24, 24));
        }

        public SettingsButton(AnAction anAction, Presentation presentation, String s, Dimension dimension) {
            super(anAction, presentation, s, dimension);
            presentation.setIcon( AllIcons.General.Settings );
            presentation.setEnabledAndVisible(true);

        }

        private static class Action extends AnAction
        {

            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {

            }
        }


}
