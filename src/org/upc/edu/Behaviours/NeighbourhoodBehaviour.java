package org.upc.edu.Behaviours;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//import jade.core.behaviours.OneShotBehaviour;
import Statistics.HouseInformation;
import Statistics.NeighbourHoodInformation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.upc.edu.Agents.Neighbourhood;

/**
 *
 * @author 
 */
public class NeighbourhoodBehaviour extends Behaviour {

    Neighbourhood agent;
    ArrayList neighbourhoodProximity;

    //vars for seller lookup
    int idHouse = 0;    //ID que va incrementandose para cada House
    int numHouses;      //Numero de casas existentes
    int step = 0;       //Paso en el que nos encontramos
    String[]AIDNs = new String[0];     //AID de Neighbourhood =>

    public NeighbourhoodBehaviour(Neighbourhood agent, int _numHouses) {
        this.agent = agent;
        numHouses = _numHouses;
    }

    public void action() {

        switch (step) {

            case 0:
                waitDistancesMatrix();
                break;

            case 1:
                 //reply with a wellcome message to every new house
                 welcomeMessages();
                  break;
                  
            case 2:
                 MessageTemplate updateTemplate = MessageTemplate.MatchContent("update");
                 ACLMessage message = agent.receive(updateTemplate);
                if (message!=null) {

                    sendStatistics();
                }
                else block();
               break;

        }
    }

    private void waitDistancesMatrix() {

         HashMap HAIDs = new HashMap();     //Clave-valor ID-AID
         HashMap NAIDs = new HashMap();     //Clave-valor ID-AID

         double[][] dists = null;               //Distancias entre Houses
         double[] distsNeighbourhood = null;    //Distancias entre Neighbourhoods

        MessageTemplate template1  = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate template2 = MessageTemplate.MatchConversationId("distancesTable");
        MessageTemplate template = MessageTemplate.and(template1, template2);
        
        ACLMessage message = agent.receive(template);
        if (message != null) {
                try {
                    Object[] distancesMap =  (Object[]) message.getContentObject();
                    dists = (double[][])distancesMap[0];
                    HAIDs = (HashMap)distancesMap[1];
                    distsNeighbourhood = (double[])distancesMap[2];
                    NAIDs = (HashMap)distancesMap[3];
                    AIDNs = (String[])distancesMap[4];
                    neighbourhoodProximity = computeRelativeDistances(distsNeighbourhood);

                } catch (UnreadableException ex) {
                    Logger.getLogger(NeighbourhoodBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }
                step = 1; //wait wellcome messages
                agent.addBehaviour(new NeighbourhoodBuyBehaviour(agent, HAIDs, dists, NAIDs));
                agent.addBehaviour(new NeighbourhoodBatteryGetResponderBehaviour(agent));
                agent.addBehaviour(new NeighbourhoodBatteryStoreResponderBehaviour(agent));
        }
    }




    public void welcomeMessages() {
        
                ACLMessage message;
                ACLMessage response = new ACLMessage(ACLMessage.INFORM);

                MessageTemplate template = MessageTemplate.MatchConversationId("house-welcome");

                message = agent.receive(template);
                if (message != null) {
                    idHouse++;
                    response.addReceiver(message.getSender());

                    //Afegim l'AID de la casa a la llista d'agents
                    Vector<AID> houses=agent.getHousesAID();
                    houses.add(message.getSender());
                    agent.setHousesAID(houses);
                    
                    try {
                        response.setContentObject(neighbourhoodProximity);
                    } catch (IOException ex) {
                        Logger.getLogger(NeighbourhoodBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    agent.send(response);
                    // System.out.println(agent.getLocalName() + ": Numero de replies de Houses = " + idHouse);

                    // Si hemos recibido la bienvenida de todas las House pasamos a step 2
                    if (idHouse >= numHouses) {
                        System.out.println(agent.getLocalName() + " received 'welcome messages' of all houses");
                        step = 2; //waiting for update to send statistics
                    }
                }
    }

    public boolean done(){
        return false;

    }

   private ArrayList computeRelativeDistances(double[] dists) {


        ArrayList list = new ArrayList();
        double last = 0;
        int indMinor = 0;
        AID AIDminor  = new AID();
        AID aid  = new AID();

        double minor;
        int numneighbourhoods =  dists.length;
        
        for (int j = 0; j < numneighbourhoods; j++) {

            minor = Double.MAX_VALUE;
            for (int i = 0; i <  numneighbourhoods; i++) {

                aid = new AID(AIDNs[i], AID.ISGUID);
                if (dists[i]<=minor && !list.contains(aid)) {
                    minor = dists[i];
                    AIDminor = aid;
                }
            }
            list.add(AIDminor);
        }
        return list;
    }

    private void sendStatistics() {
        try {
            
            NeighbourHoodInformation neighInfo = new NeighbourHoodInformation
                    (agent.getLocalName(),
                    agent.getStorageLevel(),
                    agent.getReqBuy(),
                    agent.getReqGetBattery(),
                    agent.getReqStoreBattery(),
                    agent.getReqBuyCentral(),
                    agent.getReqSellCentral());
                           
            ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
            
            mensaje.addReceiver(new AID("E",AID.ISLOCALNAME));
            mensaje.setContentObject(neighInfo);
            mensaje.setConversationId("STATISTICS_NEIGHBOURHOOD");
            
            agent.send(mensaje);
            System.out.println(agent.getLocalName()+ " sent statistics to Environment");
        } catch (IOException ex) {
            jade.util.Logger.getLogger(HouseBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
