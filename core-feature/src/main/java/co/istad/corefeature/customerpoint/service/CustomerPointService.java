package co.istad.corefeature.customerpoint.service;

import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.dto.CustomerPointTransactionResponse;
import co.istad.corefeature.customerpoint.dto.PointsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerPointService {

    CustomerPointResponse create(CustomerPointRequest request);

    Page<CustomerPointResponse> findAll(Pageable pageable);

    CustomerPointResponse findById(Long id);

    CustomerPointResponse findByCustomerId(String customerId);

    CustomerPointResponse update(Long id, CustomerPointRequest request);

    CustomerPointResponse earn(Long id, PointsRequest request);

    CustomerPointResponse redeem(Long id, PointsRequest request);

    CustomerPointResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);

    Page<CustomerPointTransactionResponse> findTransactionsByPointId(Long id, Pageable pageable);

    Page<CustomerPointTransactionResponse> findTransactionsByCustomerId(String customerId, Pageable pageable);
}
