package com.back.boundedContext.cash.out;

import com.back.boundedContext.cash.domain.CashMember;
import com.back.boundedContext.cash.domain.Wallet;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface WalletRepository extends CrudRepository<Wallet, Integer> {
    Optional<Wallet> findByHolder(CashMember holder);
}
