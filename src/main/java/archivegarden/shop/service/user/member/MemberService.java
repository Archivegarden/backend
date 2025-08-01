package archivegarden.shop.service.user.member;

import archivegarden.shop.dto.common.JoinSuccessDto;
import archivegarden.shop.dto.user.member.FindIdResultDto;
import archivegarden.shop.dto.user.member.JoinMemberForm;
import archivegarden.shop.dto.user.member.EditMemberInfoForm;
import archivegarden.shop.dto.user.member.VerificationRequestDto;
import archivegarden.shop.entity.Member;

import java.util.Optional;

public interface MemberService {

    Long join(JoinMemberForm form);

    void checkMemberDuplicate(JoinMemberForm form);

    JoinSuccessDto joinComplete(Long memberId);

    boolean isAvailableLoginId(String loginId);

    boolean isAvailableEmail(String email);

    boolean isAvailablePhonenumber(String phonenumber);

    void sendVerificationNo(String phonenumber);

    boolean validateVerificationNo(VerificationRequestDto requestDto);

    Optional<Long> checkLoginIdExistsByEmail(String name, String email);

    Optional<Long> checkLoginIdExistsByPhonenumber(String name, String phonenumber);

    FindIdResultDto findIdComplete(Long memberId);

    Optional<String> checkPasswordExistsByEmail(String loginId, String name, String email);

    Optional<String> checkPasswordExistsByPhonenumber(String loginId, String name, String phonenumber);

    boolean validateIdentity(Member loginMember, String password);

    EditMemberInfoForm getMemberInfo(Long id);

    boolean isNewPassword(String newPassword, String password);

    void editMemberInfo(Long memberId, EditMemberInfoForm form);
}
