package inventorymanagementsystem.dashboard;

import inventorymanagementsystem.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    public Dashboard retrieveDashboard(User user) {
        // Since dashboard is system-wide, we use a fixed ID (1)
        return dashboardRepository.findById(1L)
            .orElseGet(() -> {
                Dashboard dashboard = new Dashboard();
                dashboard.setId(1L);
                dashboard.setTotalCustomers(0L);
                dashboard.setTotalCategories(0L);
                dashboard.setTotalProducts(0L);
                dashboard.setTotalUnpaidOrders(0L);
                dashboard.setTotalPaidOrders(0L);
                dashboard.setTotalSales(BigDecimal.ZERO);
                return dashboard;
            });
    }
}