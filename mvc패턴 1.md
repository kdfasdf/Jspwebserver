## 서론
프레임워크를 사용해 웹 개발을 해봤다면 mvc라는 말을 들어봤을 것 이다. 나 같은 경우에는 처음 mvc 개념을 접했을 때 클라이언트 서버 구조와  mvc 패턴이 거의 같은 개념이라 생각 했었다
하지만 최근 java http 서버에서 리펙토링을 해보면서 어느정도 mvc의 개념에 감을 잡은 상태이고 jsp, 서블릿 프로젝트에도 mvc 패턴을 적용해봐야 하기 때문에 정리해보았다.
<br>

## mvc 패턴의 배경 
과거에 수많은 프로그래머들이 수많은 프로그램을 만들때 코드를 계속 작성해나가면서 수정하면서 코드가 복잡해지고 파악하기 어려워지는 경험이 많았다 그런데 특정 방식으로 개발했을 때 유지보수,
확장이 편해진다는 규칙성이 보이기 시작했고 그 패턴을 공식처럼 만들게 된게 mvc 패턴이다

<br>

## mvc 패턴을 적용한 웹 vs 적용하지 않은 웹
mvc 패턴은 사용자의 요청이 Controller(C)에 진입하여 요청 로직을 처리 후 Model(M) 데이터를 구성한 후 View(V)에 담아 응답하는 패턴이다.
<br>
mvc 패턴의 특징은 클라이언트의 요청이 처음 진입 하는 부분이 Controller라는 것이다. mvc 패턴이 적용되지 않은 웹 같은 경우 jsp가 
클라이언트 요청이 처음으로 진입하는 부분이라 jsp에 너무 많은 책임을 부여하면 코드의 복잡도가 늘어나고 구조를 파악해지기가 힘들어진다. 
아래는 학교다닐 때 데이터베이스 수업에서 웹 프로젝트를 시켰는데 mvc 패턴을 적용하지 않고 작성한 jsp 코드인데 리펙토링했던 Java Http Server 코드와 비교해보자
<br>
아래 같은 경우 열차관리 시스템에서 insertInfo.jsp에서 추가할 정보를 입력하는 form 입력 받은 데이터를 데이터베이스에 추가하는 코드이다

```

<%!// 변수 선언
   Connection conn = null;
   PreparedStatement pstmt = null;
   ResultSet rs = null;
   String id = "root";
   String pd = "root";
   String url = "jdbc:mariadb://localhost:3306/yonsei_rail";
   %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"><link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<title>홈페이지</title>
</head>
<body>
   <%
	  //본부
   	  String hid = request.getParameter("hid");
      String name = request.getParameter("name");		
      String planet = request.getParameter("planet");
      String continent = request.getParameter("continent");
      String budget = request.getParameter("budget");
      //기관사

      String sql = "insert into headquarter (hid,name,planet,continent,budget) values(?,?,?,?,?)";
      
      conn = DriverManager.getConnection(url, id, pd);
      pstmt = conn.prepareStatement(sql);
      if (hid!=""&& name!="" && planet!=""&& continent!=""&& budget!="")
      {
      pstmt.setString(1,hid);
      pstmt.setString(2,name);
      pstmt.setString(3,planet);
      pstmt.setString(4,continent);
      pstmt.setString(5,budget);
      pstmt.executeUpdate();
      pstmt.close();
      }
      conn.close();
      response.sendRedirect("after_login_manager.jsp?uid="+(String)session.getAttribute("uid"));
      %>
```
참고로 많이 생략해서 올린 코드이다. 그런데도 mvc 패턴이 적용된 jsp 코드 보다도 수행해야하는 동작이 훨씬 많다 그리고 controller가 없기 때문에 jsp에서 다른 jsp로 리다이렉트하는 코드가 굉장히 많은데 1~2개도 아니고 페이지들이 
계속 많아지다 보면 코드 파악도 힘들어지고 추가 개발하는 것도 어려워진다 반면 Controller 리펙토링이 된 JavaWebserberdml CreateUserController는 역할과 의미를 파악하기 굉장히 쉽다
```
public class CreateUserController extends AbstractController{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void doPost(HttpRequest request, HttpResponse response){
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email")
        );
        log.debug("user : {}",user);
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
    }
}
```
<br>

MVC 패턴이란?
MVC를 적용하지 않은 코드가 왜 복잡해지는지 코드를 통해 확인했으니  mvc 동작에 대해 좀 더 알아보자<br>
mvc 패턴이란 위에 설명한 대로 클라이언트의 요청이 컨트롤러(Controller)에 진입하여 요청 로직을 처리 후 데이터(Model)을 처리후 뷰(View)에 담아 응답한다. 

![image](https://github.com/kdfasdf/Jspwebserver/assets/96770726/7654579d-6bc3-4765-8e3a-982df5986144)

좀 더 의미가 와닿도록 검색 예시로도 알아보자

![image](https://github.com/kdfasdf/Jspwebserver/assets/96770726/880aa847-d0aa-4a02-8418-0b7520edd3d3)

위 설명과 그림으로 mvc의 3가지 컴포넌트를 정리하면
- Model
  - 데이터와 관련된 부분
- View
  - 사용자한테 보여지는 부분
- Controller
  - Model과 View를 이어주는 부분
으로 정리할 수 있다.

## mvc 예제
mvc 이론은 알고 있지만 코딩하다보면 mvc 패턴을 어기거나 잘못 구현하는 경우가 많다고 한다. mvc 패턴을 지키면서 코딩하는 방법 5가지를 알아보고 따르면서 코딩해보자
1. Model은 Controller와 View에 의존하지 않아야 한다
```
public class Student {
    private String name;
    private int age;
    public Student(String name, int age){
        this.name = name;
        this.age = age;
    }
    public String getName(){
        return name;
    }
    public int getAge(){
        return age;
    }
}
```
Model 내부에 Controller와 View 관련된 코드가 있으면 안된다(import 자체를 하지 말 것) Model은 데이터와 관련된 부분이다 보니 최대한 그 자체로 쓸 수 있게 하기 위해서이다
2. View는 Model에만 의존해야 하고, Controller에는 의존하면 안 된다
```
public class OutputView {
    public static void printProfile(Student student) {
        System.out.println("내 이름은" + student.getName() + "입니다.");
        System.out.println(student.getAge() + "입니다.");
    }
}
```
View 내부에 Model에 관련된 코드가 있을 수 있다 하지만 Controller 관련 코드는 있으면 안된다
3. View가 Model로부터 데이터를 받을 때는, 사용자마다 다르게 보여주어야 하는 데이터에 대해서만 받아야 한다. <br>
우리가 웹, 앱 서비스를 이용할 때 개인 회원 정보 같은 페이지를 보면 두가지로 나눌 수 있다.
모든 사용자가 똑같이 보는 부분과 사용자 개인 정보 데이터로 나눌 수 있다. 
개인정보는 사용자마다 다르게 보여줘야하는 데이터가 Model로서 모두가 똑같이 보는 레이아웃이 합쳐져 사용자에게 제공되는 화면이 View인 것이다<br>
```
```
4. Controller는 Model과 View에 의존해도 된다<br>
Controller는 Model의 데이터를 View로 전달하는 역할을 하므로 Controller에는 Model과 View 관련 코드가 있어도 된다
```
public class Controller {
    public static void main(String[] args) throws IOException {
        Student student = new Student("2학년",21);
        OutputView.printProfile(student);
    }
}
```
5. View가 Model로부터 데이터를 받을 때, 반드시 Controller에서 받아야 한다
4.에서 이어지는 설명이다 View에서 Model 데이터를 받을 때 반드시 Controller를 통해야 하며 View에서 바로 Model을 호출해서는 안된다
```
public class OutputView {
    public static void printProfile(Student student) {
        System.out.println("내 이름은" + student.getName() + "입니다.");
        System.out.println(student.getAge() + "입니다.");
    }
}
public class Student {
    private String name;
    private int age;
    public Student(String name, int age){
        this.name = name;
        this.age = age;
    }
    public String getName(){
        return name;
    }
    public int getAge(){
        return age;
    }
}
public class Controller {
    public static void main(String[] args) throws IOException {
        Student student = new Student("2학년",21);
        OutputView.printProfile(student);
    }
}
```
위 원칙을 지켜야 Model과 View가 완전히 독립적으로 동작할 수 있다.

## 예제 계산기
위 내용을 공부하면서 계산기를 구현하는데 mvc 패턴을 적용해볼 것 같아서 진행해보았다
우선 계산기의 요소들을 MVC 컴포넌트로 분리해본다
- Model(Operation.java)
  - 연산처리 부분(데이터 처리)
- View(Result.java)
  - 연산 결과 출력
- Controller(Controller.java)
  - 연산 값 입력 및 Model View 처리
Controller 쪽 설명이 부족하다 생각해 이미지로 첨부한다

![image](https://github.com/kdfasdf/Jspwebserver/assets/96770726/5f770200-366a-420a-abb6-e597d2ab69eb)

- Model(Operation.java)
  - 해당 부분은 입력 값에 대한 연산을 처리할 부분으로 사칙연산에 대한 메서드를 구현한다
```
public class Operation {
    private double result;

    public void add(double a, double b) {
        result = a + b;
    }
    public void subtract(double a, double b) {
        result = a - b;
    }
    public void multiply(double a, double b) {
        result = a * b;
    }
    public void divide(double a, double b) {
        if (b != 0) {
            result = a / b;
        } else {
            throw new ArithmeticException("Cannot divide by zero.");
        }
    }
    public double getResult() {
        return result;
    }
}
```
- View(View.java) 연산의 결과를 처리한다
```
public class View {
    void printResult(double value)
    {
        System.out.println("연산 결과: "+value);
    }
}

```
- Controller.java 연산 식을 입력받고 operation.java에 식을 넘기고 그 결과를 View에 넘김
```
public class Controller {
    Controller(Operation model, View view)throws IOException{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            double parameter1 =Double.parseDouble(br.readLine());
            String operator = br.readLine();
            double parameter2=Double.parseDouble(br.readLine());

            if(operator.equals("+"))
                model.add(parameter1,parameter2);
            else if(operator.equals("-"))
                model.subtract(parameter1,parameter2);
            else if(operator.equals("*"))
                model.multiply(parameter1,parameter2);
            else
                model.divide(parameter1,parameter2);

            double operationResult= model.getResult();
            view.printResult(operationResult);
        }
}

```
- Main.java
```
public class Main {
    public static void main(String[] args) throws IOException {
        Operation model = new Operation();
        View view = new View();
        Controller controller = new Controller(model,view);
    }
}
```
구현한 계산기에서 Model은 View와 Controller에 독립적이고 View는 Controller를 통해 Model의 연산 결과를 넘겨받을 수 있게 역할이 분리되어있다

참고 영상: https://www.youtube.com/watch?v=ogaXW6KPc8I
