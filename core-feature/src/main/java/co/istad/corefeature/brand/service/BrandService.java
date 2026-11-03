package co.istad.corefeature.brand.service;

import co.istad.corefeature.brand.dto.BrandRequest;
import co.istad.corefeature.brand.dto.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    BrandResponse createBrand(BrandRequest brandRequest);

    BrandResponse updateBrand(BrandRequest brandRequest, Long id);

    Page<BrandResponse> findAllBrands(Pageable pageable);

    BrandResponse findBrandById(Long id);

    BrandResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
