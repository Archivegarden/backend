package archivegarden.shop.repository.product;

import archivegarden.shop.dto.admin.product.product.AdminProductSummaryDto;
import archivegarden.shop.dto.admin.product.product.AdminProductPopupSearchCondition;
import archivegarden.shop.dto.admin.product.product.AdminProductSearchCondition;
import archivegarden.shop.dto.admin.product.product.QAdminProductSummaryDto;
import archivegarden.shop.entity.Category;
import archivegarden.shop.entity.ImageType;
import archivegarden.shop.entity.Product;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static archivegarden.shop.entity.QDiscount.discount;
import static archivegarden.shop.entity.QProduct.product;
import static archivegarden.shop.entity.QProductImage.productImage;
import static org.springframework.util.StringUtils.hasText;

public class AdminProductRepositoryCustomImpl implements AdminProductRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public AdminProductRepositoryCustomImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    @Override
    public List<Product> findAllInAdmin(List<Long> productIds) {
        return queryFactory
                .selectFrom(product)
                .where(product.id.in(productIds))
                .fetch();
    }

    @Override
    public Optional<Product> findProductInAdmin(Long productId) {
        Product result = queryFactory
                .selectFrom(product)
                .leftJoin(product.discount, discount).fetchJoin()
                .leftJoin(product.productImages, productImage).fetchJoin()
                .where(product.id.eq(productId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<Product> findAllProductInAdmin(AdminProductSearchCondition condition, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .leftJoin(product.discount, discount).fetchJoin()
                .leftJoin(product.productImages, productImage).fetchJoin()
                .where(
                        keywordLike(condition.getSearchKey(), condition.getKeyword()),
                        categoryEq(Category.of(condition.getCategory())),
                        imageTypeDisplay()
                )
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .where(
                        keywordLike(condition.getSearchKey(), condition.getKeyword()),
                        categoryEq(Category.of(condition.getCategory()))
                )
                .from(product);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<AdminProductSummaryDto> searchProductsInDiscountPopup(AdminProductPopupSearchCondition condition, Pageable pageable) {
        List<AdminProductSummaryDto> content = queryFactory.select(new QAdminProductSummaryDto(
                        product.id,
                        product.name,
                        product.price,
                        productImage.imageUrl
                ))
                .from(product)
                .leftJoin(product.productImages, productImage)
                .on(
                        product.id.eq(productImage.product.id),
                        imageTypeDisplay()
                )
                .where(
                        nameLike(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        product.id.notIn(condition.getSelectedProductIds())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        nameLike(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        product.id.notIn(condition.getSelectedProductIds())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordLike(String searchKey, String keyword) {
        if (keyword != null) {
            if (searchKey.equals("name")) {
                return nameLike(keyword);
            }
        }

        return null;
    }

    /**
     * 카테고리
     */
    private BooleanExpression categoryEq(Category category) {
        return category != null ? product.category.eq(category) : null;
    }

    /**
     * 상품명 검색
     * 공백 제거, 대소문자 구분 안함
     */
    private BooleanExpression nameLike(String keyword) {
        if (hasText(keyword)) {
            String replaceKeyword = StringUtils.replace(keyword, " ", "");
            StringTemplate replaceProductName = Expressions.stringTemplate("function('replace',{0},{1},{2})", product.name, " ", "");
            return replaceProductName.containsIgnoreCase(replaceKeyword);
        }

        return null;
    }

    /**
     * IMAGE TYPE = DISPLAY
     */
    private BooleanExpression imageTypeDisplay() {
        return productImage != null ? productImage.imageType.eq(ImageType.DISPLAY) : null;
    }
}
