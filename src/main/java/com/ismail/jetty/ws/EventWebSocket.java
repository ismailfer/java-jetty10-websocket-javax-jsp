//
// ========================================================================
// Copyright (c) Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package com.ismail.jetty.ws;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import lombok.extern.slf4j.Slf4j;

@ClientEndpoint
@ServerEndpoint(value = "/events/")
@Slf4j
public class EventWebSocket
{
    private final CountDownLatch closureLatch = new CountDownLatch(1);

    private Session sess = null;    
    
    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        System.out.println("Socket Connected: " + sess);
        
        this.sess = sess;
        
        sendMessagePeriodically();
    }

    @OnMessage
    public void onWebSocketText(Session sess, String message) throws IOException
    {
        System.out.println("Received TEXT message: " + message);

        if (message.toLowerCase(Locale.US).contains("bye"))
        {
            sess.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Thanks"));
        }
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("Socket Closed: " + reason);
        closureLatch.countDown();
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }

    public void awaitClosure() throws InterruptedException
    {
        System.out.println("Awaiting closure from remote");
        closureLatch.await();
    }
    
    public void sendMessagePeriodically()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                int msgCount=0;
                
                try
                {
                    while (true)
                    {
                        
                        if (sess.isOpen())
                        {
                            msgCount++;
                            
                            StringBuilder sb = new StringBuilder();
                            sb.append("{");
                            sb.append("\"socket\":\"" + hashCode() + "\"");
                            sb.append(",");
                            sb.append("\"session\":\"" + sess.hashCode() + "\"");
                            sb.append(",");
                            sb.append("\"msg\":\"" + msgCount + "\"");
                            sb.append("}");
                            
                            log.info(hashCode() + ".sendMessagePeriodically() >> " + sess.hashCode() + ": " + sb.toString());
                            
                            sess.getBasicRemote().sendText(sb.toString());
                        }
                        else
                        {
                            break;
                        }
                        
                        sleep(2000L);
                    }
                    
                    
                } catch (Throwable tt)
                {
                    tt.printStackTrace();
                }
            }
        };

        t.setDaemon(true);
        t.start();
    }
}
