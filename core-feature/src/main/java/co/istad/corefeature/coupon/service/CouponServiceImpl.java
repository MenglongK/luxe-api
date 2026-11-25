package co.istad.corefeature.coupon.service;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.coupon.dto.*;
import co.istad.corefeature.coupon.entity.CouponEntity;
import co.istad.corefeature.coupon.mapper.CouponMapper;
import co.istad.corefeature.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public CouponResponse create(CouponRequest request) {
        String normalizedCode = request.code().trim().toUpperCase();

        if (couponRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coupon with code " + normalizedCode + " already exists");
        }

        if (request.startDate() != null && request.expiryDate() != null && request.expiryDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be before start date");
        }

        CouponEntity coupon = couponMapper.toEntity(request);
        coupon.setCode(normalizedCode);
        coupon.setIsActive(true);
        coupon.setUsageCount(0);

        if (coupon.getStatus() == null) {
            coupon.setStatus(CouponStatus.ACTIVE);
        }
        if (coupon.getMinSpend() == null) {
            coupon.setMinSpend(BigDecimal.ZERO);
        }

        CouponEntity saved = couponRepository.save(coupon);
        return couponMapper.toResponse(saved);
    }

    @Override
    public Page<CouponResponse> findAll(CouponStatus status, DiscountType discountType, String search, Pageable pageable) {
        String query = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<CouponEntity> page;
        if (status == null && discountType == null && query == null) {
            page = couponRepository.findByIsActiveTrue(pageable);
        } else {
            page = couponRepository.findWithFilters(status, discountType, query, pageable);
        }
        return page.map(couponMapper::toResponse);
    }

    @Override
    public CouponResponse findById(UUID id) {
        CouponEntity coupon = couponRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with ID: " + id));
        return couponMapper.toResponse(coupon);
    }

    @Override
    public CouponResponse findByCode(String code) {
        CouponEntity coupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with code: " + code));
        return couponMapper.toResponse(coupon);
    }

    @Override
    public CouponValidationResponse validateCoupon(CouponValidateRequest request) {
        String normalizedCode = request.code().trim().toUpperCase();
        Optional<CouponEntity> optionalCoupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(normalizedCode);

        if (optionalCoupon.isEmpty()) {
            return CouponValidationResponse.builder()
                    .isValid(false)
                    .code(normalizedCode)
                    .subtotal(request.subtotal())
                    .discountAmount(BigDecimal.ZERO)
                    .totalAfterDiscount(request.subtotal())
                    .message("Coupon code " + normalizedCode + " is invalid or does not exist.")
                    .build();
        }

        CouponEntity coupon = optionalCoupon.get();
        LocalDate today = LocalDate.now();

        if (coupon.getStatus() == CouponStatus.DISABLED) {
            return buildInvalidResponse(coupon, request.subtotal(), "Coupon " + normalizedCode + " is currently disabled.");
        }

        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            return buildInvalidResponse(coupon, request.subtotal(), "Promotion begins on " + coupon.getStartDate() + ".");
        }

        if (coupon.getExpiryDate() != null && today.isAfter(coupon.getExpiryDate())) {
            return buildInvalidResponse(coupon, request.subtotal(), "Coupon has expired on " + coupon.getExpiryDate() + ".");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            return buildInvalidResponse(coupon, request.subtotal(), "Coupon redemption limit of " + coupon.getUsageLimit() + " has been reached.");
        }

        if (request.subtotal().compareTo(coupon.getMinSpend()) < 0) {
            return buildInvalidResponse(coupon, request.subtotal(),
                    "Minimum cart spend of $" + coupon.getMinSpend() + " is required to apply this coupon.");
        }

        BigDecimal discountAmount = coupon.calculateDiscount(request.subtotal());
        BigDecimal totalAfterDiscount = request.subtotal().subtract(discountAmount).max(BigDecimal.ZERO);

        return CouponValidationResponse.builder()
                .isValid(true)
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .subtotal(request.subtotal())
                .discountAmount(discountAmount)
                .totalAfterDiscount(totalAfterDiscount)
                .message("Coupon applied successfully! You saved $" + discountAmount + ".")
                .build();
    }

    @Override
    @Transactional
    public CouponResponse update(UUID id, CouponUpdateRequest request) {
        CouponEntity coupon = couponRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with ID: " + id));

        if (request.startDate() != null && request.expiryDate() != null && request.expiryDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be before start date");
        }

        couponMapper.updateEntityFromDto(request, coupon);
        CouponEntity updated = couponRepository.save(coupon);
        return couponMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public CouponResponse softDeleteById(UUID id) {
        CouponEntity coupon = couponRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with ID: " + id));

        coupon.setIsActive(false);
        CouponEntity saved = couponRepository.save(coupon);
        return couponMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void permanentDeleteById(UUID id) {
        CouponEntity coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with ID: " + id));
        couponRepository.delete(coupon);
    }

    @Override
    @Transactional
    public CouponResponse incrementUsage(String code) {
        CouponEntity coupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with code: " + code));

        coupon.setUsageCount(coupon.getUsageCount() + 1);
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            coupon.setStatus(CouponStatus.EXPIRED);
        }

        CouponEntity saved = couponRepository.save(coupon);
        return couponMapper.toResponse(saved);
    }

    private CouponValidationResponse buildInvalidResponse(CouponEntity coupon, BigDecimal subtotal, String message) {
        return CouponValidationResponse.builder()
                .isValid(false)
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .subtotal(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .totalAfterDiscount(subtotal)
                .message(message)
                .build();
    }
}
