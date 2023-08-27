package com.polimi.tiw_asteonline_ria.beans;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public class Auction {
    private int id;
    private int status;
    private double startingPrice;
    private int minimumRise;
    private int userId;
    private Double finalPrice;
    private Timestamp deadline;
    private List<Item> items;
    private Double maxOffer;
    private String nameBuyer;
    private Integer idBuyer;

    private boolean expired;

    private String itemsCodeName;

    private String timeRemaining;

    private List<Offer> offers;

    private String shippingAddressBuyer;
    private double minOfferToMake;

    public String getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public void setItemsCodeName(String itemsCodeName) {
        this.itemsCodeName = itemsCodeName;
    }



    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public Integer getIdBuyer() {
        return idBuyer;
    }

    public void setIdBuyer(Integer idBuyer) {
        this.idBuyer = idBuyer;
    }



    public String getShippingAddressBuyer() {
        return shippingAddressBuyer;
    }

    public void setShippingAddressBuyer(String shippingAddressBuyer) {
        this.shippingAddressBuyer = shippingAddressBuyer;
    }

    public void setNameBuyer(String nameBuyer) {
        this.nameBuyer = nameBuyer;
    }

    public String getNameBuyer() {
        return nameBuyer;
    }


    public double getMinOfferToMake() {
        return minOfferToMake;
    }

    public void setMinOfferToMake(double minOfferToMake) {
        this.minOfferToMake = minOfferToMake;
    }

    public String createItemsCodeName() {
        String itemsCodeName = "";
        for (Item item : items) {
            itemsCodeName += "["+item.getCode() + "]" +"-" +item.getName() + " ";
        }
        return itemsCodeName;
    }

    public String getItemsCodeName() {
        return itemsCodeName;
    }


    public Double getMaxOffer() {
        return maxOffer;
    }

    public void setMaxOffer(Double maxOffer) {
        this.maxOffer = maxOffer;
    }


    public int getItemsCount() {
        return items.size();
    }

    public Auction() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public int getMinimumRise() {
        return minimumRise;
    }

    public void setMinimumRise(int minimumRise) {
        this.minimumRise = minimumRise;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Timestamp getDeadline() {
        return deadline;
    }

    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }


    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}