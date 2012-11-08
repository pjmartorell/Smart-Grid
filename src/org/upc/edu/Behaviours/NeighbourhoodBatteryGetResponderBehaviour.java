/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.edu.Behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.upc.edu.Agents.Neighbourhood;

/**
 *
 * @author ferran.obiol
 */
public class NeighbourhoodBatteryGetResponderBehaviour extends CyclicBehaviour {

    Neighbourhood agent;

    public NeighbourhoodBatteryGetResponderBehaviour(Neighbourhood agent) {

        super(agent);
        this.agent=agent;

    }

    public void action() {
        
            MessageTemplate template = MessageTemplate.MatchConversationId("BATTERY_GET_REQUEST");

            ACLMessage request = agent.receive(template);
            if (request != null) {
                System.out.println(agent.getLocalName() + " received a BATTERY_GET_REQUEST from "+ request.getSender().getLocalName());

                double price = 0;

                //agafem de la bateria l'energia demanada
                double energyRequested = Double.valueOf(request.getContent());
                double energyNotAllocated = agent.useFromBattery(energyRequested);
                
                 //Si no en tenim prou
                ACLMessage reply = request.createReply();
                if (energyNotAllocated>0) {
                    //Send how many energy has not been allocated and how much has cost the allocated one.
                    double energyAllocated = energyRequested - energyNotAllocated;
                    price = agent.getPricePerKWH() * energyAllocated;
                    reply.setContent(String.valueOf(energyNotAllocated)+  " " +String.valueOf(price));
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(request.getSender().getLocalName() +"sent "+energyAllocated +" units of energy for price: "+price + "--- still needs "+energyNotAllocated+ "units");
                    agent.setReqBuyCentral(agent.getReqBuyCentral() + 1);

                    if(energyNotAllocated != energyRequested){
                        agent.setReqGetBattery(agent.getReqGetBattery() + 1);
                    }
                }
                else{
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    //send price
                    price = agent.getPricePerKWH() * energyRequested;
                    reply.setContent(String.valueOf(price));
                    System.out.println(agent.getLocalName() + " sent the requested energy, price: "+price);
                    agent.setReqGetBattery(agent.getReqGetBattery() + 1);

                }
                agent.send(reply);

                //get credits used to store at the battery
                agent.addCredits(price);

            } else {
                block();
            }
    }
}
