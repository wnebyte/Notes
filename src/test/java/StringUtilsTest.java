import org.junit.Assert;
import org.junit.Test;
import static com.github.wnebyte.notes.util.StringUtils.*;

@SuppressWarnings("ConstantConditions")
public class StringUtilsTest {

    @Test
    public void test00() {
        String s = "this here is a string   ";
        Assert.assertEquals("this here is a string", stripTrailing(s));
        s = "   this here is a string";
        Assert.assertEquals("this here is a string", stripLeading(s));
        s = null;
        Assert.assertEquals("", strip(s));
        s = "   this here is a string   ";
        Assert.assertEquals("this here is a string", strip(s));
    }
}
