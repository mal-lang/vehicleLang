package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CoreMachineTest {

   @Test
   public void testMachineAccess() {
   // Testing proper access to a machine.
      Machine machine = new Machine();

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(machine.connect);
      attacker.addAttackPoint(machine.authenticate);
      
      attacker.attack();

      machine.fullAccess.assertCompromisedInstantaneously();
      machine.deny.assertCompromisedInstantaneously();
   }
   
   @Test
   public void testBypassMachineAccess() {
   // Testing bypass access control to a machine.
      Machine machine = new Machine();

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(machine.bypassAccessControl);
      
      attacker.attack();

      machine.fullAccess.assertCompromisedInstantaneously();
      machine.deny.assertCompromisedInstantaneously();
   }

   @Test
   public void testSoftwareHostToGuest() {
   // Testing compromise VehicularIdentity on a machine.
   /*
      VehicularIdentity <---> Machine <---> Software2
         |             |
      Software1 <------
   */
   // TARGET: softwares ENTRY_POINT: VehicularIdentity.assume and machine.connect
      Machine machine = new Machine("Machine");
      Service software1 = new Service("Software1");
      Service software2 = new Service("Software2");
      VehicularIdentity vehicularidentity = new VehicularIdentity("VehicularIdentity");

      machine.addVehicularIdentity(vehicularidentity);
      software1.addHostMachine(machine);
      software2.addHostMachine(machine);
      software1.addHighPrivAppIAMs(vehicularidentity);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(machine.connect);
      attacker.addAttackPoint(vehicularidentity.assume);
      
      attacker.attack();

      machine.fullAccess.assertCompromisedInstantaneously();
      software1.localConnect.assertCompromisedInstantaneously();
      software1.fullAccess.assertCompromisedInstantaneously();
      software2.localConnect.assertCompromisedInstantaneously();
      software2.fullAccess.assertUncompromised();
		
   }

   @Test
   public void testSoftwareGuestToHost() {
   // Testing machine access from software.
      Machine machine = new Machine("Machine12");
      Service software = new Service("Software123");

      software.addHostMachine(machine);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(software.localConnect);
      attacker.addAttackPoint(software.fullAccess);
      
      attacker.attack();

      software.fullAccess.assertCompromisedInstantaneously();
      machine.connect.assertCompromisedInstantaneously();
      machine.fullAccess.assertUncompromised();
   }

   @Test
   public void testMachineVehicularIdentityDataRWD() {
   // Testing data read access from VehicularIdentity compromise.
   /*
      VehicularIdentity <---> Machine
         |             |
       Data(read) <----
   */
   // TARGET: Data.read ENTRY_POINT: vehicularIdentity.assume and machine.connect
      Machine machine = new Machine("Machine");
      VehicularIdentity vehicularidentity = new VehicularIdentity("VehicularIdentity");
      Data data = new Data("Data");

      machine.addVehicularIdentity(vehicularidentity);
      machine.addHostedData(data);
      vehicularidentity.addReadPrivData(data);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(machine.connect);
      attacker.addAttackPoint(vehicularidentity.assume);      

      attacker.attack();

      data.attemptRead.assertCompromisedInstantaneously();
      data.read.assertCompromisedInstantaneously();
      data.write.assertCompromisedInstantaneously();
      data.delete.assertCompromisedInstantaneously();
      
      machine.authenticate.assertCompromisedInstantaneously();
   }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }

}
