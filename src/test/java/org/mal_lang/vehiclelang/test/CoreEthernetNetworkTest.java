package org.mal_lang.vehiclelang.test;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CoreEthernetNetworkTest {

   @Test
   public void testRouterAccess() {
      Router router = new Router("Router");

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(router.networkConnectUninspected);
      attacker.addAttackPoint(router.authenticate);
      
      attacker.attack();

      router.fullAccess.assertCompromisedInstantaneously();
      router.denialOfService.assertCompromisedInstantaneously();
   	router.forwarding.assertCompromisedInstantaneously();
	}

   @Test
   public void testDataflow1() {
   // Testing single dataflows request attack.
   /*
          Service <---> Dataflow <---> Client(A)
   */
   // Entry point: client.access
      ConnectionOrientedDataflow dataflow = new ConnectionOrientedDataflow("Dataflow");
		NetworkClient client = new NetworkClient("Client");
		NetworkService service = new NetworkService("Service");

		client.addDataflows(dataflow);
		service.addDataflows(dataflow);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(client.access);
	
      attacker.attack();

      dataflow.request.assertCompromisedInstantaneously();
      service.networkConnectUninspected.assertCompromisedInstantaneously();
	}

   @Test
   public void testMultiDataflowRequest() {
   // Testing multiple dataflows request attack.
   /*
          Service1 <---> Dataflow <---> Client1(A)
                            | |
          Service2 <--------  --------> Client2
   */
   // Entry point: client1.access
      ConnectionOrientedDataflow dataflow = new ConnectionOrientedDataflow("Dataflow");
		NetworkClient client1 = new NetworkClient("Client1");
		NetworkClient client2 = new NetworkClient("Client2");
		NetworkService service1 = new NetworkService("Service1");
		NetworkService service2 = new NetworkService("Service2");

		client1.addDataflows(dataflow);
		client2.addDataflows(dataflow);
		service1.addDataflows(dataflow);
		service2.addDataflows(dataflow);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(client1.access);
	
      attacker.attack();

      dataflow.request.assertCompromisedInstantaneously();
      service1.networkConnectUninspected.assertCompromisedInstantaneously();
      service2.networkConnectUninspected.assertCompromisedInstantaneously();
		client2.networkConnectUninspected.assertCompromisedWithEffort();
	}

   @Test
   public void testMultiDataflowResponse() {
   // Testing multiple dataflows request attack.
   /*
       Service1(A) <---> Dataflow <---> Client1
                            | |
          Service2 <--------  --------> Client2
   */
   // Entry point: service1.access
      ConnectionOrientedDataflow dataflow = new ConnectionOrientedDataflow("Dataflow");
		NetworkClient client1 = new NetworkClient("Client1");
		NetworkClient client2 = new NetworkClient("Client2");
		NetworkService service1 = new NetworkService("Service1");
		NetworkService service2 = new NetworkService("Service2");

		client1.addDataflows(dataflow);
		client2.addDataflows(dataflow);
		service1.addDataflows(dataflow);
		service2.addDataflows(dataflow);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(service1.fullAccess);
	
      attacker.attack();

      dataflow.respond.assertCompromisedInstantaneously();
      dataflow.request.assertUncompromised();
      client1.networkConnectUninspected.assertCompromisedInstantaneously();
      client2.networkConnectUninspected.assertCompromisedInstantaneously();
		service2.networkConnectUninspected.assertUncompromised();
	}

   @Test
   public void testMitmNetwork1() {
   // Testing MitM attack on dataflow on an ethernet network.
   /*
           Service <---> Dataflow <---> Client
                             |
                        Ethernet(A)
   */
   // Entry point: network.manInTheMiddle
      ConnectionOrientedDataflow dataflow = new ConnectionOrientedDataflow("Dataflow");
		NetworkClient client = new NetworkClient("Client");
		NetworkService service = new NetworkService("Service");
		EthernetNetwork network = new EthernetNetwork("Network");

		client.addDataflows(dataflow);
		service.addDataflows(dataflow);
		network.addDataflows(dataflow);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(network.manInTheMiddle);
	
      attacker.attack();

      dataflow.manInTheMiddle.assertCompromisedInstantaneously();
      dataflow.request.assertCompromisedInstantaneously();
      service.networkConnectUninspected.assertCompromisedInstantaneously();
	}


	@AfterEach
    public void deleteModel() {
        Asset.allAssets.clear();
        AttackStep.allAttackSteps.clear();
        Defense.allDefenses.clear();
   }


}


