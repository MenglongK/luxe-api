package co.istad.corefeature.brand.service;

import co.istad.corefeature.brand.dto.BrandRequest;
import co.istad.corefeature.brand.dto.BrandResponse;
import co.istad.corefeature.brand.entity.BrandEntity;
import co.istad.corefeature.brand.mapper.BrandMapper;
import co.istad.corefeature.brand.repository.BrandRepository;
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
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest brandRequest) {
        if (brandRepository.existsByName(brandRequest.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand already exists");
        }
        BrandEntity brandEntity = brandMapper.toEntity(brandRequest);
        if (brandEntity.getIsActive() == null) {
            brandEntity.setIsActive(true);
        }
        return brandMapper.toResponse(brandRepository.save(brandEntity));
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(BrandRequest brandRequest, Long id) {
        BrandEntity brandEntity = brandRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));
        if (!brandEntity.getName().equals(brandRequest.name()) && brandRepository.existsByName(brandRequest.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand name already exists");
        }
        brandMapper.updateEntityFromDto(brandRequest, brandEntity);
        return brandMapper.toResponse(brandRepository.save(brandEntity));
    }

    @Override
    public Page<BrandResponse> findAllBrands(Pageable pageable) {
        Page<BrandEntity> brandPage = brandRepository.findByIsActiveTrue(pageable);
        return brandPage.map(brandMapper::toResponse);
    }

    @Override
    public BrandResponse findBrandById(Long id) {
        return brandRepository.findById(id).map(brandMapper::toResponse).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));
    }

    @Override
    @Transactional
    public BrandResponse softDeleteById(Long id) {
        BrandEntity brandEntity = brandRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));
        brandEntity.setIsActive(false);
        return brandMapper.toResponse(brandRepository.save(brandEntity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        BrandEntity brandEntity = brandRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));
        brandRepository.delete(brandEntity);
    }
}
