# WebSockets on Embedded Jetty 10 using Javax with annotation

This project gets few things into one single app; that compiles on java 17, and works!
- Websocket server using jetty
- JSP pages
- Jetty Servlets

## Description

- Uses javax.websockets APIs (annotations)
- Uses embedded Jetty 10 server
- JSP server support
- Uses example Servlets on the same server
- Compiles with java 17 (must be 11+)
- Dependency jars are packaged in a lib folder
- JSP pages are stored in src/main/resources/META-INF/resources
- Package into executable jar (including dependency jars) by Spring-boot-maven-plugin

## Thanks to

https://github.com/jetty-project/embedded-jetty-websocket-examples

https://github.com/jetty-project/embedded-jetty-jsp

