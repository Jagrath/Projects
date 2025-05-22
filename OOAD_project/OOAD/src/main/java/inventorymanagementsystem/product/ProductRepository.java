package inventorymanagementsystem.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsById(long id);

    boolean existsByName(String name);

    Optional<Product> findById(long id);

    Optional<Product> findByName(String name);

    Page<Product> findAll(Pageable pageable); // Fetch all products with pagination

    List<Product> findAll(Sort sort); // Fetch all products with sorting

    List<Product> findAllByNameContainingIgnoreCase(String name); // Fetch products by name search

}
