package inventorymanagementsystem.dashboard;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "dashboard")
@Subselect("SELECT " +
           "1 AS id, " +
           "(SELECT COUNT(id) FROM customer) AS total_customers, " +
           "(SELECT COUNT(id) FROM category) AS total_categories, " +
           "(SELECT COUNT(id) FROM product) AS total_products, " +
           "(SELECT COUNT(id) FROM \"order\" WHERE status = 'UNPAID') AS total_unpaid_orders, " +
           "(SELECT COUNT(id) FROM \"order\" WHERE status = 'PAID') AS total_paid_orders, " +
           "COALESCE((SELECT SUM(p.price * oi.quantity) " +
           "FROM order_item oi " +
           "JOIN product p ON oi.product_id = p.id " +
           "JOIN \"order\" o ON oi.order_id = o.id " +
           "WHERE o.status = 'PAID'), 0) AS total_sales")
public class Dashboard {

    @Id
    private Long id;

    @Column(name = "total_customers", nullable = false)
    private Long totalCustomers;

    @Column(name = "total_categories", nullable = false)
    private Long totalCategories;

    @Column(name = "total_products", nullable = false)
    private Long totalProducts;

    @Column(name = "total_unpaid_orders", nullable = false)
    private Long totalUnpaidOrders;

    @Column(name = "total_paid_orders", nullable = false)
    private Long totalPaidOrders;

    @Column(name = "total_sales", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSales;

    // Required no-arg constructor
    public Dashboard() {
        this.totalCustomers = 0L;
        this.totalCategories = 0L;
        this.totalProducts = 0L;
        this.totalUnpaidOrders = 0L;
        this.totalPaidOrders = 0L;
        this.totalSales = BigDecimal.ZERO;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public Long getTotalCategories() {
        return totalCategories;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public Long getTotalUnpaidOrders() {
        return totalUnpaidOrders;
    }

    public Long getTotalPaidOrders() {
        return totalPaidOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    // Protected setters for JPA
    protected void setId(Long id) {
        this.id = id;
    }

    protected void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    protected void setTotalCategories(Long totalCategories) {
        this.totalCategories = totalCategories;
    }

    protected void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    protected void setTotalUnpaidOrders(Long totalUnpaidOrders) {
        this.totalUnpaidOrders = totalUnpaidOrders;
    }

    protected void setTotalPaidOrders(Long totalPaidOrders) {
        this.totalPaidOrders = totalPaidOrders;
    }

    protected void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    // Utility method to create default dashboard
    public static Dashboard createDefault() {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(1L);
        return dashboard;
    }

    @Override
    public String toString() {
        return "Dashboard{" +
               "id=" + id +
               ", totalCustomers=" + totalCustomers +
               ", totalCategories=" + totalCategories +
               ", totalProducts=" + totalProducts +
               ", totalUnpaidOrders=" + totalUnpaidOrders +
               ", totalPaidOrders=" + totalPaidOrders +
               ", totalSales=" + totalSales +
               '}';
    }
}