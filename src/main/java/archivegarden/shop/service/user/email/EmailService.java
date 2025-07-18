package archivegarden.shop.service.user.email;

import archivegarden.shop.entity.Member;
import archivegarden.shop.exception.ajax.EntityNotFoundAjaxException;
import archivegarden.shop.exception.global.EntityNotFoundException;
import archivegarden.shop.repository.member.MemberRepository;
import archivegarden.shop.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PasswordEncoder passwordEncoder;

    @Value(("${app.base-url}"))
    private String baseUrl;

    @Value("${app.email.verification-path}")
    private String emailVerificationPath;

    @Value("${app.login-path}")
    private String loginPath;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 회원가입 완료 후 이메일 인증 링크 전송
     */
    public void sendValidationRequestEmail(String to, String name, LocalDateTime created) {
        String uuid = UUID.randomUUID().toString();
        String verificationUrl = baseUrl + emailVerificationPath + "?address=" + to + "&uuid=" + uuid;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("created", DateTimeFormatter.ofPattern("yyyy년 M월 d일").format(created));
        context.setVariable("verificationUrl", verificationUrl);

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String content = templateEngine.process("email/template/join_complete", context);

            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject("[미음키읔] 회원가입을 축하드립니다.");
            helper.setText(content, true);
        };

        redisUtil.setDataExpire(to, uuid, 3 * 60L);

        javaMailSender.send(preparator);
    }

    /**
     * 마이페이지에서 이메일 인증 링크 전송
     */
    public void sendValidationRequestEmailInMyPage(String to, String name) {
        String uuid = UUID.randomUUID().toString();
        String verificationUrl = baseUrl + emailVerificationPath + "?address=" + to + "&uuid=" + uuid;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("verificationUrl", verificationUrl);

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String content = templateEngine.process("email/template/validate_email", context);

            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject("[미음키읔] 이메일 인증 요청");
            helper.setText(content, true);
        };

        redisUtil.setDataExpire(to, uuid, 3 * 60L);

        javaMailSender.send(preparator);
    }

    /**
     * 이메일 인증
     *
     * @throws EntityNotFoundException
     */
    public String verifyEmail(String email, String uuid) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        if (redisUtil.existData(email)) {
            if (member.isEmailVerified()) {    //이미 인증 완료
                return "email/verification_complete";
            } else if (redisUtil.getData(email).equals(uuid)) {    //인증 성공
                updateEmailVerification(email, true);
                return "email/verification_success";
            } else {    //uuid 일치X
                return "email/verification_fail";
            }
        } else {    //인증 유효 시간 만료
            return "email/verification_timeout";
        }
    }

    /**
     * 임시 비밀번호 발급
     *
     * @throws EntityNotFoundAjaxException
     */
    public Long sendTempPassword(String to) {
        Member member = memberRepository.findByEmail(to).orElseThrow(() -> new EntityNotFoundAjaxException("존재하지 않는 회원입니다."));
        String tempPassword = getTempPassword();

        Context context = new Context();
        context.setVariable("name", member.getName());
        context.setVariable("tempPassword", tempPassword);
        context.setVariable("loginUrl", baseUrl + loginPath);

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String content = templateEngine.process("email/template/temp_password", context);

            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject("[미음키읔] 임시 비밀번호가 발급되었습니다.");
            helper.setText(content, true);
        };

        javaMailSender.send(preparator);

        String encodedPassword = passwordEncoder.encode(tempPassword);
        member.updatePassword(encodedPassword);

        return member.getId();
    }

    /**
     * 이메일 인증 완료 상태 변경
     */
    private void updateEmailVerification(String email, boolean isEmailVerified) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        member.updateEmailVerificationStatus(isEmailVerified);
    }

    /**
     * 임시 비밀번호 생성
     */
    private String getTempPassword() {
        char[] charSet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String password = "";
        int idx = 0;
        for (int i = 0; i < 10; i++) {
            idx = (int) (charSet.length * Math.random());
            password += charSet[idx];
        }
        return password;
    }
}
