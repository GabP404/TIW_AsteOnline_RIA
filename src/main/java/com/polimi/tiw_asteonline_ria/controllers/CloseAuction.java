package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.Auction;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.AuctionDAO;
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

@WebServlet("/CloseAuction")
@MultipartConfig
public class CloseAuction extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CloseAuction() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        int userID = user.getId();

        AuctionDAO auctionDAO = new AuctionDAO(connection);

        int auctionId;
        try {
            auctionId = Integer.parseInt(req.getParameter("auction_id"));
        } catch (NumberFormatException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Auction ID is not valid")));
            return;
        }

        Auction auction;
        try {
            auction = auctionDAO.getAuctionById(auctionId);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Cannot retrieve auction details")));
            return;
        }

        if(auction == null || auction.getUserId() != userID || auction.getStatus() == 0 || auction.isExpired() == false) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("You can't close this auction!")));
            return;
        }

        try {
            // Actually close the auction
            auctionDAO.closeAuction(auctionId);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Can't close this auction.")));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
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

