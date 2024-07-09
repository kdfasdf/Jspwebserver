package next.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/user/logout")
public class LogoutUserServlet extends HttpServlet {
    private static final long serialVersionUID=1L;
    private static final Logger log = LoggerFactory.getLogger(LogoutUserServlet.class);
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        HttpSession session = req.getSession();
        session.removeAttribute("user");
        //session.invalidate();
        resp.sendRedirect("/");
    }
}
