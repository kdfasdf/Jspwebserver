package next.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.db.DataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/user/list")
public class ListUserServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private static final Logger log = LoggerFactory.getLogger(ListUserServlet.class);
    @Override
    protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException{
        req.setAttribute("users",DataBase.findAll());
        RequestDispatcher rd=req.getRequestDispatcher("/user/list.jsp");
        rd.forward(req, resp);
    }
}
