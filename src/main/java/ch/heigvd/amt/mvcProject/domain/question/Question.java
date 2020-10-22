package ch.heigvd.amt.mvcProject.domain.question;

import ch.heigvd.amt.mvcProject.domain.IEntity;
import ch.heigvd.amt.mvcProject.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Question implements IEntity<Question, QuestionId> {

    @Setter(AccessLevel.NONE)
    private QuestionId id;

    private String title;

    private String description;

    private int vote;

    private Date creationDate;

    private UserId userId;

    private String username;

    @Override
    public Question deepClone() {
        return this.toBuilder()
                .id(new QuestionId(id.asString()))
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

            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("Title is mandatory");
            }

            if (description == null || description.isEmpty()){
                throw new IllegalArgumentException("Description is mandatory");
            }

            if (creationDate == null ){
                throw new IllegalArgumentException("CreationDate is mandatory");
            }

            if (userId == null) {
                throw new IllegalArgumentException("userId is mandatory");
            }

            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username is mandatory");
            }

            return new Question(id, title, description, vote, creationDate, userId, username);
        }
    }

}
