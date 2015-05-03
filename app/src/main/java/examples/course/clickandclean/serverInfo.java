package examples.course.clickandclean;

/**
 * Created by anusha on 28/3/15.
 */
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("serverInfo")

public class serverInfo extends ParseObject{
    //boolean photoInfo;
    // gpsLocation;
    //Date timestamp;

    public ParseGeoPoint getGPSlocation(){
        return getParseGeoPoint("gpslocation");
    }

    public boolean getphotoInfo(){
        return getBoolean("photoInfo");
    }

    public Date getDate() { return getDate("timestamp"); }

    public Number getcriticalLevel(){
        return getNumber("criticalLevel");
    }

    public boolean getIsCleanedVal(){
        return getBoolean("isCleaned");
    }

    public boolean getIsConfirmedVal(){
        return getBoolean("isConfirmed");
    }

    public String getEmailId(){
        return getString("emailId");
    }

    public void setcriticalLevel(Number n){
        put("criticalLevel",n);
    }

    public void setGPSlocation(ParseGeoPoint point){
        put("gpslocation",point);
    }

    public void settimestamp(Date date){
        put("timestamp",date);
    }

    public void setphotoInfo(Boolean info){
        put("photoInfo",info);
    }

    public void setIsCleanedVal(Boolean val){
        put("isCleaned", val);
    }

    public void setIsConfirmedVal(Boolean val){
        put("isConfirmed", val);
    }

    public void setEmailId(String email){
        put("emailId",email);
    }

}
