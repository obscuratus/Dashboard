package org.slayer.testLinkIntegration.UI;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.ActionButton;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.function.Consumer;

/**
 * Created by slayer on 4/8/16.
 */
public class FilterButton extends ActionButton {


    private static EnumMap<FILTER, ImageIcon> icons = new EnumMap<>( FILTER.class );
    private Presentation currentPresentation;
    private static Action action = new FilterButton.Action();

    static {
        ClassLoader classLoader = FilterButton.class.getClassLoader();
        icons.put( FILTER.Automated, new ImageIcon( classLoader.getResource("resources/favicon__1_.png") ) );
        icons.put( FILTER.Manual, new ImageIcon( classLoader.getResource("resources/favicon__2_.png") ) );
        icons.put( FILTER.All, new ImageIcon( classLoader.getResource("resources/favicon__3_.png") ) );
    }

    public FilterButton() {
        this(action, new Presentation("Expand all"), "Refresh", new Dimension(24, 24));
    }

    private FilterButton(AnAction anAction, Presentation presentation, String s, Dimension dimension) {
        super(anAction, presentation, s, dimension);
        this.currentPresentation = presentation;
        currentPresentation.setIcon( icons.get( action.currentState ) );
        currentPresentation.setEnabledAndVisible(true);
        action.setChangeCallback( this::changeIcon );
    }


    private void changeIcon( FILTER newFilter )
    {
        currentPresentation.setIcon( icons.get( newFilter ));
    }

    public FILTER getFilter()
    {
        return action.currentState;
    }


    private static class Action extends AnAction {

        private FILTER currentState = FILTER.All;
        private Consumer<FILTER> consumer;

        void setChangeCallback(Consumer<FILTER> consumer)
        {
            this.consumer = consumer;
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            onChange( anActionEvent );
        }

        private void onChange(AnActionEvent e)
        {
            int currentStateOrdinal = currentState.ordinal() + 1;
            FILTER[] filters = FILTER.values();
            if ( currentStateOrdinal > filters.length - 1 )
                currentStateOrdinal = 0;

            currentState = FILTER.values()[ currentStateOrdinal ];
            consumer.accept( currentState );
//            setIcon( icons.get( currentState ) );

        }


    }
}
