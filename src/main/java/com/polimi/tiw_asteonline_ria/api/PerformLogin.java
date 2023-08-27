package com.polimi.tiw_asteonline_ria.api;

import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.UserDAO;
import com.polimi.tiw_asteonline_ria.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/PerformLogin")
@MultipartConfig
public class PerformLogin extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public PerformLogin() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username, password;

        username = request.getParameter("username");
        password = request.getParameter("password");

        if(username == null || username.isEmpty() || password == null || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Missing credentials")));
            return;
        }

        UserDAO userDAO = new UserDAO(connection);
        User user;
        try {
            user = userDAO.checkCredentials(username, password);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Not possible to verify user")));
            return;
        }
        if(user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Incorrect username or password")));
        } else {
            // User is found
            //request.getSession().setAttribute("user_id", user.getId());
            request.getSession().setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(user));
        }
    }

    @Override
    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

