package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class expertSessionTest {
    
   @Test
   public void Test1() {
      // This test case was created in the reviewing session with a domain expert
      /*
											   WiFi, BT, USB
                                                    |  
	  Speaker <---> Amp. <---> Ethernet <---> InfotainmentSys
													|
												   CAN
													|
												GatewayECU
													|
											   internalCAN
											-----------------
											|		|		|
										   TMS	   BMS	   EMS
      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      boolean firewallStatus = true;
      InfotainmentSystem infosys = new InfotainmentSystem("infosys");
      GatewayECU gwECU = new GatewayECU("gwECU", false, false, firewallStatus, true, true);
      CANNetwork can = new CANNetwork("can");
      CANNetwork internalCan = new CANNetwork("internalCan");
      ECU tms = new ECU("TMS"); //Transmission
      ECU ems = new ECU("EMS"); //Engine
      ECU bms = new ECU("BMS"); //Braking
      //EthernetNetwork ethernet = new EthernetNetwork("ethernet");
      Machine amp = new Machine("AMP");

      infosys.addConnectedNetworks(can);
      //infosys.addConnectedNetworks(ethernet);
      gwECU.addTrafficVNetworks(can);
      gwECU.addTrafficVNetworks(internalCan);
      internalCan.addNetworkECUs(tms);
      internalCan.addNetworkECUs(ems);
      internalCan.addNetworkECUs(bms);
      
      Attacker atk = new Attacker();
      //atk.addAttackPoint(infosys.connect);
      atk.addAttackPoint(infosys.fullAccess);
      atk.attack();

      infosys.engineerNetworkAccess.assertCompromisedWithEffort();
      can.accessNetworkLayer.assertCompromisedWithEffort();
      internalCan.accessNetworkLayer.assertUncompromised();
      tms.fullAccess.assertUncompromised();
      ems.fullAccess.assertUncompromised();
      bms.fullAccess.assertUncompromised();
      
    }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }
    
}
