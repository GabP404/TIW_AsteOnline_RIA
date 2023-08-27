package com.polimi.tiw_asteonline_ria.api;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.Auction;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.AuctionDAO;
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
import java.util.List;


@WebServlet("/WonAuctions")
public class WonAuctions extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public WonAuctions() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        User user = (User) request.getSession().getAttribute("user");
        int userID = user.getId();

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            List<Auction> auctions = auctionDAO.getAuctionsWonByUser(userID);

            for (Auction auction : auctions) {
                auction.setItems(itemDAO.getItemsByAuctionId(auction.getId()));
                auction.setItemsCodeName(auction.createItemsCodeName());
            }

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(auctions));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Cannot retrieve won auctions")));
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
