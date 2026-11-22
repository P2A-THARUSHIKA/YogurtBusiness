package jp.ac.sanko.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jp.ac.sanko.entity.CartItem;
import jp.ac.sanko.entity.Customer;
import jp.ac.sanko.entity.CustomerOrder;
import jp.ac.sanko.entity.OrderItem;
import jp.ac.sanko.repository.CartItemRepository;
import jp.ac.sanko.repository.CustomerOrderRepository;
import jp.ac.sanko.repository.CustomerRepository;
import jp.ac.sanko.repository.OrderItemRepository;

@Controller
public class MyOrdersController {

    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public MyOrdersController(
            CustomerOrderRepository orderRepository,
            CustomerRepository customerRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository) {

        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // SHOW MY ORDERS
    @GetMapping("/my-orders")
    public String myOrders(
            Authentication authentication,
            Model model) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        Customer customer =
                customerRepository.findByEmail(authentication.getName());

        if (customer == null) {
            return "redirect:/login";
        }

        List<CustomerOrder> orders =
        		orderRepository.findByCustomer_IdAndArchivedByCustomerFalseOrderByOrderDateDesc(
        		        customer.getId());

        List<Long> orderIds = orders.stream()
                .map(CustomerOrder::getId)
                .collect(Collectors.toList());

        Map<Long, List<OrderItem>> orderItemsMap;

        if (orderIds.isEmpty()) {
            orderItemsMap = Map.of();
        } else {
            List<OrderItem> items =
                    orderItemRepository.findByCustomerOrder_IdIn(orderIds);

            orderItemsMap = items.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getCustomerOrder().getId()));
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderItemsMap", orderItemsMap);

        return "my-orders";
    }

    // REORDER
    @PostMapping("/my-orders/{id}/reorder")
    public String reorder(
            @PathVariable Long id,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        Customer customer =
                customerRepository.findByEmail(authentication.getName());

        if (customer == null) {
            return "redirect:/login";
        }

        CustomerOrder order = orderRepository.findById(id).orElse(null);

        // Check order ownership
        if (order == null || order.getCustomer() == null ||
                !order.getCustomer().getId().equals(customer.getId())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Order not found.");

            return "redirect:/my-orders";
        }

        // Reorder only delivered orders
        if (!"Delivered".equals(order.getStatus())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You can reorder delivered orders only.");

            return "redirect:/my-orders";
        }

        List<OrderItem> oldItems =
                orderItemRepository.findByCustomerOrder_IdIn(
                        List.of(order.getId()));

        String sessionId = session.getId();

        List<CartItem> currentCart =
                cartItemRepository.findBySessionId(sessionId);

        int addedCount = 0;

        for (OrderItem oldItem : oldItems) {

            jp.ac.sanko.entity.product product = oldItem.getProduct();

            if (product == null || !product.isAvailable()) {
                continue;
            }

            if (oldItem.getQuantity() < 1 ||
                    oldItem.getFlavour() == null ||
                    oldItem.getFlavour().isBlank()) {
                continue;
            }

            boolean found = false;

            for (CartItem cartItem : currentCart) {

                if (cartItem.getProduct().getId().equals(product.getId())
                        && cartItem.getFlavour().equals(oldItem.getFlavour())) {

                    cartItem.setQuantity(
                            cartItem.getQuantity() + oldItem.getQuantity());

                    cartItemRepository.save(cartItem);

                    found = true;
                    break;
                }
            }

            if (!found) {

                CartItem newItem = new CartItem(
                        product,
                        sessionId,
                        oldItem.getFlavour(),
                        oldItem.getQuantity());

                cartItemRepository.save(newItem);

                currentCart.add(newItem);
            }

            addedCount++;
        }

        if (addedCount == 0) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "These products are currently unavailable.");

            return "redirect:/my-orders";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Previous order items have been added to your cart!");

        return "redirect:/cart";
    }
    
    @PostMapping("/my-orders/{id}/delete")
    public String deleteOrder(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        Customer customer = customerRepository.findByEmail(authentication.getName());

        CustomerOrder order = orderRepository.findById(id).orElse(null);

        if (order == null ||
            order.getCustomer() == null ||
            !order.getCustomer().getId().equals(customer.getId())) {

            redirectAttributes.addFlashAttribute(
                "errorMessage", "Order not found.");
            return "redirect:/my-orders";
        }

        if (!"Delivered".equalsIgnoreCase(order.getStatus())) {
            redirectAttributes.addFlashAttribute(
                "errorMessage", "Only delivered orders can be deleted.");
            return "redirect:/my-orders";
        }

        order.setArchivedByCustomer(true);
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute(
            "successMessage", "Order removed from your history.");

        return "redirect:/my-orders";
    }
    
 // VIEW ORDER DETAILS
    @GetMapping("/order-details/{id}")
    public String viewOrderDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        Customer customer =
                customerRepository.findByEmail(authentication.getName());

        if (customer == null) {
            return "redirect:/login";
        }

        CustomerOrder order =
                orderRepository.findById(id).orElse(null);

        // Check order ownership
        if (order == null ||
                order.getCustomer() == null ||
                !order.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/my-orders";
        }

        List<OrderItem> orderItems =
                orderItemRepository.findByCustomerOrder_IdIn(
                        List.of(order.getId()));

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);

        return "order-details";
    }
}