package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class MessageInjectionTest {
    
   @Test
   public void testNetworkMessageInjection() {
      // Testing simple message injection after physical access.
      /*
          Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
                        |
                     Dataflow
      */
      // TARGET: vNet1.messageInjection ENTRY_POINT: vNet1.physicalAccess

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      
      ECU Ecu1 = new ECU ("Ecu#1");
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow");

      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      Ecu1.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      vNet1.addDataflows(dataflow);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(vNet1.physicalAccess);
      attacker.attack();
      
      vNet1.accessNetworkLayer.assertCompromisedInstantaneously();
      vNet1.messageInjection.assertCompromisedInstantaneously();
      vNet1.eavesdrop.assertCompromisedInstantaneously();
      vNet1.deny.assertCompromisedInstantaneously();

      vNet2.messageInjection.assertUncompromised();

      dataflow.deny.assertCompromisedInstantaneously();
      dataflow.eavesdrop.assertCompromisedInstantaneously();
      
      dataflow.maliciousTransmitBypassConflitionProtection.assertCompromisedWithEffort();
      dataflow.transmit.assertCompromisedInstantaneously();
    }
   
   @Test
   public void testServicekMessageInjectionConflictProtect() {
      // Testing message injection from network when confliction protection is disabled.
      /*
          Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
            |            ||
            |            |------ Dataflow#2
      Transmitter <---> Dataflow
      */
      // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: vNet1.physicalAccess

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      
      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, false); // Enabled operation mode and DISABLED message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#2");
      TransmitterService service = new TransmitterService("Transmitter");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(vNet1.physicalAccess);
      attacker.attack();
      
      vNet1.messageInjection.assertCompromisedInstantaneously();
      vNet2.messageInjection.assertUncompromised();
      
      Ecu1.bypassMessageConfliction.assertUncompromised();
      Ecu1._networkServiceMessageInjection.assertCompromisedInstantaneously();

      service.serviceMessageInjection.assertCompromisedInstantaneously();
      dataflow.maliciousTransmit.assertCompromisedInstantaneously();
      dataflow.transmit.assertCompromisedInstantaneously();
      dataflow2.transmit.assertCompromisedInstantaneously();
    }
   
   @Test
   public void testServicekMessageInjectionNoConflictProtect() {
      // Testing service message injection from network when confliction protection is enabled.
      /*
          Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
            |            ||
            |            |------ Dataflow#2
      Transmitter <---> Dataflow
      */
      // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: vNet1.physicalAccess

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name
      
      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, true); // Enabled operation mode and message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow#3");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#4");
      TransmitterService service = new TransmitterService("Transmitter");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(vNet1.physicalAccess);
      attacker.attack();
      
      vNet1.messageInjection.assertCompromisedInstantaneously();
      vNet2.messageInjection.assertUncompromised();
      
      Ecu1.bypassMessageConfliction.assertUncompromised();
      service.serviceMessageInjection.assertUncompromised();
      
      dataflow.transmit.assertCompromisedInstantaneously();
      dataflow2.transmit.assertCompromisedInstantaneously();
    }
   
    @Test
   public void testServicekMessageInjectionFromECU() {
      // Testing message injection from connected Ecu when confliction protection is disabled.
      /*
          Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
            |            ||
            |            |------ Dataflow#2
      Transmitter <---> Dataflow
      */
      // TARGET: dataflow & datafaflow2.transmit BUT uncompromised ENTRY_POINT: Ecu1.connect
    
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name

      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, false); // Enabled operation mode and disabled message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#2");
      TransmitterService service = new TransmitterService("Transmitter");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(Ecu1.connect);
      attacker.attack();
      
      //vNet1.messageInjection.assertCompromisedInstantaneously();
      //vNet2.messageInjection.assertUncompromised();
      
      //Ecu1.bypassMessageConfliction.assertUncompromised();
      Ecu1.authenticate.assertUncompromised();
      service.serviceMessageInjection.assertUncompromised();
      
      dataflow.maliciousTransmit.assertUncompromised();
      dataflow.transmit.assertUncompromised();
      
      //dataflow2.transmit.assertCompromisedWithEffort();
    }
   
   @Test
   public void testNetworkMessageInjectionAfterVuln() {
      // Testing network message injection after exploiting vulnerability.
      /*
        Vulnerability(A)           Dataflow#2
              |                        |
           VehicularIdentity <---> Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
              |            |           |
              |---> Transmitter <--> Dataflow
      */
      // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: vuln.exploit

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name

      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, true); // Enabled operation mode and message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow#5");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#6");
      TransmitterService service = new TransmitterService("Service");
      
      VehicularIdentity vehicularidentity = new VehicularIdentity("Root User");
      HardwareVulnerability vuln = new HardwareVulnerability("Vulnerability");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu1.addConnectPrivileges(vehicularidentity);
      Ecu1.addVulnerabilities(vuln);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addHighPrivAppIAMs(vehicularidentity);
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(vuln.exploit);
      attacker.addAttackPoint(Ecu1.connect);
      attacker.attack();
      
      Ecu1.fullAccess.assertCompromisedInstantaneously();
      Ecu1.bypassMessageConfliction.assertCompromisedInstantaneously();

      service.fullAccess.assertCompromisedInstantaneously();
      service.serviceMessageInjection.assertCompromisedInstantaneously();
      Ecu2.connect.assertCompromisedInstantaneously();
      
      vNet1.messageInjection.assertCompromisedInstantaneously();
      vNet2.messageInjection.assertUncompromised();
      
      dataflow.transmit.assertCompromisedInstantaneously();
      dataflow2.transmit.assertCompromisedInstantaneously();
    }

    @Test
   public void testNetworkMessageInjectionAfterConnectionVuln() {
      // Testing network message injection after exploiting vulnerability.
      /*
        Vulnerability              Dataflow#2
              |                        |
           VehicularIdentity <---> Ecu#1 <---> vNet1 <---> Ecu#2(A)  <---> vNet2
              |            |           |
              |---> Transmitter <--> Dataflow
      */
      // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: Ecu#2.fullAccess

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name

      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, true); // Enabled operation mode and message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow#5");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#6");
      TransmitterService service = new TransmitterService("Service");
      
      VehicularIdentity vehicularidentity = new VehicularIdentity("Root User");
      HardwareVulnerability vuln = new HardwareVulnerability("Vulnerability");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu1.addConnectPrivileges(vehicularidentity);
      Ecu1.addVulnerabilities(vuln);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addHighPrivAppIAMs(vehicularidentity);
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(Ecu2.fullAccess);
      attacker.attack();
      
      Ecu1.connect.assertCompromisedInstantaneously();
      Ecu1.fullAccess.assertCompromisedInstantaneously();
      Ecu1.bypassMessageConfliction.assertCompromisedInstantaneously();

      service.fullAccess.assertCompromisedInstantaneously();
      service.serviceMessageInjection.assertCompromisedInstantaneously();
      
      vNet1.accessUninspected.assertCompromisedInstantaneously();
      vNet1.messageInjection.assertCompromisedInstantaneously();
      vNet2.messageInjection.assertCompromisedInstantaneously();
      
      dataflow.transmit.assertCompromisedInstantaneously();
      
      dataflow2.transmit.assertCompromisedInstantaneously();
    }
   
   @Test
   public void testNetworkMessageInjectionAfterFirmwareUpload() {
      // Testing network message injection after directly uploading custom firmware on ECU.
      /*
                        Dataflow#2
                            |
              Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
                |           |
         Transmitter <--> Dataflow
      */
      // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: Ecu#1.maliciousFirmwareUpload

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name

      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, true); // Enabled operation mode and message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow#7");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#8");
      TransmitterService service = new TransmitterService("Service");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(Ecu1.maliciousFirmwareUpload);
      attacker.attack();
      
      Ecu1.fullAccess.assertCompromisedInstantaneously();
      Ecu1.bypassMessageConfliction.assertCompromisedInstantaneously();
      
      vNet1.messageInjection.assertCompromisedInstantaneously();
      vNet2.messageInjection.assertUncompromised();
      
      dataflow.transmit.assertCompromisedInstantaneously();
      dataflow2.maliciousTransmitBypassConflitionProtection.assertCompromisedWithEffort();
      dataflow2.transmit.assertCompromisedInstantaneously();
    }
   
   @Test
   public void testProtectedNetworkMessageInjection() {
      // Testing network message injection on a protected network.
      /*
                                   Dataflow#2
                                       |
           VehicularIdentity <---> Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
              |            |           |
              |---> Transmitter <--> Dataflow
      */
      // TARGET: dataflow & datafaflow2.tranmsit ENTRY_POINT: Ecu#1.connect & Service.networkConnectUninspected

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name

      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, true); // Enabled operation mode and message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow#9");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#10");
      TransmitterService service = new TransmitterService("Transmitter");
      User user = new User ("Root User");
      VehicularIdentity vehicularidentity = new VehicularIdentity("Root Identity");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      vehicularidentity.addUsers(user);
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu1.addUsers(user);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addHighPrivAppIAMs(vehicularidentity);
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(Ecu1.connect);
      attacker.addAttackPoint(service.networkConnectUninspected);
      attacker.attack();

      Ecu1.fullAccess.assertUncompromised();
      
      vNet1.messageInjection.assertUncompromised();
      vNet2.messageInjection.assertUncompromised();
      
      dataflow.transmit.assertUncompromised();
      dataflow2.transmit.assertUncompromised();
    }
   
   @Test
   public void testSeeminglyProtectedNetworkMessageInjection() {
      // Testing network message injection on a protected network.
      /*
                       Firmware    Dataflow#2
                           |           |
           VehicularIdentity <---> Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
              |            |           |
              |---> Transmitter <--> Dataflow
      */
      // TARGET: dataflow & datafaflow2.tranmsit ENTRY_POINT: Ecu#1.connect & Service.networkConnectUninspected

      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName()); // Printing the test's name

      ECU Ecu1 = new ECU ("Ecu#1", false, false, true, true); // Enabled operation mode and message confliction protection.
      ECU Ecu2 = new ECU ("Ecu#2");
      Firmware firmware = new Firmware ("Firmware", false, false, true, false);
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow ("Dataflow#9");
      ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow ("Dataflow#10");
      TransmitterService service = new TransmitterService("Transmitter");
      User user = new User ("Root User");
      VehicularIdentity vehicularidentity = new VehicularIdentity("Root Identity");
      
      VehicleNetwork vNet1 = new VehicleNetwork ("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork ("vNet2");
      
      vehicularidentity.addUsers(user);
      Ecu1.addVehiclenetworks(vNet1);
      Ecu1.addMachineExecutedApps(service);
      Ecu1.addUsers(user);
      Ecu1.addFirmware(firmware);
      Ecu2.addVehiclenetworks(vNet1);
      Ecu2.addVehiclenetworks(vNet2);
      
      service.addHighPrivAppIAMs(vehicularidentity);
      service.addDataflows(dataflow);
      vNet1.addDataflows(dataflow);
      vNet1.addDataflows(dataflow2);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(Ecu1.connect);
      attacker.addAttackPoint(service.networkConnectUninspected);
      attacker.attack();
      
      vNet1.messageInjection.assertUncompromised();
      vNet1.messageInjection.assertUncompromisedFrom(Ecu1.uploadFirmware);
      vNet2.messageInjection.assertUncompromised();
      
      dataflow.transmit.assertUncompromised();
      dataflow2.transmit.assertUncompromised();
    }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }
    
}
