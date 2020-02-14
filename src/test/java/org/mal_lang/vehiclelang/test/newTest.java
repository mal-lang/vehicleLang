package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class newTest {
    
   @Test
   public void acceleratorTest() {
      // This test case was created in the reviewing session with another domain specific language developer
      // Update 2018-12-05: Changed to assert the network as uncompromised because of modification to maliciousFirmwareModification parents
      // Update 2020-02-13: Changed almost all the model to make use of the new and imrpoved way to exploit known ID on bus networks.
      /*
                                -----> canID  
                                |                                 
       Transmitter <---> accelDataflow       
              |            |                                 
          accelECU <---> vNet1
                           |                     
          engineECU <-------                 
           |                                                   
        engine  
            
      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      boolean messageConflictionProtection = false;
      ECU acceleratorEcu = new ECU ("acceleratorEcu", true, messageConflictionProtection); // Enabled operation mode protection.
      ECU engineEcu = new ECU ("engineEcu", true, true); // Enabled operation mode and message confliction protection on this ECUs.
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      ConnectionlessDataflow accelerationDataflow = new ConnectionlessDataflow("accelerationDataflow");
      TransmitterService transmitter = new TransmitterService("Transmitter");
      VehicleNetworkReceiver receiver = new VehicleNetworkReceiver("Receiver");
      SensorOrActuator engine = new SensorOrActuator ("engine");
      MessageID canID = new MessageID ("CAN-ID");

      acceleratorEcu.addExecutees(transmitter);
      transmitter.addDataflows(accelerationDataflow);
      acceleratorEcu.addVehiclenetworks(vNet1);
      engineEcu.addVehiclenetworks(vNet1);
      engineEcu.addSensorsOrActuators(engine);
      engineEcu.addExecutees(receiver);
      receiver.addDataflows(accelerationDataflow);
      vNet1.addDataflows(accelerationDataflow);
      
      accelerationDataflow.addDataflowId(canID);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(vNet1.physicalAccess);
      atk.attack();

      acceleratorEcu.access.assertUncompromised();
      acceleratorEcu.idControl.assertUncompromised();
      acceleratorEcu._networkServiceMessageInjection.assertCompromisedInstantaneously();
      
      vNet1.accessNetworkLayer.assertCompromisedInstantaneously();
      vNet1.eavesdrop.assertCompromisedInstantaneously();
      accelerationDataflow.eavesdrop.assertCompromisedInstantaneously();
      
      engineEcu.access.assertUncompromised();
      transmitter.serviceMessageInjection.assertCompromisedInstantaneously();
      accelerationDataflow.eavesdropId.assertCompromisedInstantaneously();
      receiver.impersonateId.assertCompromisedInstantaneously();
      engineEcu.idControl.assertCompromisedInstantaneously();
      engine.manipulate.assertCompromisedInstantaneously();
    }

    @Test
    public void acceleratorTestAdv() {
      /*
                                -----> canID  
                                |                                 
       Transmitter <---> accelDataflow   ---> Firmware          
              |            |             |                      
          accelECU <---> vNet1 <---> GatewayECU <---> vNet2     
                           |             |                      
          engineECU <-------            IDPS                    
           |                                                   
        engine  
            
      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      boolean firewallStatus = false;
      boolean firmwareValidationStatus = true;
      boolean secureBootStatus = true;
      boolean messageConflictionProtection = false;
      ECU acceleratorEcu = new ECU ("acceleratorEcu", true, messageConflictionProtection); // Enabled operation mode and message confliction protection on all ECUs.
      ECU engineEcu = new ECU ("engineEcu", true, true);
      GatewayECU gateEcu = new GatewayECU ("GatewayECU", true, true, firewallStatus);
      //IDPS idps = new IDPS ("IDPS");
      //Firmware fw = new Firmware ("GatewayFW", firmwareValidationStatus, secureBootStatus);
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      ConnectionlessDataflow accelerationDataflow = new ConnectionlessDataflow("accelerationDataflow");
      TransmitterService transmitter = new TransmitterService("Transmitter");
      VehicleNetworkReceiver receiver = new VehicleNetworkReceiver("Receiver");
      SensorOrActuator engine = new SensorOrActuator ("engine");
      MessageID canID = new MessageID ("CAN-ID");

      acceleratorEcu.addExecutees(transmitter);
      transmitter.addDataflows(accelerationDataflow);
      acceleratorEcu.addVehiclenetworks(vNet1);
      engineEcu.addVehiclenetworks(vNet1);
      engineEcu.addSensorsOrActuators(engine);
      engineEcu.addExecutees(receiver);
      receiver.addDataflows(accelerationDataflow);
      vNet1.addDataflows(accelerationDataflow);
      gateEcu.addTrafficVNetworks(vNet1);
      gateEcu.addTrafficVNetworks(vNet2);
      //gateEcu.addIdps(idps);
      //gateEcu.addFirmware(fw);

      accelerationDataflow.addDataflowId(canID);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(vNet2.physicalAccess);
      atk.attack();
      
      gateEcu.connect.assertCompromisedInstantaneously();
      vNet2.accessNetworkLayer.assertCompromisedInstantaneously();
      vNet2.eavesdrop.assertCompromisedInstantaneously();
      //gateEcu.bypassFirewall.assertCompromisedInstantaneously();
      //gateEcu.gatewayNoIDPS.assertCompromisedInstantaneously();

      //vNet1.accessNetworkLayer.assertCompromisedInstantaneously();
      engineEcu.access.assertUncompromised();
      accelerationDataflow.eavesdrop.assertCompromisedInstantaneously();
      accelerationDataflow.eavesdropId.assertCompromisedInstantaneously();
      receiver.impersonateId.assertCompromisedInstantaneously();
      engineEcu.idControl.assertCompromisedInstantaneously();
      engine.manipulate.assertCompromisedInstantaneously();
    }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }
    
}
