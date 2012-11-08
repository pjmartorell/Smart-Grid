package org.upc.edu.Agents;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.upc.edu.Behaviours.CentralBehaviour;

/**
 *
 * @author 
 */
public class Central extends MyAgent {


    @Override
    protected void setup() {

        super.setup();

        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            double pricePerKWH = ((Number)args[0]).doubleValue();
            this.addBehaviour(new CentralBehaviour(this, pricePerKWH));
            
        } else {
            // Make the agent terminate immediately
            System.out.println(this.getLocalName() + "NOT CREATED, SPECIFY ARGS!");
            doDelete();
        }

    }
}
