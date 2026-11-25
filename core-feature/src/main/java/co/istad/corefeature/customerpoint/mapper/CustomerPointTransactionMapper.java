package co.istad.corefeature.customerpoint.mapper;

import co.istad.corefeature.customerpoint.dto.CustomerPointTransactionResponse;
import co.istad.corefeature.customerpoint.entity.CustomerPointTransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerPointTransactionMapper {

    CustomerPointTransactionResponse toResponse(CustomerPointTransactionEntity entity);
}
