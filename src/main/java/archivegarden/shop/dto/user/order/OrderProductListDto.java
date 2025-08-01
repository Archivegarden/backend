package archivegarden.shop.dto.user.order;

import archivegarden.shop.entity.Discount;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;

@Getter
@Setter
public class OrderProductListDto {

    private Long id;    //productId
    private String name;
    private int quantity;
    private String displayImageData;
    private boolean isDiscounted;
    private String totalPrice;    //상품 정가 합
    private String totalSalePrice;  //상품 판매가 합

    @QueryProjection
    public OrderProductListDto(Long productId, String name, int price, int quantity, String displayImageUrl, Discount discount) {
        this.id = productId;
        this.name = name;
        this.quantity = quantity;
        this.displayImageData = displayImageUrl;
        this.totalPrice = new DecimalFormat("###,###원").format(price * quantity);
        if (discount != null) {
            this.isDiscounted = Boolean.TRUE;
            double salePriceDouble = price - (double) price * discount.getDiscountPercent() / 100;
            this.totalSalePrice = new DecimalFormat("###,###원").format(Math.round(salePriceDouble * this.quantity));
        }
    }
}
