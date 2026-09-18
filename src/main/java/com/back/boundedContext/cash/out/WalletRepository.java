package com.back.boundedContext.cash.out;

import com.back.boundedContext.cash.domain.Wallet;
import org.springframework.data.repository.CrudRepository;

public interface WalletRepository extends CrudRepository<Wallet, Integer> {
}
