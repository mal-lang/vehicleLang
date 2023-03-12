package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CoreFirmwareTest {
    
   @Test
   public void testFirmwareValidation() {
      // Testing ECU firmware modification when firmware validation is enabled and firmware key is read. This allows complete network message injection.
      /*
         Ecu <---> Firmware
          |
          ---> Credentials(A)
      */
      // Entry point: Credentials.read and Ecu.fullAccess
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      ECU ecu = new ECU("ECU", false, false, false, true); // Enabled message confliction protection.
      Firmware fw = new Firmware("Firmware", false, false, true, false); // Firmware validation is enabled.
      Credentials creds = new Credentials("Credentials");
      VehicularIdentity id = new VehicularIdentity("VehicularIdentity");
      
      ecu.addFirmware(fw);
      id.addCredentials(creds);
      fw.addHighPrivAppIAMs(id);
      //ecu.addHostedData(creds);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(creds.read);
      attacker.addAttackPoint(ecu.fullAccess);
      attacker.attack();

      // Test expected attack path
      ecu.passFirmwareValidation.assertCompromisedInstantaneously();
      ecu.passUdsFirmwareModification.assertCompromisedInstantaneously();
      ecu.changeOperationMode.assertCompromisedInstantaneously();
      ecu.uploadFirmware.assertCompromisedInstantaneously();
      // Test that alternative attack path is much more difficult because of defenses
      ecu.maliciousFirmwareUpload.assertCompromisedWithEffort();
      //ecu.fullAccess.assertCompromisedInstantaneouslyFrom(ecu.maliciousFirmwareUpload);
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
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      ECU ecu = new ECU("ECU", false, false, false, true); // Enabled message confliction protection.
      Firmware fw = new Firmware("Firmware", false, false, true, false); // Firmware validation is enabled.
      
      ecu.addFirmware(fw);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();
      
      ecu.passFirmwareValidation.assertUncompromised();
      fw.maliciousFirmwareModification.assertCompromisedInstantaneouslyFrom(ecu.attemptChangeOperationMode);
      fw.bypassFirmwareValidation.assertUncompromised();
      fw.crackFirmwareValidation.assertCompromisedWithEffort();
      ecu.maliciousFirmwareUpload.assertCompromisedWithEffort();
      ecu.fullAccess.assertCompromisedInstantaneouslyFrom(ecu.maliciousFirmwareUpload);
    }
   
   @Test
   public void testBypassFirmwareValidation() {
      // Testing ECU firmware modification when firmware validation is disabled. This means that anybody can upload a custom firmware.
      /*
         Ecu <---> Firmware
      */
      // Entry point: Ecu.connect
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      ECU ecu = new ECU("ECU", false, false, false, true); // Enabled message confliction protection.
      Firmware fw = new Firmware("Firmware", false, false, false, false); // Firmware validation is disabled.
      
      ecu.addFirmware(fw);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();

      fw.maliciousFirmwareModification.assertCompromisedInstantaneouslyFrom(ecu.attemptChangeOperationMode);
      fw.bypassFirmwareValidation.assertCompromisedInstantaneouslyFrom(fw.maliciousFirmwareModification);
      fw.crackFirmwareValidation.assertCompromisedWithEffort();
      ecu.fullAccess.assertCompromisedInstantaneouslyFrom(fw.bypassFirmwareValidation);
    }

    @Test
   public void testCrackSecureBoot() {
      // Testing ECU firmware modification when firmware validation is enabled and Secure Boot is also enabled.
      /*
         Ecu <---> Firmware
      */
      // Entry point: Ecu.connect
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      ECU ecu = new ECU("ECU", false, false, false, true); // Enabled message confliction protection.
      Firmware fw = new Firmware("Firmware", false, false, true, true); // Firmware validation and Secure Boot are enabled.
      
      ecu.addFirmware(fw);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();

      fw.maliciousFirmwareModification.assertCompromisedInstantaneouslyFrom(ecu.attemptChangeOperationMode);
      fw.bypassSecureBoot.assertUncompromised();
      fw.bypassFirmwareValidation.assertUncompromised();
      fw.crackFirmwareValidation.assertUncompromised();
      fw.crackSecureBoot.assertCompromisedWithEffort();
      ecu.maliciousFirmwareUpload.assertCompromisedInstantaneouslyFrom(fw.crackSecureBoot);
      ecu.fullAccess.assertCompromisedWithEffort();
    }

    @Test
   public void testUDSFirmwareUpload() {
      // Testing ECU firmware modification through UDS FirmwareUpdaterService.
      /*
         (Firmware <--->) ECU <---> FirmwareUpdaterService
      */
      // Entry point: FirmwareUpdaterService.fullAccess
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      ECU ecu = new ECU("ECU", false, false, false, true); // Enabled message confliction protection.
      // Firmware fw = new Firmware("Firmware", false); // Firmware validation is disabled. Firmware is not needed for this attack (is assumed though)
      FirmwareUpdaterService fwUpdater =  new FirmwareUpdaterService("FirmwareUpdater", false, false, false); // Turned off UDS SecurityAccess
      
      // ecu.addFirmware(fw);
      ecu.addFirmwareUpdater(fwUpdater);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(fwUpdater.fullAccess);
      attacker.attack();

      ecu.udsFirmwareModification.assertCompromisedInstantaneously();
      fwUpdater.udsFirmwareUpload.assertCompromisedInstantaneously();
      ecu.fullAccess.assertCompromisedInstantaneously();
    }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }
    
}
