package co.istad.corefeature.coupon.service;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.coupon.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CouponService {

    CouponResponse create(CouponRequest request);

    Page<CouponResponse> findAll(CouponStatus status, DiscountType discountType, String search, Pageable pageable);

    CouponResponse findById(UUID id);

    CouponResponse findByCode(String code);

    CouponValidationResponse validateCoupon(CouponValidateRequest request);

    CouponResponse update(UUID id, CouponUpdateRequest request);

    CouponResponse softDeleteById(UUID id);

    void permanentDeleteById(UUID id);

    CouponResponse incrementUsage(String code);
}
