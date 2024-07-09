package next.web;

import core.db.DataBase;
import next.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/user/login")
public class LoginUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(LoginUserServlet.class);
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("userId");
        String password = req.getParameter("password");
        User user = DataBase.findUserById(userId);
        if(user==null)
        {
            req.setAttribute("loginFailed",true);
            forward("/user/login.jsp",req,resp);
            return;
        }
        else{
            if(user.matchPassword(password)){
                HttpSession session = req.getSession();
                session.setAttribute(UserSessionUtils.USER_SESSION_KEY,user);
                resp.sendRedirect("/");
            }
            else{
                req.setAttribute("loginFailed",true);
                forward("/user/login.jsp",req,resp);
            }
        }

    }

    private void forward(String forwardUrl, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher(forwardUrl);
        rd.forward(req,resp);
    }

}
