package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.Auction;
import com.polimi.tiw_asteonline_ria.beans.Item;
import com.polimi.tiw_asteonline_ria.beans.Offer;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.AuctionDAO;
import com.polimi.tiw_asteonline_ria.dao.ItemDAO;
import com.polimi.tiw_asteonline_ria.dao.OfferDAO;
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
import java.util.List;


@WebServlet("/AuctionDetails")
public class AuctionDetails extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public AuctionDetails() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //int userID = (int) request.getSession().getAttribute("user_id");
        User user = (User) request.getSession().getAttribute("user");
        int userID = user.getId();

        int auctionID;
        try {
            auctionID = Integer.parseInt(request.getParameter("auction_id"));
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }



        AuctionDAO auctionDAO = new AuctionDAO(connection);
        OfferDAO offerDAO = new OfferDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);

        Auction auction;
        List<Item> items;
        List<Offer> offers;
        try {

            auction = auctionDAO.getAuctionDetailedById(auctionID);
            offers = offerDAO.getAllOffersForAuction(auctionID,0);
            items = itemDAO.getItemsByAuctionId(auctionID);
            auction.setOffers(offers);
            auction.setItems(items);
            auction.setItemsCodeName(Checks.createItemsCodeName(auction.getItems()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Cannot retrieve auction")));
            return;
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(auction));
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
