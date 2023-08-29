package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.Auction;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.AuctionDAO;
import com.polimi.tiw_asteonline_ria.dao.OfferDAO;
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

@WebServlet("/CreateOffer")
@MultipartConfig
public class CreateOffer extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CreateOffer() {
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

        // Get and parse parameters
        int auctionID;
        float offer;
        try {
            auctionID = Integer.parseInt(req.getParameter("auction_id"));
        } catch (NumberFormatException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid Auction ID")));
            return;
        }

        try {
            offer = Float.parseFloat(req.getParameter("offer"));
        } catch (NumberFormatException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid offer")));
            return;
        }

        if (auctionID <= 0 || offer <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid auction ID or offer")));
            return;
        }

        OfferDAO offerDAO = new OfferDAO(connection);
        AuctionDAO auctionDAO = new AuctionDAO(connection);

        try {
            Auction auction = auctionDAO.getAuctionDetailedById(auctionID);

            if (auction == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("Auction not found")));
                return;
            }

            if (auction.getStatus() == 0) {
            	resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("Auction closed")));
                return;
            }

            if(auction.isExpired()){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("Auction is expired")));
                return;
            }

            if (auction.getUserId() == userID){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("You can't make an offer on your own auction")));
                return;
            }

            if(offer < auction.getMinOfferToMake()){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                if(offer < auction.getMinOfferToMake()){
                    if (auction.getMinOfferToMake() == auction.getStartingPrice())
                        resp.getWriter().println(new Gson().toJson(new Error("Offer must be greater than starting price")));
                    else
                        resp.getWriter().println(new Gson().toJson(new Error("Offer must be greater than current max offer + minimum rise")));
                }
                return;
            }

            if(auction.getMaxOffer() != 0 && auction.getIdBuyer() == userID) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("You are already the highest bidder")));
                return;
            }

            offerDAO.createOffer(userID, auctionID, offer);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Can't create offer")));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");

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

