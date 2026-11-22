package jp.ac.sanko.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.ac.sanko.repository.ProductRepository;

@Controller
public class HomeController {

    private final ProductRepository productRepository;

    public HomeController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/products-page")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/who-are-we")
    public String whoWeAre() {
        return "who-are-we";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}