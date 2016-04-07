package org.slayer.testLinkIntegration.UI;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * Created by slayer on 3/2/16.
 */
@FunctionalInterface
public interface TreeAction extends TreeSelectionListener {

    void valueChanged(TreeSelectionEvent e);
}
