package org.cloudname.uritemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author vlarsen
 */
public class UriTemplateTest {
    private Map<String,String> testMap = new HashMap<String,String>();

    @Before
    public void setUp() {
        testMap.put("dom", "example.com");
        testMap.put("dub", "me/too");
        testMap.put("hello", "Hello World!");
        testMap.put("half", "50%");
        testMap.put("var", "value");
        testMap.put("who", "fred");
        testMap.put("base", "http://example.com/home/");
        testMap.put("path", "/foo/bar");

        testMap.put("v", "6");
        testMap.put("x", "1024");
        testMap.put("y", "768");
        testMap.put("empty", "");
        testMap.put("undef", null);
    }


    @Test
    public void testNoExpand() throws Exception {
        UriTemplate template = new UriTemplate("http://www.example.com/xyzzy#foobar");
        assertThat(template.expand(null), is("http://www.example.com/xyzzy#foobar"));
    }

    @Test
    public void testUnclosedExpession() {
        UriTemplate template = new UriTemplate("http://www.example.com/users/{/userid");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        String result = template.expand(vars);
        UriTemplate.ErrorState error = template.getErrorState();
        assertTrue("should be error", error.isError());
        assertThat("should point to start of unclosed expression", error.getAt(), is(30));
    }

    @Test
    public void testJustOpenedExpression() {
        UriTemplate template = new UriTemplate("http://www.example.com/users/{");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        String result = template.expand(vars);
        UriTemplate.ErrorState error = template.getErrorState();
        assertTrue("should be error", error.isError());
        assertThat("should point to start of unclosed expression", error.getAt(), is(30));
    }

    @Test
    public void testSimpleStringExpansion() throws Exception {
        assertThat(new UriTemplate("{var}").expand(testMap),       is("value"));
        assertThat(new UriTemplate("{hello}").expand(testMap),     is("Hello%20World%21"));
        assertThat(new UriTemplate("{half}").expand(testMap),      is("50%25"));
        assertThat(new UriTemplate("O{empty}X").expand(testMap),   is("OX"));
        assertThat(new UriTemplate("O{undef}X").expand(testMap),   is("OX"));
        assertThat(new UriTemplate("{x,y}").expand(testMap),       is("1024,768"));
        assertThat(new UriTemplate("{x,hello,y}").expand(testMap), is("1024,Hello%20World%21,768"));
        assertThat(new UriTemplate("?{x,empty}").expand(testMap),  is("?1024,"));
        assertThat(new UriTemplate("?{x,undef}").expand(testMap),  is("?1024"));
        assertThat(new UriTemplate("?{undef,y}").expand(testMap),  is("?768"));
    }

    @Test
    public void testPlusExpand() throws Exception {
        UriTemplate template = new UriTemplate("http://www.example.com/{+segment}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("segment", "xyzzy#foobar");
        assertThat(template.expand(vars), is("http://www.example.com/xyzzy#foobar"));
    }

    @Test
    public void testDotExpand() {
        UriTemplate template = new UriTemplate("http://www.example.com/users{.userid,type}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        vars.put("type", "email");
        assertThat(template.expand(vars), is("http://www.example.com/users.12345.email"));
    }

    @Test
    public void testPathExpand() {
        UriTemplate template = new UriTemplate("http://www.example.com/users{/userid}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        assertThat(template.expand(vars), is("http://www.example.com/users/12345"));
    }

    @Test
    public void testPathMultiExpand() {
        UriTemplate template = new UriTemplate("http://www.example.com/users{/userid}/rights{/rightid}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        vars.put("rightid", "67890");
        assertThat(template.expand(vars), is("http://www.example.com/users/12345/rights/67890"));
    }

    @Test
    public void testPathStyleExpand() {
        UriTemplate template = new UriTemplate("http://www.example.com/users{;userid,rightid}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        vars.put("rightid", "67890");
        assertThat(template.expand(vars), is("http://www.example.com/users;userid=12345;rightid=67890"));
    }

    @Test
    public void testQueryExpand() {
        UriTemplate template = new UriTemplate("http://www.example.com/users{?userid,type}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        vars.put("type", "email");
        assertThat(template.expand(vars), is("http://www.example.com/users?userid=12345&type=email"));
    }

    @Test
    public void testQueryExtendExpand() {
        UriTemplate template = new UriTemplate("http://www.example.com/users?locale=en{&userid,type}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        vars.put("type", "email");
        assertThat(template.expand(vars), is("http://www.example.com/users?locale=en&userid=12345&type=email"));
    }

    @Test
    public void testFragmentdExpand() {
        UriTemplate template = new UriTemplate("http://www.example.com/users?locale=en{#userid,type}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("userid", "12345");
        vars.put("type", "email");
        assertThat(template.expand(vars), is("http://www.example.com/users?locale=en#12345,email"));
    }

    @Test
    public void testNumberOfExpressions() {
        UriTemplate template = new UriTemplate(
                "http://www.example.com/users?locale=en{#userid,type}");

        UriTemplate template2 = new UriTemplate(
                "http://{+domainname}/users?locale=en{#userid,type}");

        Set<String> extractedVars = template.extractVars();
        assertThat(extractedVars, containsInAnyOrder("userid", "type"));
        Set<String> extractedVars2 = template2.extractVars();
        assertThat(extractedVars2, containsInAnyOrder("domainname", "userid", "type"));
    }
}
