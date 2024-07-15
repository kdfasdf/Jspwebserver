package core.mvc;

import next.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class RequestMapping  {
    private static final long serialVersion=1L;
    private final Logger log = LoggerFactory.getLogger(RequestMapping.class);
    private static HashMap<String,Controller> controllers = new HashMap<>();
    void initMapping(){
        controllers.put("/user/create",new CreateUserController());
        controllers.put("/user/list",new ListUserController());
        controllers.put("/user/login",new LoginUserController());
        controllers.put("/user/logout",new LogoutUserController());
        controllers.put("/user/updateForm",new UpdateUserFormController());
        controllers.put("/user/update",new UserUpdateController());
        controllers.put("/user/loginForm",new ForwardController("/user/login.jsp"));
        controllers.put("/user/createForm",new ForwardController("/user/form.jsp"));
    }
    Controller getController(String controller)
    {
        log.debug("getController: {}",controller);
        return controllers.get(controller);
    }
}
