package ch.heigvd.amt.mvcProject.application.question;


import ch.heigvd.amt.mvcProject.application.ServiceRegistry;
import ch.heigvd.amt.mvcProject.application.authentication.AuthenticationFacade;
import ch.heigvd.amt.mvcProject.application.authentication.register.RegisterCommand;
import ch.heigvd.amt.mvcProject.application.authentication.register.RegistrationFailedException;
import ch.heigvd.amt.mvcProject.domain.question.QuestionId;
import ch.heigvd.amt.mvcProject.domain.user.User;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.inject.Inject;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
// TODO remove each insertion in DB => FixMethodOrder can't be removed
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QuestionFacadeTestIT {

    private final static String WARNAME = "arquillian-managed.war";

    private User user;

    private AuthenticationFacade authenticationFacade;
    private QuestionFacade questionFacade;


    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, WARNAME)
                .addPackages(true, "ch.heigvd.amt");
        return archive;
    }


    @Inject
    ServiceRegistry serviceRegistry;


    @Before
    public void init() throws RegistrationFailedException {
        authenticationFacade = serviceRegistry.getAuthenticationFacade();

        questionFacade = serviceRegistry.getQuestionFacade();


        user = User.builder()
                .email((Math.random() * 100) + "@heig")
                .clearTextPassword("1234")
                .username(String.valueOf(Math.random() * 100))
                .build();

        RegisterCommand registerCommand = RegisterCommand.builder()
                .email(user.getEmail())
                .confirmationClearTxtPassword("1234")
                .clearTxtPassword("1234")
                .username(user.getUsername())
                .build();

        authenticationFacade.register(registerCommand);

    }

    @After
    public void cleanUp() {
        // TODO delete user after test
    }


    @Test
    public void GetQuestionWhenEmptyReturnEmptyList() {

        assertEquals(0, questionFacade.getQuestions(null).getQuestions().size());
    }

    @Test
    public void addQuestionShouldWork() throws QuestionFailedException {

        QuestionCommand command = QuestionCommand.builder()
                .title("Titre")
                .description("Description")
                .creationDate(new Date())
                .userId(user.getId())
                .build();

        QuestionsDTO.QuestionDTO question = questionFacade.addQuestion(command);

        QuestionsDTO view = questionFacade.getQuestions(null);
        assertNotNull(view);
        assertEquals(1, view.getQuestions().size());
        assertEquals(command.getTitle(), view.getQuestions().get(0).getTitle());

        questionFacade.delete(question.getId());
    }


    @Test
    public void C_getQuestionByIdShouldWork() throws QuestionFailedException {

        QuestionCommand command = QuestionCommand.builder()
                .title("Titre")
                .description("Description")
                .creationDate(new Date())
                .userId(user.getId())
                .build();

        questionFacade.addQuestion(command);

        QuestionsDTO view = questionFacade.getQuestions(null);
        QuestionId id = view.getQuestions().get(0).getId();
        QuestionQuery query = QuestionQuery.builder()
                .questionId(id)
                .build();

        QuestionsDTO.QuestionDTO viewID = questionFacade.getQuestionById(query);
        assertEquals(id, viewID.getId());
    }

}
