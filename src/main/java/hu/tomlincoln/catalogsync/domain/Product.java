package hu.tomlincoln.catalogsync.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @Column(length = 50, nullable = false)
    private String id;

    @Column(length = 150, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    //SQLite does not support "ENUM" type definition, so we at least restrict length
    @Column(length = 15, nullable = false)
    private ProductAvailability availability;

    @Enumerated(EnumType.STRING)
    //SQLite does not support "ENUM" type definition, so we at least restrict length
    @Column(length = 11, nullable = false)
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

    @Column(length = 5000, nullable = false)
    private String link;

    @Column(length = 5000, nullable = false)
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

    public static Product fromStringArray(String[] array) {
        return fromStringArray(array, null);
    }

    public static Product fromStringArray(String[] array, Product product) {
        Product newOrExistingProduct = Optional.ofNullable(product).orElseGet(Product::new);
        String[] cleanArray = Product.cleanProductArrayFromParenthesis(array);
        newOrExistingProduct.setId(cleanArray[0]);
        newOrExistingProduct.setTitle(cleanArray[1]);
        newOrExistingProduct.setDescription(cleanArray[2]);
        newOrExistingProduct.setAvailability(ProductAvailability.valueOf(cleanArray[3].toUpperCase().replace(" ", "_")));
        newOrExistingProduct.setCondition(ProductCondition.valueOf(cleanArray[4].toUpperCase().replace(" ", "_")));
        Price currentPrice = Optional.ofNullable(newOrExistingProduct.getPrice()).orElseGet(Price::new);
        currentPrice.setValue(new BigDecimal(cleanArray[5].split(" ")[0]));
        currentPrice.setCurrency(Currency.getInstance(cleanArray[5].split(" ")[1]));
        newOrExistingProduct.setPrice(currentPrice);
        Price currentSalePrice = newOrExistingProduct.getPrice();
        currentSalePrice.setValue(new BigDecimal(cleanArray[5].split(" ")[0]));
        currentSalePrice.setCurrency(Currency.getInstance(cleanArray[5].split(" ")[1]));
        newOrExistingProduct.setSalePrice(currentSalePrice);
        newOrExistingProduct.setLink(cleanArray[7]);
        newOrExistingProduct.setBrand(cleanArray[8]);
        newOrExistingProduct.setImageLink(cleanArray[9]);
        try {
            newOrExistingProduct.setAgeGroup(AgeGroup.valueOf(Optional.ofNullable(cleanArray[10])
                    .orElseGet(() -> String.valueOf(AgeGroup.NOT_SPECIFIED)).toUpperCase().replace(" ", "_")));
        } catch (IllegalArgumentException e) {
            newOrExistingProduct.setAgeGroup(AgeGroup.NOT_SPECIFIED);
        }
        newOrExistingProduct.setGoogleProductCategory(cleanArray[11]);
        return newOrExistingProduct;
    }

    public static String[] cleanProductArrayFromParenthesis(String[] product) {
        return Arrays.stream(product)
                .map(string -> Objects.requireNonNullElse(string, "")).map(s -> {
                    if (s.startsWith("\"")) {
                        s = s.substring(1);
                    }
                    if (s.endsWith("\"")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    return s;
                }).toArray(String[]::new);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
                Objects.equals(title, product.title) &&
                Objects.equals(description, product.description) &&
                availability == product.availability &&
                condition == product.condition &&
                Objects.equals(price, product.price) &&
                Objects.equals(salePrice, product.salePrice) &&
                Objects.equals(brand, product.brand) &&
                Objects.equals(link, product.link) &&
                Objects.equals(imageLink, product.imageLink) &&
                ageGroup == product.ageGroup &&
                Objects.equals(googleProductCategory, product.googleProductCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, availability, condition, price, salePrice, brand, link, imageLink, ageGroup, googleProductCategory);
    }
}
