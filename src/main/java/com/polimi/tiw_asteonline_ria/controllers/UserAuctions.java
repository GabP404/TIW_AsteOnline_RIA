package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.Auction;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.AuctionDAO;
import com.polimi.tiw_asteonline_ria.dao.ItemDAO;
import com.polimi.tiw_asteonline_ria.utils.Checks;
import com.polimi.tiw_asteonline_ria.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/UserAuctions")
public class UserAuctions extends HttpServlet {

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


        AuctionDAO auctionDAO = new AuctionDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            List<Auction> openAuctions = auctionDAO.getAuctionsCreatedByUser(userID,1);
            List<Auction> closedAuctions = auctionDAO.getAuctionsCreatedByUser(userID,0);
            List<Auction> allAuctions = new ArrayList<>();
            allAuctions.addAll(openAuctions);
            allAuctions.addAll(closedAuctions);

            for (Auction auction : allAuctions) {
                auction.setItems(itemDAO.getItemsByAuctionId(auction.getId()));
                auction.setItemsCodeName(Checks.createItemsCodeName(auction.getItems()));
            }

            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(allAuctions));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Cannot retrieve won auctions")));
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
