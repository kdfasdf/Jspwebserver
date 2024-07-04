package core.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="dispatcher",urlPatterns="/",loadOnStartup=1)
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersion=1L;
    private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
    private RequestMapping rm;
    @Override
    public void init() throws ServletException {
        rm = new RequestMapping();
        rm.initMapping();
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        Controller reqController = rm.getController(uri);
        System.out.println(uri);
        logger.debug("Method: {}, uri: {}, controller: {}",request.getMethod(),uri,reqController);
        try {
            String re=reqController.execute(request, response);
            if(re.startsWith("redirect:")) {
                logger.debug("redirect");
                response.sendRedirect(re.substring("redirect:".length()));
            }
            else
            {
                RequestDispatcher rd = request.getRequestDispatcher(re);
                logger.debug("forward");
                rd.forward(request,response);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }
}
