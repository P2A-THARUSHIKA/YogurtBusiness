package jp.ac.sanko.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.ac.sanko.repository.ProductRepository;

@RestController
public class ProductController {

	// ProductRepositoryを使うための変数
	private final ProductRepository productRepository;

	// ProductRepositoryを受け取るコンストラクタ
	public ProductController(ProductRepository productRepository) {
	    // 受け取ったProductRepositoryを変数に代入する
	    this.productRepository = productRepository;
    }
  


    @GetMapping("/products")
    public Object getProducts() {
        return productRepository.findAll();
    }
}