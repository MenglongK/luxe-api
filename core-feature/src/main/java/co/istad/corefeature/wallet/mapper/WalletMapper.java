package co.istad.corefeature.wallet.mapper;

import co.istad.corefeature.wallet.dto.WalletCreateRequest;
import co.istad.corefeature.wallet.dto.WalletResponse;
import co.istad.corefeature.wallet.dto.WalletTransactionResponse;
import co.istad.corefeature.wallet.entity.WalletEntity;
import co.istad.corefeature.wallet.entity.WalletTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "walletNumber", ignore = true)
    @Mapping(target = "balance", source = "initialBalance")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    WalletEntity toEntity(WalletCreateRequest request);

    WalletResponse toResponse(WalletEntity entity);

    WalletTransactionResponse toTransactionResponse(WalletTransactionEntity entity);
}
