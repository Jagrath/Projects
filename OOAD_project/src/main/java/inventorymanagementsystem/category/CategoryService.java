package inventorymanagementsystem.category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            logger.warn("Attempt to create duplicate category: {}", category.getName());
            throw new CategoryNameTakenException();
        }
        Category savedCategory = categoryRepository.save(category);
        logger.info("Created new category: {}", savedCategory.getName());
        return savedCategory;
    }

    @Transactional(readOnly = true)
    public Page<Category> listCategories(int page, int size, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(direction, "name"));
        logger.info("Listing categories with pagination");
        return categoryRepository.findAll(pageRequest);
    }

    @Transactional(readOnly = true)
    public List<Category> listCategories() {
        logger.info("Listing all categories sorted by name");
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public List<Category> searchCategories(String name) {
        logger.info("Searching for categories containing name: {}", name);
        if (StringUtils.hasText(name)) {
            return categoryRepository.searchByNameContainingIgnoreCase(name);
        }
        return categoryRepository.findAllOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Category getCategory(long id) {
        logger.info("Fetching category with ID {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Category not found with ID: {}", id);
                    return new CategoryNotFoundException();
                });
    }

    @Transactional
    public Category updateCategory(long id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        if (!existingCategory.getName().equals(updatedCategory.getName())) {
            if (categoryRepository.existsByName(updatedCategory.getName())) {
                logger.warn("Category name already exists: {}", updatedCategory.getName());
                throw new CategoryNameTakenException();
            }
            existingCategory.setName(updatedCategory.getName());
            logger.info("Updated category ID {} to new name: {}", id, updatedCategory.getName());
        }
        return categoryRepository.save(existingCategory);
    }

    @Transactional
    public void deleteCategory(long id) {
        if (!categoryRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent category ID: {}", id);
            throw new CategoryNotFoundException();
        }
        categoryRepository.deleteById(id);
        logger.info("Deleted category ID: {}", id);
    }
}
