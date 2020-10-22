package ch.heigvd.amt.mvcProject.infrastructure.persistence.jdbc;

import ch.heigvd.amt.mvcProject.domain.question.IQuestionRepository;
import ch.heigvd.amt.mvcProject.domain.question.Question;
import ch.heigvd.amt.mvcProject.domain.question.QuestionId;
import ch.heigvd.amt.mvcProject.domain.user.User;
import ch.heigvd.amt.mvcProject.domain.user.UserId;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

@ApplicationScoped
@Named("JdbcQuestionRepository")
public class JdbcQuestionRepository implements IQuestionRepository {

    @Resource(lookup = "jdbc/help2000DS")
    DataSource dataSource;

    public JdbcQuestionRepository() {
    }

    public JdbcQuestionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Question question) {

        // TODO : gérer l'ajout des tags

        try{
            PreparedStatement statement = dataSource.getConnection().prepareStatement(
                    "INSERT INTO tblQuestion(id, title, description, creationDate, tblUser_id) VALUES(?,?,?,?,?)"
            );

            java.sql.Date creationDate = new java.sql.Date(question.getCreationDate().getTime());

            statement.setString(1, question.getId().asString());
            statement.setString(2, question.getTitle());
            statement.setString(3, question.getDescription());
            statement.setDate(4, creationDate);
            statement.setString(5, question.getAuthorId().asString());

            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void remove(QuestionId id) {
        try{
            PreparedStatement statement = dataSource.getConnection().prepareStatement(
                    "DELETE FROM tblQuestion WHERE id = ?"
            );

            statement.setString(1, id.asString());
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public Optional<Question> findById(QuestionId id) {

        // TODO : gérer les tags toujours

        Optional<Question> optionalQuestion = Optional.empty();

        try {
            PreparedStatement statement = dataSource.getConnection().prepareStatement(
                    "SELECT * FROM tblQuestion WHERE id = ?",
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );

            statement.setString(1, id.asString());

            ResultSet rs = statement.executeQuery();

            if(rs.next()){

                rs.first();

                DateFormat df = DateFormat.getInstance();
                df.setTimeZone(TimeZone.getTimeZone("UTC"));

                Question foundQuestion = Question.builder()
                        .id(new QuestionId(rs.getString("id")))
                        .description(rs.getString("description"))
                        .title(rs.getString("title"))
                        .creationDate(new Date(rs.getDate("creationDate").getTime()))
                        .authorId(new UserId(rs.getString("tblUser_id")))
                        .build();

                optionalQuestion = Optional.of(foundQuestion);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return optionalQuestion;
    }

    @Override
    public Collection<Question> findAll() {
        Collection<Question> questions = new ArrayList<>();

        try {
            PreparedStatement statement = dataSource.getConnection().prepareStatement(
                    "SELECT * FROM tblQuestion",
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );

            questions = getQuestions(statement.executeQuery());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return questions;
    }

    @Override
    public void upvote(UserId userId, QuestionId questionId) {
        addVote(userId, questionId, 1);
    }

    @Override
    public void downvote(UserId userId, QuestionId questionId) {
        addVote(userId, questionId, 0);
    }

    @Override
    public int getVotes(QuestionId questionId) {

        int totalVotes = 0;

        try {
            // First we get the total number of votes
            PreparedStatement voteStatement = dataSource.getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM tblUser_vote_tblQuestion" +
                            "WHERE tblQuestion_id = ?",
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            voteStatement.setString(1, questionId.asString());

            ResultSet rs = voteStatement.executeQuery();

            while (rs.next()) {
                totalVotes = rs.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return totalVotes;
    }

    public void addVote(UserId userId, QuestionId questionId, int positive) {
        try {
            PreparedStatement statement = dataSource.getConnection().prepareStatement(
                    "INSERT INTO tblUser_vote_tblQuestion (tblUser_id, tblQuestion_id, positiv)" +
                            "VALUES (?, ?, ?)",
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );

            statement.setString(1, userId.asString());
            statement.setString(2, questionId.asString());
            statement.setInt(3, positive);

            statement.execute();

            // Update the votes
            //updateQuestionVotes(questionId);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Get all questions corresponding to the given result set
     * @param rs : result set
     * @return list of questions
     * @throws SQLException
     */
    private ArrayList<Question> getQuestions(ResultSet rs) throws SQLException {
        ArrayList<Question> questions = new ArrayList<>();

        while(rs.next()) {

            Question foundQuestion = Question.builder()
                    .id(new QuestionId(rs.getString("id")))
                    .creationDate(new Date(rs.getDate("creationDate").getTime()))
                    .authorId(new UserId(rs.getString("tblUser_id")))
                    .description(rs.getString("description"))
                    .title(rs.getString("title"))
                    .build();

            questions.add(foundQuestion);
        }

        rs.close();

        return questions;
    }

}
