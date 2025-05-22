package inventorymanagementsystem.order;

import inventorymanagementsystem.customer.Customer;
import inventorymanagementsystem.customer.CustomerRepository;
import inventorymanagementsystem.product.ProductRepository;
import inventorymanagementsystem.util.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderDocumentGenerator orderDocumentGenerator;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository, OrderDocumentGenerator orderDocumentGenerator) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderDocumentGenerator = orderDocumentGenerator;
    }

    @Transactional
    public void createOrder(Order order) {
        checkCustomer(order.getCustomer());
        checkProductAvailabilityForNewItems(order.getItems());
        updateProductQuantities(order.getItems(), "decrease");
        orderRepository.save(order);
        logger.info("Order created for customer {}", order.getCustomer().getName());
    }

    @Transactional(readOnly = true)
    public Page<Order> listOrders(OrderStatus status, int page) {
        logger.info("Listing {} orders paginated", status);
        return orderRepository.findAllByStatus(status, PageRequest.of(page - 1, 8, Sort.by("date")));
    }

    @Transactional(readOnly = true)
    public List<Order> findOrders(OrderStatus status, String customerName) {
        logger.info("Finding {} orders containing customer name {}", status, customerName);
        return orderRepository.findAllByStatusAndCustomerNameContainingIgnoreCase(status, customerName);
    }

    @Transactional(readOnly = true)
    public Order findOrder(long id) {
        logger.info("Finding order with id {}", id);
        return orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Document printOrder(long id) {
        var order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        logger.info("Printing order with id {}", id);
        return orderDocumentGenerator.generateOrderDocument(order);
    }

    @Transactional
    public void updateOrder(long id, Order updatedOrder) {
        var order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        checkCustomer(updatedOrder.getCustomer());
        updateProductQuantitiesForExistingItems(order, updatedOrder);
        decreaseProductQuantitiesForNewItems(order, updatedOrder);
        resetProductQuantitiesForRemovedItems(order, updatedOrder);
        updateOrderDetails(order, updatedOrder);
        orderRepository.save(order);
        logger.info("Order with id {} updated", order.getId());
    }

    @Transactional
    public void deleteOrder(long id) {
        var order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        if (order.getStatus() == OrderStatus.UNPAID) {
            logger.info("Order status is UNPAID, reset associated product quantities");
            updateProductQuantities(order.getItems(), "increase");
        }
        orderRepository.delete(order);
        logger.info("Order with id {} deleted", id);
    }

    private void checkCustomer(Customer customer) {
        if (!customerRepository.existsById(customer.getId())) {
            logger.info("Customer with id {} not found, throwing exception", customer.getId());
            throw new InvalidCustomerException();
        }
    }

    private void checkProductAvailabilityForNewItems(List<OrderItem> items) {
        for (var item : items) {
            var product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(InvalidProductException::new);
            if (item.getQuantity() > product.getQuantity()) {
                logger.info("Order items contains products with insufficient stock, throwing exception");
                throw new ProductWithInsufficientStockException();
            }
        }
    }

    private void updateProductQuantities(List<OrderItem> items, String operation) {
        for (var item : items) {
            var product = productRepository.findById(item.getProduct().getId()).orElseThrow();
            switch (operation) {
                case "increase" -> product.increaseQuantity(item.getQuantity());
                case "decrease" -> product.decreaseQuantity(item.getQuantity());
                default -> throw new IllegalArgumentException("No case found for operation " + operation);
            }
            logger.info("Updating product {}, new quantity is {}", product.getName(), product.getQuantity());
        }
    }

    private void updateProductQuantitiesForExistingItems(Order order, Order updatedOrder) {
        var updatedItems = updatedOrder.getItems().stream().filter(order.getItems()::contains).toList();
        var items = order.getItems().stream().filter(updatedItems::contains).toList();
        checkProductAvailabilityForExistingItems(items, updatedItems);
        updateProductQuantities(items, "increase");
        updateProductQuantities(updatedItems, "decrease");
    }

    private void checkProductAvailabilityForExistingItems(List<OrderItem> items, List<OrderItem> updatedItems) {
        IntStream.range(0, updatedItems.size()).forEach(i -> {
            var product = productRepository.findById(items.get(i).getProduct().getId())
                    .orElseThrow(InvalidProductException::new);
            var quantity = (int) items.get(i).getQuantity();
            var updatedQuantity = (int) updatedItems.get(i).getQuantity();
            if (updatedQuantity - quantity > product.getQuantity()) {
                logger.info("Order items contains products with insufficient stock, throwing exception");
                throw new ProductWithInsufficientStockException();
            }
        });
    }

    private void decreaseProductQuantitiesForNewItems(Order order, Order updatedOrder) {
        var newItems = updatedOrder.getItems().stream().filter(item -> !order.getItems().contains(item)).toList();
        checkProductAvailabilityForNewItems(newItems);
        updateProductQuantities(newItems, "decrease");
    }

    private void resetProductQuantitiesForRemovedItems(Order order, Order updatedOrder) {
        var removedItems = order.getItems().stream().filter(item -> !updatedOrder.getItems().contains(item)).toList();
        updateProductQuantities(removedItems, "increase");
    }

    private void updateOrderDetails(Order order, Order updatedOrder) {
        order.setStatus(updatedOrder.getStatus());
        order.setCustomer(updatedOrder.getCustomer());
        order.setItems(updatedOrder.getItems());
    }
}
