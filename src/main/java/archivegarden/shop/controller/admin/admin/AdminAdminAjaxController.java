package archivegarden.shop.controller.admin.admin;

import archivegarden.shop.dto.ResultResponse;
import archivegarden.shop.service.admin.admin.AdminAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ajax/admin")
@RequiredArgsConstructor
public class AdminAdminAjaxController {

    private final AdminAdminService adminService;

    /**
     *  관리자 로그인 아이디 중복 여부를 검사하는 메서드
     */
    @PostMapping("/check/loginId")
    public ResultResponse checkLoginIdDuplicate(@RequestParam(name = "loginId") String loginId) {
        boolean isLoginIdAvailable = adminService.isLoginIdAvailable(loginId);
        if(isLoginIdAvailable) {
            return new ResultResponse(HttpStatus.OK.value(), "사용 가능한 아이디입니다.");
        } else {
            return new ResultResponse(HttpStatus.BAD_REQUEST.value(), "이미 사용 중인 아이디입니다.");
        }
    }

    /**
     *  관리자 이메일 중복 여부를 검사하는 메서드
     */
    @PostMapping("/check/email")
    public ResultResponse checkEmailDuplicate(@RequestParam(name = "email") String email) {
        boolean isEmailAvailable = adminService.isEmailAvailable(email);
        if(isEmailAvailable) {
            return new ResultResponse(HttpStatus.OK.value(), "사용 가능한 이메일입니다.");
        } else {
            return new ResultResponse(HttpStatus.BAD_REQUEST.value(), "이미 사용 중인 이메일입니다.");
        }
    }

    /**
     * 관리자 삭제 요청을 처리하는 메서드
     */
    @DeleteMapping("/admin")
    public ResultResponse deleteAdmin(@RequestParam("adminId") Long adminId) {
        adminService.deleteAdmin(adminId);
        return new ResultResponse(HttpStatus.OK.value(), "삭제되었습니다.");
    }

    /**
     * 관리자 권한 부여 요청을 처리하는 메서드
     */
    @PostMapping("/admin/auth")
    public ResultResponse authorizeAdmin(@RequestParam("adminId") Long adminId) {
        adminService.authorizeAdmin(adminId);
        return new ResultResponse(HttpStatus.OK.value(), "승인되었습니다.");
    }
}
