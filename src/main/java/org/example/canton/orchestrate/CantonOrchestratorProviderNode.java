package org.example.canton.orchestrate;

import org.example.canton.contact.init.InitializeContract;
import org.example.canton.contact.mint.MintRequest;
import org.example.canton.deploy.DeployDar;
import org.example.canton.deploy.ReadDeployedPackage;
import org.example.canton.user.onboarding.CreateUser;
import org.example.canton.user.onboarding.PartyOnboarding;
import org.example.canton.user.onboarding.UserToPartyMapping;

import java.util.Properties;

public class CantonOrchestratorProviderNode {

    public static void main(String[] args) throws Exception {
        Properties props = System.getProperties();
        props.put("issuer_jsonapibase", "http://localhost:3975");
        props.put("issuer_realm", "AppProvider");
        props.put("issuer_clientid", "app-provider-validator");
        props.put("issuer_clientsecret", "AL8648b9SfdTFImq7FV56Vd0KHifHBuC");

        props.put("issuer_minter_clientid", "app-user-minter");
        props.put("issuer_minter_clientsecret", "FDeeAc1WQztjGMxaGVmSI5MSaBZ0eIr5");


        props.put("user1_jsonapibase", "http://localhost:2975");
        props.put("user1_realm", "AppUser");
        props.put("user1_clientid", "app-user-validator");
        props.put("user1_clientsecret", "6m12QyyGl81d9nABWQXMycZdXho6ejEX");

        props.put("keycloakbase_url", "http://localhost:8082");
        props.put("dar_path", "/Users/thomaseapen/IdeaProjects/canton-poc/daml/.daml/dist/my-token2-0.0.2.dar");

        //System.setProperties(props);

        /****
         * Figure out the template id of your dar
         * daml damlc inspect-dar --json /Users/thomaseapen/IdeaProjects/canton-poc/daml/.daml/dist/my-token1-0.0.2.dar
         * Look for ---> "main_package_id": "6771ae179d3feb2f3e36b31ab0933374e7274c9731305f8cdd885d36418ea5a2"
         */

        props.put("drl_package_id", "6771ae179d3feb2f3e36b31ab0933374e7274c9731305f8cdd885d36418ea5a2");

        /****
         * Deployment of the DAR can only be done once
         */
        String darDeployIssuerNodeReponse = new DeployDar().deployDar(System.getProperty("issuer_jsonapibase"), System.getProperty("dar_path"), System.getProperty("issuer_realm"), System.getProperty("issuer_clientid"), System.getProperty("issuer_clientsecret"));
        System.out.println("darDeployIssuerNodeReponse : " + darDeployIssuerNodeReponse);


        /*****
         * Read the deployed packages
         */

        String deployedPakagesInIssuerNode = new ReadDeployedPackage().getPackages(System.getProperty("issuer_jsonapibase"), System.getProperty("issuer_realm"), System.getProperty("issuer_clientid"), System.getProperty("issuer_clientsecret"), System.getProperty("drl_package_id"));
        System.out.println("deployedPakagesInIssuerNode : " + deployedPakagesInIssuerNode);

        /****
         * Create issuer party on Provider Node
         * Response - issuerPArtyCreateResponse : {"partyDetails":{"party":"issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549","isLocal":true,"localMetadata":{"resourceVersion":"0","annotations":{}},"identityProviderId":""}}
         */

        /*String issuerPArtyCreateResponse = new PartyOnboarding().createParty(System.getProperty("issuer_jsonapibase"), System.getProperty("issuer_realm"), System.getProperty("issuer_clientid"), System.getProperty("issuer_clientsecret"), "issuerParty", "Issuer Party");
        System.out.println("issuerPArtyCreateResponse : "+ issuerPArtyCreateResponse);*/

        /**
         *  Create a user in Provider keycloak with client_id=app-user-minter. User has a prefix serice-account and hence we will sub claim id 972c49c6-ee4f-4bcd-a012-2e34902b7cb0 -> This can be found from the token egenrated
         *  Then this principal will be onboarded as a user in the provider node. Also supply the party careted above as the default primaryPArty - default role it will act as
         *  createMinterUSerOnProviderNodeREsponse : {"user":{"id":"972c49c6-ee4f-4bcd-a012-2e34902b7cb0","primaryParty":"issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549","isDeactivated":false,"metadata":{"resourceVersion":"0","annotations":{}},"identityProviderId":""}}
         */


        /*String createMinterUSerOnProviderNodeREsponse = new CreateUser().createParticipantUser(System.getProperty("issuer_jsonapibase"), System.getProperty("issuer_realm"), System.getProperty("issuer_clientid"), System.getProperty("issuer_clientsecret"), "972c49c6-ee4f-4bcd-a012-2e34902b7cb0", "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549");
        System.out.println("createMinterUSerOnProviderNodeREsponse : "+ createMinterUSerOnProviderNodeREsponse);*/


        /**
         * Set the CanActAs for the user to the party. We will need to use sub claim id 972c49c6-ee4f-4bcd-a012-2e34902b7cb0 -> This can be found from the token egenrated
         * canActAsIssuerPRoviderNoderResp : {"newlyGrantedRights":[{"kind":{"CanActAs":{"value":{"party":"issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549"}}}}]}
         */

     /*String canActAsIssuerPRoviderNoderResp = new UserToPartyMapping().grantActAs(System.getProperty("issuer_jsonapibase"),
             System.getProperty("issuer_realm"),
             System.getProperty("issuer_clientid"),
             System.getProperty("issuer_clientsecret"),
             "972c49c6-ee4f-4bcd-a012-2e34902b7cb0",
             "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549");
     System.out.println("canActAsIssuerPRoviderNoderResp : "+ canActAsIssuerPRoviderNoderResp);*/


        /**
         * Set the CanReadAs for the user to the party. We will need to use sub claim id 972c49c6-ee4f-4bcd-a012-2e34902b7cb0 -> This can be found from the token egenrated
         * canReadAsIssuerPRoviderNoderResp : {"newlyGrantedRights":[{"kind":{"CanReadAs":{"value":{"party":"issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549"}}}}]}
         */

     /*String canReadAsIssuerPRoviderNoderResp = new UserToPartyMapping().grantReadAs(System.getProperty("issuer_jsonapibase"),
             System.getProperty("issuer_realm"),
             System.getProperty("issuer_clientid"),
             System.getProperty("issuer_clientsecret"),
             "972c49c6-ee4f-4bcd-a012-2e34902b7cb0",
             "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549");
     System.out.println("canReadAsIssuerPRoviderNoderResp : "+ canReadAsIssuerPRoviderNoderResp);*/


        /****
         * Deploy the InstrumentConfig(defines the token name and details of token) and MintAuthority(wjo is allowed to mint) on issuer node
         *issuerConfigResp : {"updateId":"1220e4b6938a44fff4111541dd177a862998972a516c5cbf6d019b15e69c4d41c0d0","completionOffset":10470}
         * mintAutorityResponse : {"updateId":"12203a9513f85baf9d11eb6735245d365bfeadcf2967aa1c534df1735384eec8ac23","completionOffset":10473}
         */

   /*String issuerConfigResp = new InitializeContract().createIssuerConfig(
             System.getProperty("issuer_jsonapibase"),
             System.getProperty("issuer_realm"),
             System.getProperty("issuer_minter_clientid"),
             System.getProperty("issuer_minter_clientsecret"),
             System.getProperty("drl_package_id"),
             "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549",
             "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549",
             "USD_TOKEN",
             "Demo USD Token",
             "dUSD"
     );

     System.out.println("issuerConfigResp : "+ issuerConfigResp);

     String mintAutorityResponse = new InitializeContract().createMintAuthority(
             System.getProperty("issuer_jsonapibase"),
             System.getProperty("issuer_realm"),
             System.getProperty("issuer_minter_clientid"),
             System.getProperty("issuer_minter_clientsecret"),
             System.getProperty("drl_package_id"),
             "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549",
             "USD_TOKEN"

     );

     System.out.println("mintAutorityResponse : "+ mintAutorityResponse);*/


        /*String receiverParty = "receiverParty::12200c976252f32c7f6328d158172a5ff7e4eddbd3e5db9116560478587433ba7f4b";//Generated after party creation on user node
        String issuerParty = "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549";//Generated after creating issuer party on provider node
        //Fetch the mintauthority(offset should be from the offset coming from mintAutorityResponse
        String mintAuthorityContractId = new InitializeContract().findMintAuthorityContractId(System.getProperty("issuer_jsonapibase"), System.getProperty("issuer_realm"),
                System.getProperty("issuer_minter_clientid"),
                System.getProperty("issuer_minter_clientsecret"),
                System.getProperty("drl_package_id"),
                "issuerParty::12201b77d72e0ed7442d250d61ca79319569f98cbfd215a044a74531ae86ba6b1549",
                "USD_TOKEN",
                10473L
        );
        System.out.println("mintAuthorityContractId : " + mintAuthorityContractId);

    String mintResponse = new MintRequest().mintToUser(System.getProperty("issuer_jsonapibase"), System.getProperty("issuer_realm"),
            System.getProperty("issuer_minter_clientid"),
            System.getProperty("issuer_minter_clientsecret"),
            System.getProperty("drl_package_id"),
            issuerParty,
            mintAuthorityContractId,
            receiverParty,
            "100"
            );

    System.out.println("mintResponse : " + mintResponse);*/

    }
    }

