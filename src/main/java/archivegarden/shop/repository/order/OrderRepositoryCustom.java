package archivegarden.shop.repository.order;

import archivegarden.shop.dto.user.order.OrderProductListDto;

import java.util.List;

public interface OrderRepositoryCustom {

    List<OrderProductListDto> findOrderProducts(Long orderId);
}
