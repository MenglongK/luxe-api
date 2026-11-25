package co.istad.corefeature.order.mapper;

import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.dto.AddressResponse;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.order.dto.OrderRequest;
import co.istad.corefeature.order.dto.OrderResponse;
import co.istad.corefeature.order.entity.OrderEntity;
import co.istad.corefeature.order.order_item.dto.OrderItemResponse;
import co.istad.corefeature.order.order_item.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "grandTotal", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderEntity toEntity(OrderRequest request);

    OrderResponse toResponse(OrderEntity entity);

    @Mapping(target = "productVariantId", source = "productVariant.id")
    OrderItemResponse toItemResponse(OrderItemEntity item);

    List<OrderItemResponse> toItemResponseList(List<OrderItemEntity> items);

    Address toAddress(AddressRequest request);

    AddressResponse toAddressResponse(Address address);
}
