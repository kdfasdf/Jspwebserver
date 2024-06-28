mvc1.md 에서 mvc 개념에 대해서 알아봤다 <br>
이제 서블릿 서버에 mvc 패턴을 적용해보자
## 요구사항
요구사항은 MVC 패턴을 지원하는 프레임워크를 구현하는 것이다. MVC 패턴을 지원하는 기본적인 구조는 HTTP 웹 서버 리펙토링 단계에서 다양한 분기문을 제거할 때 적용한 방법을 그대로 사용하면 된다.
단 이와 같은 구조로 변경하려면 모든 요청을 RequestHandler가 받아서 요청 URL에 따라 분기 처리했듯이서블릿도 모든 요청을 하나의 서블릿이 받은 후 요청 URL에 따라 분기 처리하는 방식으로 구현하면 된다.
MVC 패턴은 기본적으로 사용자의 최초 진입 지점이 컨트롤러가 된다. 뷰에 직접 접근하는 것을 막고 항상 컨트롤러를 통해 접근하도록 해야한다 따라서 지금까지 회원가입(/user/form.jsp), 로그인(/user/login.jsp)와 같이
JSP로 직접 접근하지 않도록 해야 한다.<br>
MVC 프레임워크를 구현했을 때의 결과가 다음 구조를 가지는 것을 목표로 한다

![image](https://github.com/kdfasdf/Jspwebserver/assets/96770726/90ebf345-f1dc-471c-a585-b12ab5e4c1de)


- 모든 클라이언트 요청은 먼저 DispatcherServlet이 받은 후 요청 URL에 따라 해당 컨트롤러에 작업을 위임하도록 구현
- @WebServlet으로 URL을 매핑할 때 urlPatterns="/"와 같이 설정하면 모든 요청 URL이 DispatcherServlet으로 연결된다.
  - 단 ,CSS,자바스크립트 이미지와 같은 정적자원은 굳이 컨트롤러가 필요 없다 따라서 CSS 자바스크립트, 이미지를 처리하는 서블릿 필터는 저자가 구현해놓았다
    - 서블릿 필터란?


요구사항들은 아래와 같다
- 모든 요청을 서블릿 하나(예를 들어 DisPatcherServlet)가 받을 수 있도록 URL을 매핑한다
- Controller 인터페이스를 추가한다
- 서블릿으로 구현되어 있는 회원관리 기능을 아 단계에서 추가한 Controller 인터페이스 기반으로 다시 구현한다. execute() 메서드의 반환 값은 리다이렉트 방식으로 이동할 경우 redirect:로 시작하고 포워드 방식으로 이동할 경우 JSP 경로를 반환한다.
- RequestMapping 클래스를 추가해 요청 URL과 컨트롤러 매핑을 설정한다
- 컨트롤러를 추가하다보니 회원가입 화면(/user/form.jsp), 로그인 화면(/user/login.jsp)와 같이 특별한 로직을 구현할 필요가 없는 경우에도 매번 컨트롤러를 생성하는 것은 불합리하다는 생각이 든다. 이와 같이 특별한 로직 없이 뷰(jsp)에 대한 이동만을 담당하는 ForwardController를 추가한다
- DispatcherServlet에서 요청 URL에 해당하는 Controller를 찾아 execute()메서드를 호출해 실질적인 작업을 위임한다.
- Controller의 execute() 메서드 반환 값 String을 받아 서블릿에서 JSP로 이동할 때의 중복을 제거한다.
