package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CoreDataTest {

   @Test
   public void testDataAccess() {
   // Testing data access after partial authentication.
      Data data = new Data("Data");
      
      Attacker attacker = new Attacker();
      attacker.addAttackPoint(data.attemptRead);
      attacker.addAttackPoint(data.authorizedRead);
      
      attacker.attack();

      data.read.assertCompromisedInstantaneously();
      data.write.assertUncompromised();
      data.delete.assertUncompromised();
    }
   
   @Test
   public void testDataflow1DataAccess() {
   // Testing connection oriented dataflow's data access after Man-in-the-Middle attack.
      Data data = new Data("Data");
      ConnectionOrientedDataflow dataflow = new ConnectionOrientedDataflow("Dataflow");
      
      dataflow.addData(data);
      
      Attacker attacker = new Attacker();
      attacker.addAttackPoint(dataflow.adversaryInTheMiddle);
      
      attacker.attack();

      data.read.assertCompromisedInstantaneously();
      data.write.assertCompromisedInstantaneously();
      data.delete.assertCompromisedInstantaneously();
    }
   
   @Test
   public void testDataflow2DataAccess() {
   // Testing connectionless dataflow's data access after Man-in-the-Middle attack.
      Data data = new Data("Data");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
      
      dataflow.addData(data);
      
      Attacker attacker = new Attacker();
      attacker.addAttackPoint(dataflow.adversaryInTheMiddle);
      
      attacker.attack();

      data.read.assertCompromisedInstantaneously();
      data.write.assertCompromisedInstantaneously();
      data.delete.assertCompromisedInstantaneously();
    }

    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }

}
