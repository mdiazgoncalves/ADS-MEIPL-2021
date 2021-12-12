package pt.iul.ista.ads.heroku;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.servlet.ServletContainer;

import io.swagger.v3.jaxrs2.integration.OpenApiServlet;

/**
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application. The Java
 * command that is used for launching should fire this main method.
 */
public class Main {

    public static void main(String[] args) throws Exception{
        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }
        
        Server server = new Server(Integer.valueOf(webPort));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        ServletHolder apiServlet = context.addServlet(ServletContainer.class, "/*");
        apiServlet.setInitOrder(1);
        apiServlet.setInitParameter("jersey.config.server.provider.packages", "pt.iul.ista.ads.services,pt.iul.ista.ads.exceptionmappers");
        
        ServletHolder swaggerServlet = context.addServlet(OpenApiServlet.class, "/swagger");
        swaggerServlet.setInitOrder(2);

        FilterHolder filterHolder = context.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        filterHolder.setInitParameter("allowedOrigins", "*");
        filterHolder.setInitParameter("allowedMethods", "GET, HEAD, POST, PUT, OPTIONS, DELETE");
        
        server.start();
        server.join();
    }
}
