package inventorymanagementsystem.category;

import inventorymanagementsystem.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/create")
    public String retrieveCreateCategoryPage(Model model) {
        model.addAttribute("category", new CategoryForm());
        model.addAttribute("mode", "create");
        return "category/category-form";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute CategoryForm category, Model model) {
        try {
            categoryService.createCategory(category.toEntity());  // Removed 'user' argument
        } catch (CategoryNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("category", category);
            model.addAttribute("mode", "create");
            return "category/category-form";
        }
        return "redirect:/categories/list";
    }

    @GetMapping("/list")
    public String listCategories(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        var categoryPage = categoryService.listCategories(page, 10, "asc");  // Removed 'null' argument and updated to match service signature
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", categoryPage.getNumber() + 1);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        return "category/category-table";
    }

    @GetMapping("/search")
    public String searchCategories(@RequestParam("name") String name, Model model) {
        var categories = categoryService.searchCategories(name);  // Removed 'user' argument
        model.addAttribute("categories", categories);
        return "category/category-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateCategoryPage(@PathVariable("id") long id, Model model) {
        var category = categoryService.getCategory(id);  // Removed 'user' argument
        model.addAttribute("category", category.toForm());
        model.addAttribute("id", category.getId());
        model.addAttribute("mode", "update");
        return "category/category-form";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable("id") long id, @Valid @ModelAttribute CategoryForm category, Model model) {
        try {
            categoryService.updateCategory(id, category.toEntity());  // Removed 'user' argument
        } catch (CategoryNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("category", category);
            model.addAttribute("id", id);
            model.addAttribute("mode", "update");
            return "category/category-form";
        }
        return "redirect:/categories/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);  // Removed 'user' argument
        } catch (CategoryNotFoundException e) {
            redirectAttributes.addFlashAttribute("deleteNotAllowed", true);
        }
        return "redirect:/categories/list";
    }
}
