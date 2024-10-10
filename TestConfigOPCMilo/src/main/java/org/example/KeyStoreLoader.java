package org.example;

//этот класс отвечает за управление жизненным циклом хранилища ключей,
// используемого сервером OPC UA. Он может либо загрузить существующее хранилище ключей,
// либо создать новое с самозаверяющим сертификатом, если такового не существует.
// Загруженная информация хранилища ключей (сертификат, цепочка сертификатов и пара ключей)
// затем становится доступной через предоставленные методы получения.

import com.google.common.collect.Sets;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

class KeyStoreLoader {

    //Определяет шаблон ip adress-a
    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    // Псевдоним используемый для хранения ключа(-ей) в хранилище ключей
    private static final String SERVER_ALIAS = "server-ai";

    // Пароль используемый для хранения ключа(-ей) в хранилище ключей
    private static final char[] PASSWORD = "password".toCharArray();

    private final Logger logger = LoggerFactory.getLogger(getClass());


    //массив в котором хранится цепочка сертификатов
    private X509Certificate[] serverCertificateChain;

    //Хранится собственный сертификат
    private X509Certificate serverCertificate;

    //хранится пара ключей
    private KeyPair serverKeyPair;

    KeyStoreLoader load(Path baseDir) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        File serverKeyStore = baseDir.resolve("example-server.pfx").toFile();

        logger.info("Loading KeyStore at {}", serverKeyStore);

        if (!serverKeyStore.exists()) {
            keyStore.load(null, PASSWORD);

            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

            String applicationUri = "urn:eclipse:milo:examples:server:" + UUID.randomUUID();

            //используется для создания самозаверяющего сертификата
            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                    //Общее имя (имя сервера)
                    .setCommonName("Eclipse Milo Example Server")
                    //Детали организации
                    .setOrganization("digitalpetri")
                    .setOrganizationalUnit("dev")
                    .setLocalityName("Folsom")
                    //Код страны
                    .setStateName("CA")
                    .setCountryCode("US")
                    //URI приложения
                    .setApplicationUri(applicationUri);

            // Get as many hostnames and IP addresses as we can listed in the certificate.
            //Код извлекает все возможные имена хостов и IP-адреса сервера, используя HostnameUtil.
            Set<String> hostnames = Sets.union(
                    Sets.newHashSet(HostnameUtil.getHostname()),
                    HostnameUtil.getHostnames("0.0.0.0", false)
            );
            //КОД перебирает полученные имена хостов, проверяя, являются ли они IP-адресами,
            // используя определенный шаблон.
            for (String hostname : hostnames) {
                if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
                    builder.addIpAddress(hostname);
                } else {
                    builder.addDnsName(hostname);
                }
            }
            //Создает самозаверяющий сертификат
            X509Certificate certificate = builder.build();
            //Сертификат и пара ключей хранятся в хранилище ключей с использованием заданного псевдонима и пароля.
            keyStore.setKeyEntry(SERVER_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});

            keyStore.store(new FileOutputStream(serverKeyStore), PASSWORD);
        } else {
            keyStore.load(new FileInputStream(serverKeyStore), PASSWORD);
        }

        Key serverPrivateKey = keyStore.getKey(SERVER_ALIAS, PASSWORD);
        if (serverPrivateKey instanceof PrivateKey) {
            serverCertificate = (X509Certificate) keyStore.getCertificate(SERVER_ALIAS);

            serverCertificateChain = Arrays.stream(keyStore.getCertificateChain(SERVER_ALIAS))
                    .map(X509Certificate.class::cast)
                    .toArray(X509Certificate[]::new);

            PublicKey serverPublicKey = serverCertificate.getPublicKey();

            //объект, объединяющий полученные открытый и закрытый ключи.
            serverKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
        }

        return this;
    }

    //метод возвращает сертификат X.509 сервера.
    X509Certificate getServerCertificate() {
        return serverCertificate;
    }

    //метод возвращает массив сертификатов X.509, образующих цепочку сертификатов.
    public X509Certificate[] getServerCertificateChain() {
        return serverCertificateChain;
    }

    //этот метод возвращает KeyPair объект, содержащий открытый и закрытый ключи сервера.
    KeyPair getServerKeyPair() {
        return serverKeyPair;
    }

}

