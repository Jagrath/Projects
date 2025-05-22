package inventorymanagementsystem.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Check if category exists by name (system-wide unique)
    boolean existsByName(String name);

    // Find category by exact name match
    Optional<Category> findByName(String name);

    // Case-insensitive search by name with partial matching
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> searchByNameContainingIgnoreCase(@Param("name") String name);

    // Find all categories with pagination
    Page<Category> findAll(Pageable pageable);

    // Find all categories sorted by name ascending
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllOrderByNameAsc();

    // Find all categories sorted by name descending
    @Query("SELECT c FROM Category c ORDER BY c.name DESC")
    List<Category> findAllOrderByNameDesc();

    // Count all categories
    long count();
}