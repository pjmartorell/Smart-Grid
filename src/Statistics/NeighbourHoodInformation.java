/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Statistics;

import jade.core.AID;
import java.io.Serializable;

/**
 *
 * @author ferran.obiol
 */
public class NeighbourHoodInformation implements Serializable{

    String name;
    double storageLevel;
    int reqBuy;
    int reqGetBattery;
    int reqStoreBattery;
    int reqSellCentral;
    int reqBuyCentral;

    public NeighbourHoodInformation(String name, double storageLevel, int reqBuy, int reqGetBattery, int reqStoreBattery, int reqBuyCentral, int reqSellCentral) {
        this.name = name;
        this.storageLevel = storageLevel;
        this.reqBuy = reqBuy;
        this.reqGetBattery = reqGetBattery;
        this.reqStoreBattery = reqStoreBattery;
        this.reqBuyCentral = reqBuyCentral;
        this.reqSellCentral = reqSellCentral;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReqBuy() {
        return reqBuy;
    }

    public void setReqBuy(int reqBuy) {
        this.reqBuy = reqBuy;
    }

    public int getReqGetBattery() {
        return reqGetBattery;
    }

    public void setReqGetBattery(int reqGetBattery) {
        this.reqGetBattery = reqGetBattery;
    }

    public int getReqStoreBattery() {
        return reqStoreBattery;
    }

    public void setReqStoreBattery(int reqStoreBattery) {
        this.reqStoreBattery = reqStoreBattery;
    }

    public double getStorageLevel() {
        return storageLevel;
    }

    public void setStorageLevel(double storageLevel) {
        this.storageLevel = storageLevel;
    }

    public int getReqBuyCentral() {
        return reqBuyCentral;
    }

    public void setReqBuyCentral(int reqBuyCentral) {
        this.reqBuyCentral = reqBuyCentral;
    }

    public int getReqSellCentral() {
        return reqSellCentral;
    }

    public void setReqSellCentral(int reqSellCentral) {
        this.reqSellCentral = reqSellCentral;
    }
}
