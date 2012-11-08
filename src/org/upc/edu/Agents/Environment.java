package org.upc.edu.Agents;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import View.SmartGridGui;
import jade.core.AID;
import jade.core.Profile;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.upc.edu.Behaviours.EnvironmentStatisticsBehaviour;
import org.upc.edu.Behaviours.TimeBehaviour;

/**
 *
 * @author 
 */
public class Environment extends MyAgent {

    AgentContainer ac;
    ArrayList neighbourHoods;
    HashMap HAIDs;   //Clave-valor ID-AID de Houses
    HashMap NAIDs;
    String[] AIDNs;
    int numHouses, numNeighbourhoods, numHousesNeighbourhood;
    SmartGridGui myGui;
    boolean updates = true;


    @Override
    protected void setup() {

        super.setup();

        // Create and show the GUI
	myGui = new SmartGridGui(this);
	myGui.showGui();

        String centralAID = "";

        //create MAIN CONTROLLER
        // get a JADE runtime
        Runtime rt = Runtime.instance();
        // create a default profile
        Profile p = new ProfileImpl();
        // create the Main-container
        ac = rt.createAgentContainer(p);

        //Create central
        try {
            //2n param = pricePerKWH
            centralAID = createCentral("CENTRAL", 30);
        } catch (StaleProxyException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }

//        int[][] pairH = {{0, 3}, {2, 1}, {2, 0}, {0, 2},
//            {31, 3}, {32, 1}, {32, 0}, {30, 2},
//            {0, 33}, {2, 31}, {2, 30}, {0, 32},
//            {30, 33}, {32, 31}, {32, 30}, {30, 32}};

        //Creació de coordenades
        int[][] pairN = {{0, 0}, {30, 0}, {0, 30}};
        int[] numHNeigh = {3, 3, 3};

        int[][] pairH = {{0, 3}, {2, 1}, {2, 0}, {0, 2},
            {31, 3}, {32, 1}, {32, 0}, {30, 2},
            {0, 33}};

        //                  H1  H2  H3  H4  H5  H6  H7  H8  H9
        int[] belongs   = {  0,  0,  0,  1,  1,  1,  2,  2,  2};
        int[] stores    = {  0,  0,  0,  0,  0,  0,  0,  0,  0};
        int[] rsvd      = { 10,  6,  6, 12, 12, 12,  9, 12, 15};

        int[] prods     = {140,160,170,140,150,120,150,150,150};
        int[] cons      = {150,150,160,150,140,130,120,140,170};

        int[] prods2    = {100,160,170, 95,150, 50,150,150,150};
        int[] cons2     = {150,100,100,150,140,130, 70,140, 50};

        HAIDs = new HashMap();  //Hashmap de Houses
        NAIDs = new HashMap();  //Hashmap de Neighbourhoods
        AIDNs = new String[pairN.length];
        neighbourHoods = new ArrayList();
        numNeighbourhoods = pairN.length;
        numHouses = pairH.length;
        
        if(!updates) createWorld(pairN, pairH, belongs, numHNeigh, prods, cons, stores, rsvd, centralAID);
        else createWorld(pairN, pairH, belongs, numHNeigh, prods2, cons2, stores, rsvd, centralAID);
        doWait(300);

        //H2 VENDEDOR CON 50 KW/H que ACEPTA MINIMO DE 15 EUROS y posicion 0,3
        //H3 VENDEDOR CON 50 KW/H Y QUE ACEPTA MINIMO DE 9 EUROS y posicion 2,1
        //H4 VENDEDOR CON 50 KW/H Y QUE ACEPTA MINIMO DE 9 EUROS y posicion 2,0
        //H1 COMPRADOR DE 50 KW/H Y 10 EUROS y y posicion 0,2
        //H5 COMPRADOR DE 50 KW/H Y 10 EUROS y y posicion 2,2

        try {

            System.out.println("\n########## Correspondencias HouseAID => ID ##########");
            printHash(HAIDs);
            Object[] distancesMaps = new Object[5];
            System.out.println("\n######### Distancias entre Houses ##########");

            double[][] houseDistances = computeDistances(pairH, numHouses);
            distancesMaps[0] = houseDistances;
            distancesMaps[1] = HAIDs;

            System.out.println("\n######### Correspondencias NeighbourhoodAID => ID ##########");
            printHash(NAIDs);
            System.out.println("\n######### Distancias entre Neighbourhoods ##########");
            double[][] neighbourhoodDistances = computeDistances(pairN, numNeighbourhoods);
            distancesMaps[3] = NAIDs;
            distancesMaps[4] = AIDNs;

            sendDistancesMatrix(distancesMaps, neighbourhoodDistances);
        } catch (IOException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }

        TimeBehaviour myBehaviour = new TimeBehaviour(this, 3000);
        this.addBehaviour(myBehaviour);
        EnvironmentStatisticsBehaviour statistics = new EnvironmentStatisticsBehaviour(this);
        this.addBehaviour(statistics);
    }

    public int getNumHousesNeighbourhood() {
        return numHousesNeighbourhood;
    }

    public void setNumHousesNeighbourhood(int numHousesNeighbourhood) {
        this.numHousesNeighbourhood = numHousesNeighbourhood;
    }

    private void createHouse(String houseName, String neighbourhoodName, int productionLevel, int consumLevel, int storageLevel, int reservedPrice, int coordX, int coordY, int idHouse, String centralAID) {

         double credits = 3000;
        Object[] args = {new Integer(productionLevel), new Integer(consumLevel), new Integer(storageLevel), new Integer(reservedPrice), neighbourhoodName, new Integer(coordX), new Integer(coordY), new String(centralAID), new Double(credits)};

        try {
            AgentController houseAgent = ac.createNewAgent(houseName, "org.upc.edu.Agents.House", args);

            // start the agent
            houseAgent.start();
            //Guardamos una clave-valor id-AID
            System.out.println("NEW HOUSE => " + houseAgent.getName());

            //update structures
            HAIDs.put(houseAgent.getName(), idHouse);

        } catch (jade.wrapper.StaleProxyException e) {
            System.err.println("Error launching houseAgent...");
            e.printStackTrace();
        }
    }

    public void createNeighbourhood(String name, int coordX, int coordY, int numHNeigh, int idNeighbourhood) {

        try {
            double credits = 30000;
            // create agent
            Object[] args1 = {this, numHNeigh, new Double(credits)}; //single instance of environment and number of houses of this neigh
            AgentController neighBourhoodAgent = ac.createNewAgent(name, "org.upc.edu.Agents.Neighbourhood", args1);

            // start the agent
            neighBourhoodAgent.start();
            System.out.println("NEW NEIGHBOURHOOD => " + neighBourhoodAgent.getName());
            //update structures
            NAIDs.put(neighBourhoodAgent.getName(), idNeighbourhood);
            AIDNs[idNeighbourhood] = neighBourhoodAgent.getName();
            neighbourHoods.add(neighBourhoodAgent.getName());

        } catch (jade.wrapper.StaleProxyException e) {
            System.err.println("Error launching neighBourhoodAgent...");
            e.printStackTrace();
        }
    }

    public String createCentral(String name, double pricePerKWH) throws StaleProxyException {


        // create agent
        Object[] args1 = {new Double(pricePerKWH)}; //attach nelighbourhood with the single instance of environment
        AgentController centralAgent = ac.createNewAgent(name, "org.upc.edu.Agents.Central", args1);

        // start the agent
        centralAgent.start();
        String a = centralAgent.getName();
        System.out.println("NEW CENTRAL=> " + centralAgent.getName());

        return centralAgent.getName();
    }

    @Override
    public void broadcast(String content) {
        super.broadcast(content);
    }

    public int getNumHouses() {

        return numHouses;
    }

    public int getNumNeighbourhoods() {
        return numNeighbourhoods;
    }

    // Construye la matriz de distancias entre Houses
    private double[][] computeDistances(int[][] coords, int numAgents) throws IOException {

        double[][] dists = new double[numAgents][numAgents];
        //Distancias entre Houses

        int aux_x1,
                aux_x2,
                aux_y1,
                aux_y2;

        for (int i = 0; i < numAgents; i++) {
            aux_x1 = coords[i][0];
            aux_y1 = coords[i][1];
            for (int j = 0; j < numAgents; j++) {
                if (i == j) {
                    dists[i][j] = 0;
                } else {
                    aux_x2 = coords[j][0];
                    aux_y2 = coords[j][1];


                    dists[i][j] = computeDistance(aux_x1, aux_x2, aux_y1, aux_y2);

                    System.out.print("Distancia de " + i + " a " + j + " és de: " + dists[i][j]);
                    System.out.println();

                }
            }
        }
        return dists;
    }

    private void sendDistancesMatrix(Object[] distancesMap, double[][] neighbourhoodDistances) throws IOException {

        //Send matrix to each neighbourhood
        ACLMessage response = new ACLMessage(ACLMessage.INFORM);
        response.setConversationId("distancesTable");
        AID aid;
        Iterator i = neighbourHoods.iterator();
        int index = 0;

        while (i.hasNext()) {
            //Adapt message to neighbourhood i
            //only send one entry of distances map (the one that indicates his dist to the other neighbourhoods)
            distancesMap[2] = neighbourhoodDistances[index];
            response.setContentObject(distancesMap);
            aid = new AID((String) i.next(), true);
            response.addReceiver(aid);
            this.send(response);

            index++;
        }
    }

    // Computa la distancia entre 2 puntos
    private double computeDistance(int x1, int x2, int y1, int y2) {

        double dist;
        dist = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        return dist;
    }

    // Imprime la clave => valor de un HashMap
    private void printHash(HashMap hash) {

        Set<Map.Entry<String, Double>> set = hash.entrySet();
        for (Map.Entry<String, Double> me : set) {
            AID key = new AID(me.getKey(), true);
            System.out.print(key.getLocalName() + " => ");
            System.out.println(me.getValue());
        }
    }

    private void createWorld(int[][] coordsN, int[][] coordsH, int[] belongs, int[] numHNeigh, int[] prods, int[] cons, int[] stores, int[] rsvd, String centralAID) {
        int idN;
        //Creació de Neighbourhoods
        for (int i = 0; i < coordsN.length; i++) {
            createNeighbourhood("N" + (i + 1), coordsN[i][0], coordsN[i][1], numHNeigh[i], i);
        }

        //Creació de Houses
        for (int i = 0; i < coordsH.length; i++) {
            idN = belongs[i] + 1;
            createHouse("H" + (i + 1), "N" + idN, prods[i], cons[i], stores[i], rsvd[i], coordsH[i][0], coordsH[i][1], i, centralAID);
        }
    }

    public void pause(){
        this.doSuspend();
    }

    public void resume(){
        this.doActivate();
    }

    public void sendGui(Vector housesVector, Vector neighVector){

        myGui.updateGui(housesVector, neighVector);

    }
}
