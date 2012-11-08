package org.upc.edu.Behaviours;


import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import org.upc.edu.Agents.Environment;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 public class TimeBehaviour extends TickerBehaviour{

     Environment agent;
     int round;

        public TimeBehaviour(Environment a, long period)
        {
            super(a,period);
            this.agent = a;
            round =0;
        }

         public void onStart()
        {
             //System.out.println("TIME ON START \n");
        }

        public int onEnd()
        {
            //System.out.println("TIME ON END \n");
            return 1;
        }

        public void onTick()
        {
            round++;
            System.out.println("\nUPDATE= "+round);
            agent.broadcast("update");
        }

    }