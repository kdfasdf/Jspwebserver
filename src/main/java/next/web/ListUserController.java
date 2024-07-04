package next.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.db.DataBase;
import core.mvc.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListUserController extends HttpServlet implements Controller {
    private static final long serialVersionUID=1L;
    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);
    @Override
    public String execute(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException{
        if (!UserSessionUtils.isLogined(req.getSession())) {
            return "redirect:/user/login.jsp";
        }
        req.setAttribute("users",DataBase.findAll());
        return "/user/list.jsp";
    }
}
