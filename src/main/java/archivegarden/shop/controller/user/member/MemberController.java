package archivegarden.shop.controller.user.member;

import archivegarden.shop.constant.SessionConstants;
import archivegarden.shop.dto.user.member.JoinMemberForm;
import archivegarden.shop.dto.user.member.FindIdResultDto;
import archivegarden.shop.dto.common.JoinSuccessDto;
import archivegarden.shop.exception.common.DuplicateEntityException;
import archivegarden.shop.service.user.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입 폼을 반환하는 메서드
     */
    @GetMapping("/join")
    public String addMemberForm(@ModelAttribute("joinForm") JoinMemberForm form) {
        return "user/member/join";
    }

    /**
     * 회원가입 요청을 처리하는 메서드
     */
    @PostMapping("/join")
    public String join(@Validated @ModelAttribute("joinForm") JoinMemberForm form, BindingResult bindingResult,
                       HttpSession session, RedirectAttributes redirectAttributes) {
        validateJoin(form, bindingResult);
        if (bindingResult.hasErrors()) {
            return "user/member/join";
        }

        try {
            memberService.checkMemberDuplicate(form);
        } catch(DuplicateEntityException e) {
            redirectAttributes.addFlashAttribute("duplicateError", e.getMessage());
            return "redirect:/member/join";
        }

        Long memberId = memberService.join(form);
        session.setAttribute(SessionConstants.JOIN_MEMBER_ID_KEY, memberId);
        return "redirect:/member/join/complete";
    }

    /**
     * 회원가입 완료 후 결과 페이지를 반환하는 메서드
     */
    @GetMapping("/join/complete")
    public String joinComplete(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if(session == null) return "redirect:/member/join";
        Long memberId = (Long) session.getAttribute(SessionConstants.JOIN_MEMBER_ID_KEY);
        if (memberId == null) return "redirect:/member/join";

        session.removeAttribute(SessionConstants.JOIN_MEMBER_ID_KEY);

        JoinSuccessDto joinCompletionInfoDto = memberService.joinComplete(memberId);
        model.addAttribute("memberInfo", joinCompletionInfoDto);
        return "user/member/join_complete";
    }

    /**
     * 아이디 찾기 폼을 반환하는 메서드
     */
    @GetMapping("/find-id")
    public String findId() {
        return "user/member/find_id";
    }

    /**
     * 아이디 찾기 완료 후 결과 페이지를 처리하는 메서드
     */
    @GetMapping("/find-id/complete")
    public String findIdResult(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if(session == null) return "redirect:/member/find-id";
        Long memberId = (Long) session.getAttribute(SessionConstants.FIND_ID_MEMBER_ID_KEY);
        if (memberId == null) return "redirect:/member/find-id";

        session.removeAttribute(SessionConstants.FIND_ID_MEMBER_ID_KEY);

        FindIdResultDto findIdResultDto = memberService.findIdComplete(memberId);
        model.addAttribute("member", findIdResultDto);
        return "user/member/find_id_complete";
    }

    /**
     * 비밀번호 찾기 폼을 반환하는 메서드
     */
    @GetMapping("/find-password")
    public String findPassword() {
        return "user/member/find_password";
    }

    /**
     * 임시 비밀번호가 전송될 이메일 주소를 확인하는 페이지를 반환하는 메서드
     */
    @GetMapping("/find-password/send")
    public String displaySendTempPasswordEmailPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/member/find-password";
        String email = (String) session.getAttribute(SessionConstants.FIND_PASSWORD_EMAIL_KEY);
        if (email == null) return "redirect:/member/find-password";

        model.addAttribute("email", email);
        return "user/member/send_temporary_password";
    }

    /**
     * 비밀번호 찾기 완료 후 결과 페이지를 반환하는 메서드
     */
    @GetMapping("/find-password/complete")
    public String findPasswordResult(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String email = (String) session.getAttribute(SessionConstants.FIND_PASSWORD_EMAIL_KEY);
        if (session == null || email == null) {
            return "redirect:/member/find-password";
        }
        session.removeAttribute(SessionConstants.FIND_PASSWORD_EMAIL_KEY);

        model.addAttribute("email", email);
        return "user/member/find_password_complete";
    }

    /**
     * 회원가입 폼의 복합적인 유효성 검증을 수행하는 메서드
     * - 비밀번호 확인: 비밀번호와 비밀번호 확인이 일치하는지 검사합니다.
     * - 주소: 우편번호와 기본 주소의 형식을 검사합니다.
     * - 휴대전화번호: 휴대전화번호의 형식이 올바른지 검사합니다.
     */
    private void validateJoin(JoinMemberForm form, BindingResult bindingResult) {
        if (StringUtils.hasText(form.getPassword())) {
            if (!form.getPassword().equals(form.getPasswordConfirm())) {
                bindingResult.rejectValue("passwordConfirm", "error.field.passwordConfirm.mismatch");
            }
        }

        if (StringUtils.hasText(form.getZipCode()) && StringUtils.hasText(form.getBasicAddress())) {
            if (!Pattern.matches("^[\\d]{5}$", form.getZipCode()) || !Pattern.matches("^[가-힣\\d\\W]{1,40}$", form.getBasicAddress())) {
                bindingResult.rejectValue("zipCode", "error.field.address.invalidFormat");
            }
        } else {
            bindingResult.rejectValue("zipCode", "error.field.address.required");
        }

        if (StringUtils.hasText(form.getPhonenumber1()) && StringUtils.hasText(form.getPhonenumber2()) && StringUtils.hasText(form.getPhonenumber3())) {
            if (!Pattern.matches("^01(0|1|[6-9])$", form.getPhonenumber1()) || !Pattern.matches("^[\\d]{3,4}$", form.getPhonenumber2()) || !Pattern.matches("^[\\d]{4}$", form.getPhonenumber3())) {
                bindingResult.rejectValue("phonenumber1", "error.field.phonenumber.invalidFormat");
            }
        } else {
            bindingResult.rejectValue("phonenumber1", "error.field.phonenumber.required");
        }
    }
}
