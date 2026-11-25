package co.istad.corefeature.wallet.service;

import co.istad.corefeature.common.enums.WalletStatus;
import co.istad.corefeature.common.enums.WalletTransactionType;
import co.istad.corefeature.wallet.dto.*;
import co.istad.corefeature.wallet.entity.WalletEntity;
import co.istad.corefeature.wallet.entity.WalletTransactionEntity;
import co.istad.corefeature.wallet.mapper.WalletMapper;
import co.istad.corefeature.wallet.repository.WalletRepository;
import co.istad.corefeature.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public WalletResponse create(WalletCreateRequest request) {
        if (walletRepository.existsByCustomerId(request.customerId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Loyalty wallet already exists for customer: " + request.customerId());
        }

        WalletEntity entity = walletMapper.toEntity(request);
        if (entity.getBalance() == null) {
            entity.setBalance(BigDecimal.ZERO);
        }
        if (entity.getCurrency() == null || entity.getCurrency().isBlank()) {
            entity.setCurrency("USD");
        }
        entity.setStatus(WalletStatus.ACTIVE);
        entity.setIsActive(true);
        entity.setWalletNumber("WAL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());

        entity = walletRepository.save(entity);

        if (entity.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(
                    entity.getId(),
                    entity.getCustomerId(),
                    WalletTransactionType.DEPOSIT,
                    entity.getBalance(),
                    entity.getBalance(),
                    null,
                    "Initial luxury store credit deposit"
            );
        }

        return walletMapper.toResponse(entity);
    }

    @Override
    public Page<WalletResponse> findAll(WalletStatus status, String search, Pageable pageable) {
        return walletRepository.findAllWithFilter(status, search, pageable)
                .map(walletMapper::toResponse);
    }

    @Override
    public WalletResponse findById(UUID id) {
        return walletRepository.findByIdAndIsActiveTrue(id)
                .map(walletMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + id));
    }

    @Override
    public WalletResponse findByCustomerId(String customerId) {
        return walletRepository.findByCustomerIdAndIsActiveTrue(customerId)
                .map(walletMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found for customer: " + customerId));
    }

    @Override
    public WalletResponse findByWalletNumber(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .filter(WalletEntity::getIsActive)
                .map(walletMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + walletNumber));
    }

    @Override
    @Transactional
    public WalletResponse deposit(UUID id, WalletDepositRequest request) {
        WalletEntity wallet = walletRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + id));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot deposit to wallet with status: " + wallet.getStatus().getDisplayName());
        }

        BigDecimal newBalance = wallet.getBalance().add(request.amount());
        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);

        String paymentMethodStr = request.paymentMethod() != null ? request.paymentMethod().name() : "PATRON_CREDIT";
        String description = request.description() != null ? request.description() : "Store credit deposit via " + paymentMethodStr;

        recordTransaction(
                wallet.getId(),
                wallet.getCustomerId(),
                WalletTransactionType.DEPOSIT,
                request.amount(),
                newBalance,
                request.referenceId(),
                description
        );

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse pay(UUID id, WalletPaymentRequest request) {
        WalletEntity wallet = walletRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + id));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet is currently " + wallet.getStatus().getDisplayName() + " and cannot process checkout payments");
        }

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient wallet balance. Available: $" + wallet.getBalance() + " " + wallet.getCurrency());
        }

        BigDecimal newBalance = wallet.getBalance().subtract(request.amount());
        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);

        String description = request.description() != null ? request.description() : "Store credit payment for order: " + (request.orderId() != null ? request.orderId() : "N/A");

        recordTransaction(
                wallet.getId(),
                wallet.getCustomerId(),
                WalletTransactionType.PAYMENT,
                request.amount(),
                newBalance,
                request.orderId(),
                description
        );

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse updateStatus(UUID id, WalletStatusUpdateRequest request) {
        WalletEntity wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + id));

        wallet.setStatus(request.status());
        wallet = walletRepository.save(wallet);
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse softDeleteById(UUID id) {
        WalletEntity wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + id));

        wallet.setIsActive(false);
        wallet.setStatus(WalletStatus.CLOSED);
        return walletMapper.toResponse(walletRepository.save(wallet));
    }

    @Override
    @Transactional
    public void permanentDeleteById(UUID id) {
        WalletEntity wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + id));

        walletRepository.delete(wallet);
    }

    @Override
    public Page<WalletTransactionResponse> findTransactionsByWalletId(UUID walletId, Pageable pageable) {
        if (!walletRepository.existsById(walletId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loyalty wallet not found: " + walletId);
        }
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable)
                .map(walletMapper::toTransactionResponse);
    }

    @Override
    public Page<WalletTransactionResponse> findTransactionsByCustomerId(String customerId, Pageable pageable) {
        return walletTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(walletMapper::toTransactionResponse);
    }

    private void recordTransaction(UUID walletId, String customerId, WalletTransactionType type,
                                   BigDecimal amount, BigDecimal balanceAfter, String referenceId, String description) {
        WalletTransactionEntity transaction = WalletTransactionEntity.builder()
                .walletId(walletId)
                .customerId(customerId)
                .transactionType(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .referenceId(referenceId)
                .description(description)
                .build();
        walletTransactionRepository.save(transaction);
    }
}
