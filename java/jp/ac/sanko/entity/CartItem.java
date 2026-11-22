package jp.ac.sanko.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CART_ITEMS")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private product product;

    @Column(name = "SESSION_ID")
    private String sessionId;

    @Column(name = "FLAVOUR")
    private String flavour;

    @Column(name = "QUANTITY")
    private int quantity;

    public CartItem() {
    }

    public CartItem(product product, String sessionId,
                    String flavour, int quantity) {
        this.product = product;
        this.sessionId = sessionId;
        this.flavour = flavour;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public product getProduct() {
        return product;
    }

    public void setProduct(product product) {
        this.product = product;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFlavour() {
        return flavour;
    }

    public void setFlavour(String flavour) {
        this.flavour = flavour;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getImage() {

        Long productId = product.getId();

        if (productId == 1L) {
            return "yogurt.jpg";
        }

        if (productId == 2L) {
            switch (flavour) {
                case "Strawberry":
                    return "drinking-yogurt-strawberry.jpg";
                case "Vanilla":
                    return "drinking-yogurt-vanila.jpg";
                case "Chocolate":
                    return "drinking-yogurt-chocolate.jpg";
            }
        }

        if (productId == 3L) {
            switch (flavour) {
                case "Strawberry":
                    return "jelly-yogurt-strawberry.jpg";
                case "Green Apple":
                    return "jelly-yogurt-green-apple.jpg";
                case "Mixed Fruit":
                    return "jelly-yogurt-mixed-fruit.jpg";
            }
        }

        if (productId == 4L) {
            switch (flavour) {
                case "Vanilla":
                    return "ice-cream-vanilla.jpg";
                case "Chocolate":
                    return "ice-cream-chocolate.jpg";
                case "Strawberry":
                    return "ice-cream-strawberry.jpg";
            }
        }

        if (productId == 5L) {
            switch (flavour) {
                case "Vanilla":
                    return "milk-ice-vanilla.jpg";
                case "Chocolate":
                    return "milk-ice-chocolate.jpg";
                case "Strawberry":
                    return "milk-ice-strawberry.jpg";
                case "Wood Apple":
                    return "milk-ice-wood-apple.jpg";
            }
        }

        return product.getImage();
    }
}