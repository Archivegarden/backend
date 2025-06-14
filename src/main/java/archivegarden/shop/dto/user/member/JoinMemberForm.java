package archivegarden.shop.dto.user.member;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinMemberForm {

    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)[a-z\\d]{5,20}+$", message = "5~20자의 영문 소문자, 숫자 조합을 사용해 주세요.")
    private String loginId;

    @Pattern(regexp = "^(?=.*[a-zA-z])(?=.*\\d)(?=.*\\W)[a-zA-Z\\d\\W]{8,16}+$", message = "8~16자의 영문 대/소문자, 숫자, 특수문자 조합을 사용해 주세요.")
    private String password;

    private String passwordConfirm;

    @Pattern(regexp = "^[a-zA-z가-힣]{2,30}$", message = "2~30자의 한글, 영문 대/소문자를 사용해 주세요. (특수기호, 공백 사용 불가)")
    private String name;

    private String zipCode;
    private String basicAddress;
    private String detailAddress;
    private String phonenumber1;
    private String phonenumber2;
    private String phonenumber3;

    @Pattern(regexp = "^[a-zA-Z\\d]([-_.]?[a-zA-Z\\d])*@[a-zA-Z\\d]*\\.[a-zA-Z]{2,3}$", message = "유효한 이메일을 입력해 주세요.")
    private String email;

    @AssertTrue(message = "이용약관에 동의해 주세요.")
    private boolean agreeToTermsOfUse;

    @AssertTrue(message = "개인정보 수집 및 이용 방침에 동의해 주세요.")
    private boolean agreeToPersonalInformation;

    private boolean agreeToReceiveSms;
    private boolean agreeToReceiveEmail;

    public String getFormattedPhonenumber() {
        return this.getPhonenumber1() + this.getPhonenumber2() + this.getPhonenumber3();
    }
}
