import org.junit.Test;
import org.junit.After;

import vehicle.*;
import core.*;
public class newTest {
    
   @Test
   public void acceleratorTest() {
      // This test case was created in the reviewing session with another domain specific language developer
      // Update 2018-12-05: Changed to assert the network as uncompromised because of modification to maliciousFirmwareModification parents
      /*
                                ---------------------------------   
                                |                               |
       Transmitter <---> accelDataflow   ---> Firmware          |
              |            |             |                      |
          accelECU <---> vNet1 <---> GatewayECU <---> vNet2     |
                           |             |                      |
          engineECU <-------            IDPS                    |
           |    |                                               |
        engine  -----> acceleratorAccount <---> canID <----------
            
      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      boolean firewallStatus = true;
      boolean firmwareValidationStatus = false;
      boolean secureBootStatus = false;
      ECU acceleratorEcu = new ECU ("acceleratorEcu", true, true); // Enabled operation mode and message confliction protection on all ECUs.
      ECU engineEcu = new ECU ("engineEcu", true, true);
      GatewayECU gateEcu = new GatewayECU ("GatewayECU", firewallStatus, true, true);
      IDPS idps = new IDPS ("IDPS");
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      ConnectionlessDataflow accelarationDataflow = new ConnectionlessDataflow("accelarationDataflow");
      TransmitterService transmitter = new TransmitterService("Transmitter");
      Firmware fw = new Firmware ("fw", firmwareValidationStatus, secureBootStatus);
      SensorOrActuator engine = new SensorOrActuator ("engine");
      MessageID canID = new MessageID ("CAN-ID");
      Account acceleratorAccount = new Account ("acceleratorAccount");

      acceleratorEcu.addExecutees(transmitter);
      transmitter.addDataflows(accelarationDataflow);
      acceleratorEcu.addVehiclenetworks(vNet1);
      engineEcu.addVehiclenetworks(vNet1);
      engineEcu.addSensorsOrActuators(engine);
      vNet1.addDataflows(accelarationDataflow);
      gateEcu.addTrafficVNetworks(vNet1);
      gateEcu.addTrafficVNetworks(vNet2);
      gateEcu.addIdps(idps);
      gateEcu.addFirmware(fw);
      
      acceleratorAccount.addCredentials(canID);
      accelarationDataflow.addData(canID);
      engineEcu.addAccount(acceleratorAccount);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(vNet2.physicalAccess);
      atk.addAttackPoint(canID.read);
      atk.attack();
      
      vNet2.physicalAccess.assertCompromisedInstantaneously();
      vNet2.accessNetworkLayer.assertCompromisedInstantaneously();
      gateEcu.connect.assertCompromisedInstantaneously();
      fw.maliciousFirmwareModification.assertUncompromised();
      vNet1.accessNetworkLayer.assertUncompromised();
      accelarationDataflow.transmit.assertUncompromised();
      accelarationDataflow.eavesdrop.assertUncompromised();
      
      //engine.access.assertUncompromised();
      acceleratorAccount.idAuthenticate.assertCompromisedInstantaneously();
      engineEcu.idAccess.assertCompromisedInstantaneously();
      engine.manipulate.assertCompromisedInstantaneously();
    }
   
    @After
    public void deleteModel() {
            Asset.allAssets.clear();
            AttackStep.allAttackSteps.clear();
            Defense.allDefenses.clear();
    }
    
}
