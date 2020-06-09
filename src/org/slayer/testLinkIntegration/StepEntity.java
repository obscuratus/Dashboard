package org.slayer.testLinkIntegration;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slayer on 23.09.14.
 */
public class StepEntity {

    String step;
    List<String> verifySteps;
    int order;

    public StepEntity( String step )
    {
        this.step = StringEscapeUtils.escapeJava( step );
        this.verifySteps = new ArrayList<String>();
    }

    public void addVerify( String verify )
    {
        verify = StringEscapeUtils.escapeJava( verify );
        verifySteps.add( verify );
    }

    public String toString()
    {
        return step.trim() + " \n";
    }

    public List<String> getVerifySteps()
    {
        return verifySteps;
    }

}
