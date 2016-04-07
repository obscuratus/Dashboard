package org.slayer.testLinkIntegration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slayer on 23.09.14.
 */
public class StepEntity {

    String step;
    List<String> verifySteps;

    public StepEntity( String step )
    {
        this.step = step;
        this.verifySteps = new ArrayList<String>();
    }

    public void addVerify( String verify )
    {
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
