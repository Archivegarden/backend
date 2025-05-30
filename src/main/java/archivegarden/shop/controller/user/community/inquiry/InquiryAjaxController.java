package archivegarden.shop.controller.user.community.inquiry;

import archivegarden.shop.dto.ResultResponse;
import archivegarden.shop.dto.user.community.inquiry.AddInquiryForm;
import archivegarden.shop.dto.user.community.inquiry.EditInquiryForm;
import archivegarden.shop.dto.user.community.inquiry.ProductPageInquiryListDto;
import archivegarden.shop.entity.Member;
import archivegarden.shop.service.user.community.InquiryService;
import archivegarden.shop.web.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/ajax")
@RequiredArgsConstructor
public class InquiryAjaxController {

    private final InquiryService inquiryService;

    /**
     * 상품 문의 등록 요청을 처리하는 메서드
     */
    @PostMapping("/inquiries/add")
    public ResultResponse addInquiry(@Valid @ModelAttribute("form") AddInquiryForm form, BindingResult bindingResult,
                                     @CurrentUser Member loginMember, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            new ResultResponse(HttpStatus.BAD_REQUEST.value(), "상품 문의 등록 중 오류가 발생했습니다.");
        }

        Long inquiryId = inquiryService.saveInquiry(form, loginMember);
        redirectAttributes.addAttribute("inquiryId", inquiryId);
        return new ResultResponse(HttpStatus.OK.value(), "상품 문의가 등록되었습니다.");
    }


    /**
     * 상품 문의 목록 조회 요청을 처리하는 메서드
     */
    @GetMapping("/inquiries/{productId}")
    public Page<ProductPageInquiryListDto> inquiries(@PathVariable("productId") Long productId, @RequestParam(name = "page", defaultValue = "1") int page,
                                                     @CurrentUser Member loginMember) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return inquiryService.getInquiresInProduct(productId, pageRequest, loginMember);
    }

    /**
     * 상품 문의 수정 요청을 처리하는 메서드
     */
    @PostMapping("/inquiries/{inquiryId}/edit")
    public ResultResponse editInquiry(@Valid @ModelAttribute("form") EditInquiryForm form, BindingResult bindingResult,
                                      @PathVariable("inquiryId") Long inquiryId, @CurrentUser Member loginMember) {
        if (bindingResult.hasErrors()) {
            new ResultResponse(HttpStatus.BAD_REQUEST.value(), "상품 문의 수정 중 오류가 발생했습니다.");
        }

        inquiryService.editInquiry(form, inquiryId);
        return new ResultResponse(HttpStatus.OK.value(), "상품 문의가 수정되었습니다.");
    }

    /**
     * 상품 문의 삭제 요청을 처리하는 메서드
     */
    @DeleteMapping("/inquiries")
    public ResultResponse deleteInquiry(@RequestParam("inquiryId") Long inquiryId) {
        inquiryService.deleteInquiry(inquiryId);
        return new ResultResponse(HttpStatus.OK.value(), "삭제가 완료되었습니다.");
    }
}
