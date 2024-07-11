package hu.tomlincoln.catalogsync.domain;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Product {

    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 150)
    private String title;

    @Column(length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    //SQLite does not support "ENUM" type definition, so we at least restrict length
    @Column(length = 15)
    private ProductAvailability availability;

    @Enumerated(EnumType.STRING)
    //SQLite does not support "ENUM" type definition, so we at least restrict length
    @Column(length = 11)
    private ProductCondition condition;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "price_value")),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Price price;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "sale_price_value")),
            @AttributeOverride(name = "currency", column = @Column(name = "sale_price_currency"))
    })
    private Price salePrice;

    @Column(length = 70)
    private String brand;

    @Column(length = 5000)
    private String link;

    @Column(length = 5000)
    private String imageLink;

    @Enumerated(EnumType.STRING)
    //SQLite does not support "ENUM" type definition, so we at least restrict length
    @Column(length = 7)
    private AgeGroup ageGroup;

    @Column
    private String googleProductCategory;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(ProductAvailability availability) {
        this.availability = availability;
    }

    public ProductCondition getCondition() {
        return condition;
    }

    public void setCondition(ProductCondition condition) {
        this.condition = condition;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Price getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Price salePrice) {
        this.salePrice = salePrice;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getGoogleProductCategory() {
        return googleProductCategory;
    }

    public void setGoogleProductCategory(String googleProductCategory) {
        this.googleProductCategory = googleProductCategory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
