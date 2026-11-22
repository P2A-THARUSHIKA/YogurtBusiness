package jp.ac.sanko.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.ac.sanko.entity.CustomerOrder;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

	List<CustomerOrder> findByCustomer_IdAndArchivedByCustomerFalseOrderByOrderDateDesc(Long customerId);
}