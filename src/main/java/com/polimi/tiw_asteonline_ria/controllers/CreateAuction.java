package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.Item;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.AuctionDAO;
import com.polimi.tiw_asteonline_ria.dao.ItemDAO;
import com.polimi.tiw_asteonline_ria.utils.Checks;
import com.polimi.tiw_asteonline_ria.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;

@WebServlet("/CreateAuction")
@MultipartConfig
public class CreateAuction extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CreateAuction() {
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
        double starting_price = 0.0;

        // Get item codes from request
        String[] codes = req.getParameterValues("items");

        if (codes == null || codes.length == 0){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("No items selected")));
            return;
        }


        int[] codes_int = new int[codes.length];
        try {
            for (int i = 0; i < codes.length; i++) {
                codes_int[i] = Integer.parseInt(codes[i]);
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Parsing error")));
            return;
        }

        if (Checks.hasDuplicates(codes_int)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Duplicate items")));
            return;
        }

        if (Checks.hasNegatives(codes_int)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Negative items codes")));
            return;
        }


        int minimumRise;
        try {
            minimumRise = Integer.parseInt(req.getParameter("minimum_rise"));
        } catch (NullPointerException | NumberFormatException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid minimum rise")));
            return;
        }

        if (minimumRise <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid minimum rise")));
            return;
        }

        ItemDAO itemDAO = new ItemDAO(connection);
        for (int i = 0; i < codes_int.length; i++) {
            Item item = null;
            try {
                item = itemDAO.getItemById(codes_int[i]);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (item == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("Invalid item")));
                return;
            }
            starting_price += item.getPrice();
        }



        // Get deadline date from request and parse it
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date deadline;
        try {
            deadline = simpleDateFormat.parse(req.getParameter("deadline"));
        } catch (ParseException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid deadline date")));
            return;
        }

        Timestamp timestamp = new Timestamp(deadline.getTime());
        Instant now = null;
        now = new java.util.Date(System.currentTimeMillis()).toInstant();

        if (deadline.before(Date.from(now))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid deadline date")));
            return;
        }


        AuctionDAO auctionDAO = new AuctionDAO(connection);
        int id = 0;
        try {
            connection.setAutoCommit(false);
            id = auctionDAO.createAuction(1, starting_price, minimumRise,userID, timestamp);

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Not created auction")));
            return;
        }

        try {
            for (int i = 0; i < codes_int.length; i++) {
                itemDAO.updateItemWithAuction(codes_int[i], id);
            }
            connection.setAutoCommit(true);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().println("{\"id\": " + id + "}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Error while storing the item in the database and auction not created")));
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
