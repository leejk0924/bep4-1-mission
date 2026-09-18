package com.back.boundedContext.cash.out;

import com.back.boundedContext.cash.domain.CashMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMemberRepository extends JpaRepository<CashMember, Integer> {

}
