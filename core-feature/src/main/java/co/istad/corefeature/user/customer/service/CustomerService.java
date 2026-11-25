package co.istad.corefeature.user.customer.service;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.user.customer.dto.CustomerRequest;
import co.istad.corefeature.user.customer.dto.CustomerResponse;
import co.istad.corefeature.user.customer.dto.CustomerSummaryResponse;
import co.istad.corefeature.user.customer.dto.CustomerUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    Page<CustomerResponse> findAll(CustomerTier tier, String search, Pageable pageable);

    CustomerResponse findById(UUID id);

    CustomerResponse findByCustomerCode(String customerCode);

    CustomerResponse findByEmail(String email);

    CustomerResponse update(UUID id, CustomerUpdateRequest request);

    CustomerResponse softDeleteById(UUID id);

    void permanentDeleteById(UUID id);

    CustomerSummaryResponse getSummary(UUID id);
}
