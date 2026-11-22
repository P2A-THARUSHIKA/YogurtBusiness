package jp.ac.sanko.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ProductDetailsController {

    @GetMapping("/product-details")
    public String productDetails(
            @RequestParam int id,
            Model model,
            Authentication authentication) {

        model.addAttribute("productId", id);

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("loggedIn", loggedIn);

        switch (id) {

            case 1:
                model.addAttribute("productName", "Set Yogurt");
                model.addAttribute("description",
                        "Smooth, creamy and delicious plain set yogurt.");
                model.addAttribute("price", 100);
                break;

            case 2:
                model.addAttribute("productName", "Drinking Yogurt");
                model.addAttribute("description",
                        "A refreshing and delicious dairy drink.");
                model.addAttribute("price", 150);
                break;

            case 3:
                model.addAttribute("productName", "Jelly Yogurt");
                model.addAttribute("description",
                        "A delightful combination of creamy yogurt and jelly.");
                model.addAttribute("price", 120);
                break;

            case 4:
                model.addAttribute("productName", "Ice Cream");
                model.addAttribute("description",
                        "Sweet, creamy and delicious ice cream.");
                model.addAttribute("price", 200);
                break;

            case 5:
                model.addAttribute("productName", "Milk Ice");
                model.addAttribute("description",
                        "A cool and refreshing frozen dairy treat.");
                model.addAttribute("price", 80);
                break;

            default:
                return "redirect:/products-page";
        }

        return "product-details";
    }
}