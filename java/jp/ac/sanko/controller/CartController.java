package jp.ac.sanko.controller;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jp.ac.sanko.entity.CartItem;
import jp.ac.sanko.entity.product;
import jp.ac.sanko.repository.CartItemRepository;
import jp.ac.sanko.repository.ProductRepository;

@Controller
public class CartController {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartController(
            ProductRepository productRepository,
            CartItemRepository cartItemRepository) {

        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // ADD TO CART
    @PostMapping("/cart/add/{id}")
    public String addToCart(
            @PathVariable Long id,
            @RequestParam String flavour,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            Authentication authentication) {

        // Check Login
        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (!loggedIn) {
            return "redirect:/login";
        }

        product item = productRepository.findById(id).orElse(null);

        if (item == null || !item.isAvailable()
                || flavour == null || flavour.isBlank()
                || quantity < 1 || quantity > 99) {

            return "redirect:/products-page";
        }

        String sessionId = session.getId();

        List<CartItem> cartItems =
                cartItemRepository.findBySessionId(sessionId);

        boolean found = false;

        for (CartItem cartItem : cartItems) {

            if (cartItem.getProduct().getId().equals(id)
                    && cartItem.getFlavour().equals(flavour)) {

                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItemRepository.save(cartItem);

                found = true;
                break;
            }
        }

        if (!found) {

            CartItem newItem =
                    new CartItem(item, sessionId, flavour, quantity);

            cartItemRepository.save(newItem);
        }

        return "redirect:/cart";
    }

    // SHOW CART
    @GetMapping("/cart")
    public String showCart(
            HttpSession session,
            Model model,
            Authentication authentication) {

        List<CartItem> cartItems =
                cartItemRepository.findBySessionId(session.getId());

        double total = 0;

        for (CartItem item : cartItems) {

            total += item.getProduct().getPrice()
                    * item.getQuantity();
        }

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("loggedIn", loggedIn);

        return "cart";
    }
 // REMOVE FROM CART
    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(
            @PathVariable Long id,
            HttpSession session,
            Authentication authentication) {

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (!loggedIn) {
            return "redirect:/login";
        }

        CartItem item = cartItemRepository.findById(id).orElse(null);

        if (item != null && item.getSessionId().equals(session.getId())) {
            cartItemRepository.delete(item);
        }

        return "redirect:/cart";
    }
}