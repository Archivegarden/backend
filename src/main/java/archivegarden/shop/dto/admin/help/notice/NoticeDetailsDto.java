package archivegarden.shop.dto.admin.help.notice;

import archivegarden.shop.entity.Notice;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeDetailsDto {

    private Long id;
    private String title;
    private String content;
    private String createdAt;

    public NoticeDetailsDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.createdAt = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").format(notice.getCreatedAt());
    }
}
