package ut.com.avagulov.jira;

import org.junit.Test;
import com.avagulov.jira.api.MyPluginComponent;
import com.avagulov.jira.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null, null, null, null, null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}