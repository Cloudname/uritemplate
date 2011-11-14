package org.cloudname.uritemplate;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author vlarsen
 */
public class UriTemplateTest {
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
    public void testExpandPlain() throws Exception {
        UriTemplate template = new UriTemplate("http://www.example.com/{segment}");
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("segment", "xyzzy#foobar");
        assertThat(template.expand(vars), is("http://www.example.com/xyzzy#foobar"));
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
}
