import org.junit.Test;
import org.junit.After;

import vehicle.*;
import core.*;
public class PublicInterfacesTest {
    
   @Test
   public void OBD2ConnectTest() {
      /*
      This test case models a simple OBD-II connector

      OBD-II <---> CAN Bus <---> ECU#1        
      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      OBD2Connector obd2 = new OBD2Connector("obd2");
      CANNetwork can = new CANNetwork("CAN");

      obd2.addInterfacingNetworks(can);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(obd2.physicalAccess);
      atk.attack();
      
      can.accessNetworkLayer.assertCompromisedInstantaneously();
      can.eavesdrop.assertCompromisedInstantaneously();
      can.messageInjection.assertCompromisedInstantaneously();
    }

   @Test
   public void ChargingPlugTest() {
      /*
      This test case models a real topology of an electric vehicle

      ChargingPlugConnector <---> CAN Bus <---> BMS <----
                                    |                   |
                                Dataflow <---> TransmitterService
      Naming:
      BMS = Battery Management System
      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      ChargingPlugConnector chgPlug = new ChargingPlugConnector("chgPlug");
      CANNetwork can = new CANNetwork("CAN");
      ECU bms = new ECU("BMS");
      TransmitterService bmsService = new TransmitterService("BMS_Service");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow("BatteryDataflow");

      chgPlug.addConnectedNetwork(can);
      bms.addVehiclenetworks(can);
      bms.addExecutees(bmsService);
      bmsService.addDataflows(dataflow);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(chgPlug.physicalAccess);
      atk.attack();
      
      can.accessNetworkLayer.assertCompromisedInstantaneously();
      can.eavesdrop.assertCompromisedInstantaneously();
      can.messageInjection.assertCompromisedInstantaneously();
      bms.connect.assertCompromisedInstantaneously();
      bmsService.connect.assertCompromisedInstantaneously();
      dataflow.transmit.assertCompromisedInstantaneously();
    }

   @Test
   public void AftermarketDongleTest() {
      /*
      This test case models an aftermarket dongle connected on the OBD-II port of a vehicle

      Dongle <---> OBD-II <---> CAN Bus

      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      boolean dongleIsHardened = true;
      AftermarketDongle dongle = new AftermarketDongle("dongle", dongleIsHardened);
      OBD2Connector obd2 = new OBD2Connector("obd2");
      CANNetwork can = new CANNetwork("CAN");

      dongle.addConnector(obd2);
      obd2.addInterfacingNetworks(can);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(dongle.connectDongle);
      atk.attack();

      if (dongleIsHardened == false)
      {
        dongle._connectToNetwork.assertCompromisedInstantaneously();
        can.accessNetworkLayer.assertCompromisedInstantaneously();
        can.eavesdrop.assertCompromisedInstantaneously();
        can.messageInjection.assertCompromisedInstantaneously();
      }
      else
      {
        dongle._connectToNetwork.assertUncompromised();
        can.accessNetworkLayer.assertUncompromised();
      }
    }
   
    @After
    public void deleteModel() {
            Asset.allAssets.clear();
            AttackStep.allAttackSteps.clear();
            Defense.allDefenses.clear();
    }
    
}
