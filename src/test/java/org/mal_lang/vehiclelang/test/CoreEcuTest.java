package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CoreEcuTest {
    
   @Test
   public void testConnectEcuAttacks() {
      // Testing ECU attacks on connect with all defenses enabled.
      ECU ecu = new ECU("ECU", false, false, true, true);  // Enabled operation mode and message confliction protection.

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();
      
      ecu.changeOperationMode.assertUncompromised();
      ecu.fullAccess.assertUncompromised();
      ecu.gainLINAccessFromCAN.assertUncompromised();
    }
   
   @Test
   public void testConnectEcuAttacks2() {
      // Testing ECU attacks on connect with some defenses enabled.
      ECU ecu = new ECU("ECU2", false, false, false, true);  // Enabled only message confliction protection.

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.connect);
      attacker.attack();
      
      ecu.attemptChangeOperationMode.assertCompromisedWithEffort();
      ecu.changeOperationMode.assertUncompromised();
      ecu.fullAccess.assertUncompromised();
      ecu.gainLINAccessFromCAN.assertUncompromised();
    }
   
   @Test
   public void testAccessEcuAttacks() {
      // Testing ECU attacks on access with all defenses enabled.
      ECU ecu = new ECU("ECU3", false, false, true, true);  // Enabled operation mode and message confliction protection.

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.fullAccess);
      attacker.attack();
      
      ecu.changeOperationMode.assertUncompromised();
      ecu.gainLINAccessFromCAN.assertCompromisedInstantaneously();
    }
   
   @Test
   public void testAccessEcuAttacks2() {
      // Testing ECU attacks on access with some defenses enabled.
      ECU ecu = new ECU("ECU4", false, false, false, true);  // Enabled only message confliction protection.

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(ecu.fullAccess);
      attacker.attack();
      
      ecu.changeOperationMode.assertCompromisedInstantaneously();
      ecu.attemptChangeOperationMode.assertUncompromised();
      ecu.bypassMessageConfliction.assertCompromisedInstantaneously();
      ecu.gainLINAccessFromCAN.assertCompromisedInstantaneously();
    }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }
    
}
