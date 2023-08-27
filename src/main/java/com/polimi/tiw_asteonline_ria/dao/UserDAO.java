package com.polimi.tiw_asteonline_ria.dao;

import com.polimi.tiw_asteonline_ria.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection connection;

    /**
     * Constructor of the class
     * @param connection the connection to the database
     */
    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Check if the credentials are correct
     * @param username
     * @param password
     * @return User if the credentials are correct, null otherwise
     */
    public User checkCredentials(String username, String password) throws SQLException {
        String query = "SELECT user_id, username, firstname, lastname, shipping_address FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery();) {
                if (!resultSet.isBeforeFirst())
                    return null;
                else {
                    resultSet.next();
                    User user = new User();
                    user.setId(resultSet.getInt("user_id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setFirstname(resultSet.getString("firstname"));
                    user.setLastname(resultSet.getString("lastname"));
                    user.setShippingAddress(resultSet.getString("shipping_address"));
                    return user;
                }
            }
        }
    }

    public User getUserById(int userId) throws SQLException {
        String query = "SELECT user_id, username, firstname, lastname, shipping_address FROM user WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery();) {
                if (!resultSet.isBeforeFirst())
                    return null;
                else {
                    resultSet.next();
                    User user = new User();
                    user.setId(resultSet.getInt("user_id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setFirstname(resultSet.getString("firstname"));
                    user.setLastname(resultSet.getString("lastname"));
                    user.setShippingAddress(resultSet.getString("shipping_address"));
                    return user;
                }
            }
        }
    }


}
