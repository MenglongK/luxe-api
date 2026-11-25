package co.istad.corefeature.wallet.service;

import co.istad.corefeature.common.enums.WalletStatus;
import co.istad.corefeature.wallet.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WalletService {

    WalletResponse create(WalletCreateRequest request);

    Page<WalletResponse> findAll(WalletStatus status, String search, Pageable pageable);

    WalletResponse findById(UUID id);

    WalletResponse findByCustomerId(String customerId);

    WalletResponse findByWalletNumber(String walletNumber);

    WalletResponse deposit(UUID id, WalletDepositRequest request);

    WalletResponse pay(UUID id, WalletPaymentRequest request);

    WalletResponse updateStatus(UUID id, WalletStatusUpdateRequest request);

    WalletResponse softDeleteById(UUID id);

    void permanentDeleteById(UUID id);

    Page<WalletTransactionResponse> findTransactionsByWalletId(UUID walletId, Pageable pageable);

    Page<WalletTransactionResponse> findTransactionsByCustomerId(String customerId, Pageable pageable);
}
