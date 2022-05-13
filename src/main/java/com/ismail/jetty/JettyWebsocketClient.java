package com.ismail.jetty;

import java.io.IOException;
import java.net.URI;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.util.component.LifeCycle;

import com.ismail.jetty.ws.EventWebSocket;

import lombok.extern.slf4j.Slf4j;

/**
 * Websocket client that conects to a websocket server
 */
@Slf4j
public class JettyWebsocketClient
{

    public void run(URI uri) throws InterruptedException, IOException, DeploymentException
    {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        try
        {
            // Create client side endpoint
            EventWebSocket clientEndpoint = new EventWebSocket();

            // Attempt Connect
            Session session = container.connectToServer(clientEndpoint,uri);

            // Send a message
            session.getBasicRemote().sendText("Hello");

            // Send another message
            session.getBasicRemote().sendText("Goodbye");

            // Wait for remote to close
            clientEndpoint.awaitClosure();

            // Close session
            session.close();
        }
        finally
        {
            // Force lifecycle stop when done with container.
            // This is to free up threads and resources that the
            // JSR-356 container allocates. But unfortunately
            // the JSR-356 spec does not handle lifecycles (yet)
            LifeCycle.stop(container);
        }
    }
    
    public static void main(String[] args)
    {
        JettyWebsocketClient client = new JettyWebsocketClient();
        
        URI uri = URI.create("ws://localhost:9080/events/");
        
        try
        {
            client.run(uri);
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }
}
