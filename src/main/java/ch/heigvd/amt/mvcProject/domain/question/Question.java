package ch.heigvd.amt.mvcProject.domain.question;

import ch.heigvd.amt.mvcProject.domain.IEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Question implements IEntity {

    @Setter(AccessLevel.NONE)
    private QuestionId id;

    private String title;

    private String description;

    private List<String> tags;

    private int ranking;

    @Override
    public IEntity deepClone() {
        return this.toBuilder()
                .id(new QuestionId(id.asString()))
                .title(title)
                .description(description)
                .tags(tags)
                .ranking(ranking)
                .build();
    }

    /**
     * Override the builder generated by Lombok
     */
    public static class QuestionBuilder {

        public Question build() {
            if (id == null) {
                id = new QuestionId();
            }

            if (title == null) {
                title = "Untitled";
            }


            return new Question(id, title, description, tags, ranking);
        }
    }

}
