package com.ismail.jetty;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer;

import com.ismail.jetty.ws.EventWebSocket;

import lombok.extern.slf4j.Slf4j;

/**
 * Runs a simple Jetty server with Websocket
 * 
 */
@Slf4j
public class JettyServer
{

    private final Server server;
    private final ServerConnector connector;
    private ServletContextHandler context;
    
    public JettyServer()
    {
        server = new Server();
        connector = new ServerConnector(server);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        server.setHandler(context);

        // Initialize javax.websocket layer
        JavaxWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) ->
        {
            // This lambda will be called at the appropriate place in the
            // ServletContext initialization phase where you can initialize
            // and configure  your websocket container.

            // Configure defaults for container
            wsContainer.setDefaultMaxTextMessageBufferSize(65535);

            
            
            // Add WebSocket endpoint to javax.websocket layer
            wsContainer.addEndpoint(EventWebSocket.class);
        });
    }

    public void setPort(int port)
    {
        connector.setPort(port);
    }

    public void start() throws Exception
    {
        server.start();
    }

    public URI getURI()
    {
        return server.getURI();
    }

    public void stop() throws Exception
    {
        server.stop();
    }

    public void join() throws InterruptedException
    {
        System.out.println("Use Ctrl+C to stop server");
        server.join();
    }
    
    public static void main(String[] args) throws Exception
    {
        JettyServer server = new JettyServer();
        server.setPort(9080);
        server.start();
        server.join();
    }

}
