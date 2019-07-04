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
      ECU ecu = new ECU("ECU", false, true); // Enabled message confliction protection.
      Firmware fw = new Firmware("Firmware", true); // Firmware validation is enabled.
      Credentials creds = new Credentials("Credentials");
      
      ecu.addFirmware(fw);
      ecu.addData(creds);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(creds.read);
      attacker.addAttackPoint(ecu.access);
      attacker.attack();

      // Test expected attack path
      ecu.passFirmwareValidation.assertCompromisedInstantaneously();
      ecu.passUdsFirmwareModification.assertCompromisedInstantaneously();
      ecu.changeOperationMode.assertCompromisedInstantaneously();
      ecu.uploadFirmware.assertCompromisedInstantaneously();
      // Test that alternative attack path is much more difficult because of defenses
      ecu.maliciousFirmwareUpload.assertCompromisedWithEffort();
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
      ECU ecu = new ECU("ECU", false, true); // Enabled message confliction protection.
      Firmware fw = new Firmware("Firmware", true); // Firmware validation is enabled.
      
      ecu.addFirmware(fw);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();
      
      ecu.passFirmwareValidation.assertUncompromised();
      fw.maliciousFirmwareModification.assertCompromisedInstantaneouslyFrom(ecu.attemptChangeOperationMode);
      fw.bypassFirmwareValidation.assertUncompromised();
      //fw.bypassFirmwareValidation.assertUncompromisedFrom(ecu.connect);
      fw.crackFirmwareValidation.assertCompromisedWithEffort();
      ecu.maliciousFirmwareUpload.assertCompromisedWithEffort();
      ecu.access.assertCompromisedInstantaneouslyFrom(ecu.maliciousFirmwareUpload);
    }
   
   @Test
   public void testBypassFirmwareValidation() {
      // Testing ECU firmware modification when firmware validation is disabled. This means that anybody can upload a custom firmware.
      /*
         Ecu <---> Firmware
      */
      // Entry point: Ecu.connect
      ECU ecu = new ECU("ECU", false, true); // Enabled message confliction protection.
      Firmware fw = new Firmware("Firmware", false); // Firmware validation is disabled.
      
      ecu.addFirmware(fw);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();

      fw.maliciousFirmwareModification.assertCompromisedInstantaneouslyFrom(ecu.attemptChangeOperationMode);
      fw.bypassFirmwareValidation.assertCompromisedInstantaneouslyFrom(fw.maliciousFirmwareModification);
      fw.crackFirmwareValidation.assertCompromisedWithEffort();
      ecu.access.assertCompromisedInstantaneouslyFrom(fw.bypassFirmwareValidation);
    }

    @Test
   public void testUDSFirmwareUpload() {
      // Testing ECU firmware modification through UDS FirmwareUpdaterService.
      /*
         (Firmware <--->) ECU <---> FirmwareUpdaterService
      */
      // Entry point: FirmwareUpdaterService.access
      ECU ecu = new ECU("ECU", false, true); // Enabled message confliction protection.
      // Firmware fw = new Firmware("Firmware", false); // Firmware validation is disabled. Firmware is not needed for this attack (is assumed though)
      FirmwareUpdaterService fwUpdater =  new FirmwareUpdaterService("FirmwareUpdater", false); // Turned off UDS SecurityAccess
      
      // ecu.addFirmware(fw);
      ecu.addFirmwareUpdater(fwUpdater);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(fwUpdater.access);
      attacker.attack();

      ecu.udsFirmwareModification.assertCompromisedInstantaneously();
      fwUpdater.udsFirmwareUpload.assertCompromisedInstantaneously();
      ecu.access.assertCompromisedInstantaneously();
    }
   
    @After
    public void deleteModel() {
            Asset.allAssets.clear();
            AttackStep.allAttackSteps.clear();
            Defense.allDefenses.clear();
    }
    
}
