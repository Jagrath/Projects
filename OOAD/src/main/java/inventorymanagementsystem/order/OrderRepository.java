package inventorymanagementsystem.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    boolean existsByCustomerId(long customerId);

    boolean existsByItemsProductId(long productId);

    // Removed owner filtering, as orders are now shared
    Optional<Order> findById(long id);

    // Removed filtering by 'owner' here too
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    // Removed owner filtering from the query
    List<Order> findAllByStatusAndCustomerNameContainingIgnoreCase(OrderStatus status, String customerName);

    // Keep the query for fetching orders with their items (no change needed)
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items")
    List<Order> findAllWithItems();

}
