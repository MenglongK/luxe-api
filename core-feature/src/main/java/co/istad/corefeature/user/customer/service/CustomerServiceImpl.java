package co.istad.corefeature.user.customer.service;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import co.istad.corefeature.customerpoint.repository.CustomerPointRepository;
import co.istad.corefeature.order.entity.OrderEntity;
import co.istad.corefeature.order.repository.OrderRepository;
import co.istad.corefeature.user.customer.dto.CustomerRequest;
import co.istad.corefeature.user.customer.dto.CustomerResponse;
import co.istad.corefeature.user.customer.dto.CustomerSummaryResponse;
import co.istad.corefeature.user.customer.dto.CustomerUpdateRequest;
import co.istad.corefeature.user.customer.entity.CustomerEntity;
import co.istad.corefeature.user.customer.mapper.CustomerMapper;
import co.istad.corefeature.user.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerPointRepository customerPointRepository;
    private final OrderRepository orderRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer with email " + request.email() + " already exists");
        }

        if (request.customerCode() != null && !request.customerCode().isBlank()) {
            if (customerRepository.existsByCustomerCode(request.customerCode().trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer code " + request.customerCode() + " already exists");
            }
        }

        CustomerEntity customer = customerMapper.toEntity(request);
        customer.setEmail(request.email().trim().toLowerCase());
        customer.setIsActive(true);

        if (customer.getTier() == null) {
            customer.setTier(CustomerTier.STANDARD);
        }

        CustomerEntity savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public Page<CustomerResponse> findAll(CustomerTier tier, String search, Pageable pageable) {
        String query = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<CustomerEntity> customerPage;
        if (tier == null && query == null) {
            customerPage = customerRepository.findByIsActiveTrue(pageable);
        } else {
            customerPage = customerRepository.findWithFilters(tier, query, pageable);
        }
        return customerPage.map(customerMapper::toResponse);
    }

    @Override
    public CustomerResponse findById(UUID id) {
        CustomerEntity customer = customerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with ID: " + id));
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse findByCustomerCode(String customerCode) {
        CustomerEntity customer = customerRepository.findByCustomerCodeAndIsActiveTrue(customerCode.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with code: " + customerCode));
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse findByEmail(String email) {
        CustomerEntity customer = customerRepository.findByEmailAndIsActiveTrue(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with email: " + email));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(UUID id, CustomerUpdateRequest request) {
        CustomerEntity customer = customerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with ID: " + id));

        if (request.email() != null && !request.email().isBlank()) {
            String normalizedEmail = request.email().trim().toLowerCase();
            if (!normalizedEmail.equalsIgnoreCase(customer.getEmail()) && customerRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email " + request.email() + " is already in use by another customer");
            }
            customer.setEmail(normalizedEmail);
        }

        customerMapper.updateEntityFromDto(request, customer);

        if (request.primaryAddress() != null) {
            customer.setPrimaryAddress(customerMapper.toAddress(request.primaryAddress()));
        }

        CustomerEntity updatedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public CustomerResponse softDeleteById(UUID id) {
        CustomerEntity customer = customerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with ID: " + id));
        customer.setIsActive(false);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void permanentDeleteById(UUID id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with ID: " + id));
        customerRepository.delete(customer);
    }

    @Override
    public CustomerSummaryResponse getSummary(UUID id) {
        CustomerEntity customer = customerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with ID: " + id));

        Integer points = customerPointRepository.findByCustomerId(customer.getCustomerCode())
                .map(CustomerPointEntity::getTotalPoints)
                .orElse(0);

        List<OrderEntity> orders = orderRepository.findByCustomerIdAndIsActiveTrue(customer.getCustomerCode());
        long totalOrders = orders.size();
        BigDecimal totalSpent = orders.stream()
                .map(OrderEntity::getGrandTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime lastOrderDate = orders.stream()
                .map(OrderEntity::getCreatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return CustomerSummaryResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .avatarUrl(customer.getAvatarUrl())
                .tier(customer.getTier())
                .totalPoints(points)
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .lastOrderDate(lastOrderDate)
                .build();
    }
}
