package com.ismail.jetty.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * https://www.vogella.com/tutorials/Jetty/article.html
 * 
 * @author ismail
 */
public class ExampleServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        response.setContentType("application/json");
        
        response.setStatus(HttpServletResponse.SC_OK);
        
        response.getWriter().println("{ \"hello\": \"world\"}");
    }

}
