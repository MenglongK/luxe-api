package co.istad.corefeature.customerpoint.service;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.common.enums.PointTransactionType;
import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.dto.CustomerPointTransactionResponse;
import co.istad.corefeature.customerpoint.dto.PointsRequest;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import co.istad.corefeature.customerpoint.entity.CustomerPointTransactionEntity;
import co.istad.corefeature.customerpoint.mapper.CustomerPointMapper;
import co.istad.corefeature.customerpoint.mapper.CustomerPointTransactionMapper;
import co.istad.corefeature.customerpoint.repository.CustomerPointRepository;
import co.istad.corefeature.customerpoint.repository.CustomerPointTransactionRepository;
import co.istad.corefeature.user.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerPointServiceImpl implements CustomerPointService {

    private static final String MEMBERSHIP_NOT_FOUND = "Membership point account not found: ";

    private final CustomerPointRepository customerPointRepository;
    private final CustomerPointTransactionRepository customerPointTransactionRepository;
    private final CustomerRepository customerRepository;
    private final CustomerPointMapper customerPointMapper;
    private final CustomerPointTransactionMapper customerPointTransactionMapper;

    @Override
    @Transactional
    public CustomerPointResponse create(CustomerPointRequest request) {
        String customerId = request.customerId().trim();
        if (customerPointRepository.existsByCustomerId(customerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership points already exist for customer: " + customerId);
        }

        int initialPoints = request.totalPoints() != null ? request.totalPoints() : 0;
        CustomerPointEntity entity = customerPointMapper.toEntity(request);
        entity.setCustomerId(customerId);
        entity.setTotalPoints(initialPoints);
        entity.setLifetimePoints(initialPoints);
        entity.setIsActive(true);
        entity.setMembershipNumber(generateMembershipNumber());
        entity.setTier(CustomerTier.fromPoints(initialPoints));

        syncMembershipTier(entity);
        entity = customerPointRepository.save(entity);

        if (entity.getTotalPoints() > 0) {
            recordTransaction(
                    entity.getId(),
                    entity.getCustomerId(),
                    PointTransactionType.EARNED,
                    entity.getTotalPoints(),
                    entity.getTotalPoints(),
                    null,
                    "Initial membership points balance"
            );
        }

        return customerPointMapper.toResponse(entity);
    }

    @Override
    public Page<CustomerPointResponse> findAll(CustomerTier tier, String search, Pageable pageable) {
        String query = (search != null && !search.isBlank()) ? search.trim() : null;
        return customerPointRepository.findAllWithFilter(tier, query, pageable)
                .map(customerPointMapper::toResponse);
    }

    @Override
    public CustomerPointResponse findById(Long id) {
        return customerPointMapper.toResponse(findActiveById(id));
    }

    @Override
    public CustomerPointResponse findByCustomerId(String customerId) {
        return customerPointMapper.toResponse(findActiveByCustomerId(customerId));
    }

    @Override
    public CustomerPointResponse findByMembershipNumber(String membershipNumber) {
        return customerPointRepository.findByMembershipNumberAndIsActiveTrue(membershipNumber.trim())
                .map(customerPointMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + membershipNumber));
    }

    @Override
    @Transactional
    public CustomerPointResponse update(Long id, CustomerPointRequest request) {
        CustomerPointEntity entity = findActiveById(id);

        if (request.totalPoints() != null) {
            if (request.totalPoints() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total points cannot be negative");
            }
            int oldPoints = entity.getTotalPoints() != null ? entity.getTotalPoints() : 0;
            int newPoints = request.totalPoints();
            if (oldPoints != newPoints) {
                int diff = newPoints - oldPoints;
                recordTransaction(
                        entity.getId(),
                        entity.getCustomerId(),
                        PointTransactionType.ADJUSTED,
                        diff,
                        newPoints,
                        null,
                        "Manual membership points adjustment"
                );
            }
            entity.setTotalPoints(newPoints);
            int currentLifetime = entity.getLifetimePoints() != null ? entity.getLifetimePoints() : 0;
            if (newPoints > currentLifetime) {
                entity.setLifetimePoints(newPoints);
            }
            syncMembershipTier(entity);
        }

        entity = customerPointRepository.save(entity);
        return customerPointMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CustomerPointResponse earn(Long id, PointsRequest request) {
        return earnPoints(findActiveById(id), request);
    }

    @Override
    @Transactional
    public CustomerPointResponse earnByCustomerId(String customerId, PointsRequest request) {
        return earnPoints(findActiveByCustomerId(customerId), request);
    }

    @Override
    @Transactional
    public CustomerPointResponse redeem(Long id, PointsRequest request) {
        return redeemPoints(findActiveById(id), request);
    }

    @Override
    @Transactional
    public CustomerPointResponse redeemByCustomerId(String customerId, PointsRequest request) {
        return redeemPoints(findActiveByCustomerId(customerId), request);
    }

    @Override
    @Transactional
    public CustomerPointResponse softDeleteById(Long id) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + id));

        entity.setIsActive(false);
        return customerPointMapper.toResponse(customerPointRepository.save(entity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + id));

        customerPointRepository.delete(entity);
    }

    @Override
    public Page<CustomerPointTransactionResponse> findTransactionsByPointId(Long id, Pageable pageable) {
        if (!customerPointRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + id);
        }
        return customerPointTransactionRepository.findByCustomerPointIdOrderByCreatedAtDesc(id, pageable)
                .map(customerPointTransactionMapper::toResponse);
    }

    @Override
    public Page<CustomerPointTransactionResponse> findTransactionsByCustomerId(String customerId, Pageable pageable) {
        return customerPointTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId.trim(), pageable)
                .map(customerPointTransactionMapper::toResponse);
    }

    private CustomerPointResponse earnPoints(CustomerPointEntity entity, PointsRequest request) {
        int current = entity.getTotalPoints() != null ? entity.getTotalPoints() : 0;
        int currentLifetime = entity.getLifetimePoints() != null ? entity.getLifetimePoints() : current;

        int newTotal = current + request.points();
        int newLifetime = currentLifetime + request.points();

        entity.setTotalPoints(newTotal);
        entity.setLifetimePoints(newLifetime);
        syncMembershipTier(entity);
        entity = customerPointRepository.save(entity);

        recordTransaction(
                entity.getId(),
                entity.getCustomerId(),
                PointTransactionType.EARNED,
                request.points(),
                newTotal,
                request.referenceId(),
                request.description() != null ? request.description() : "Membership points earned"
        );

        return customerPointMapper.toResponse(entity);
    }

    private CustomerPointResponse redeemPoints(CustomerPointEntity entity, PointsRequest request) {
        int current = entity.getTotalPoints() != null ? entity.getTotalPoints() : 0;
        if (current < request.points()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient membership points to redeem. Available: " + current);
        }

        int newTotal = current - request.points();
        entity.setTotalPoints(newTotal);
        entity = customerPointRepository.save(entity);

        recordTransaction(
                entity.getId(),
                entity.getCustomerId(),
                PointTransactionType.REDEEMED,
                request.points(),
                newTotal,
                request.referenceId(),
                request.description() != null ? request.description() : "Membership points redeemed for reward or discount"
        );

        return customerPointMapper.toResponse(entity);
    }

    /**
     * Evaluates lifetime points against tier thresholds and auto-upgrades both the membership
     * point account and the linked customer profile without downgrading existing higher tiers.
     */
    private void syncMembershipTier(CustomerPointEntity entity) {
        int lifetime = entity.getLifetimePoints() != null ? entity.getLifetimePoints() : 0;
        CustomerTier earnedTier = CustomerTier.fromPoints(lifetime);
        final CustomerTier targetTier = CustomerTier.higherOf(entity.getTier(), earnedTier);

        CustomerTier resolvedTier = customerRepository.findByCustomerCodeAndIsActiveTrue(entity.getCustomerId())
                .map(customer -> {
                    CustomerTier customerUpgradedTier = CustomerTier.higherOf(customer.getTier(), targetTier);
                    if (customerUpgradedTier != customer.getTier()) {
                        customer.setTier(customerUpgradedTier);
                        customerRepository.save(customer);
                    }
                    return customerUpgradedTier;
                })
                .orElse(targetTier);

        entity.setTier(resolvedTier);
    }

    private CustomerPointEntity findActiveById(Long id) {
        return customerPointRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + id));
    }

    private CustomerPointEntity findActiveByCustomerId(String customerId) {
        return customerPointRepository.findByCustomerIdAndIsActiveTrue(customerId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership point account not found for customer: " + customerId));
    }

    private String generateMembershipNumber() {
        return "MEM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private void recordTransaction(Long pointId, String customerId, PointTransactionType type, Integer points,
                                   Integer balanceAfter, String referenceId, String description) {
        CustomerPointTransactionEntity transaction = CustomerPointTransactionEntity.builder()
                .customerPointId(pointId)
                .customerId(customerId)
                .transactionType(type)
                .points(points)
                .balanceAfter(balanceAfter)
                .referenceId(referenceId)
                .description(description)
                .build();
        customerPointTransactionRepository.save(transaction);
    }
}
