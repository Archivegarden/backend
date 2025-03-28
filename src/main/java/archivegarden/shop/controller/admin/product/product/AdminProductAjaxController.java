package archivegarden.shop.controller.admin.product.product;

import archivegarden.shop.dto.ResultResponse;
import archivegarden.shop.dto.admin.product.product.AdminProductPopupSearchCondition;
import archivegarden.shop.dto.admin.product.product.AdminProductSummaryDto;
import archivegarden.shop.service.admin.product.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ajax/admin")
@RequiredArgsConstructor
public class AdminProductAjaxController {

    private final AdminProductService productService;

    /**
     *  상품명 중복 여부를 검사하는 메서드
     */
    @PostMapping("/product/check/name")
    public ResultResponse checkProductName(@RequestParam("name") String name) {
        boolean isAvailable = productService.isAvailableName(name);
        if(isAvailable) {
            return new ResultResponse(HttpStatus.OK.value(), "사용 가능한 상품명입니다.");
        } else {
            return new ResultResponse(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 상품명입니다.");
        }
    }

    /**
     * 상품 1개 삭제 요청을 처리하는 메서드
     */
    @DeleteMapping("/product")
    public ResultResponse deleteProduct(@RequestParam("productId") Long productId) {
        productService.deleteProduct(productId);
        return new ResultResponse(HttpStatus.OK.value(), "삭제가 완료되었습니다.");
    }

    /**
     * 상품 여러개 삭제 요청을 처리하는 메서드
     */
    @DeleteMapping("/products")
    public ResultResponse deleteProducts(@RequestBody List<Long> productIds) {
        productService.deleteProducts(productIds);
        return new ResultResponse(HttpStatus.OK.value(), "삭제가 완료되었습니다.");
    }

    /**
     * 할인 적용할 상품을 팝업창에서 검색하는 메서드
     */
    @ResponseBody
    @GetMapping("/products/search")
    public Page<AdminProductSummaryDto> searchProductsInPopup(@ModelAttribute("condition") AdminProductPopupSearchCondition condition) {
        PageRequest pageRequest = PageRequest.of(condition.getPage() - 1, condition.getLimit());
        Page<AdminProductSummaryDto> productPopupDtos = productService.searchProductsInPopup(condition, pageRequest);
        return productPopupDtos;
    }
}
