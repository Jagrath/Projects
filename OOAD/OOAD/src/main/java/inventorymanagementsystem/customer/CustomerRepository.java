package inventorymanagementsystem.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByName(String name);

    Optional<Customer> findById(Long id);

    Optional<Customer> findByName(String name);

    Page<Customer> findAll(Pageable pageable);

    List<Customer> findAll(Sort sort);

    List<Customer> findAllByNameContainingIgnoreCase(String name);

}
