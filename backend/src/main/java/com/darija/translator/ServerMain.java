package com.darija.translator;

import com.darija.translator.presentation.BasicAuthFilter;
import com.darija.translator.presentation.CorsFilter;
import com.darija.translator.presentation.TranslatorResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Embedded Grizzly2 HTTP Container Launcher for the Jakarta RESTful Web Service.
 * Deploys the 3-tier application at http://localhost:8080/api/translator
 */
public class ServerMain {

    public static final String BASE_URI = "http://0.0.0.0:8080/api/";

    public static HttpServer startServer() {
        final ResourceConfig config = new ResourceConfig()
                .register(TranslatorResource.class)
                .register(BasicAuthFilter.class)
                .register(CorsFilter.class)
                .register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final HttpServer server = startServer();
        System.out.println("==================================================================");
        System.out.println(" Moroccan Darija LLM Translator REST Service Started Successfully!");
        System.out.println(" Base URL        : http://localhost:8080/api/translator");
        System.out.println(" Translate (POST): http://localhost:8080/api/translator/translate");
        System.out.println(" Translate (GET) : http://localhost:8080/api/translator/translate?text=Hello");
        System.out.println(" Speech-to-Darija: http://localhost:8080/api/translator/speech-to-darija");
        System.out.println(" Read-Aloud (TTS): http://localhost:8080/api/translator/tts");
        System.out.println(" History (GET)   : http://localhost:8080/api/translator/history");
        System.out.println(" Auth Credentials: admin / password123 (Basic Auth)");
        System.out.println("==================================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        Thread.currentThread().join();
    }
}

