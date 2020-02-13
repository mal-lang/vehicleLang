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
      ECU acceleratorEcu = new ECU ("acceleratorEcu", true, true); // Enabled operation mode and message confliction protection on all ECUs.
      ECU engineEcu = new ECU ("engineEcu", true, true);
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
      
      vNet1.accessNetworkLayer.assertCompromisedInstantaneously();
      vNet1.eavesdrop.assertCompromisedInstantaneously();
      accelerationDataflow.eavesdrop.assertCompromisedInstantaneously();
      
      engineEcu.access.assertUncompromised();
      accelerationDataflow.eavesdropId.assertCompromisedInstantaneously();
      engineEcu.idAccess.assertCompromisedInstantaneously();
      engine.manipulate.assertCompromisedInstantaneously();
    }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }
    
}
