package co.istad.corefeature.customerpoint.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerPointServiceImpl implements CustomerPointService {

    private final CustomerPointRepository customerPointRepository;
    private final CustomerPointTransactionRepository customerPointTransactionRepository;
    private final CustomerPointMapper customerPointMapper;
    private final CustomerPointTransactionMapper customerPointTransactionMapper;

    @Override
    @Transactional
    public CustomerPointResponse create(CustomerPointRequest request) {
        if (customerPointRepository.existsByCustomerId(request.customerId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer points already exist for customer: " + request.customerId());
        }

        CustomerPointEntity entity = customerPointMapper.toEntity(request);
        if (entity.getTotalPoints() == null) {
            entity.setTotalPoints(0);
        }

        entity = customerPointRepository.save(entity);

        if (entity.getTotalPoints() > 0) {
            recordTransaction(
                    entity.getId(),
                    entity.getCustomerId(),
                    PointTransactionType.EARNED,
                    entity.getTotalPoints(),
                    entity.getTotalPoints(),
                    null,
                    "Initial patron points balance"
            );
        }

        return customerPointMapper.toResponse(entity);
    }

    @Override
    public Page<CustomerPointResponse> findAll(Pageable pageable) {
        Page<CustomerPointEntity> pointPage = customerPointRepository.findByIsActiveTrue(pageable);
        return pointPage.map(customerPointMapper::toResponse);
    }

    @Override
    public CustomerPointResponse findById(Long id) {
        return customerPointRepository.findById(id)
                .map(customerPointMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));
    }

    @Override
    public CustomerPointResponse findByCustomerId(String customerId) {
        return customerPointRepository.findByCustomerId(customerId)
                .map(customerPointMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found for customer: " + customerId));
    }

    @Override
    @Transactional
    public CustomerPointResponse update(Long id, CustomerPointRequest request) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));

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
                        "Manual patron points adjustment"
                );
            }
            entity.setTotalPoints(newPoints);
        }

        entity = customerPointRepository.save(entity);
        return customerPointMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CustomerPointResponse earn(Long id, PointsRequest request) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));

        int current = entity.getTotalPoints() != null ? entity.getTotalPoints() : 0;
        int newTotal = current + request.points();
        entity.setTotalPoints(newTotal);
        entity = customerPointRepository.save(entity);

        recordTransaction(
                entity.getId(),
                entity.getCustomerId(),
                PointTransactionType.EARNED,
                request.points(),
                newTotal,
                request.referenceId(),
                request.description() != null ? request.description() : "Points earned from purchase or bespoke patron action"
        );

        return customerPointMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CustomerPointResponse redeem(Long id, PointsRequest request) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));

        int current = entity.getTotalPoints() != null ? entity.getTotalPoints() : 0;
        if (current < request.points()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient points to redeem");
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
                request.description() != null ? request.description() : "Points redeemed for reward or checkout deduction"
        );

        return customerPointMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CustomerPointResponse softDeleteById(Long id) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));

        entity.setIsActive(false);
        return customerPointMapper.toResponse(customerPointRepository.save(entity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));

        customerPointRepository.delete(entity);
    }

    @Override
    public Page<CustomerPointTransactionResponse> findTransactionsByPointId(Long id, Pageable pageable) {
        if (!customerPointRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id);
        }
        return customerPointTransactionRepository.findByCustomerPointIdOrderByCreatedAtDesc(id, pageable)
                .map(customerPointTransactionMapper::toResponse);
    }

    @Override
    public Page<CustomerPointTransactionResponse> findTransactionsByCustomerId(String customerId, Pageable pageable) {
        return customerPointTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(customerPointTransactionMapper::toResponse);
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
