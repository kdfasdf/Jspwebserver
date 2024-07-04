package core.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForwardController implements Controller {
    private String fo;
    private static Logger log = LoggerFactory.getLogger(ForwardController.class);
    ForwardController(String forward){
        this.fo=forward;
    }
    @Override
    public String execute(HttpServletRequest requset, HttpServletResponse response) throws Exception
    {
       // log.debug("fc: {}",fo);
        return fo;
    }
}