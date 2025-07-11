package archivegarden.shop.repository.admin;

import archivegarden.shop.config.TestAuditingConfig;
import archivegarden.shop.dto.admin.AdminSearchCondition;
import archivegarden.shop.dto.admin.admin.AdminListDto;
import archivegarden.shop.dto.admin.admin.JoinAdminForm;
import archivegarden.shop.entity.Admin;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AdminAdminRepositoryCustomImpl 단위 테스트")
@DataJpaTest
@ActiveProfiles("test")
@Import(TestAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminAdminRepositoryCustomImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    AdminAdminRepositoryCustomImpl adminRepositoryCustom;

    private Admin authorizedAdmin1;
    private Admin authorizedAdmin2;
    private Admin authorizedAdmin3;
    private Admin unauthorizedAdmin1;
    private Admin unauthorizedAdmin2;

    @BeforeEach
    void setUp() {
        TestAuditingConfig.setTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        //createdAt: 2025년 1월 1일 0시 0분 0초
        JoinAdminForm authAdmin1Form = new JoinAdminForm("auth1", "auth1234!", "auth1234!", "관리자1", "auth1@example.com");
        authorizedAdmin1 = Admin.createAdmin(authAdmin1Form);
        em.persist(authorizedAdmin1);
        authorizedAdmin1.authorize();

        TestAuditingConfig.advanceSeconds(10);

        //createdAt: 2025년 1월 1일 0시 0분 10초
        JoinAdminForm authAdmin2Form = new JoinAdminForm("auth2", "auth1234!", "auth1234!", "관리자2", "auth2@example.com");
        authorizedAdmin2 = Admin.createAdmin(authAdmin2Form);
        em.persist(authorizedAdmin2);
        authorizedAdmin2.authorize();

        TestAuditingConfig.advanceSeconds(10);

        //createdAt: 2025년 1월 1일 0시 0분 20초
        JoinAdminForm unauthAdmin1Form = new JoinAdminForm("unauth1", "auth1234!", "auth1234!", "관리자3", "unauth1@example.com");
        unauthorizedAdmin1 = Admin.createAdmin(unauthAdmin1Form);
        em.persist(unauthorizedAdmin1);

        TestAuditingConfig.advanceMonth(2);

        //createdAt: 2025년 3월 1일 0시 0분 20초
        JoinAdminForm authAdmin3Form = new JoinAdminForm("auth3", "auth1234!", "auth1234!", "관리자4", "auth3@example.com");
        authorizedAdmin3 = Admin.createAdmin(authAdmin3Form);
        em.persist(authorizedAdmin3);
        authorizedAdmin3.authorize();

        TestAuditingConfig.advanceMonth(2);

        //createdAt: 2025년 5월 1일 0시 0분 20초
        JoinAdminForm unauthAdmin2Form = new JoinAdminForm("unauth2", "auth1234!", "auth1234!", "관리자5", "unauth2@example.com");
        unauthorizedAdmin2 = Admin.createAdmin(unauthAdmin2Form);
        em.persist(unauthorizedAdmin2);

        em.flush();
        em.clear();
    }

    @DisplayName("관리자 목록 조회 - 검색 조건 없을 경우 기본 정렬 순서로 반환")
    @Test
    void 관리자조회_검색조건X_기본정렬반환() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        //then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(5);
        assertThat(resultPage.getContent()).hasSize(5);

        List<AdminListDto> content = resultPage.getContent();

        assertThat(content.get(0).getLoginId()).isEqualTo(unauthorizedAdmin1.getLoginId());   //createdAt: 2025년 1월 1일 0시 0분 20초
        assertThat(content.get(1).getLoginId()).isEqualTo(unauthorizedAdmin2.getLoginId());   //createdAt: 2025년 5월 1일 0시 0분 20초
        assertThat(content.get(2).getLoginId()).isEqualTo(authorizedAdmin3.getLoginId());     //createdAt: 2025년 3월 1일 0시 0분 20초
        assertThat(content.get(3).getLoginId()).isEqualTo(authorizedAdmin2.getLoginId());     //createdAt: 2025년 1월 1일 0시 0분 10초
        assertThat(content.get(4).getLoginId()).isEqualTo(authorizedAdmin1.getLoginId());     //createdAt: 2025년 1월 1일 0시 0분 0초
    }

    @DisplayName("관리자 목록 조회 - 로그인 아이디 키워드로 검색 시 해당 목록 반환")
    @Test
    void 관리자조회_로그인아이디검색_결과반환() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        cond.setSearchKey("loginId");
        cond.setKeyword("auth1");   //authorizedAdmin1, unauthorizedAdmin1 포함
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        //then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent()).hasSize(2);
        assertThat(resultPage.getContent().get(0).getLoginId()).isEqualTo(unauthorizedAdmin1.getLoginId());
        assertThat(resultPage.getContent().get(1).getLoginId()).isEqualTo(authorizedAdmin1.getLoginId());
    }

    @DisplayName("관리자 목록 조회 - 이름 키워드로 검색 시 해당 목록 반환")
    @Test
    void 관리자조회_이름검색_결과반환() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        cond.setSearchKey("name");
        cond.setKeyword("자3"); //authorizedAdmin3 포함
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        //then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().get(0).getName()).isEqualTo(unauthorizedAdmin1.getName());
    }

    @DisplayName("관리자 목록 조회 - 이메일 키워드로 검색 시 해당 목록 반환")
    @Test
    void 관리자조회_이메일검색_결과반환() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        cond.setSearchKey("email");
        cond.setKeyword("unauth2@example.com"); //unauthorizedAdmin2 포함
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        //then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().get(0).getEmail()).isEqualTo(unauthorizedAdmin2.getEmail());
    }

    @DisplayName("관리자 목록 조회 - 키워드 검색 시 일치하는 결과 없으면 빈 목록 반환")
    @Test
    void 관리자조회_키워드검색_일치하는결과X() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        cond.setSearchKey("loginId");
        cond.setKeyword("nonexistent");
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        //then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(0);
        assertThat(resultPage.getContent()).isEmpty();
    }

    @DisplayName("관리자 목록 조회 - 생성일 범위(시작일~종료일)로 검색 시 해당 목록 반환")
    @Test
    void 관리자조회_생성일범위검색_결과반환() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        cond.setStartDate(LocalDate.of(2025, 1, 1));
        cond.setEndDate(LocalDate.of(2025, 1, 2)); //authorizedAdmin1, authorizedAdmin2, unauthorizedAdmin1 포함
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        //then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(3);
        assertThat(resultPage.getContent()).hasSize(3);

        assertThat(resultPage.getContent().get(0).getLoginId()).isEqualTo(unauthorizedAdmin1.getLoginId());
        assertThat(resultPage.getContent().get(1).getLoginId()).isEqualTo(authorizedAdmin2.getLoginId());
        assertThat(resultPage.getContent().get(2).getLoginId()).isEqualTo(authorizedAdmin1.getLoginId());
    }

    @DisplayName("관리자 목록 조회 - 생성일 범위로 검색 시 일치하는 결과 없으면 빈 목록 반환")
    @Test
    void 관리자조회_생성일범위검색_일치하는결과X() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        cond.setStartDate(LocalDate.of(2024, 1, 1));
        cond.setEndDate(LocalDate.of(2024, 1, 31));
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        // Then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(0);
        assertThat(resultPage.getContent()).isEmpty();
    }

    @DisplayName("관리자 목록 조회 - 키워드와 생성일 범위 모두 지정 시 해당 목록 반환")
    @Test
    void 관리자조회_키워드_날짜_복합검색() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        cond.setSearchKey("name");
        cond.setKeyword("관리자");    //모든 관리자 포함
        cond.setStartDate(LocalDate.of(2025, 3, 1));
        cond.setEndDate(LocalDate.of(2025, 5, 31));     //authorizedAdmin3, unauthorizedAdmin2 포함
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AdminListDto> resultPage = adminRepositoryCustom.findAdmins(cond, pageable);

        //then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent()).hasSize(2);
        assertThat(resultPage.getContent().get(0).getLoginId()).isEqualTo(unauthorizedAdmin2.getLoginId());
        assertThat(resultPage.getContent().get(1).getLoginId()).isEqualTo(authorizedAdmin3.getLoginId());
    }

    @DisplayName("관리자 목록 조회 - 페이징 조건 적용 시 페이지 단위로 목록 반환")
    @Test
    void 관리자조회_페이징적용_정상반환() {
        //given
        AdminSearchCondition cond = new AdminSearchCondition();
        Pageable pageable1 = PageRequest.of(0, 2); //1페이지: size 2
        Pageable pageable2 = PageRequest.of(1, 2); //2페이지: size 2
        Pageable pageable3 = PageRequest.of(2, 2); //3페이지, size 2 (요소 1개)

        //when
        Page<AdminListDto> resultPage1 = adminRepositoryCustom.findAdmins(cond, pageable1);
        Page<AdminListDto> resultPage2 = adminRepositoryCustom.findAdmins(cond, pageable2);
        Page<AdminListDto> resultPage3 = adminRepositoryCustom.findAdmins(cond, pageable3);

        //then
        assertThat(resultPage1).isNotNull();
        assertThat(resultPage1.getTotalElements()).isEqualTo(5);
        assertThat(resultPage1.getTotalPages()).isEqualTo(3);
        assertThat(resultPage1.getContent()).hasSize(2);
        assertThat(resultPage1.getContent().get(0).getLoginId()).isEqualTo(unauthorizedAdmin1.getLoginId());
        assertThat(resultPage1.getContent().get(1).getLoginId()).isEqualTo(unauthorizedAdmin2.getLoginId());

        assertThat(resultPage2).isNotNull();
        assertThat(resultPage2.getTotalElements()).isEqualTo(5);
        assertThat(resultPage2.getTotalPages()).isEqualTo(3);
        assertThat(resultPage2.getContent()).hasSize(2);
        assertThat(resultPage2.getContent().get(0).getLoginId()).isEqualTo(authorizedAdmin3.getLoginId());
        assertThat(resultPage2.getContent().get(1).getLoginId()).isEqualTo(authorizedAdmin2.getLoginId());

        assertThat(resultPage3).isNotNull();
        assertThat(resultPage3.getTotalElements()).isEqualTo(5);
        assertThat(resultPage3.getTotalPages()).isEqualTo(3);
        assertThat(resultPage3.getContent()).hasSize(1);
        assertThat(resultPage3.getContent().get(0).getLoginId()).isEqualTo(authorizedAdmin1.getLoginId());
    }
}