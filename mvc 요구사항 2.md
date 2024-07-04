이제요구사항에 맞추어 mvc 패턴을 적용해보자
## 모든 요청을 서블릿 하나(예를 들어 DisPatcherServlet)가 받을 수 있도록 URL을 매핑한다
이전에 쓴 mvc 패턴1.md 에서 알아봤던 ResourceFilter와 WebServlet url Pattern 매칭 원리에서 알아보았다 정적 리소스에 대한 처리는 ResourceFilter의 @WebServlet("/*")을 통해
한번 걸러지게 되고 url pattern이 정확이 일치하는 요청은 해당 서블릿에 의해 처리되고 그렇지 않은 요청이라 할지라도 마지막 우선순위인 default matching에 의해 WebServlet("/")에 의해 처리될 수 있음을 알게되었다.
따라서 힌트에 있던 아래 방식의 url 매핑이 모든 요청을 받을 수 있다는 것을 알게되었다
```
WebServlet(name="dispatcher",urlPatterns="/",loadOnStartUp=1) 
```
DispatcherServlet은 이 프로젝트에서 뿐만 아니라 일반적으로 스프링에서 모든 요청을 받아 적합한 컨트롤러에 위임해주는 역할을 한다 이에 따라 구현해줄 Dispatcher에서도 request에서 url을 추출하여 
해당 url에 적합한 controller를 위임하고 controller 처리 결과에 따라 페이지 이동을 하는 부분을 구현해주자

- DispatcherServlet.java
```
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
        try {
            String re=reqController.execute(request, response);
            if(re.startsWith("redirect:"))
                response.sendRedirect(re);
            else
            {
                RequestDispatcher rd = request.getRequestDispatcher(re);
                rd.forward(request,response);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
```
## Controller 인터페이스를 추가한다, 서블릿으로 구현되어 있는 기능을 Controller 인터페이스 기반으로 다시 구현한다 execute() 메서드의 반환 값은 리다이렉트 방식으로 이동할 경우 redirect:로 시작하고 포워드 방식으로 이동할 경우 JSP 경로를 반환한다.
RequestMapping 클래스를 추가해 요청 URL과 컨트롤러 매핑을 설정한다
```
public interface Controller{
  String execute(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
```
해당 부분은 이미 핵심 로직들이 구현되어 있어 어렵지 않았다. 기존 서블릿에서는 페이지 이동이 필요한 서블릿 마다 sendRedirect나 RequestDispatcher의 forward 메서드를 이용해왔다. 
mvc model2 에서는 이동할 페이지의 이름을 스트링으로 반환해서 dispatcherservlet에서 리다이렉트나 포워딩을 한다
```
public String execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("userId");
        String password = req.getParameter("password");
        User user = DataBase.findUserById(userId);
        if(user==null)
        {
            req.setAttribute("loginFailed",true);
            return "/user/login.jsp";
        }
        else{
            if(user.matchPassword(password)){
                HttpSession session = req.getSession();
                session.setAttribute(UserSessionUtils.USER_SESSION_KEY,user);
                return "redirect:/";
            }
            else{
                req.setAttribute("loginFailed",true);
                return "/user/login.jsp";
            }
        }
    }
```

## 컨트롤러를 추가하다보니 회원가입 화면(/user/form.jsp), 로그인 화면(/user/login.jsp)와 같이 특별한 로직을 구현할 필요가 없는 경우에도 매번 컨트롤러를 생성하는 것은 불합리하다는 생각이 든다. 이와 같이 특별한 로직 없이 뷰(jsp)에 대한 이동만을 담당하는 ForwardController를 추가한다
DispatcherServlet에서 요청 URL에 해당하는 Controller를 찾아 execute()메서드를 호출해 실질적인 작업을 위임한다.

이 요구사항을 수행하기 위해서는 기존의 jsp를 수정할 필요가 있었다. 우선 로그인 페이지나 회원가입 페이지는 네비게이션 바에서 로그인, 회원 버튼을 통해 페이지 이동을 했는데 네비게이션바에 링크를 아래처럼 걸어놓아 클라이언트의
요청이 jsp에 들어가기 떄문에 mvc model2에 어긋난다
```
                        <li><a href="/user/login.jsp" role="button">로그인</a></li>
                        <li><a href="/user/form.jsp" role="button">회원가입</a></li>
```
따라서 로그인과 회원가입 페이지에 대한 요청도 컨트롤러가 받을 수 있도록 추가해주자 일단 기존 jsp 에 대한 요청을 수정해준다
```
                        <li><a href="/user/loginForm role="button">로그인</a></li>
                        <li><a href="/user/createForm" role="button">회원가입</a></li>
```
새롭게 추가된 요청들은 뷰에 대한 이동만 처리하면 되는데 이를 위한 FrontController를 구현해준다
```
public class ForwardController implements Controller {
    private static String fo;
    ForwardController(String forward){
        this.fo=forward;
    }
    @Override
    public String execute(HttpServletRequest requset, HttpServletResponse response) throws Exception
    {
        return fo;
    }
}
```
로그인 페이지, 회원가입 페이지에 따라 포워딩해야하는 페이지가 다르므로 다른 컨트롤러와 달리 생성자의 파라미터로 포워드할 주소를 받는다
- RequestMapping
```
public class RequestMapping extends HttpServlet {
    private static final long serialVersion=1L;
    private final Logger logger = LoggerFactory.getLogger(RequestMapping.class);
    private static HashMap<String,Controller> controllers = new HashMap<>();
    void initMapping(){
        controllers.put("/user/create",new CreateUserController());
        controllers.put("/user/list",new ListUserController());
        controllers.put("/user/login",new LoginUserController());
        controllers.put("/user/logout",new LogoutUserController());
        controllers.put("/user/updateForm",new UpdateUserFormController());
        controllers.put("/user/loginForm",new ForwardController("/user/login.jsp"));
        controllers.put("/user/createForm",new ForwardController("/user/form.jsp"));
    }
    Controller getController(String controller)
    {
        return controllers.get(controller);
    }
}
```
- Controller의 execute() 메서드 반환 값 String을 받아 서블릿에서 JSP로 이동할 때의 중복을 제거한다.
execute를 통해 RequestDispatcher를 통한 중복 부분이 상당 수 제거되었다

## 이슈
로그인을 눌러도 회원 가입 페이지로 이동하는 문제(해결) <br>
-> ForwardController의 fo 변수를 정적 변수로 선언하여 발생했던 문제 (해시맵에 컨트롤러를 저장할 때 FowardController의 fo 변수가 /user/form.jsp으로 고정되었음)<br>

로그인 회원가입 처리 오류 (해당 컨트롤러가 HttpMethod POST, GET 처리 오류 발생)(해결)<br> 
-> 매핑 충돌문제로 Controller에 WebServlet어노테이션을 지우지 않아 생겼던 문제

## 결과 정리
- 모든 페이지에 대해서 작업을 끝낸 것은 아니지만 mvc 패턴을 적용함으로써 view단과 controller를 분리하였다(login.jsp,form.jsp에 대한 은닉) 
- 다만 아직 ResourceFilter에서 filterConfig.getServletContext().getNamedDispatcher("default"); 부분을 디버깅 까지 했는데도 이해를 못했는데 default 서블릿이 어디있는지 못찾았다
