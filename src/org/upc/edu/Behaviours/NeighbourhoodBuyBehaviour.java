/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Vector;
import org.upc.edu.Agents.Neighbourhood;

/**
 *
 * @author sergio.jurado
 */
public class NeighbourhoodBuyBehaviour extends CyclicBehaviour {

    Neighbourhood agent;
    MessageTemplate CPFResponseTemplate, RequestTemplate;
    double[][] dists;
    HashMap HAIDs;
    HashMap NAIDs;

    //vars for seller lookup
    double bestDistance;
    private int step = 0;
    int replies;
    ACLMessage request;
    AID bestSeller;
    AID buyerAID;
    ACLMessage bestProposal;

    public NeighbourhoodBuyBehaviour(Neighbourhood agent, HashMap HAIDs, double[][] dists, HashMap NAIDs) {

        super(agent);

        this.agent = agent;
        this.dists = dists;
        this.HAIDs = HAIDs;
        this.NAIDs = NAIDs;
        this.RequestTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchConversationId("BUY_REQUEST"));

        MessageTemplate proposeTemplate = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        MessageTemplate refuseTemplate = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
        MessageTemplate notUnderstoodTemplate = MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD);
        this.CPFResponseTemplate = MessageTemplate.or(notUnderstoodTemplate, MessageTemplate.or(proposeTemplate,refuseTemplate));

    }

    public void initVars(){

        replies = 0;
        bestDistance = 100000;
        bestSeller = null;
        bestProposal = null;
    }

    public void action() {
        
        switch (step) {

            case 0:
                //block until a message of type REQUEST is received
                initVars();
                //System.out.println(agent.getLocalName() + " waiting for a buy message REQUEST");
                waitForBuyRequest();
                break;

            case 1:
                // Sending the cfp to all houses
                ACLMessage mensajeCFP = createCFPmessage(request.getContent());
                agent.send(mensajeCFP);
                step = 2;
                break;

            case 2:
                // Receiving all proposals/refusals from seller houses
                selectBestProposal(agent.getNumHouses());
                break;
            case 3:
                //System.out.println(agent.getLocalName() + " CFP Finished!");
                informBuyer();
                step = 0; //Start again
                break;
        }
    }

    private void waitForBuyRequest() {

        request = agent.receive(RequestTemplate);
        if (request != null) {
//            System.out.println(agent.getLocalName() + " received a buy REQUEST from " + request.getSender().getLocalName()
//                    + " with content '" + request.getContent() + "'");
            buyerAID = request.getSender();
            step = 1;
        } else {
            block();
        }
    }

    private ACLMessage createCFPmessage(String content) {

        ACLMessage mensajeCFP = new ACLMessage(ACLMessage.CFP);
        mensajeCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        mensajeCFP.setContent(content);
        //mensajeCFP.setReplyByDate(new Date(System.currentTimeMillis() + 1000)); //set time left
        Vector<AID> recievers=agent.getHousesAID();
        for (int i=0; i <recievers.size();i++){
            //broadcast message
            mensajeCFP.addReceiver((AID)recievers.get(i));
        }
 
//        System.out.println(agent.getLocalName() + " starts CFP to look for local sellers");
        return mensajeCFP;
    }

    private void selectBestProposal(int numHouses) {
        // wait CFPduration receiving offers from sellers (responses to cfp messages)
        //numHouses -> max num of sellers -> max num of repliers to the cfp message

        int buyerID, sellerID;
        AID sellerAID;
        ACLMessage propose = agent.receive(CPFResponseTemplate);

        if (propose != null) {
            // Reply received
             sellerAID = propose.getSender();
             if (propose.getPerformative() == ACLMessage.PROPOSE) {
                // An offer received
                sellerID = (Integer) HAIDs.get(sellerAID.getName());
                buyerID = (Integer) HAIDs.get(buyerAID.getName());
                double distance = dists[buyerID][sellerID];
//                System.out.println(agent.getLocalName() + " received PROPOSE from seller " 
//                        + sellerAID.getLocalName() + " at distance: "+distance+")");

                if (distance < bestDistance) {
                    // This is the nearest seller at present
                    bestDistance = distance;
                    // We reject the latest best Seller and save the new one
                    if (bestSeller != null) {
                        createRejection(bestProposal);
                    }
                    bestSeller = sellerAID;
                    bestProposal = propose;

                } else {
                    createRejection(propose);
                }
            }
            else if (propose.getPerformative() == ACLMessage.REFUSE) { //refuse
//                System.out.println(agent.getLocalName() + " received REFUSE from seller: " + sellerAID.getLocalName());
            }
            else{ //not understood
//                System.out.println(agent.getLocalName() + " received NOT UNDERSTOOD from seller: " + sellerAID.getLocalName());

            }

            replies++;
            if (replies >=  numHouses) {
                // We received all replies
                if (bestSeller != null) {
                    ACLMessage response = bestProposal.createReply();
                    response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    System.out.println(agent.getLocalName() + " ACCEPTED propose from "
                            + bestSeller.getLocalName()+ " at distance: " + bestDistance);
                    agent.send(response);
                }
                step = 3; //inform buyer
            }
        } else {
            block();
        }
    }

    private void createRejection(ACLMessage propose) {
        //create a  REJECT_PROPOSAL reply for this seller propose

        AID sellerAID = propose.getSender();

//        System.out.println(agent.getLocalName() + " REJECTED propose from " + sellerAID.getLocalName());

        ACLMessage rejection = propose.createReply();
        rejection.setPerformative(ACLMessage.REJECT_PROPOSAL);
        agent.send(rejection);
    }

   private void createAcceptance(ACLMessage propose) {
        //create a  REJECT_PROPOSAL reply for this seller propose

        AID sellerAID = propose.getSender();

//        System.out.println(agent.getLocalName() + " ACCEPTED propose from " + sellerAID.getLocalName());

        ACLMessage rejection = propose.createReply();
        rejection.setPerformative(ACLMessage.REJECT_PROPOSAL);
        agent.send(rejection);
    }

    private void informBuyer() {

        //Reply BUYER with INFORM
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(buyerAID);

        if (bestSeller != null) {
//            System.out.println(agent.getLocalName() + " send the AID of seller " +
//                    bestSeller.getLocalName() + " to buyer " + buyerAID.getLocalName());
            //Send Seller AID
            reply.setContent(bestSeller.getLocalName());
            agent.setReqBuy(agent.getReqBuy() + 1);
        } else {
//            System.out.println(agent.getLocalName() + " send a message 'SELLER NOT FOUND' to buyer "
//                    + buyerAID.getLocalName());
            reply.setContent("seller NOT FOUND");
        }

        agent.send(reply);


    }



}
