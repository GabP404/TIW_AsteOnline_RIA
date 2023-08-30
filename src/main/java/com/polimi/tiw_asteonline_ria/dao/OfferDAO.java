package com.polimi.tiw_asteonline_ria.dao;

import com.polimi.tiw_asteonline_ria.beans.Offer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OfferDAO {
    private Connection connection;

    public OfferDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Offer> getAllOffersForAuction(int auctionId, int order) throws SQLException {
        String query = "SELECT offer.offer_id, offer.user_id, offer.auction_id, offer.price, offer.created_at, user.username " +
                "FROM offer JOIN user ON offer.user_id = user.user_id " +
                "WHERE auction_id = ? ORDER BY created_at";
        if (order == 0)
            query += " ASC";
        else
            query += " DESC";

        List<Offer> offers = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, auctionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Offer offer = new Offer();
                    offer.setId(resultSet.getInt("offer_id"));
                    offer.setUserId(resultSet.getInt("user_id"));
                    offer.setAuctionId(resultSet.getInt("auction_id"));
                    offer.setPrice(resultSet.getDouble("price"));
                    offer.setCreatedAt(resultSet.getTimestamp("created_at"));
                    offer.setUsername(resultSet.getString("username"));
                    offers.add(offer);
                }
            }
        }
        return offers;
    }

    public boolean createOffer(int userId, int auctionId, double price) throws SQLException {
        String query = "INSERT INTO offer (user_id, auction_id, price, created_at) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, auctionId);
            preparedStatement.setDouble(3, price);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
