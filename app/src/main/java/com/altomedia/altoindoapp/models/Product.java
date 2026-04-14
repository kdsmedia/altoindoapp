package com.altomedia.altoindoapp.models;

import java.util.List;

public class Product {
    public String id;
    public String name;
    public String description;
    public long price;
    public long discountPrice;
    public double commissionPercent;
    public List<String> variants;
    public List<String> imageUrls;
    public boolean active;

    public Product() {}

    public Product(String id, String name, String description, long price, long discountPrice, double commissionPercent, List<String> variants, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discountPrice = discountPrice;
        this.commissionPercent = commissionPercent;
        this.variants = variants;
        this.imageUrls = imageUrls;
        this.active = true;
    }
}
