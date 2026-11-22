package jp.ac.sanko.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
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
public class CheckoutController {

    private final CartItemRepository cartItemRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;

    public CheckoutController(
            CartItemRepository cartItemRepository,
            CustomerOrderRepository customerOrderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository) {

        this.cartItemRepository = cartItemRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
    }

    // SHOW CHECKOUT PAGE
    @GetMapping("/checkout")
    public String showCheckout(
            HttpSession session,
            Model model) {

        List<CartItem> cartItems =
                cartItemRepository.findBySessionId(session.getId());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        double total = 0;

        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice()
                    * item.getQuantity();
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);

        return "checkout";
    }

    // PLACE ORDER
    @PostMapping("/checkout/place-order")
    @Transactional
    public String placeOrder(

            @RequestParam String customerName,
            @RequestParam String phone,
            @RequestParam String deliveryAddress,
            @RequestParam String paymentMethod,

            @RequestParam(value = "receiptFile", required = false)
            MultipartFile receiptFile,

            HttpSession session,
            Model model,
            Authentication authentication) {

        // CHECK CUSTOMER LOGIN
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return "redirect:/login";
        }

        // FIND LOGGED-IN CUSTOMER
        Customer customer = customerRepository
                .findByEmail(authentication.getName());

        if (customer == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems =
                cartItemRepository.findBySessionId(session.getId());

        if (cartItems.isEmpty()
                || customerName.isBlank()
                || phone.isBlank()
                || deliveryAddress.isBlank()) {

            return "redirect:/checkout";
        }

        // CHECK PAYMENT METHOD
        if (!"COD".equals(paymentMethod)
                && !"BANK".equals(paymentMethod)) {

            return "redirect:/checkout";
        }

        // BANK RECEIPT VALIDATION
        if ("BANK".equals(paymentMethod)) {

            if (receiptFile == null || receiptFile.isEmpty()) {
                return "redirect:/checkout?receiptError";
            }

            String contentType = receiptFile.getContentType();

            if (contentType == null ||
                    !(contentType.equals("image/jpeg")
                    || contentType.equals("image/png")
                    || contentType.equals("image/webp"))) {

                return "redirect:/checkout?receiptError";
            }
        }

        // CALCULATE TOTAL
        double total = 0;

        for (CartItem item : cartItems) {

            total += item.getProduct().getPrice()
                    * item.getQuantity();
        }

        // CREATE ORDER
        CustomerOrder order = new CustomerOrder(
                customerName,
                phone,
                deliveryAddress,
                total
        );

        // CONNECT ORDER WITH CUSTOMER
        order.setCustomer(customer);

        order.setPaymentMethod(paymentMethod);

        if ("COD".equals(paymentMethod)) {

            order.setPaymentStatus("Pending");

        } else {

            order.setPaymentStatus("Pending Verification");
        }

        // SAVE BANK RECEIPT
        if ("BANK".equals(paymentMethod)) {

            try {

                String extension;

                switch (receiptFile.getContentType()) {

                    case "image/jpeg":
                        extension = ".jpg";
                        break;

                    case "image/png":
                        extension = ".png";
                        break;

                    case "image/webp":
                        extension = ".webp";
                        break;

                    default:
                        return "redirect:/checkout?receiptError";
                }

                Path uploadPath =
                        Paths.get("uploads", "receipts");

                Files.createDirectories(uploadPath);

                String fileName =
                        UUID.randomUUID().toString() + extension;

                Path filePath = uploadPath.resolve(fileName);

                Files.copy(receiptFile.getInputStream(), filePath);

                order.setReceiptImage(fileName);

            } catch (IOException e) {

                e.printStackTrace();

                return "redirect:/checkout?uploadError";
            }
        }

        // SAVE ORDER
        customerOrderRepository.save(order);

        // SAVE ORDER ITEMS
        for (CartItem item : cartItems) {

            OrderItem orderItem = new OrderItem(
                    order,
                    item.getProduct(),
                    item.getFlavour(),
                    item.getQuantity(),
                    item.getProduct().getPrice()
            );

            orderItemRepository.save(orderItem);
        }

        // CLEAR CART AFTER ORDER SAVED
        cartItemRepository.deleteAll(cartItems);

        // SAVE ORDER ID IN SESSION
        session.setAttribute("lastOrderId", order.getId());

        model.addAttribute("order", order);

        return "order-pending";
    }


    // CUSTOMER ORDER STATUS PAGE
    @GetMapping("/order-status")
    public String showOrderStatus(
            HttpSession session,
            Model model,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return "redirect:/login";
        }

        Customer customer = customerRepository
                .findByEmail(authentication.getName());

        if (customer == null) {
            return "redirect:/login";
        }

        Long orderId = (Long) session.getAttribute("lastOrderId");

        if (orderId == null) {
            return "redirect:/";
        }

        CustomerOrder order = customerOrderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null
                || order.getCustomer() == null
                || !order.getCustomer().getId().equals(customer.getId())) {

            return "redirect:/";
        }

        model.addAttribute("order", order);

        return "order-status";
    }
    
 // AUTOMATIC PAYMENT STATUS CHECK
    @GetMapping("/checkout/payment-status")
    @ResponseBody
    public Map<String, String> checkPaymentStatus(
            @RequestParam Long orderId,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return Map.of(
                    "paymentStatus", "Unauthorized",
                    "orderStatus", "Unauthorized"
            );
        }

        Customer customer = customerRepository
                .findByEmail(authentication.getName());

        if (customer == null) {
            return Map.of(
                    "paymentStatus", "Unauthorized",
                    "orderStatus", "Unauthorized"
            );
        }

        CustomerOrder order = customerOrderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null
                || order.getCustomer() == null
                || !order.getCustomer().getId().equals(customer.getId())) {

            return Map.of(
                    "paymentStatus", "Not Found",
                    "orderStatus", "Not Found"
            );
        }

        return Map.of(
                "paymentStatus", order.getPaymentStatus(),
                "orderStatus", order.getStatus()
        );
    }
    
 // CUSTOMER UPLOAD NEW PAYMENT RECEIPT
    @PostMapping("/my-orders/{id}/upload-receipt")
    @Transactional
    public String uploadNewReceipt(
            @PathVariable Long id,
            @RequestParam("receiptFile") MultipartFile receiptFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        Customer customer =
                customerRepository.findByEmail(authentication.getName());

        if (customer == null) {
            return "redirect:/login";
        }

        CustomerOrder order = customerOrderRepository.findById(id)
                .orElse(null);

        if (order == null
                || order.getCustomer() == null
                || !order.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/my-orders";
        }

        if (!"BANK".equals(order.getPaymentMethod())
                || !"Try Again".equals(order.getPaymentStatus())) {
            return "redirect:/my-orders";
        }

        if (receiptFile == null || receiptFile.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Please select a payment receipt.");
            return "redirect:/my-orders";
        }

        String contentType = receiptFile.getContentType();

        if (contentType == null
                || !(contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp"))) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Please upload JPG, PNG or WEBP image.");
            return "redirect:/my-orders";
        }

        try {
            String extension;

            switch (contentType) {
                case "image/jpeg":
                    extension = ".jpg";
                    break;
                case "image/png":
                    extension = ".png";
                    break;
                case "image/webp":
                    extension = ".webp";
                    break;
                default:
                    return "redirect:/my-orders";
            }

            Path uploadPath = Paths.get("uploads", "receipts");
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(receiptFile.getInputStream(), filePath);

            order.setReceiptImage(fileName);
            order.setPaymentStatus("Pending Verification");

            customerOrderRepository.save(order);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "New payment receipt uploaded successfully!");

        } catch (IOException e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Receipt upload failed. Please try again.");
        }

        return "redirect:/my-orders";
    }
}