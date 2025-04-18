package archivegarden.shop.service.admin.product;

import archivegarden.shop.dto.admin.AdminSearchCondition;
import archivegarden.shop.dto.admin.product.discount.AdminAddDiscountForm;
import archivegarden.shop.dto.admin.product.discount.AdminDiscountDetailsDto;
import archivegarden.shop.dto.admin.product.discount.AdminDiscountListDto;
import archivegarden.shop.dto.admin.product.discount.AdminEditDiscountForm;
import archivegarden.shop.dto.admin.product.product.AdminProductSummaryDto;
import archivegarden.shop.entity.Discount;
import archivegarden.shop.entity.Product;
import archivegarden.shop.exception.ajax.AjaxEntityNotFoundException;
import archivegarden.shop.exception.common.EntityNotFoundException;
import archivegarden.shop.repository.discount.DiscountRepository;
import archivegarden.shop.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminDiscountService {

    private final AdminProductImageService productImageService;
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final Executor customAsyncExecutor;

    /**
     * 할인 저장
     */
    public Long saveDiscount(AdminAddDiscountForm form) {
        List<Product> products = productRepository.findAllInAdmin(form.getProductIds());
        Discount discount = Discount.createDiscount(form, products);
        discountRepository.save(discount);
        return discount.getId();
    }

    /**
     * 할인 단건 조회
     *
     * @throws EntityNotFoundException
     */
    @Transactional(readOnly = true)
    public AdminDiscountDetailsDto getDiscount(Long discountId) {
        Discount discount = discountRepository.findByIdWithProducts(discountId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품 할인입니다."));
        AdminDiscountDetailsDto adminDiscountDetailsDto = new AdminDiscountDetailsDto(discount);
        List<AdminProductSummaryDto> products = adminDiscountDetailsDto.getProducts();

        List<CompletableFuture<Void>> futures = products.stream()
                .map(product -> CompletableFuture.runAsync(() -> {
                    // 비동기적으로 이미지 다운로드
                    String encodedImageData = productImageService.getEncodedImageDataAsync(product.getDisplayImageData()).join();
                    product.setDisplayImageData(encodedImageData);  // 이미지 인코딩된 데이터 설정
                }, customAsyncExecutor))  // customAsyncExecutor 사용
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return adminDiscountDetailsDto;
    }

    /**
     * 할인 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<AdminDiscountListDto> getDiscountList(AdminSearchCondition condition, Pageable pageable) {
        return discountRepository.findAll(condition, pageable).map(d -> new AdminDiscountListDto(d));
    }

    /**
     * 할인 수정 폼 조회
     *
     * @throws EntityNotFoundException
     */
    @Transactional(readOnly = true)
    public AdminEditDiscountForm getEditDiscountForm(Long discountId) {
        Discount discount = discountRepository.findByIdWithProducts(discountId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품 할인입니다."));
        AdminEditDiscountForm adminEditDiscountForm = new AdminEditDiscountForm(discount);

        List<AdminProductSummaryDto> products = adminEditDiscountForm.getProducts();

        List<CompletableFuture<Void>> futures = products.stream()
                .map(product -> CompletableFuture.runAsync(() -> {
                    // 비동기적으로 이미지 다운로드
                    String encodedImageData = productImageService.getEncodedImageDataAsync(product.getDisplayImageData()).join();
                    product.setDisplayImageData(encodedImageData);  // 이미지 인코딩된 데이터 설정
                }, customAsyncExecutor))  // customAsyncExecutor 사용
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return adminEditDiscountForm;
    }

    /**
     * 할인 수정
     *
     * @throws EntityNotFoundException
     */
    public void updateDiscount(Long discountId, AdminEditDiscountForm form) {
        Discount discount = discountRepository.findById(discountId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 할인 혜택입니다."));

        discount.update(form);  //적용 상품 외 필드 수정

        List<Long> existingProductIds = productRepository.findByDiscountId(discountId)
                .stream().map(p -> p.getId())
                .collect(Collectors.toList());

        List<Long> newProductIds = form.getProductIds();

        // 새로 추가할 상품
        List<Long> productsToAdd = newProductIds.stream()
                .filter(productId -> !existingProductIds.contains(productId))
                .collect(Collectors.toList());


        if(productsToAdd.size() > 0){
            for (Long productId : productsToAdd) {
                Product product = productRepository.findById(productId).get();
                product.updateDiscount(discount);
            }
        }

        // 삭제할 상품
        List<Long> productsToRemove = existingProductIds.stream()
                .filter(productId -> !newProductIds.contains(productId))
                .collect(Collectors.toList());

        if(!productsToRemove.isEmpty()) {
            for (Long productId : productsToRemove) {
                Product product = productRepository.findById(productId).get();
                product.removeDiscount();
            }
        }
    }

    /**
     * Ajax: 할인 단건 삭제
     *
     * @throws AjaxEntityNotFoundException
     */
    public void deleteDiscount(Long discountId) {
        Discount discount = discountRepository.findById(discountId).orElseThrow(() -> new AjaxEntityNotFoundException("존재하지 않는 할인입니다."));

        List<Product> productsWithDiscount = productRepository.findByDiscountId(discountId);
        if (!productsWithDiscount.isEmpty()) {
            for (Product product : productsWithDiscount) {
                product.removeDiscount();
            }
        }

        discountRepository.delete(discount);
    }

    /**
     * Ajax: 할인명 중복 검사
     */
    public boolean isNameAvailable(String name) {
        return discountRepository.findByName(name).isEmpty();
    }
}
