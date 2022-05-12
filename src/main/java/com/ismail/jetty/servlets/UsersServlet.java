package com.ismail.jetty.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * https://www.vogella.com/tutorials/Jetty/article.html
 * 
 * publishes list of users
 * 
 * @author ismail
 */
public class UsersServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        response.setContentType("application/json");
        
        response.setStatus(HttpServletResponse.SC_OK);
        
        PrintWriter out = response.getWriter();
        
        out.print("[");
        out.print("{\"username\": \"ali\"},");
        out.print("{\"username\": \"john\"},");
        out.print("{\"username\": \"smith\"},");
        out.print("{\"username\": \"alex\"}");
        out.print("]");
        
        out.flush();
        
    }

}
