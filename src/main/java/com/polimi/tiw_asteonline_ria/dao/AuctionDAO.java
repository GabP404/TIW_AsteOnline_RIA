package com.polimi.tiw_asteonline_ria.dao;

import com.polimi.tiw_asteonline_ria.beans.Auction;
import com.polimi.tiw_asteonline_ria.utils.AuctionsUtilities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {
    private Connection connection;

    public AuctionDAO(Connection connection) {
        this.connection = connection;
    }

    public int createAuction(int status, double starting_price, int minimum_rise, int user_id, Timestamp deadline) throws SQLException {
        int auctionId = -1;
        String query = "INSERT INTO auction (status, starting_price, minimum_rise, user_id, deadline) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, status);
            preparedStatement.setDouble(2, starting_price);
            preparedStatement.setInt(3, minimum_rise);
            preparedStatement.setInt(4, user_id);
            preparedStatement.setTimestamp(5, deadline);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    auctionId = generatedKeys.getInt(1);
                }
                generatedKeys.close();
            }
        }
        return auctionId;
    }


    public Auction getAuctionDetailedById(int auctionId) throws SQLException {
        String query = "SELECT * " +
                "FROM detailed_auction LEFT JOIN user on offered_by_user_id = user.user_id " +
                "WHERE auction_id = ?";
        Auction auction = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, auctionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    auction = new Auction();
                    auction.setId(resultSet.getInt("auction_id"));
                    auction.setStartingPrice(resultSet.getDouble("starting_price"));
                    auction.setMinimumRise(resultSet.getInt("minimum_rise"));
                    auction.setUserId(resultSet.getInt("created_by_user_id"));
                    auction.setDeadline(resultSet.getTimestamp("deadline"));
                    auction.setMaxOffer(resultSet.getDouble("max_offer"));
                    auction.setStatus(resultSet.getInt("status"));
                    if (resultSet.getString("firstname") != null) {
                        auction.setNameBuyer(resultSet.getString("firstname") + " " + resultSet.getString("lastname"));
                    }
                    auction.setShippingAddressBuyer(resultSet.getString("shipping_address"));
                    auction.setIdBuyer(resultSet.getInt("offered_by_user_id"));
                    auction.setExpired(AuctionsUtilities.checkExpired(auction.getDeadline()));
                    auction.setTimeRemaining(AuctionsUtilities.calculateTimeRemaining(auction.getDeadline()));
                    if (auction.getMaxOffer() == 0)
                        auction.setMinOfferToMake(auction.getStartingPrice());
                    else
                        auction.setMinOfferToMake(auction.getMaxOffer() + auction.getMinimumRise());
                }
            }
        }
        return auction;
    }

    public Auction getAuctionById(int auctionId) throws SQLException {
        String query = "SELECT * FROM detailed_auction WHERE auction_id = ?";
        Auction auction = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, auctionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    auction = new Auction();
                    auction.setId(resultSet.getInt("auction_id"));
                    auction.setStartingPrice(resultSet.getDouble("starting_price"));
                    auction.setMinimumRise(resultSet.getInt("minimum_rise"));
                    auction.setUserId(resultSet.getInt("created_by_user_id"));
                    auction.setDeadline(resultSet.getTimestamp("deadline"));
                    auction.setMaxOffer(resultSet.getDouble("max_offer"));
                    auction.setStatus(resultSet.getInt("status"));
                    auction.setExpired(AuctionsUtilities.checkExpired(auction.getDeadline()));
                    auction.setTimeRemaining(AuctionsUtilities.calculateTimeRemaining(auction.getDeadline()));
                    if (auction.getMaxOffer() == 0)
                        auction.setMinOfferToMake(auction.getStartingPrice());
                    else
                        auction.setMinOfferToMake(auction.getMaxOffer() + auction.getMinimumRise());
                }
            }
        }
        return auction;
    }

    public void closeAuction(int auctionId) throws SQLException {
        String updateQuery = "UPDATE auction SET status = 0 WHERE auction_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, auctionId);
            preparedStatement.executeUpdate();
        }
    }


    public List<Auction> searchOpenAuctions(String search, int userID) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query = "SELECT DISTINCT (detailed_auction.auction_id), detailed_auction.deadline, detailed_auction.minimum_rise, " +
                "detailed_auction.starting_price, detailed_auction.max_offer " +
                "FROM detailed_auction " +
                "JOIN item ON detailed_auction.auction_id = item.auction_id " +
                "WHERE (LOWER(item.name) LIKE CONCAT('%', LOWER(?), '%') OR LOWER(item.description) LIKE CONCAT('%', LOWER(?), '%')) " +
                "AND detailed_auction.status = 1 " +
                "AND detailed_auction.deadline > CURRENT_TIMESTAMP " +
                "AND detailed_auction.created_by_user_id != ? ORDER BY deadline DESC";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, search);
        preparedStatement.setString(2, search);
        preparedStatement.setInt(3, userID);

        ResultSet result = preparedStatement.executeQuery();

        while (result.next()) {
            Auction auction = new Auction();
            auction.setId(result.getInt("auction_id"));
            auction.setDeadline(result.getTimestamp("deadline"));
            auction.setStartingPrice(result.getFloat("starting_price"));
            auction.setMinimumRise(result.getInt("minimum_rise"));
            auction.setMaxOffer(result.getDouble("max_offer"));
            auction.setExpired(AuctionsUtilities.checkExpired(auction.getDeadline()));
            auction.setTimeRemaining(AuctionsUtilities.calculateTimeRemaining(auction.getDeadline()));
            auctions.add(auction);
        }
        result.close();
        preparedStatement.close();

        return auctions;
    }


    public List<Auction> getAuctionsWonByUser(int userId) throws SQLException {
        String query = "SELECT * "
                + "FROM detailed_auction "
                + "WHERE offered_by_user_id = ? AND status = 0";
        ;
        List<Auction> auctions = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Auction auction = new Auction();
                    auction.setId(resultSet.getInt("auction_id"));
                    auction.setUserId(resultSet.getInt("offered_by_user_id"));
                    auction.setMaxOffer(resultSet.getDouble("max_offer"));
                    auction.setDeadline(resultSet.getTimestamp("deadline"));
                    auction.setExpired(AuctionsUtilities.checkExpired(auction.getDeadline()));
                    auction.setTimeRemaining(AuctionsUtilities.calculateTimeRemaining(auction.getDeadline()));
                    auctions.add(auction);
                }
            }
        }
        return auctions;
    }


    public List<Auction> getAuctionsCreatedByUser(int userId, int status) throws SQLException {
        String query = "SELECT * FROM detailed_auction WHERE created_by_user_id = ? AND status = ? ORDER BY deadline ASC";
        List<Auction> auctions = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, status);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Auction auction = new Auction();
                    auction.setId(resultSet.getInt("auction_id"));
                    auction.setStartingPrice(resultSet.getDouble("starting_price"));
                    auction.setMinimumRise(resultSet.getInt("minimum_rise"));
                    auction.setUserId(resultSet.getInt("created_by_user_id"));
                    auction.setDeadline(resultSet.getTimestamp("deadline"));
                    auction.setMaxOffer(resultSet.getDouble("max_offer"));
                    auction.setStatus(resultSet.getInt("status"));
                    auction.setIdBuyer(resultSet.getInt("offered_by_user_id"));
                    auction.setExpired(AuctionsUtilities.checkExpired(auction.getDeadline()));
                    auction.setTimeRemaining(AuctionsUtilities.calculateTimeRemaining(auction.getDeadline()));
                    if (auction.getMaxOffer() == 0)
                        auction.setMinOfferToMake(auction.getStartingPrice());
                    else
                        auction.setMinOfferToMake(auction.getMaxOffer() + auction.getMinimumRise());
                    auctions.add(auction);
                }
            }
        }
        return auctions;
    }
}


