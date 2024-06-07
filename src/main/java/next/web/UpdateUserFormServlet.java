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
import java.io.IOException;

@WebServlet(value = { "/users/update", "/users/updateForm" })

public class UpdateUserFormServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(UpdateUserFormServlet.class);
    @Override
    protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException{
        String userId=req.getParameter("userId");
        User user = DataBase.findUserById(userId);
        req.setAttribute("user",user);
        RequestDispatcher rd =  req.getRequestDispatcher("/user/updateForm.jsp");
        rd.forward(req,resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String userId=req.getParameter("userId");
        User user = DataBase.findUserById(userId);
        User updateUser = new User(req.getParameter("userId"),req.getParameter("password"),
                req.getParameter("name"),req.getParameter("email"));
        log.debug("Update User : {}",updateUser);
        user.update(updateUser);
        resp.sendRedirect("/");
    }

}
