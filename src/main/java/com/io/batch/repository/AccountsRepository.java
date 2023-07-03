package com.io.batch.repository;

import com.io.batch.domain.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository  extends JpaRepository<Accounts, Integer> {
}
