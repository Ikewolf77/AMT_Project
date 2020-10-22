package ch.heigvd.amt.mvcProject.application.question;

import ch.heigvd.amt.mvcProject.domain.question.QuestionId;
import ch.heigvd.amt.mvcProject.domain.user.UserId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;
import java.util.List;

/**
 * Encapsulate question information
 */
@Builder
@Getter
@EqualsAndHashCode
public class QuestionCommand {

    private String title;

    private String description;

    private List<String> tags;

    private int vote;

    private Date creationDate;

    private UserId userId;

    private String username;
}
