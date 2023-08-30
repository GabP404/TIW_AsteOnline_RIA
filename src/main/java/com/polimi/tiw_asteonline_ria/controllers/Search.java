package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.Auction;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.AuctionDAO;
import com.polimi.tiw_asteonline_ria.dao.ItemDAO;
import com.polimi.tiw_asteonline_ria.utils.AuctionsUtilities;
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

@WebServlet("/Search")
public class Search extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public Search() {
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

        String search = request.getParameter("keyword");

        if(search == null || search.isEmpty()){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            List<Auction> searchResults = auctionDAO.searchOpenAuctions(search, userID);
            for (Auction auction : searchResults) {
                auction.setItems(itemDAO.getItemsByAuctionId(auction.getId()));
                auction.setItemsCodeName(AuctionsUtilities.createItemsCodeName(auction.getItems()));
            }
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(searchResults));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Cannot to search auctions")));
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
