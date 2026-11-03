package co.istad.corefeature.customerpoint.service;

import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.dto.PointsRequest;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import co.istad.corefeature.customerpoint.mapper.CustomerPointMapper;
import co.istad.corefeature.customerpoint.repository.CustomerPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerPointServiceImpl implements CustomerPointService {

    private final CustomerPointRepository customerPointRepository;
    private final CustomerPointMapper customerPointMapper;

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
            entity.setTotalPoints(request.totalPoints());
        }

        entity = customerPointRepository.save(entity);
        return customerPointMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CustomerPointResponse earn(Long id, PointsRequest request) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));

        entity.setTotalPoints(entity.getTotalPoints() + request.points());
        entity = customerPointRepository.save(entity);
        return customerPointMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CustomerPointResponse redeem(Long id, PointsRequest request) {
        CustomerPointEntity entity = customerPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer points not found: " + id));

        if (entity.getTotalPoints() < request.points()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient points to redeem");
        }

        entity.setTotalPoints(entity.getTotalPoints() - request.points());
        entity = customerPointRepository.save(entity);
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
}
