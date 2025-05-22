package inventorymanagementsystem.dashboard;

import inventorymanagementsystem.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String retrieveDashboardPage(@AuthenticationPrincipal User user, Model model) {
        Dashboard dashboard = dashboardService.retrieveDashboard(user);
        model.addAttribute("dashboard", dashboard);
        return "dashboard/dashboard";
    }
}