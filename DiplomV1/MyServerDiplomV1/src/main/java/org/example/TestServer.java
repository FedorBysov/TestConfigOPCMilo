package org.example;

import com.digitalpetri.opcua.sdk.server.OpcUaServer;
import com.digitalpetri.opcua.sdk.server.api.config.OpcUaServerConfig;
import com.digitalpetri.opcua.sdk.server.identity.UsernameIdentityValidator;
import com.digitalpetri.opcua.server.ctt.CttNamespace;
import com.digitalpetri.opcua.stack.core.Stack;
import com.digitalpetri.opcua.stack.core.application.CertificateManager;
import com.digitalpetri.opcua.stack.core.application.CertificateValidator;
import com.digitalpetri.opcua.stack.core.application.DefaultCertificateManager;
import com.digitalpetri.opcua.stack.core.application.DefaultCertificateValidator;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.structured.UserTokenPolicy;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.common.collect.Lists.newArrayList;

public class TestServer {
    private static DatabaseManager dbManager ;
    private static ModbusConnector modbusConnector;
    private final OpcUaServer server;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        TestServer serverExample = new TestServer();
        serverExample.startup();

        try {
            //final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


            dbManager = new DatabaseManager("jdbc:mysql://localhost:3306/sensor_data", "root", "password");
            modbusConnector = new ModbusConnector("WIN-A5SE79QKVGU", 502);
            //telnet WIN-A5SE79QKVGU 12685


            modbusConnector.setTimeout(10000);
            //modbusConnector.connectM();



            //чтение регистров
            double registerValues = modbusConnector.readDoubleValues(0,4 );

            System.out.println("@@@@@@@@@@@@@@ "+ registerValues+"@@@@@@@@@@@@@@@@");

            //for (int value : registerValues) {
                dbManager.insertSensorData(1, registerValues, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                      .format(new Date()));
           // }


//            List<SensorData> dataList = dbManager.readSensorData();
//            dataList.forEach(data -> System.out.println(data));


        } catch (Exception e) {
            e.printStackTrace();
        }

        serverExample.shutdownFuture().get();

    }



    public TestServer() {
        UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(
                true, // allow anonymous access
                authenticationChallenge ->
                        "user".equals(authenticationChallenge.getUsername()) &&
                                "password".equals(authenticationChallenge.getPassword())
        );

        List<UserTokenPolicy> userTokenPolicies = newArrayList(
                OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS,
                OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME
        );

        CertificateManager certificateManager = new DefaultCertificateManager();
        CertificateValidator certificateValidator = new DefaultCertificateValidator(new File("./security"));

        OpcUaServerConfig config = OpcUaServerConfig.builder()
                .setApplicationName(LocalizedText.english("FedorByssov opc-ua server"))
                .setApplicationUri("urn:FedorByssov:opcua:server")
                .setCertificateManager(certificateManager)
                .setCertificateValidator(certificateValidator)
                .setIdentityValidator(identityValidator)
                .setUserTokenPolicies(userTokenPolicies)
                .setProductUri("urn:FedorByssov:opcua:sdk")
                .setServerName("")
                .build();

        server = new OpcUaServer(config);

        // register a CttNamespace so we have some nodes to play with
        server.getNamespaceManager().registerAndAdd(
                CttNamespace.NAMESPACE_URI,
                idx -> new CttNamespace(server, idx));
    }

    public void startup() {
        server.startup();
    }

    private CompletableFuture<Void> shutdownFuture() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            dbManager.disconnect();
            modbusConnector.disconnect();
            Stack.releaseSharedResources();
            future.complete(null);
        }));

        return future;
    }

}

