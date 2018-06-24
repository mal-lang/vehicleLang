import org.junit.Test;
import org.junit.After;

import vehicle.*;
import core.*;
public class CoreFirmwareTest {
    
   @Test
   public void testFirmwareValidation() {
      // Testing ECU firmware modification when firmware validation is enabled and firmware key is read. This allows complete network message injection.
      /*
         Ecu <---> Firmware
          |
          ---> Credentials(A)
      */
      // Entry point: Credentials.read and Ecu.access
      ECU ecu = new ECU("ECU", true, true); // Enabled operation mode and message confliction protection.
      Firmware fw = new Firmware("Firmware", true); // Firmware validation is enabled.
      Credentials creds = new Credentials("Credentials");
      
      ecu.addFirmware(fw);
      ecu.addData(creds);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(creds.read);
      attacker.addAttackPoint(ecu.access);
      attacker.attack();
      
      ecu.passFirmwareValidation.assertCompromisedInstantaneously();
      ecu.uploadFirmware.assertCompromisedInstantaneously();
      ecu.maliciousFirmwareUpload.assertUncompromised();
      fw.maliciousFirmwareModification.assertUncompromised();
      //ecu.access.assertCompromisedInstantaneouslyFrom(ecu.maliciousFirmwareUpload);
    }
   
   @Test
   public void testFirmwareValidation2() {
      // Testing ECU firmware modification when firmware validation is enabled but no firmware key is not present.
      /*
         Ecu(A) <---> Firmware
           |
           ---X No credentials are stored
      */
      // Entry point: Ecu.connect
      ECU ecu = new ECU("ECU", true, true); // Enabled operation mode and message confliction protection.
      Firmware fw = new Firmware("Firmware", true); // Firmware validation is enabled.
      
      ecu.addFirmware(fw);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();
      
      ecu.passFirmwareValidation.assertUncompromised();
      fw.maliciousFirmwareModification.assertCompromisedInstantaneously();
      fw.bypassFirmwareValidation.assertUncompromised();
      //fw.bypassFirmwareValidation.assertUncompromisedFrom(ecu.connect);
      fw.crackFirmwareValidation.assertCompromisedWithEffort();
      ecu.maliciousFirmwareUpload.assertCompromisedWithEffort();
      ecu.access.assertCompromisedWithEffort();
    }
   
   @Test
   public void testBypassFirmwareValidation() {
      // Testing ECU firmware modification when firmware validation is disabled. This means that anybody can upload a custom firmware.
      /*
         Ecu <---> Firmware
      */
      // Entry point: Ecu.connect
      ECU ecu = new ECU("ECU", true, true); // Enabled operation mode and message confliction protection.
      Firmware fw = new Firmware("Firmware", false); // Firmware validation is disabled.
      
      ecu.addFirmware(fw);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();
      
      fw.maliciousFirmwareModification.assertCompromisedInstantaneously();
      fw.bypassFirmwareValidation.assertCompromisedInstantaneously();
      fw.crackFirmwareValidation.assertCompromisedWithEffort();
      ecu.access.assertCompromisedInstantaneouslyFrom(fw.bypassFirmwareValidation);
    }
   
    @After
    public void deleteModel() {
            Asset.allAssets.clear();
            AttackStep.allAttackSteps.clear();
            Defense.allDefenses.clear();
    }
    
}
