package org.slayer.testLinkIntegration.UI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by slayer on 3/2/16.
 */
@FunctionalInterface
public interface ActionFunc extends ActionListener{

    public void actionPerformed(ActionEvent e);
}
