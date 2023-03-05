package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class InfotainmentTest {
    
   @Test
   public void NetworkAccessFromInfotainmentTest() {
      /*
      This test case models an attack from the infotainment system which has a network access service (which might be stopped by default)

       ---> VehicularIdentity 
       |       |
       |  Infotainment <---> VehicleNetwork
       |       |
      NetworkAccessService

      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      InfotainmentSystem infosys = new InfotainmentSystem("InfoSys");
      VehicleNetwork vNet = new VehicleNetwork("vNet");
      NetworkAccessService netSrv = new NetworkAccessService("NetService");
      VehicularIdentity vehicularidentity = new VehicularIdentity("VehicularIdentity");

      infosys.addConnectedNetworks(vNet);
      infosys.addMachineExecutedApps(netSrv);
      infosys.addVehicularIdentity(vehicularidentity);
      netSrv.addVehicularIdentity(vehicularidentity);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(infosys.connect);
      atk.addAttackPoint(vehicularidentity.assume);
      atk.attack();
      
      infosys.fullAccess.assertCompromisedInstantaneously();
      netSrv.fullAccess.assertCompromisedInstantaneously();
      infosys.gainNetworkAccess.assertCompromisedInstantaneously();

      vNet.accessNetworkLayer.assertCompromisedInstantaneously();
    }

    @Test
   public void EngineerNetworkAccessFromInfotainmentTest() {
      /*
      This test case models an attack from the infotainment system which has not a network access service so the attacker must engineer it!

         VehicularIdentity 
            |
      Infotainment <---> VehicleNetwork

      */
      System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
      // Start of test
      InfotainmentSystem infosys = new InfotainmentSystem("InfoSys");
      VehicleNetwork vNet = new VehicleNetwork("vNet");
      VehicularIdentity vehicularidentity = new VehicularIdentity("VehicularIdentity");

      infosys.addConnectedNetworks(vNet);
      infosys.addVehicularIdentity(vehicularidentity);
      
      Attacker atk = new Attacker();
      atk.addAttackPoint(infosys.connect);
      atk.addAttackPoint(vehicularidentity.assume);
      atk.attack();
      
      infosys.fullAccess.assertCompromisedInstantaneously();
      infosys.gainNetworkAccess.assertUncompromised();
      infosys.engineerNetworkAccess.assertCompromisedWithEffort();
      vNet.accessNetworkLayer.assertCompromisedWithEffort();
    }
   
    @AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }
    
}
