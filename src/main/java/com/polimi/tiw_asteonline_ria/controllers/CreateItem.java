package com.polimi.tiw_asteonline_ria.controllers;
import com.google.gson.Gson;
import com.polimi.tiw_asteonline_ria.beans.User;
import com.polimi.tiw_asteonline_ria.dao.ItemDAO;
import com.polimi.tiw_asteonline_ria.utils.ConnectionHandler;
import com.polimi.tiw_asteonline_ria.utils.FileManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;


@WebServlet("/CreateItem")
@MultipartConfig
public class CreateItem extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CreateItem() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        int userID = user.getId();
        // Get and validate code, name and description
        String name = request.getParameter("name");
        String description = request.getParameter("description");

        if(name == null || name.isEmpty() || description == null || description.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().println(new Gson().toJson(new Error("Some fields are missing")));
            return;
        }

        // Get and parse price
        Double price;
        try {
            price = Double.parseDouble(request.getParameter("price"));
        } catch (NullPointerException | NumberFormatException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().println(new Gson().toJson(new Error("Price is not valid")));
            return;
        }

        if (price < 0){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().println(new Gson().toJson(new Error("Price should be greater than 0")));
            return;
        }

        // Get uploaded image
        Part imagePart = request.getPart("image");

        if(imagePart == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().println(new Gson().toJson(new Error("The image is missing")));
            return;
        }

        // Get filename and mimeType
        String code = UUID.randomUUID().toString();
        String filename = code + "_" + imagePart.getSubmittedFileName();
        String mimeType = getServletContext().getMimeType(filename);

        // Validate mimetype
        if(mimeType == null || !mimeType.startsWith("image/")){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().println(new Gson().toJson(new Error("Uploaded file is not an image")));
            return;
        }

        // Store image
        InputStream imageStream = imagePart.getInputStream();
        FileManager.storeFile(getServletContext(), "item_images", filename, imageStream);
        // Create a new item bean
        ItemDAO itemDAO = new ItemDAO(connection);
        String imagePath= "item_images/" + filename;
        try {
            itemDAO.createItem(name, description, imagePath, price, user.getId());
        } catch (SQLException e){
            FileManager.removeFile(getServletContext(), imagePath);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().println(new Gson().toJson(new Error("Error while storing the item in the database")));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
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

