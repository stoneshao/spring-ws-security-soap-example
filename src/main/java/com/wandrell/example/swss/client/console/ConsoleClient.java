/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 the original author or authors.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.wandrell.example.swss.client.console;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.client.WebServiceIOException;

import com.wandrell.example.swss.client.EntityClient;
import com.wandrell.example.ws.generated.entity.Entity;

/**
 * Runnable shell-based client.
 *
 * @author Bernardo Martínez Garrido
 */
public class ConsoleClient {

    private static final String ENCRYPTION_WSS4J      = "encryption_wss4j";
    private static final String ENCRYPTION_XWSS       = "encryption_xwss";
    private static final String ENDPOINT_URL_TEMPLATE = "http://localhost:8080/swss%s";
    private static final String PASSWORD_DIGEST_WSS4J = "password_digest_wss4j";
    private static final String PASSWORD_DIGEST_XWSS  = "password_digest_xwss";
    private static final String PASSWORD_PLAIN_WSS4J  = "password_plain_wss4j";
    private static final String PASSWORD_PLAIN_XWSS   = "password_plain_xwss";
    private static final String PROPERTY_ENDPOINT_URI = "wsdl.locationUri";
    private static final String SIGNATURE_WSS4J       = "signature_wss4j";
    private static final String SIGNATURE_XWSS        = "signature_xwss";
    private static final String UNSECURE              = "unsecure";

    /**
     * Main runnable method.
     *
     * @param args
     *            command line arguments
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        final PrintStream output;

        output = System.out;

        printTitle(output);
        printHelp(output);

        runMainLoop(output, getClients(), getUris());
    }

    private static final void callEndpoint(final EntityClient client,
            final String uri, final PrintStream output, final Scanner scanner) {
        final Entity entity;
        final Integer id;

        output.print("Give the id of the entity to query: ");
        id = getInteger(scanner);

        output.println();
        output.println("*****************************************************");
        output.println(String.format("Querying %s", uri));

        try {
            entity = client.getEntity(uri, id);

            if (entity == null) {
                output.println(
                        String.format("No entity with id %d exists", id));
            } else {
                output.println(String.format(
                        "Received entity with id %1$d and name %2$s",
                        entity.getId(), entity.getName()));
            }
        } catch (final WebServiceIOException e) {
            output.println(String.format("Error: %s",
                    e.getMostSpecificCause().getMessage()));
        }

        output.println("*****************************************************");

        waitForKeyPress(output, scanner);
    }

    private static final Map<String, EntityClient> getClients()
            throws IOException {
        final Map<String, EntityClient> clients = new LinkedHashMap<String, EntityClient>();

        clients.put(UNSECURE,
                getEntityClient("context/client/client-unsecure.xml"));
        clients.put(PASSWORD_PLAIN_XWSS, getEntityClient(
                "context/client/password/plain/xwss/client-password-plain-xwss.xml",
                "context/client/password/plain/xwss/client-password-plain-xwss.properties"));
        clients.put(PASSWORD_PLAIN_WSS4J, getEntityClient(
                "context/client/password/plain/wss4j/client-password-plain-wss4j.xml"));
        clients.put(PASSWORD_DIGEST_XWSS, getEntityClient(
                "context/client/password/digest/xwss/client-password-digest-xwss.xml",
                "context/client/password/digest/xwss/client-password-digest-xwss.properties"));
        clients.put(PASSWORD_DIGEST_WSS4J, getEntityClient(
                "context/client/password/digest/wss4j/client-password-digest-wss4j.xml"));
        //clients.put(SIGNATURE_XWSS, getEntityClient(
        //        "context/client/signature/xwss/client-signature-xwss.xml",
        //        "context/client/signature/xwss/client-signature-xwss.properties",
        //        "context/keystore/keystore.properties"));
        //clients.put(SIGNATURE_WSS4J, getEntityClient(
        //        "context/client/signature/wss4j/client-signature-wss4j.xml",
        //        "context/keystore/keystore.properties",
        //        "context/keystore/keystore-wss4j.properties"));
        //clients.put(ENCRYPTION_XWSS, getEntityClient(
        //        "context/client/encryption/xwss/client-encryption-xwss.xml",
        //        "context/client/encryption/xwss/client-encryption-xwss.properties",
        //        "context/keystore/keystore.properties"));
        //clients.put(ENCRYPTION_WSS4J, getEntityClient(
        //        "context/client/encryption/wss4j/client-encryption-wss4j.xml",
        //        "context/keystore/keystore.properties",
        //        "context/keystore/keystore-wss4j.properties"));

        return clients;
    }

    private static final String getEndpointUri(final String propertiesPath)
            throws IOException {
        final Properties properties;

        properties = new Properties();
        properties.load(new ClassPathResource(propertiesPath).getInputStream());

        return String.format(ENDPOINT_URL_TEMPLATE,
                (String) properties.get(PROPERTY_ENDPOINT_URI));
    }

    private static final EntityClient getEntityClient(final String contextPath,
            final String... propertiesPath) throws IOException {
        final ClassPathXmlApplicationContext context;
        final PropertyPlaceholderConfigurer configurer;
        final Properties propertiesBase;
        final EntityClient client;
        final Collection<Properties> propertiesCol;

        propertiesBase = new Properties();
        propertiesBase
                .load(new ClassPathResource("context/client/client.properties")
                        .getInputStream());

        propertiesCol = new LinkedList<Properties>();
        propertiesCol.add(propertiesBase);
        for(final String path : propertiesPath){
            propertiesBase.load(
                    new ClassPathResource(path).getInputStream());
        }
        
        configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(propertiesBase);

        context = new ClassPathXmlApplicationContext(contextPath);
        context.addBeanFactoryPostProcessor(configurer);

        client = context.getBean(EntityClient.class);

        context.close();

        return client;
    }

    private static final Integer getInteger(final Scanner scanner) {
        Integer value = null;
        Boolean valid;

        valid = false;
        while (!valid) {
            valid = scanner.hasNextInt();
            if (valid) {
                value = scanner.nextInt();
            } else {
                scanner.nextLine();
            }
        }

        return value;
    }

    private static final Map<String, String> getUris() throws IOException {
        final Map<String, String> uris = new LinkedHashMap<String, String>();

        uris.put(UNSECURE, getEndpointUri(
                "context/endpoint/endpoint-unsecure.properties"));
        uris.put(PASSWORD_PLAIN_XWSS, getEndpointUri(
                "context/client/password/plain/xwss/client-password-plain-xwss.xml"));
        uris.put(PASSWORD_PLAIN_WSS4J, getEndpointUri(
                "context/client/password/plain/wss4j/client-password-plain-wss4j.xml"));
        uris.put(PASSWORD_DIGEST_XWSS, getEndpointUri(
                "context/client/password/digest/xwss/client-password-digest-xwss.xml"));
        uris.put(PASSWORD_DIGEST_WSS4J, getEndpointUri(
                "context/client/password/digest/wss4j/client-password-digest-wss4j.xml"));
        uris.put(SIGNATURE_XWSS, getEndpointUri(
                "context/client/signature/xwss/client-signature-xwss.xml"));
        uris.put(SIGNATURE_WSS4J, getEndpointUri(
                "context/client/signature/wss4j/client-signature-wss4j.xml"));
        uris.put(ENCRYPTION_XWSS, getEndpointUri(
                "context/client/encryption/xwss/client-encryption-xwss.xml"));
        uris.put(ENCRYPTION_WSS4J, getEndpointUri(
                "context/client/encryption/wss4j/client-encryption-wss4j.xml"));

        return uris;
    }

    private static final void printClientOptions(final PrintStream output) {
        output.println("Choose a client:");
        output.println();
        output.println("1.- Unsecure");
    }

    private static final void printHelp(final PrintStream output) {
        output.println("-------------------------------------------------");
        output.println("Pick an option. Write 'exit' to close the client.");
        output.println("-------------------------------------------------");
    }

    private static final void printTitle(final PrintStream output) {
        output.println();
        output.println("Spring WSS example console client.");
        output.println();
    }

    private static final void runMainLoop(final PrintStream output,
            final Map<String, EntityClient> clients,
            final Map<String, String> uris) {
        final Scanner scanner = new Scanner(System.in);
        EntityClient client = null;
        String uri;
        String command;

        do {
            output.println();
            printClientOptions(output);
            output.println();
            output.print("Pick an option: ");
            command = scanner.next();
            output.println();
            switch (command) {
                case "1":
                    client = clients.get(UNSECURE);
                    uri = uris.get(UNSECURE);
                    break;
                default:
                    client = null;
                    uri = null;
            }

            if (client != null) {
                callEndpoint(client, uri, output, scanner);
            }
        } while (!command.equalsIgnoreCase("exit"));

        scanner.close();
    }

    private static final void waitForKeyPress(final PrintStream output,
            final Scanner scanner) {

        output.println();
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        output.println("Press Enter to continue.");
        scanner.nextLine();
    }

    /**
     * Constructs a {@code ShellClient}.
     */
    public ConsoleClient() {
        super();
    }

}
