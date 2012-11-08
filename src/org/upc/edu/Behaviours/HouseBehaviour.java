package org.upc.edu.Behaviours;

import Statistics.HouseInformation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.util.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.upc.edu.Agents.House;

/**
 *
 * @author
 */
public class HouseBehaviour extends CyclicBehaviour {

    AID centralId=null;
    AID neighbourHoodId;
    House agent;
    HouseBuyBehaviour buyBehaviour;
    HouseSellerBehaviour sellerBehaviour;
    ACLMessage CFPmessage;  //"template" of a cfp message
    MessageTemplate buyerTemplate, sellerTemplate, currentTemplate;
    ArrayList neighbourhoodProximity;   //aids of neighbourhood sorted by proximity with mine

    public HouseBehaviour(AID neighbourHoodId, House agent, ArrayList neighbourhoodProximity) {
        this.neighbourHoodId = neighbourHoodId;
        this.agent = agent;
        buyBehaviour = null;
        sellerBehaviour = null;
        this.neighbourhoodProximity = neighbourhoodProximity;

        //for each role of the house, define the type of messages that are going to be handled by this behaviour

        //buyer role:   UPDATE AND CFP MESSAGES
        MessageTemplate updateTemplate = MessageTemplate.MatchContent("update");
        MessageTemplate template1 = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        MessageTemplate template2 = MessageTemplate.MatchSender(neighbourHoodId);
        MessageTemplate CFPTemplate = MessageTemplate.and(template1, template2);
        buyerTemplate = MessageTemplate.or(updateTemplate, CFPTemplate);

        //seller role: ONLY UPDATE MESSAGES, cfp messages will be handled by housesellerbehaviour
        //this works if only one contractnet protocol at a time
        sellerTemplate = updateTemplate;

        //inicialmente solo procesa update messages que es el que le permite decidir que rol le corresponde
        currentTemplate = updateTemplate;

    }

    public void action() {

        ACLMessage message = agent.receive(currentTemplate);

        if (message != null) {

            if (message.getContent().equals("update")) {

                sendStatistics();

                
                //no more time to find seller/buyer - try battery or plant

                //remover current buy/sell behaviour (if any)
                removeCurrentRole();

                //actualitzar estat energia
                 agent.updateLevels();

                //actualitzar desired price del comprador y vendedor con los valores iniciales (porque puede haberlos modificado (durante la negociacion)

                //decide new role (if necessary)
                setCurrentRole();
            } else {//cfp from neighbourhood
                // I am a buyer (because template is buyerTemplate), I don't want to buy, send don't understood
//                System.out.println(agent.getLocalName() + " CFP received => sends NOT UNDERSTOOD");
                sendNotUnderstood(message);
            }
        } else {
            block();
        }
    }

    protected void sendNotUnderstood(ACLMessage cfp) {

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        agent.send(propose);
    }

    public void setBuyerTemplate() {
        //aixo ho fa un comprador o una casa sense rol (perque acaba de vendre o comprar)
        //processo missatges update i rebutjo (not understood) missatges cfp
        currentTemplate = buyerTemplate;
    }

    public void removeCurrentRole() {

        if(buyBehaviour != null){
                agent.removeBehaviour(buyBehaviour);
                buyBehaviour = null;
                if (agent.getUseBattery()) agent.useBattery();
        }
        else if (sellerBehaviour != null){
                agent.removeBehaviour(sellerBehaviour);
                sellerBehaviour = null;
                if (agent.getUseBattery()) agent.storeBattery();
        }
    }

    public void setCurrentRole() {

        double balance = agent.getProductionLevel() - agent.getConsumLevel();
        if (balance >= 0) {

            agent.setDesiredPrice(1.2 * agent.getReservedPrice());
            //balance is the energy excess
            sellerBehaviour = new HouseSellerBehaviour(neighbourHoodId, agent, balance);
            agent.addBehaviour(sellerBehaviour);

            System.out.println(agent.getLocalName() + " as Seller with energy balance(" + balance + ")");
            currentTemplate = sellerTemplate;

        } else if (balance < 0) {

            agent.setDesiredPrice(0.8 * agent.getReservedPrice());
            buyBehaviour = new HouseBuyBehaviour(neighbourHoodId, agent, Math.abs(balance),neighbourhoodProximity);
            agent.addBehaviour(buyBehaviour);

            currentTemplate = buyerTemplate;
            System.out.println(agent.getLocalName() + " as Buyer with energy balance(" + balance + ")");

        } else {
            System.out.println(agent.getLocalName() + " balance(0), no tinc cap comportament assignat!!!");
        }
    }

   private void sendStatistics() {
        try {

            HouseInformation houseInfo = new HouseInformation
                    (agent.getLocalName(),
                    agent.getNeighbourhoodAID().getLocalName(),
                    agent.getCredits(),
                    agent.getConsumLevel(),
                    agent.getProductionLevel(),
                    agent.getStorageLevel(),
                    agent.getTransactionType(),
                    agent.getAdversary(),
                    agent.getQtyTransaction(),
                    agent.getPriceTransaction(),
                    agent.getBatteryOperationType(),
                    agent.getQtyBattery(),
                    agent.getPriceBattery(),
                    agent.getCentralOperationType(),
                    agent.getQtyCentral(),
                    agent.getPriceCentral());
                           
            ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
            
            mensaje.addReceiver(new AID("E",AID.ISLOCALNAME));
            mensaje.setContentObject(houseInfo);
            mensaje.setConversationId("STATISTICS");
            
            agent.send(mensaje);
            System.out.println(agent.getLocalName()+ " sent statistics to Environment");
        } catch (IOException ex) {
            Logger.getLogger(HouseBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

   
}
