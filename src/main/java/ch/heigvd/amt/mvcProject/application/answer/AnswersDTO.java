package ch.heigvd.amt.mvcProject.application.answer;


import ch.heigvd.amt.mvcProject.domain.answer.AnswerId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class AnswersDTO {

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class AnswerDTO {
        private AnswerId id;
        private String description;
    }

    @Singular
    private List<AnswerDTO> answers;
}
