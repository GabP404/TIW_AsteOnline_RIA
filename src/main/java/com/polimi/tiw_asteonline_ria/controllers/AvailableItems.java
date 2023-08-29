package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.ItemDAO;
import com.polimi.tiw_asteonline_ria.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/AvailableItems")
public class AvailableItems extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        int userID = user.getId();

        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(itemDAO.getItemsAvailableForAuction(userID)));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Cannot retrieve items")));
            return;
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
