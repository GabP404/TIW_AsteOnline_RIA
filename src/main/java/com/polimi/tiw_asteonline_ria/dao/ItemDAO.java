package com.polimi.tiw_asteonline_ria.dao;

import com.polimi.tiw_asteonline_ria.beans.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO{
    private Connection connection;

    public ItemDAO(Connection connection) {
        this.connection = connection;
    }

    public void createItem(String name, String description, String image_path, double price, int user_id)
            throws SQLException {
        String query = "INSERT INTO item (name, description, image_path, price, user_id, auction_id) " +
                "VALUES (?, ?, ?, ?, ?, NULL);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, image_path);
            preparedStatement.setDouble(4, price);
            preparedStatement.setInt(5, user_id);
            preparedStatement.executeUpdate();
        }
    }

    public void updateItemWithAuction(int itemId, int auctionId) throws SQLException {
        String query = "UPDATE item SET auction_id = ? WHERE item_id = ?;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, auctionId);
            preparedStatement.setInt(2, itemId);
            preparedStatement.executeUpdate();
        }
    }


    public List<Item> getItemsByUserWithoutAuction(int user_id) throws SQLException {
        String query = "SELECT * FROM item WHERE user_id = ? AND auction_id IS NULL";
        List<Item> items = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, user_id);
            try (ResultSet resultSet = preparedStatement.executeQuery();) {
                while (resultSet.next()) {
                    Item item = new Item();
                    item.setCode(resultSet.getInt("item_id"));
                    item.setName(resultSet.getString("name"));
                    item.setDescription(resultSet.getString("description"));
                    item.setImagePath(resultSet.getString("image_path"));
                    item.setPrice(resultSet.getDouble("price"));
                    item.setUserId(resultSet.getInt("user_id"));
                    item.setAuctionId(-1); // Assuming -1 represents no auction
                    items.add(item);
                }
            }
            return items;
        }
    }


    /**
     * This method returns a list of items available for auction of a given user.
     * @param userID the ID of the user
     * @return a list of item
     */
    public List<Item> getItemsAvailableForAuction(int userID) throws SQLException {
        List<Item> items = new ArrayList<>();

        String query = "SELECT item_id, name, description, image_path, price "
                + "FROM item "
                + "WHERE user_id = ? AND auction_id IS NULL";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userID);

        ResultSet result = preparedStatement.executeQuery();

        while(result.next()){
            Item item = new Item();
            item.setCode(result.getInt("item_id"));
            item.setName(result.getString("name"));
            item.setDescription(result.getString("description"));
            item.setImagePath(result.getString("image_path"));
            item.setPrice(result.getFloat("price"));
            item.setUserId(userID);

            items.add(item);
        }

        result.close();
        preparedStatement.close();
        return items;
    }




    public Item getItemById(int itemId) throws SQLException {
        Item item = null;
        String query = "SELECT * FROM item WHERE item_id = ?;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, itemId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    item = new Item();
                    item.setCode(resultSet.getInt("item_id"));
                    item.setName(resultSet.getString("name"));
                    item.setDescription(resultSet.getString("description"));
                    item.setImagePath(resultSet.getString("image_path"));
                    item.setPrice(resultSet.getDouble("price"));
                    item.setUserId(resultSet.getInt("user_id"));
                    item.setAuctionId(resultSet.getInt("auction_id"));
                }
            }
        }
        return item;
    }

    public Item getItem(int itemId, int userId) throws SQLException {
        Item item = null;
        String query = "SELECT * FROM item WHERE item_id = ? AND user_id = ? AND auction_id is NULL";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, itemId);
            preparedStatement.setInt(2,userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    item = new Item();
                    item.setCode(resultSet.getInt("item_id"));
                    item.setName(resultSet.getString("name"));
                    item.setDescription(resultSet.getString("description"));
                    item.setImagePath(resultSet.getString("image_path"));
                    item.setPrice(resultSet.getDouble("price"));
                    item.setUserId(resultSet.getInt("user_id"));
                    item.setAuctionId(resultSet.getInt("auction_id"));
                }
            }
        }
        return item;
    }

    public List<Item> getItemsByAuctionId(int auctionId) throws SQLException {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM item WHERE auction_id = ?;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, auctionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Item item = new Item();
                    item.setCode(resultSet.getInt("item_id"));
                    item.setName(resultSet.getString("name"));
                    item.setDescription(resultSet.getString("description"));
                    item.setImagePath(resultSet.getString("image_path"));
                    item.setPrice(resultSet.getDouble("price"));
                    item.setUserId(resultSet.getInt("user_id"));
                    item.setAuctionId(resultSet.getInt("auction_id"));
                    items.add(item);
                }
            }
        }
        return items;
    }


}
