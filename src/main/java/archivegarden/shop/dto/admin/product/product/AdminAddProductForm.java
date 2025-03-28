package archivegarden.shop.dto.admin.product.product;

import archivegarden.shop.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AdminAddProductForm {

    @NotBlank(message = "상품명을 입력해 주세요.")
    private String name;

    @NotNull(message = "카테고리를 선택해 주세요.")
    private Category category;

    @NotNull(message = "가격을 입력해 주세요.")
    @Positive(message = "유효한 값을 입력해 주세요.")
    private Integer price;

    @NotNull(message = "재고를 입력해 주세요.")
    @PositiveOrZero(message = "0 이상의 값을 입력해 주세요.")
    private Integer stockQuantity;

    private String details;
    private String size;
    private String shipping;
    private String notice;

    private MultipartFile displayImage;
    private MultipartFile hoverImage;
    private List<MultipartFile> detailImages;
}
