package jp.ac.sanko.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.ac.sanko.entity.product;

public interface ProductRepository extends JpaRepository<product, Long> {

}