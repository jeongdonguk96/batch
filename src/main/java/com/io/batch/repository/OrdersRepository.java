package com.io.batch.repository;

import com.io.batch.domain.Accounts;
import com.io.batch.domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
}
