package co.istad.corefeature.coupon.service;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.coupon.dto.*;
import co.istad.corefeature.coupon.entity.CouponEntity;
import co.istad.corefeature.coupon.mapper.CouponMapper;
import co.istad.corefeature.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    private CouponService couponService;

    private UUID couponId;
    private CouponEntity percentageCoupon;
    private CouponEntity fixedCoupon;
    private CouponResponse couponResponse;

    @BeforeEach
    void setUp() {
        couponService = new CouponServiceImpl(couponRepository, couponMapper);

        couponId = UUID.randomUUID();

        percentageCoupon = CouponEntity.builder()
                .id(couponId)
                .code("ATELIER15")
                .description("15% welcome discount")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15.00))
                .minSpend(BigDecimal.valueOf(300.00))
                .maxDiscount(BigDecimal.valueOf(500.00))
                .usageLimit(500)
                .usageCount(50)
                .startDate(LocalDate.now().minusDays(10))
                .expiryDate(LocalDate.now().plusDays(30))
                .status(CouponStatus.ACTIVE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        fixedCoupon = CouponEntity.builder()
                .id(UUID.randomUUID())
                .code("VIPROYAL200")
                .description("$200 gift voucher")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(200.00))
                .minSpend(BigDecimal.valueOf(1500.00))
                .usageLimit(100)
                .usageCount(20)
                .startDate(LocalDate.now().minusDays(5))
                .expiryDate(LocalDate.now().plusDays(60))
                .status(CouponStatus.ACTIVE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        couponResponse = CouponResponse.builder()
                .id(couponId)
                .code("ATELIER15")
                .description("15% welcome discount")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15.00))
                .minSpend(BigDecimal.valueOf(300.00))
                .maxDiscount(BigDecimal.valueOf(500.00))
                .usageLimit(500)
                .usageCount(50)
                .status(CouponStatus.ACTIVE)
                .isCurrentlyUsable(true)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should create coupon successfully")
    void createCoupon_success() {
        CouponRequest request = CouponRequest.builder()
                .code("ATELIER15")
                .description("15% welcome discount")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15.00))
                .minSpend(BigDecimal.valueOf(300.00))
                .build();

        when(couponRepository.existsByCodeIgnoreCase("ATELIER15")).thenReturn(false);
        when(couponMapper.toEntity(request)).thenReturn(percentageCoupon);
        when(couponRepository.save(percentageCoupon)).thenReturn(percentageCoupon);
        when(couponMapper.toResponse(percentageCoupon)).thenReturn(couponResponse);

        CouponResponse response = couponService.create(request);

        assertNotNull(response);
        assertEquals("ATELIER15", response.code());
        verify(couponRepository, times(1)).save(percentageCoupon);
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if coupon code already exists")
    void createCoupon_duplicateCode() {
        CouponRequest request = CouponRequest.builder()
                .code("ATELIER15")
                .description("15% welcome discount")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15.00))
                .build();

        when(couponRepository.existsByCodeIgnoreCase("ATELIER15")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> couponService.create(request));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already exists"));
        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST if expiryDate is before startDate")
    void createCoupon_invalidDates() {
        CouponRequest request = CouponRequest.builder()
                .code("INVALIDDATES")
                .description("Test invalid dates")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .startDate(LocalDate.now().plusDays(10))
                .expiryDate(LocalDate.now().plusDays(2))
                .build();

        when(couponRepository.existsByCodeIgnoreCase("INVALIDDATES")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> couponService.create(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Expiry date cannot be before start date"));
    }

    @Test
    @DisplayName("Should retrieve active coupon by ID")
    void findById_success() {
        when(couponRepository.findByIdAndIsActiveTrue(couponId)).thenReturn(Optional.of(percentageCoupon));
        when(couponMapper.toResponse(percentageCoupon)).thenReturn(couponResponse);

        CouponResponse response = couponService.findById(couponId);

        assertNotNull(response);
        assertEquals(couponId, response.id());
    }

    @Test
    @DisplayName("Should throw 404 NOT_FOUND when coupon ID does not exist")
    void findById_notFound() {
        when(couponRepository.findByIdAndIsActiveTrue(couponId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> couponService.findById(couponId));
    }

    @Test
    @DisplayName("Should validate percentage coupon and compute savings")
    void validateCoupon_percentage_success() {
        CouponValidateRequest request = new CouponValidateRequest("ATELIER15", BigDecimal.valueOf(1000.00));

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("ATELIER15")).thenReturn(Optional.of(percentageCoupon));

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertTrue(response.isValid());
        assertEquals("ATELIER15", response.code());
        // 15% of $1,000 = $150
        assertEquals(new BigDecimal("150.00"), response.discountAmount());
        assertEquals(new BigDecimal("850.00"), response.totalAfterDiscount());
        assertTrue(response.message().contains("You saved $150.00"));
    }

    @Test
    @DisplayName("Should cap percentage discount when exceeding maxDiscount")
    void validateCoupon_percentage_cappedByMaxDiscount() {
        CouponValidateRequest request = new CouponValidateRequest("ATELIER15", BigDecimal.valueOf(5000.00));

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("ATELIER15")).thenReturn(Optional.of(percentageCoupon));

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertTrue(response.isValid());
        // 15% of $5,000 would be $750, but maxDiscount is $500.00
        assertEquals(BigDecimal.valueOf(500.00), response.discountAmount());
        assertEquals(BigDecimal.valueOf(4500.00), response.totalAfterDiscount());
    }

    @Test
    @DisplayName("Should validate fixed voucher coupon")
    void validateCoupon_fixed_success() {
        CouponValidateRequest request = new CouponValidateRequest("VIPROYAL200", BigDecimal.valueOf(2000.00));

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("VIPROYAL200")).thenReturn(Optional.of(fixedCoupon));

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertTrue(response.isValid());
        assertEquals(BigDecimal.valueOf(200.00), response.discountAmount());
        assertEquals(BigDecimal.valueOf(1800.00), response.totalAfterDiscount());
    }

    @Test
    @DisplayName("Should reject coupon when minimum spend is not met")
    void validateCoupon_minSpendNotMet() {
        CouponValidateRequest request = new CouponValidateRequest("VIPROYAL200", BigDecimal.valueOf(1000.00));

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("VIPROYAL200")).thenReturn(Optional.of(fixedCoupon));

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertFalse(response.isValid());
        assertEquals(BigDecimal.ZERO, response.discountAmount());
        assertTrue(response.message().contains("Minimum cart spend of $1500.00 is required"));
    }

    @Test
    @DisplayName("Should reject coupon when expired")
    void validateCoupon_expired() {
        percentageCoupon.setExpiryDate(LocalDate.now().minusDays(1));
        CouponValidateRequest request = new CouponValidateRequest("ATELIER15", BigDecimal.valueOf(1000.00));

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("ATELIER15")).thenReturn(Optional.of(percentageCoupon));

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertFalse(response.isValid());
        assertTrue(response.message().contains("Coupon has expired"));
    }

    @Test
    @DisplayName("Should reject coupon when usage limit is reached")
    void validateCoupon_usageLimitReached() {
        percentageCoupon.setUsageLimit(50);
        percentageCoupon.setUsageCount(50);
        CouponValidateRequest request = new CouponValidateRequest("ATELIER15", BigDecimal.valueOf(1000.00));

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("ATELIER15")).thenReturn(Optional.of(percentageCoupon));

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertFalse(response.isValid());
        assertTrue(response.message().contains("redemption limit"));
    }

    @Test
    @DisplayName("Should update coupon successfully")
    void updateCoupon_success() {
        CouponUpdateRequest request = CouponUpdateRequest.builder()
                .description("Updated 18% discount")
                .discountValue(BigDecimal.valueOf(18.00))
                .build();

        when(couponRepository.findByIdAndIsActiveTrue(couponId)).thenReturn(Optional.of(percentageCoupon));
        when(couponRepository.save(percentageCoupon)).thenReturn(percentageCoupon);
        when(couponMapper.toResponse(percentageCoupon)).thenReturn(couponResponse);

        CouponResponse response = couponService.update(couponId, request);

        assertNotNull(response);
        verify(couponMapper, times(1)).updateEntityFromDto(request, percentageCoupon);
        verify(couponRepository, times(1)).save(percentageCoupon);
    }

    @Test
    @DisplayName("Should soft delete coupon by setting isActive to false")
    void softDeleteById_success() {
        when(couponRepository.findByIdAndIsActiveTrue(couponId)).thenReturn(Optional.of(percentageCoupon));
        when(couponRepository.save(percentageCoupon)).thenReturn(percentageCoupon);
        when(couponMapper.toResponse(percentageCoupon)).thenReturn(couponResponse);

        CouponResponse response = couponService.softDeleteById(couponId);

        assertNotNull(response);
        assertFalse(percentageCoupon.getIsActive());
        verify(couponRepository, times(1)).save(percentageCoupon);
    }

    @Test
    @DisplayName("Should hard delete coupon permanently")
    void permanentDeleteById_success() {
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(percentageCoupon));

        couponService.permanentDeleteById(couponId);

        verify(couponRepository, times(1)).delete(percentageCoupon);
    }

    @Test
    @DisplayName("Should increment usage count and mark expired when limit reached")
    void incrementUsage_success() {
        percentageCoupon.setUsageLimit(51);
        percentageCoupon.setUsageCount(50);

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("ATELIER15")).thenReturn(Optional.of(percentageCoupon));
        when(couponRepository.save(percentageCoupon)).thenReturn(percentageCoupon);
        when(couponMapper.toResponse(percentageCoupon)).thenReturn(couponResponse);

        CouponResponse response = couponService.incrementUsage("ATELIER15");

        assertNotNull(response);
        assertEquals(51, percentageCoupon.getUsageCount());
        assertEquals(CouponStatus.EXPIRED, percentageCoupon.getStatus());
        verify(couponRepository, times(1)).save(percentageCoupon);
    }
}
