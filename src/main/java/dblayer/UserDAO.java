package dblayer;

import model.*;
import org.hibernate.Session;

import java.util.Optional;

/**
 * Created by martinsenden on 2017-05-04.
 */
public class UserDAO {
    private static UserDAO instance;


    private UserDAO(){

    }

    public static UserDAO getInstance(){
        if (instance == null){
            instance = new UserDAO();
        }
        return instance;
    }
    public Optional<User> getUserById(Long friendId){
        try(Session session = HibernateUtil.getInstance().openSession()){
            return session.byId(User.class).loadOptional(friendId);
        }
    }

}