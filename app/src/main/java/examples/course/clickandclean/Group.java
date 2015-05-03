package examples.course.clickandclean;

/**
 * Created by anusha on 27/4/15.
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Group {

    public ArrayList<String> objectIds = new ArrayList<>();
    public LinkedList<String> channels = new LinkedList<String>();
    public String string;
    public final List<String> children = new ArrayList<String>();

    public Group(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return "Group{" +
                "string='" + string + '\'' +
                ", children=" + children.toString() +
                '}';
    }
}
