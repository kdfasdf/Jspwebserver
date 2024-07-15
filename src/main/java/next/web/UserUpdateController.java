package next.web;

import core.mvc.Controller;
import next.dao.UserDao;
import next.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class UserUpdateController implements Controller {
    Logger log = LoggerFactory.getLogger(UserUpdateController.class);
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        User user = new User(request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email"));
        if(user.getEmail()==null || user.getName()==null||user.getPassword()==null)
        {
            return "/user/updateForm.jsp";
        }
        UserDao dao = new UserDao();
        try {
            dao.updateUser(user);
        }catch(SQLException e)
        {
            log.error(e.getMessage());
        }
        return "redirect:/user/list";
    }
}
