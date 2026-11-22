package jp.ac.sanko.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.ac.sanko.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findBySessionId(String sessionId);

}