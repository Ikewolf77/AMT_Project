package ch.heigvd.amt.mvcProject.ui.web.question.handler;

import ch.heigvd.amt.mvcProject.application.ServiceRegistry;
import ch.heigvd.amt.mvcProject.application.authentication.CurrentUserDTO;
import ch.heigvd.amt.mvcProject.application.question.QuestionFacade;
import ch.heigvd.amt.mvcProject.application.question.QuestionFailedException;
import ch.heigvd.amt.mvcProject.application.user.exceptions.UserFailedException;
import ch.heigvd.amt.mvcProject.domain.question.QuestionId;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "QuestionVoteHandler", urlPatterns = "/vote")
public class QuestionVoteHandler extends HttpServlet {

    @Inject
    private ServiceRegistry serviceRegistry;
    private QuestionFacade questionFacade;

    @Override
    public void init() throws ServletException {
        super.init();
        questionFacade = serviceRegistry.getQuestionFacade();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CurrentUserDTO currentUser = (CurrentUserDTO) req.getSession().getAttribute("currentUser");
        QuestionId questionId = new QuestionId(req.getParameter("id"));
        boolean upvote = req.getParameter("vote").equals("upvote");
        boolean downvote = req.getParameter("vote").equals("downvote");

        try {
            if(upvote) {
                questionFacade.upvote(currentUser.getUserId(), questionId);
            } else if (downvote) {
                questionFacade.downvote(currentUser.getUserId(), questionId);
            }
            resp.sendRedirect("/question?id=" + questionId.asString());
        } catch (QuestionFailedException e) {
            e.printStackTrace();
        } catch (UserFailedException e) {
            e.printStackTrace();
        }
    }
}
