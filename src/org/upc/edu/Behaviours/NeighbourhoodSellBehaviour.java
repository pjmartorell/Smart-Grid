/*
 *acts as initiator in the contract net protocol implemented to find a seller
 */
package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.upc.edu.Agents.Neighbourhood;

/**
 *
 * @author sergio.jurado
 */
public class NeighbourhoodSellBehaviour extends ContractNetInitiator {
//public class NeighbourhoodSellBehaviour extends CyclicBehaviour{

    Neighbourhood agent;
    int nResponders;
    Vector cfpMessages;

    public NeighbourhoodSellBehaviour(Neighbourhood a, ACLMessage cfpMessage) {

        super(a, cfpMessage);

        //super(a);
        this.agent = a;
        this.nResponders = 2;

        cfpMessages = new Vector();
        cfpMessages.addElement(cfpMessage); //same cfp message for all agents
    }

    protected Vector prepareCfps() {
        System.out.println("CONSULTANDO VECTOR");

        return cfpMessages;

    }

    @Override
    public int onEnd() {
        System.out.println("eEND OF NEIGH SELL BEHAVIOUR");

        /*	if(finnish == false) {
        addBehaviour(new InitiatorBehaviour(agent, new ACLMessage(ACLMessage.CFP)));
        }
         */
        return super.onEnd();


    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector v) {
        System.out.println("Agent '" + propose.getSender().getName() + "' proposed '" + propose.getContent() + "'");
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        //Activated when reply date arrives (specified in field replyDate of the cfp message)
        System.out.println("processing all cfp responses -- time: " + System.currentTimeMillis());

        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        }
        // Evaluate proposals.
        int bestProposal = -1;
        AID bestProposer = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();

        //create a REJECT PROPOSAL for all proposes received
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);
                int proposal = Integer.parseInt(msg.getContent());
                if (proposal > bestProposal) {
                    bestProposal = proposal;
                    bestProposer = msg.getSender();
                    accept = reply;
                }
            }
        }

        //if there have been responses to the cfp:
        // modify the one created for the best proposal to be ACCEPT_PROPOSAL
        if (accept != null) {
            System.out.println("Accepting proposal '" + bestProposal + "' from responder '" + bestProposer.getName() + "'");
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }

        //send all responses
        Iterator iterator = acceptances.iterator();
        while (iterator.hasNext()) {
            agent.send((ACLMessage) iterator.next());
        }

    }

    @Override
    protected void handleInform(ACLMessage inform) {
        // System.out.println("Agent '"+inform.getSender().getName()+"' successfully performed the requested action");

        System.out.println("Agent '" + inform.getSender().getName() + "' confirms that has been selected as seller");

    }

    /*
    
    public void action() {

    System.out.println(agent.getLocalName()+" in NEIGHBOURGHOOD BUY behaviour");

    //block until a message of type REQUEST
    //  ACLMessage request = waitForBuyRequests();


    MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    System.out.println(agent.getLocalName()+" waiting for a message REQUEST");
    ACLMessage request = agent.blockingReceive(template);

    //look for local sellers during CFPduration ms
    System.out.println(agent.getLocalName()+" looking for local sellers, time left: "+20000 + " ms");
    //  this.addBehaviour();
    callForProposals(request.getContent(), 20000);

    //send response to buyer

    //inform that there are no sellers
    System.out.println(agent.getLocalName()+"-- no sellers found for "+request.getSender());

    // Creación del objeto ACLMessage
    ACLMessage response = new ACLMessage(ACLMessage.INFORM);
    //rEllenar los campos necesarios del mensaje
    response.setSender(agent.getAID());
    response.addReceiver(request.getSender());
    response.setContent("No sellers");

    }

    void callForProposals(String content, int timeLeft) {

    ACLMessage mensajeCFP = new ACLMessage(ACLMessage.CFP);
    agent.setBroadcastReceiver(mensajeCFP); //broadcast message
    mensajeCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
    mensajeCFP.setContent(content);
    mensajeCFP.setReplyByDate(new Date(System.currentTimeMillis() + timeLeft)); //set  time left ()
    agent.send(mensajeCFP);
    }

    private ACLMessage waitForBuyRequests(){

    MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    System.out.println(agent.getLocalName()+" waiting for a message REQUEST");
    ACLMessage message = agent.blockingReceive(template);
    return message;
    }


     */
}
