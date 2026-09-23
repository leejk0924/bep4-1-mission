package com.back.boundedContext.cash.app;

import com.back.shared.payout.dto.PayoutDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.back.boundedContext.cash.domain.CashLog.EventType.*;

@Service
@RequiredArgsConstructor
public class CashCompletePayoutUseCase {
    private final CashSupport cashSupport;

    public void completePayout(PayoutDto payout) {
        var holdingWallet = cashSupport.findHoldingWallet().get();
        var payeeWallet = cashSupport.findWalletByHolderId(payout.payeeId()).get();

        holdingWallet.debit(
                payout.amount(),
                payout.isPayeeSystem() ?
                        정산지급__상품판매_수수료 :
                        정산지급__상품판매_대금,
                payout.getModelTypeCode(),
                payout.id()
        );

        payeeWallet.credit(
                payout.amount(),
                payout.isPayeeSystem() ?
                        정산수령__상품판매_수수료 :
                        정산수령__상품판매_대금,
                payout.getModelTypeCode(),
                payout.id()
        );

    }
}
