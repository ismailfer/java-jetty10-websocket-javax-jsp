package com.ismail.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;

import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer.Configurator;

import com.ismail.jetty.servlets.ExampleServlet;
import com.ismail.jetty.servlets.UsersServlet;
import com.ismail.jetty.ws.EventWebSocket;

import lombok.extern.slf4j.Slf4j;

/**
 * https://stackoverflow.com/questions/65637135/how-to-enable-jsp-in-a-jetty-server-in-a-jar-file
 * 
 * Starts up the server, including a DefaultServlet that handles static files,
 * and any servlet classes annotated with the @WebServlet annotation.
 */
@Slf4j
public class JettyJspAndWebsocketServer
{
    private Server server;

    private ServerConnector connector;

    private WebAppContext webapp;

    public JettyJspAndWebsocketServer() throws Exception
    {
        // Create a server that listens on port 8080.
        server = new Server();
        connector = new ServerConnector(server);
        server.addConnector(connector);

        webapp = new WebAppContext();
        server.setHandler(webapp);

        // Load static content from inside the jar file.
        URL webAppDir = JettyJspAndWebsocketServer.class.getClassLoader().getResource("META-INF/resources");
        webapp.setResourceBase(webAppDir.toURI().toString());

        // Enable annotations so the server sees classes annotated with @WebServlet.
        webapp.setConfigurations(new Configuration[] { new AnnotationConfiguration(), new WebInfConfiguration(), });

        // Look for annotations in the classes directory (dev server) and in the
        // jar file (live server)
        webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/target/classes/|.*\\.jar");

        // Handle static resources, e.g. html files.
        webapp.addServlet(DefaultServlet.class, "/");

        // add other servlets mappings

        webapp.addServlet(ExampleServlet.class, "/hello");
        webapp.addServlet(UsersServlet.class, "/users");

        webapp.addServlet(UsersServlet.class, "/user");
        webapp.addServlet(UsersServlet.class, "/user-add");

        // Configure JSP support.
        enableEmbeddedJspSupport(webapp);

        // --------------------------------------------------------------------------------------------
        // Initialize javax.websocket layer
        // --------------------------------------------------------------------------------------------
        JavaxWebSocketServletContainerInitializer.configure(webapp, new SocketConfigurer());
    }

    public static class SocketConfigurer implements Configurator
    {
        public void accept(ServletContext servletContext, ServerContainer serverContainer) throws DeploymentException
        {
            // This lambda will be called at the appropriate place in the
            // ServletContext initialization phase where you can initialize
            // and configure  your websocket container.

            // Configure defaults for container
            serverContainer.setDefaultMaxTextMessageBufferSize(65535);

            // Add WebSocket endpoint to javax.websocket layer
            serverContainer.addEndpoint(EventWebSocket.class);
        }
    }

    /**
     * Setup JSP Support for ServletContextHandlers.
     * <p>
     *   NOTE: This is not required or appropriate if using a WebAppContext.
     * </p>
     *
     * @param servletContextHandler the ServletContextHandler to configure
     * @throws IOException if unable to configure
     */
    private void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler) throws IOException
    {
        // Establish Scratch directory for the servlet context (used by JSP compilation)
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

        if (!scratchDir.exists())
        {
            if (!scratchDir.mkdirs())
            {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }
        servletContextHandler.setAttribute("javax.servlet.context.tempdir", scratchDir);

        // Set Classloader of Context to be sane (needed for JSTL)
        // JSP requires a non-System classloader, this simply wraps the
        // embedded System classloader in a way that makes it suitable
        // for JSP to use
        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], JettyJspAndWebsocketServer.class.getClassLoader());
        servletContextHandler.setClassLoader(jspClassLoader);

        // Manually call JettyJasperInitializer on context startup
        servletContextHandler.addBean(new JspStarter(servletContextHandler));

        // Create / Register JSP Servlet (must be named "jsp" per spec)
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.8");
        holderJsp.setInitParameter("compilerSourceVM", "1.8");
        holderJsp.setInitParameter("keepgenerated", "true");
        servletContextHandler.addServlet(holderJsp, "*.jsp");
    }

    /**
     * JspStarter for embedded ServletContextHandlers
     *
     * This is added as a bean that is a jetty LifeCycle on the ServletContextHandler.
     * This bean's doStart method will be called as the ServletContextHandler starts,
     * and will call the ServletContainerInitializer for the jsp engine.
     *
     */
    public class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller
    {
        JettyJasperInitializer sci;

        ServletContextHandler context;

        public JspStarter(ServletContextHandler context)
        {
            this.sci = new JettyJasperInitializer();
            this.context = context;
            this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
        }

        @Override
        protected void doStart() throws Exception
        {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            try
            {
                sci.onStartup(null, context.getServletContext());
                super.doStart();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
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
        JettyJspAndWebsocketServer server = new JettyJspAndWebsocketServer();
        server.setPort(9080);

        server.start();

        server.join();
    }

}