package ch.heigvd.amt.mvcProject.application.question;

import ch.heigvd.amt.mvcProject.application.answer.AnswerFacade;
import ch.heigvd.amt.mvcProject.application.answer.AnswerFailedException;
import ch.heigvd.amt.mvcProject.application.answer.AnswerQuery;
import ch.heigvd.amt.mvcProject.application.answer.AnswerFailedException;
import ch.heigvd.amt.mvcProject.application.answer.AnswersDTO;
import ch.heigvd.amt.mvcProject.application.comment.CommentFacade;
import ch.heigvd.amt.mvcProject.application.comment.CommentFailedException;
import ch.heigvd.amt.mvcProject.application.comment.CommentQuery;
import ch.heigvd.amt.mvcProject.application.comment.CommentsDTO;
import ch.heigvd.amt.mvcProject.application.user.UserFacade;
import ch.heigvd.amt.mvcProject.application.user.UserQuery;
import ch.heigvd.amt.mvcProject.application.user.UsersDTO;
import ch.heigvd.amt.mvcProject.application.user.exceptions.UserFailedException;
import ch.heigvd.amt.mvcProject.domain.comment.Comment;
import ch.heigvd.amt.mvcProject.domain.question.IQuestionRepository;
import ch.heigvd.amt.mvcProject.domain.question.Question;
import ch.heigvd.amt.mvcProject.domain.question.QuestionId;
import ch.heigvd.amt.mvcProject.domain.user.UserId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Link the question and the domain, what we offer to the user to interact with the domain
 * In this class we pass a command (to modify data) of a query (to get data)
 */
public class QuestionFacade {

    // Vote values
    private static final int UPVOTE   =  1;
    private static final int NOVOTE   =  0;
    private static final int DOWNVOTE = -1;

    private IQuestionRepository questionRepository;

    private UserFacade userFacade;
    private AnswerFacade answerFacade;
    private CommentFacade commentFacade;

    public QuestionFacade() {
    }

    public QuestionFacade(IQuestionRepository questionRepository, UserFacade userFacade,
                          AnswerFacade answerFacade, CommentFacade commentFacade) {
        this.questionRepository = questionRepository;
        this.userFacade = userFacade;
        this.answerFacade = answerFacade;
        this.commentFacade = commentFacade;
    }

    public void setQuestionRepository(IQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void setUserFacade(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    public void setAnswerFacade(AnswerFacade answerFacade) {
        this.answerFacade = answerFacade;
    }

    public void setCommentFacade(CommentFacade commentFacade) {
        this.commentFacade = commentFacade;
    }

    /**
     * Add a question to the repository
     * @param command : question with only relevant data
     * @return added question
     * @throws UserFailedException
     * @throws QuestionFailedException
     */
    public QuestionsDTO.QuestionDTO addQuestion(QuestionCommand command)
            throws UserFailedException, QuestionFailedException, AnswerFailedException, CommentFailedException {

        checkIfUserExists(command.getUserId());

        // Create submitted question
        Question submittedQuestion = Question.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .userId(command.getUserId())
                .creationDate(command.getCreationDate())
                .build();

        // Save to repository
        questionRepository.save(submittedQuestion);

        // Get author for the questionDTO
        UsersDTO.UserDTO user = userFacade.getUsers(
                UserQuery.builder().userId(command.getUserId()).build()
        ).getUsers().get(0);

        // Get the answers for the questionDTO
        Collection<AnswersDTO.AnswerDTO> answersDTO = getAnswers(submittedQuestion);

        // create questionDTO of the submitted quesiton
        QuestionsDTO.QuestionDTO newQuestion = QuestionsDTO.QuestionDTO.builder()
                .description(submittedQuestion.getDescription())
                .id(submittedQuestion.getId())
                .title(submittedQuestion.getTitle())
                .username(user.getUsername())
                .votes(0)
                .creationDate(submittedQuestion.getCreationDate())
                .userId(submittedQuestion.getUserId())
                .answersDTO(AnswersDTO.builder().answers(answersDTO).build())
                .build();

        return newQuestion;
    }

    /**
     * Upvotes a question, if already upvoted it will remove the upvote
     * @param userId : user that is upvoting
     * @param questionId : question being upvoted
     * @throws QuestionFailedException
     * @throws UserFailedException
     */
    public void upvote(UserId userId, QuestionId questionId) throws QuestionFailedException, UserFailedException {
        checkIfUserExists(userId);
        vote(userId, questionId, UPVOTE);
    }

    /**
     * Downvotes a question, if already downvoted it will remove the downvote
     * @param userId : user that is upvoting
     * @param questionId : question being upvoted
     * @throws QuestionFailedException
     * @throws UserFailedException
     */
    public void downvote(UserId userId, QuestionId questionId) throws QuestionFailedException, UserFailedException {
        checkIfUserExists(userId);
        vote(userId, questionId, DOWNVOTE);
    }


    /**
     * Vote on a question
     * @param userId : id of the user voting
     * @param questionId : id of the question being voted
     * @param vote : value that is being done (upvote / downvote)
     */
    private void vote(UserId userId, QuestionId questionId, int vote) {
        int voteValue = 0;

        // Check if the user already voted
        if (questionRepository.hasAlreadyVoted(userId, questionId)) {
            voteValue = questionRepository.getVoteValue(userId, questionId);
        }

        // Update the vote value
        voteValue = getNewVoteValue(voteValue, vote);

        questionRepository.addVote(userId, questionId, voteValue);
    }

    /**
     * Get the vote value when voting on a quesiton
     * @param startVoteValue : start vote value of the user (if he already voted on the question)
     * @param voteValue : vote value of the current vote
     * @return the new vote value
     */
    private int getNewVoteValue(int startVoteValue, int voteValue) {
        int result = 0;

        switch (startVoteValue) {
            // User voted : we reset the vote
            case UPVOTE:
            case DOWNVOTE:
                result = 0;
                break;

            // User didn't vote : make it a downvote!
            case NOVOTE:
                result = voteValue;
                break;
        }

        return result;
    }

    /**
     * Retrieve all question in the repository
     * @return all questions as DTO
     */
    public QuestionsDTO getQuestions() throws UserFailedException, QuestionFailedException,
            AnswerFailedException, CommentFailedException {
        return getQuestionsDTO(questionRepository.findAll());
    }

    /**
     * Retrieve questions asked by the query
     * @param query Query passed
     * @return return the result asked by the query as DTO
     * @throws QuestionFailedException
     */
    public QuestionsDTO getQuestions(QuestionQuery query) throws QuestionFailedException,
            UserFailedException, AnswerFailedException, CommentFailedException {
        Collection<Question> questionsFound = new ArrayList<>();

        if (query == null)
            throw new QuestionFailedException("Query is null");

        if(query.getQuestionId() == null)
            throw new QuestionFailedException("Query invalid");

        Question question = questionRepository.findById(query.getQuestionId())
                .orElseThrow(() -> new QuestionFailedException("The question hasn't been found"));
        questionsFound.add(question);

        return getQuestionsDTO(questionsFound);
    }

    /**
     * Return a single Question asked by query
     * @param query Query passed
     * @return the single question asked
     * @throws QuestionFailedException
     */
    public QuestionsDTO.QuestionDTO getQuestion(QuestionQuery query)
            throws QuestionFailedException, UserFailedException,
            AnswerFailedException, CommentFailedException {

        if (query == null || query.getQuestionId() == null)
            throw new QuestionFailedException("Query is null or invalid");

        Question question;

        if(query.isWithDetail()) {
            question = questionRepository.findByIdWithAllDetails(query.getQuestionId())
                    .orElseThrow(() -> new QuestionFailedException("The question with details hasn't been found"));
        } else {
            question = questionRepository.findById(query.getQuestionId())
                    .orElseThrow(() -> new QuestionFailedException("The question hasn't been found"));
        }

        return getQuestionDTO(question);
    }

    /**
     * Transform a collection of Question into DTO
     *
     * @param questionsFound collection of questions
     * @return Questions DTO
     */
    private QuestionsDTO getQuestionsDTO(Collection<Question> questionsFound)
            throws UserFailedException, QuestionFailedException, AnswerFailedException, CommentFailedException {
        List<QuestionsDTO.QuestionDTO> QuestionsDTOFound = new ArrayList<>();

        for (Question question : questionsFound) {
            QuestionsDTO.QuestionDTO questionDTO = getQuestion(
                    QuestionQuery.builder()
                    .questionId(question.getId())
                    .build());
            QuestionsDTOFound.add(questionDTO);
        }

        return QuestionsDTO.builder().questions(QuestionsDTOFound).build();
    }

    /**
     * Remove a question
     *
     * @param id the id of the question to be removed
     * @throws QuestionFailedException
     */
    public void removeQuestion(QuestionId id) throws QuestionFailedException {
        questionRepository.findById(id)
                .orElseThrow(() -> new QuestionFailedException("The question hasn't been found"));

        questionRepository.remove(id);
    }

    /**
     * Return the DTO of the question in the parameter
     *
     * @param question question to transform
     * @return the DTO corresponding to the parameter
     */
    private QuestionsDTO.QuestionDTO getQuestionDTO(Question question)
            throws UserFailedException, QuestionFailedException,
            AnswerFailedException, CommentFailedException {
        return QuestionsDTO.QuestionDTO.builder()
                .title(question.getTitle())
                .description(question.getDescription())
                .id(question.getId())
                .votes(questionRepository.getVotes(question.getId()))
                .userId(question.getUserId())
                .username(getAuthor(question).getUsername())
                .creationDate(question.getCreationDate())
                .answersDTO(AnswersDTO.builder().answers(getAnswers(question)).build())
                .commentsDTO(CommentsDTO.builder().comments(getComments(question)).build())
                .build();
    }

    /**
     * Retrieve all answers associate to the question
     * @param question Which question want the ansers
     * @return a collection of answer DTO associate to the question
     */
    private List<AnswersDTO.AnswerDTO> getAnswers(Question question) throws UserFailedException, AnswerFailedException, QuestionFailedException, CommentFailedException {
        return question.getAnswers().stream().map(
                answer -> {
                    Collection<CommentsDTO.CommentDTO> commentsDTO = new ArrayList<>();
                    UsersDTO.UserDTO author = null;
                    try {
                        commentsDTO = commentFacade.getComments(CommentQuery.builder()
                                        .answerId(answer.getId())
                                        .build()).getComments();
                        author = userFacade.getUsers(UserQuery.builder()
                                .userId(answer.getUserId()).build()).getUsers().get(0);

                    } catch (CommentFailedException e) {
                        e.printStackTrace();
                    } catch (AnswerFailedException e) {
                        e.printStackTrace();
                    } catch (QuestionFailedException e) {
                        e.printStackTrace();
                    } catch (UserFailedException e) {
                        e.printStackTrace();
                    }

                    return AnswersDTO.AnswerDTO.builder()
                            .id(answer.getId())
                            .creationDate(answer.getCreationDate())
                            .description(answer.getDescription())
                            .username(author.getUsername())
                            .comments(CommentsDTO.builder().comments(commentsDTO).build())
                            .build();
                }
        ).collect(Collectors.toList());
    }

    /**
     * Retrieve all comments associate to the question
     * @param question Which question want the ansers
     * @return a collection of comments DTO associate to the question
     */
    private List<CommentsDTO.CommentDTO> getComments(Question question) throws UserFailedException, AnswerFailedException, QuestionFailedException, CommentFailedException {
        return commentFacade.getComments(CommentQuery.builder().questionId(question.getId()).build()).getComments();
    }

    /**
     * Get the author of the question
     * @param question : question id
     * @return author of the id (user)
     * @throws UserFailedException
     */
    private UsersDTO.UserDTO getAuthor(Question question) throws UserFailedException {
        return userFacade.getUsers(UserQuery.builder().userId(question.getUserId()).build()).getUsers().get(0);
    }

    /**
     * Checks if the given user id is linked to an actual user
     * @param userId : id of the user we want to search
     * @throws QuestionFailedException
     * @throws UserFailedException
     */
    private void checkIfUserExists(UserId userId) throws QuestionFailedException, UserFailedException {
        UsersDTO existingUser = userFacade.getUsers(UserQuery.builder().userId(userId).build());

        if (existingUser.getUsers().size() == 0)
            throw new QuestionFailedException("The user hasn't been found");
    }
}
