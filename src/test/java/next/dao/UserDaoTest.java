package next.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import next.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserDaoTest {

    @Test
    public void crud() throws Exception {
        User expected = new User("user1", "password", "name", "javajigi@email.com");
        UserDao userDao = new UserDao();
        userDao.insert(expected);

        User actual = userDao.findByUserId(expected.getUserId());
        assertEquals(expected, actual);
    }
    @Test
    public void allUser() throws Exception {
        List<User> users =new ArrayList<>();
        User expected = new User("user4", "password", "name", "1@1");
        UserDao userDao = new UserDao();
        userDao.deleteUser();
        users.add(expected);
        userDao.insert(expected);

        expected = new User("user5", "password2", "name2", "2@2");
        users.add(expected);
        userDao.insert(expected);

        List<User> result = userDao.findAll();
        assertEquals(users, result);
    }
    @Test
    public void update() throws Exception{
        UserDao userDao = new UserDao();
        userDao.deleteUser();
        User original = new User("user4", "password", "name", "1@1");
        userDao.insert(original);
        User updateUser = new User("user4", "update", "update", "update");
        userDao.updateUser(updateUser);
        assertEquals(userDao.findByUserId("user4").getEmail(), updateUser.getEmail());
    }

}
