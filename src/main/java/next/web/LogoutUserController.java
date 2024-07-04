package next.web;

import core.mvc.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


public class LogoutUserController implements Controller {
    private static final long serialVersionUID=1L;
    private static final Logger log = LoggerFactory.getLogger(LogoutUserController.class);
    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        HttpSession session = req.getSession();
        session.removeAttribute("user");
        //session.invalidate();
        return "/";
    }
}
