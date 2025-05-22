package inventorymanagementsystem.product;

import inventorymanagementsystem.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Category category;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer quantity = 0;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    // Removed the 'owner' field and associated mapping

    public Product() {}

    public Product(Long id, String name, Category category, Integer quantity, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public Product(String name, Category category, Integer quantity, String price) {
        this(null, name, category, quantity, new BigDecimal(price));
    }

    public Product(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        this.quantity -= amount;
    }

    public ProductForm toForm() {
        var categoryId = category != null ? category.getId() : null;
        return new ProductForm(name, categoryId, quantity, price);
    }
}
