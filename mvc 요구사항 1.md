mvc 패턴을 적용하면서 처음으로 해야하는 일이 클라이언트의 모든 요청을 하나의 서블릿으로 받을 수 있게 바꾸는 것이다.<br>
그런데 Http 웹서버를 구현해보면서 경험해 보았듯이(그리고 상위 1%를 위한 네트워크에서 공부한 내용으로) /index.jsp를 요청했을 때 index.jsp 페이지에 대한 응답이 오긴하지만 
index.jsp 페이지에 html에 이미지 태그나 있거나 css 등의 내용이 있으면 해당 내용을 다시 요청하게 된다<br>

- index.html을 요청했을 때 찍혔던 요청메시지 로그
```
15:29:39.258 [DEBUG] [Thread-0] [util.HttpRequest] - request line : GET /index.html HTTP/1.1
15:29:39.319 [DEBUG] [Thread-1] [util.HttpRequest] - request line : GET /css/bootstrap.min.css HTTP/1.1
15:29:39.330 [DEBUG] [Thread-3] [util.HttpRequest] - request line : GET /js/jquery-2.2.0.min.js HTTP/1.1
15:29:39.330 [DEBUG] [Thread-4] [util.HttpRequest] - request line : GET /js/bootstrap.min.js HTTP/1.1
```

힌트에서는 하나의 서블릿에 모든 요청을 받기 위해 
```
@WebServlet(name= "dispatcher", urlPattern ="/", loadOnStartUp=1)
```
을 사용하도록 안내하고 있다. 그런데 위의 로그에서 보다시피 jsp 페이지에 css, 부트스트랩 등이 포함되어 있다면 해당 파일도 /css/... 형식으로
요청이 들어오기 때문에 이 요청도 모든 요청을 받는 서블릿이 받게 된다 해당 파일은 src가 아닌 webapp에서 관리하기 때문에 서블릿에서 응답하면 
제대로된 응답을 할 수 없게 된다 따라서 서블릿으로 들어오는 요청에 대해서 필터링을 처리를 해줘야 한다 필터 구현은 이미 되어있으므로 분석만 해보았다

- 클라이언트에서 컨트롤러까지의 흐름
```
HTTP 요청 -> WAS -> 필터 -> Dispatcher Servlet -> Controller
/*
* 필터의 역할을 다음과 같다
* 서블릿이 호출되기 전에 서블릿 요청을 가로채는 기능
* 서블릿이 호출되기 전에 요청 내용을 점검하는 기능
* 요청 헤더의 수정과 조정 기능
* 서블릿이 호출된 후에 서블릿 응답을 가로채는 기능
* 응답 헤더의 수정과 조정
*/
```
필터는 여러가지를 추가할 수 있다<br>
본론으로 돌아와 /으로 들어오는 css,js(부트스트랩 파일),fonts 등의 요청을 필터링하기 위한 코드는 아래와 같다
- ResourceFilter
```

@WebServlet("/*")
public class ResourceFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ResourceFilter.class);
    private static final List<String> resourcePrefixs = new ArrayList<>();
    static{
        resourcePrefixs.add("/css");
        resourcePrefixs.add("/js");
        resourcePrefixs.add("/fonts");
        resourcePrefixs.add("/images");
        resourcePrefixs.add("/favicon.ico");
    }
    private RequestDispatcher defaultRequestDispatcher;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException{
        this.defaultRequestDispatcher = filterConfig.getServletContext().getNamedDispatcher("default");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,ServletException{
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if(isResourceUrl(path)){
            logger.debug("path: {}",path);
            defaultRequestDispatcher.forward(request,response);
        }
    }
    private boolean isResourceUrl(String url){
        for(String prefix: resourcePrefixs)
        {
            if(url.startsWith(prefix))
                return true;
        }
        return false;
    }
    @Override
    public void destroy(){

    }
}
```
그러면 localhost:8080/login.jsp 으로 요청한 후 jsp 안에 정적 리소스가 존재하여 
우선 ResourceFilter의 WebServlet("/*)이 DispatcherServlet으로 들어가는 요청을 가로챈다
그리고 아래와 같이 정적리소스에 대한 요청이 어떻게 처리되는지 알아보자
```
15:29:39.319 [DEBUG] [Thread-1] [util.HttpRequest] - request line : GET /css/bootstrap.min.css HTTP/1.1
```
```
request: localhost:8080/css/bootstrap.min.css
req.getRequestURI().substring(req.getContextPath().length()); 
-> "/css/bootstrap.min.css".substring("".length());
-> "/css/bootstrap.min.css".substring("".length());
```
getContextPath()는 root ContextPath()에 대해서는 빈문자열("")을 반환한다.
path에는 "/css/bootstrap.min.css"이 저장되고 리스트에 저장해둔 문자열 중 하나로 시작한다면 defaultDispatcher에 의해 정적리소스를 처리한다

## 후기
중간에 contextPath에 대해 잘못 이해해서 이해하는데 오래걸렸다 다만 아직 this.defaultRequestDispatcher = filterConfig.getServletContext().getNamedDispatcher("default"); 인 부분에서 "default"에 해당하는 dispatcher를 찾기 못했는데
mvc 패턴 적용이 끝나면 디버그 찍어봐야할 것 같다
