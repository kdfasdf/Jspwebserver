## 서론
사실 UserDao 구현한지는 시간이 좀 되었다. 원래는 UserDao와 리펙토링하는 과정까지 한번에 기록으로 남기려고 했으나 UserDao를 리펙토링하기 전에 점검해봐야할 내용이 좀 있어서 텀이 너무 길어질까봐 기록으로 남긴다.<br>
mvc-refactoring1 까지의 브랜치에서는 회원가입을 한 계정이 데이터베이스 객체에 저장되었다. 데이터베이스를 사용하지 않기에 프로젝트를 다시 실행할 때 마다  데이터베이스 객체에 저장되어 있는 계정 정보가 날아가는 한계가 있었다.<br>
프로젝트를 실행해도 가입했던 회원 정보가 날아가게 하지 않기 위해 데이터베이스를 연동하고 UserDao를 구현하여 기존에 DateBase객체가 하던 일을 UserDao로 위임해보자
<br>
데이터베이스는 h2 데이터베이스를 사용하고 jar 파일이 들어가 있었다 데이터 베이스를 연동하는 역할을 하는 클래스는 ContextLoaderListener이었고 다음과 같이 구현되어 있었다
- ContextLoaderListner.java
```
@WebListener
public class ContextLoaderListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(ContextLoaderListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(); //데이터베이스 초기화 객체 생성
        populator.addScript(new ClassPathResource("jwp.sql"));  //스크립트 추가
        DatabasePopulatorUtils.execute(populator, ConnectionManager.getDataSource());// 스크립트 실행 getdatasource: 데이터베이스 풀 연결

        logger.info("Completed Load ServletContext!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}

```
우선 ServletContextListener는 웹 애플리케이션이나 필터가 초기화되기 이전에@WebListener를 통해 ServletContext의 초기화 이벤트를 수신할 수 있다. contextInitialized 메서드 안에 데이터베이스 연결 로직을 구현하여 데이터베이스를 연동할 수 있다. 
contextInitialized 메서드는 필터나 웹 애플리케이션이 초기화되기 이전에 호출된다
- jwp.sql
```
DROP TABLE IF EXISTS USERS;

CREATE TABLE USERS ( 
	userId          varchar(12)		NOT NULL, 
	password		varchar(12)		NOT NULL,
	name			varchar(20)		NOT NULL,
	email			varchar(50),	
  	
	PRIMARY KEY               (userId)
);

INSERT INTO USERS VALUES('admin', 'password', '자바지기', 'admin@slipp.net');
```

jwp.sql의 내용인데 스크립트를 실행하면 users 테이블을 만들고 계정을 하나 넣게끔 되어 있다
기존에 데이터베이스 객체에 유저 정보를 저장하거나 조회하는 로직은 다음과 같았다

```
public class DataBase {
    private static Map<String, User> users = Maps.newHashMap();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}

/* CreateUserControlle.java
    public String execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = new User(req.getParameter("userId"), req.getParameter("password"), req.getParameter("name"),
                req.getParameter("email"));
        log.debug("user : {}", user);
        DataBase.addUser(user);
        return "redirect:/user/list";
    }
*/
/*ListUserController.java
public class ListUserController extends HttpServlet implements Controller {
    private static final long serialVersionUID=1L;
    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);
    @Override
    public String execute(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException{
        if (!UserSessionUtils.isLogined(req.getSession())) {
            return "redirect:/user/login.jsp";
        }
        req.setAttribute("users",DataBase.findAll());
        return "/user/list.jsp";
    }
}
*/
```
크게 바꿀 것은 없고 user객체에 받았던 회원정보다 DataBase.findAll()의 기능을 데이터베이스와 연동하여 수행할 수 있도록 userDao에 구현을 해주고 controller들에 userDao의 메서드와 객체로 변경해주면 된다.
다만 서비스를 하는 관점에서 기존의 코드가 안정적으로 돌아가는 상황에서 새로 교체할 userDao의 안정성을 확보하기 위해 테스트코드를 작성하며 진행하였다
- userDao.java
```
public class UserDao {
    public void insert(User user) throws SQLException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = ConnectionManager.getConnection();
            String sql = "INSERT INTO USERS VALUES (?, ?, ?, ?)";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }

            if (con != null) {
                con.close();
            }
        }
    }

    public static User findByUserId(String userId) throws SQLException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectionManager.getConnection();
            String sql = "SELECT userId, password, name, email FROM USERS WHERE userid=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, userId);

            rs = pstmt.executeQuery();

            User user = null;
            if (rs.next()) {
                user = new User(rs.getString("userId"), rs.getString("password"), rs.getString("name"),
                        rs.getString("email"));
            }

            return user;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }
    public List<User> findAll() throws SQLException{
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        try
        {
            con = ConnectionManager.getConnection();
            String sql = "SELECT userId,password,name,email FROM USERS";
            pstmt = con.prepareStatement(sql);
            rs=pstmt.executeQuery();
            while(rs.next())
            {
                users.add(new User(rs.getString("userId"),rs.getString("password"),rs.getString("name"),rs.getString("email")));
            }
            return users;
        }finally{
            if(rs!=null){
                rs.close();
            }
            if(pstmt!=null){
                pstmt.close();
            }
            if(con!=null)
                con.close();
        }

    }
    public void deleteUser() throws SQLException{
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectionManager.getConnection();
            String sql = "TRUNCATE TABLE USERS";
            pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
        }finally{
            if(rs!=null)
            {
                rs.close();
            }
            if(pstmt!=null)
            {
                pstmt.close();
            }
            if(con!=null){
                con.close();
            }
        }
    }
    public void updateUser(User user) throws SQLException{
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            con = ConnectionManager.getConnection();
            String sql = "UPDATE USERS SET PASSWORD=?,NAME=?,EMAIL=? WHERE USERID=?";
            pstmt=con.prepareStatement(sql);
            pstmt.setString(1,user.getPassword());
            pstmt.setString(2,user.getName());
            pstmt.setString(3,user.getEmail());
            pstmt.setString(4,user.getUserId());
            pstmt.executeUpdate();
        }finally{
            if(rs!=null)
            {
                rs.close();
            }
            if(pstmt!=null){
                pstmt.close();
            }
            if(con!=null){
                con.close();
            }
        }
    }
}

```
계정 생성, 회원 수정, 회원 목록 출력, id로 해당 유저 객체 반환, 회원 정보 수정,테이블 삭제를 하는 메서드를 구현했다 해당 메서드들을 테스트하기 위한 코드는 아래와 같다
```
public class UserDaoTest {

    @Test
    public void crud() throws Exception {//회원가입 테스트
        User expected = new User("user1", "password", "name", "javajigi@email.com");
        UserDao userDao = new UserDao();
        userDao.insert(expected);

        User actual = userDao.findByUserId(expected.getUserId());
        assertEquals(expected, actual);
    }
    @Test
    public void allUser() throws Exception {//삭제-회원가입-회원가입-목록 가져오기
        List<User> users =new ArrayList<>();
        UserDao userDao = new UserDao();
        userDao.deleteUser();
        User expected = new User("user4", "password", "name", "1@1");
        users.add(expected);
        userDao.insert(expected);

        expected = new User("user5", "password2", "name2", "2@2");
        users.add(expected);
        userDao.insert(expected);

        List<User> result = userDao.findAll();
        assertEquals(users, result);
    }
    @Test
    public void update() throws Exception{//삭제-회원가입-업데이트
        UserDao userDao = new UserDao();
        userDao.deleteUser();
        User original = new User("user4", "password", "name", "1@1");
        userDao.insert(original);
        User updateUser = new User("user4", "update", "update", "update");
        userDao.updateUser(updateUser);
        assertEquals(userDao.findByUserId("user4").getEmail(), updateUser.getEmail());
    }

}
```
메서드 하나하나에 대한 테스트를 진행하기보다는 삭제-회원가입-업데이트, 삭제-회원가입-회원가입-목록 가져오기 식으로 할 수밖에 없었는데 테스트코드여도 crud에서 회원가입 기능을 테스트하면 실제로 데이터베이스에 회원 정보가 저장되어 다른 테스트 코드에 영향을 미쳤기 때문이었다. 
위의 테스트 코드가 정상적으로 동작하여 기존 코드에 Datebase 클래스를 사용한 코드, User클래스를 사용한 코드들을 일부 수정해주었다 CreateController의 service와 ListUserController의 service 구현부가 아래처럼 바뀌었다. 다른 Controller에 대한 변경점은 mvc-refactoring1과 UserDao-implementation1브랜치를 통해 확인할 수 있다.
CreateUserController
```
//변경 전
    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = new User(req.getParameter("userId"), req.getParameter("password"), req.getParameter("name"),
                req.getParameter("email"));
        log.debug("user : {}", user);
        DataBase.addUser(user);
        return "redirect:/user/list";
    }
//변경 후
    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = new User(req.getParameter("userId"), req.getParameter("password"), req.getParameter("name"),
                req.getParameter("email"));
        log.debug("user : {}", user);
        UserDao userDao = new UserDao();
        try {
            userDao.insert(user);
        }catch(SQLException e)
        {
            log.error(e.getMessage());
        }
        DataBase.addUser(user);
        return "redirect:/user/list";
    }
```
ListUserController
```
//변경 전
    @Override
    public String execute(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException{
        if (!UserSessionUtils.isLogined(req.getSession())) {
            return "redirect:/user/login.jsp";
        }
        req.setAttribute("users",DataBase.findAll());
        return "/user/list.jsp";
    }
//변경 후
    @Override
    public String execute(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
        if (!UserSessionUtils.isLogined(request.getSession())) {
            return "redirect:/user/login.jsp";
        }
        UserDao userDao = new UserDao();
        try{
            List<User>users=userDao.findAll();
            request.setAttribute("users",users);
        }catch(SQLException e){
            log.error(e.getMessage());
        }
        return "/user/list.jsp";
    }
```
