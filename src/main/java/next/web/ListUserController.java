package next.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.db.DataBase;
import core.mvc.Controller;
import next.dao.UserDao;
import next.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListUserController extends HttpServlet implements Controller {
    private static final long serialVersionUID=1L;
    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);
    @Override
    public String execute(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
        if (!UserSessionUtils.isLogined(request.getSession())) {
            return "redirect:/user/login.jsp";
        }
        UserDao userDao = new UserDao();
        try{
            List<User>users=userDao.findAll();
            request.setAttribute("users",users);
        }catch(SQLException e){
            log.error(e.getMessage());
        }

        return "/user/list.jsp";
    }
}
