package org.example.canton.orchestrate;

import org.example.canton.contact.receiver.PendingHoldings;
import org.example.canton.deploy.DeployDar;
import org.example.canton.deploy.ReadDeployedPackage;
import org.example.canton.ledger.LedgerOffsetClient;
import org.example.canton.user.onboarding.CreateUser;
import org.example.canton.user.onboarding.PartyOnboarding;
import org.example.canton.user.onboarding.UserToPartyMapping;
import org.example.canton.util.TokenGenerator;

import java.util.List;
import java.util.Properties;

public class CantonOrchestratorUserNode {

    public static void main(String[] args) throws Exception {
        Properties props = System.getProperties();
        props.put("user1_jsonapibase", "http://localhost:2975");

        props.put("user1_realm", "AppUser");
        props.put("user1_admin_clientid", "app-user-validator");
        props.put("user1_admin_clientsecret", "6m12QyyGl81d9nABWQXMycZdXho6ejEX");


        props.put("user1_holder1_clientid", "user1-holder1");
        props.put("user1_holder1_clientsecret", "vwSIO9JPiZduCbBWEChWUExW95JGevcG");
        props.put("holder1_party", "receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b");
        props.put("holder2_party", "receiverParty1::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b");



        props.put("keycloakbase_url", "http://localhost:8082");
        props.put("dar_path", "/Users/thomaseapen/IdeaProjects/canton-poc/daml/.daml/dist/my-token2-0.0.2.dar");


        /****
         * Figure out the template id of your dar
         * daml damlc inspect-dar --json /Users/thomaseapen/IdeaProjects/canton-poc/daml/.daml/dist/my-token1-0.0.2.dar
         * Look for ---> "main_package_id": "6771ae179d3feb2f3e36b31ab0933374e7274c9731305f8cdd885d36418ea5a2"
         */

        props.put("drl_package_id", "6771ae179d3feb2f3e36b31ab0933374e7274c9731305f8cdd885d36418ea5a2");

        String darDeployUserNodeReponse = new DeployDar().deployDar(System.getProperty("user1_jsonapibase"), System.getProperty("dar_path"), System.getProperty("user1_realm"), System.getProperty("user1_admin_clientid"), System.getProperty("user1_admin_clientsecret"));
        System.out.println("darDeployUserNodeReponse : "+ darDeployUserNodeReponse);

        /*****
         * Read the deployed packages
         */

        String deployedPakagesInIssuerNode = new ReadDeployedPackage().getPackages(System.getProperty("user1_jsonapibase"),System.getProperty("user1_realm"), System.getProperty("user1_admin_clientid"), System.getProperty("user1_admin_clientsecret"), System.getProperty("drl_package_id"));
        System.out.println("deployedPakagesInIssuerNode : "+ deployedPakagesInIssuerNode);



        /****
         * Create receiverparty party on Provider Node
         * holderPArtyCreateResponse : {"partyDetails":{"party":"receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b","isLocal":true,"localMetadata":{"resourceVersion":"0","annotations":{}},"identityProviderId":""}}
         * holderPArtyCreateResponse : {"partyDetails":{"party":"receiverParty1::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b","isLocal":true,"localMetadata":{"resourceVersion":"0","annotations":{}},"identityProviderId":""}}
         */

        /*String holderPArtyCreateResponse = new PartyOnboarding().createParty(System.getProperty("user1_jsonapibase"), System.getProperty("user1_realm"), System.getProperty("user1_admin_clientid"), System.getProperty("user1_admin_clientsecret"), "receiverParty", "Receiver Party");
        System.out.println("holderPArtyCreateResponse : "+ holderPArtyCreateResponse);*/

       //Create another party for transfer
        /*String holderPArtyCreateResponse = new PartyOnboarding().createParty(System.getProperty("user1_jsonapibase"), System.getProperty("user1_realm"), System.getProperty("user1_admin_clientid"), System.getProperty("user1_admin_clientsecret"), "receiverParty1", "Receiver Party1");
        System.out.println("holderPArtyCreateResponse : "+ holderPArtyCreateResponse);*/
        


        /****
         * TokenGenerator.generateToken(System.getProperty("user1_realm"), System.getProperty("user1_holder1_clientid"), System.getProperty("user1_holder1_clientsecret"));
         * Create User on the AppUSer. I user user1-holder1. Genertae the secret and extract the sub value: 0bb575d1-b3ef-46aa-bb75-36e3fb8dc5e1(using above command to check the token
         * USe the receiver id created from above step
         * createholderUserOnUserNode : {"user":{"id":"0bb575d1-b3ef-46aa-bb75-36e3fb8dc5e1","primaryParty":"receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b","isDeactivated":false,"metadata":{"resourceVersion":"0","annotations":{}},"identityProviderId":""}}
         */

        //String createholderUserOnUserNode = new CreateUser().createParticipantUser(System.getProperty("user1_jsonapibase"), System.getProperty("user1_realm"), System.getProperty("user1_admin_clientid"), System.getProperty("user1_admin_clientsecret"), "0bb575d1-b3ef-46aa-bb75-36e3fb8dc5e1", "receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b");
        //System.out.println("createholderUserOnUserNode : "+ createholderUserOnUserNode);


        /**
         * Set the CanActAs for the user to the party. Remeber Wee will need to use sub claim id 972c49c6-ee4f-4bcd-a012-2e34902b7cb0 -> This can be found from the token egenrated
         * canActAsUserNoderResp : {"newlyGrantedRights":[{"kind":{"CanActAs":{"value":{"party":"receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b"}}}}]}
         */

     /*String canActAsUserNoderResp = new UserToPartyMapping().grantActAs(System.getProperty("user1_jsonapibase"),
             System.getProperty("user1_realm"),
             System.getProperty("user1_admin_clientid"),
             System.getProperty("user1_admin_clientsecret"),
             "0bb575d1-b3ef-46aa-bb75-36e3fb8dc5e1",
             "receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b");
     System.out.println("canActAsUserNoderResp : "+ canActAsUserNoderResp);


     String canActAsUserNoderResp = new UserToPartyMapping().grantActAs(System.getProperty("user1_jsonapibase"),
             System.getProperty("user1_realm"),
             System.getProperty("user1_admin_clientid"),
             System.getProperty("user1_admin_clientsecret"),
             "0bb575d1-b3ef-46aa-bb75-36e3fb8dc5e1",
             "receiverParty1::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b");
     System.out.println("canActAsUserNoderResp : "+ canActAsUserNoderResp);*/

        /**
         * Set the canReadAs as well
         *canActAsUserNoderResp : {"newlyGrantedRights":[{"kind":{"CanReadAs":{"value":{"party":"receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b"}}}}]}
         */

        /*String canReadAsUserNoderResp = new UserToPartyMapping().grantReadAs(System.getProperty("user1_jsonapibase"),
                System.getProperty("user1_realm"),
                System.getProperty("user1_admin_clientid"),
                System.getProperty("user1_admin_clientsecret"),
                "0bb575d1-b3ef-46aa-bb75-36e3fb8dc5e1",
                "receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b");
        System.out.println("canActAsUserNoderResp : "+ canReadAsUserNoderResp);

        String canReadAsUserNoderResp = new UserToPartyMapping().grantReadAs(System.getProperty("user1_jsonapibase"),
                System.getProperty("user1_realm"),
                System.getProperty("user1_admin_clientid"),
                System.getProperty("user1_admin_clientsecret"),
                "0bb575d1-b3ef-46aa-bb75-36e3fb8dc5e1",
                "receiverParty1::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b");
        System.out.println("canActAsUserNoderResp : "+ canReadAsUserNoderResp);*/

/**
 * USe latest offset - need to figure out how
 */ 
                Long latestOffset = new LedgerOffsetClient().getLedgerEnd(
                System.getProperty("user1_jsonapibase"),
                System.getProperty("user1_realm"),
                System.getProperty("user1_holder1_clientid"),
                System.getProperty("user1_holder1_clientsecret")
        );


        String getHoldings = new PendingHoldings().findHoldingContractIds(System.getProperty("user1_jsonapibase"),
                System.getProperty("user1_realm"),
                System.getProperty("user1_holder1_clientid"),
                System.getProperty("user1_holder1_clientsecret"),
                System.getProperty("drl_package_id"),
                System.getProperty("holder1_party"),
                latestOffset);
        System.out.println("getHoldings : "+ getHoldings);

        List<String> holdingCids = new PendingHoldings().extractHoldingCids(getHoldings);
        if (!holdingCids.isEmpty()) {
            String transferHoldingResponse = new PendingHoldings().transferHolding(
                    System.getProperty("user1_jsonapibase"),
                    System.getProperty("user1_realm"),
                    System.getProperty("user1_holder1_clientid"),
                    System.getProperty("user1_holder1_clientsecret"),
                    System.getProperty("drl_package_id"),
                    System.getProperty("holder1_party"),
                    holdingCids.get(0),
                    System.getProperty("holder2_party"),
                    "5"
            );
            System.out.println("transferHoldingResponse : " + transferHoldingResponse);
        
        }
    }
}
