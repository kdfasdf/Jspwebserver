package next.web;

import core.db.DataBase;
import core.mvc.Controller;
import next.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class UpdateUserFormController implements Controller {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(UpdateUserFormController.class);
    @Override
    public String execute(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException{
        String userId=req.getParameter("userId");
        User user = DataBase.findUserById(userId);
        if(!UserSessionUtils.isSameUser(req.getSession(),user))
            throw new IllegalStateException("다른 사람의 계정은 수정할 수 없습니다");
        req.setAttribute("user",user);
        return "/user/updateForm.jsp";
    }

    /*
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String userId=req.getParameter("userId");
        User user = DataBase.findUserById(userId);
        User updateUser = new User(req.getParameter("userId"),req.getParameter("password"),
                req.getParameter("name"),req.getParameter("email"));
        log.debug("Update User : {}",updateUser);
        user.update(updateUser);
        resp.sendRedirect("/");
    }*/

}
