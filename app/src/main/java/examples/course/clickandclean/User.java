package examples.course.clickandclean;

/**
 * Created by anusha on 26/3/15.
 */
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("User")

public class User extends ParseObject{
    String name;
    String email;
    String password;

    public User(){

    }


    public void setName(String name) {
        put("name",name);
    }

    public void setEmail(String email) {
        put("email_id",email);
    }

    public void setPassword(String password) {
        put("password",password);
    }

    public String getName() {
        return getString("name");
    }

    public String getEmail() {
        return getString("email_id");
    }

    public String getPassword() {
        return getString("password");
    }

    @Override
    public String toString() {
        return "User{" +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
