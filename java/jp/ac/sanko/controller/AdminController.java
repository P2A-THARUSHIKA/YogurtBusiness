package jp.ac.sanko.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jp.ac.sanko.entity.CustomerOrder;
import jp.ac.sanko.repository.CustomerOrderRepository;
@Controller
public class AdminController {

    private final CustomerOrderRepository customerOrderRepository;

    public AdminController(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    // Show all orders
    @GetMapping("/admin/orders")
    public String showOrders(Model model) {

        model.addAttribute("orders", customerOrderRepository.findAll());

        return "admin-orders";
    }

    // Approve order
    @PostMapping("/admin/orders/{id}/approve")
    public String approveOrder(@PathVariable Long id) {

        CustomerOrder order = customerOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Bank payment must have a receipt before approval
        if ("BANK".equals(order.getPaymentMethod())) {

            if (order.getReceiptImage() == null ||
                    order.getReceiptImage().isBlank()) {

                return "redirect:/admin/orders";
            }

            order.setPaymentStatus("Verified");
        }

        order.setStatus("Approved");

        customerOrderRepository.save(order);

        return "redirect:/admin/orders";
    }

    // Request another receipt
    @PostMapping("/admin/orders/{id}/try-again")
    public String tryAgain(@PathVariable Long id) {

        CustomerOrder order = customerOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("BANK".equals(order.getPaymentMethod())) {

            order.setPaymentStatus("Try Again");

            customerOrderRepository.save(order);
        }

        return "redirect:/admin/orders";
    }

    // Mark order as Delivered
    @PostMapping("/admin/orders/{id}/delivered")
    public String markAsDelivered(@PathVariable Long id) {

        CustomerOrder order = customerOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("Approved".equals(order.getStatus())) {

            order.setStatus("Delivered");

            customerOrderRepository.save(order);
        }

        return "redirect:/admin/orders";
    }
 // VIEW CUSTOMER PAYMENT RECEIPT
    @GetMapping("/admin/orders/{id}/receipt")
    public ResponseEntity<Resource> viewReceipt(@PathVariable Long id) {

        CustomerOrder order = customerOrderRepository.findById(id)
                .orElse(null);

        if (order == null || order.getReceiptImage() == null
                || order.getReceiptImage().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path uploadPath = Paths.get("uploads", "receipts")
                    .toAbsolutePath().normalize();

            Path filePath = uploadPath.resolve(order.getReceiptImage())
                    .normalize();

            if (!filePath.startsWith(uploadPath) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            String contentType = Files.probeContentType(filePath);

            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}