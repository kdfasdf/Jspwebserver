## load on startup
- 서블릿은 브라우저에서 최초 요청 시 init() 메서드를 실행한 후 메모리에 로드되어 기능을 수행함
- 따라서 최초 요청이 있어야 하며 때에 따라 실행 시간이 길어질 수 있는 단점이 존재
- 이런 단점을 보안하기 위해 생긴 기능이 load-on-startup 기능
#### load on startup의 특징
- 톰캣 컨테이너가 실행되면서 미리 서블릿 실행
- 지정한 국자가 0보다 크면 톰캣 컨테이너가 실행되면서 서블릿이 초기화됨
- 지정한 숫자는 우선순위를 의미하며, 작은 숫자부터 먼저 초기화 됨


## WebServlet의 Url 패턴
mvc 패턴을 적용하고 있는 첫 단계에서 가장 헷갈리는 것은 WebServlet url 패턴처리에 관한 것 이었다. 힌트에서 DispatcherServlet의 WebServlet이 사용하는 urlPattern이 "/"이었는데 이것이 어떻게 "/user/create"를 
처리할 수 있는지 그리고 ResourceFilter의 WebServlet("/*")와 어떤 차이가 있는지였다.<br> 
DispatcherSevlet이 어떻게 정적 리소스를 제외한 모든 요청을 처리할까? <br>
resourceFilter를 통해 정적 리소스가 제외되고 /user/create와 같은 요청이 들어오는데 우선 /user/create에 매핑되는 서블릿이 있는지 확인한다 해당 요청을 처리하는 서블릿이 없으면 "/"을 가진 서블릿을 찾고 존재하면 
해당 서블릿에서 요청을 처리한다
<br>
이런 URL 패턴 처리에는 우선순위가 있으며 다음과 같다
||URL pattern|요청 URL|
|--|--|--|
|1.exact mapping|/myapp/example|localhost/myapp/example|
|2.path mapping|/myapp/*|localhost/myapp/example<br> localhost/myapp/list<br> localhost/myapp/test|
|3.extenion mapping|*.txt|localhost/myapp/text1.txt <br> localhost/myapp/text2.txt|
|4.default mapping|/|locahost/myapp/example<br> localhost/myapp/list<br> localhost/myapp/test<br> localhost/myapp/text1.txt|

앞선 예시를 표를 보고 다시 설명하자면 localhost/form.jsp에 회원 정보를 이용해서 /user/create로 요청이 들어갔다고 치자 <br>
그러면 URL 패턴 우선순위에 따라 1. WebServlet("/user/create")가 등록된 서블릿이 있는지 확인한다 프로젝트에는 오직 DispatcherServlet에만 WebServlet("/")이 등록되어있기 때문에 2,3은 건너뛰고 4.에 해당하는 Dispatcher에서 
가 처리하게 된다.

<br>
이에 따라 필터링이 매칭되는 과정도 다시 볼수 있는데 /css와 같은 정적파일은 1.에 해당하는 것이 없고 WebServlet("/*"), 2.에는 해당하니 ResourceFilter에서 처리된다.
