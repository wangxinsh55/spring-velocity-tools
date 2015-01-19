package org.spring.velocity.view;

import org.apache.struts.mock.MockHttpServletRequest;
import org.apache.struts.mock.MockServletContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.junit.Before;
import org.junit.Test;
import scope.Application;
import scope.Request;
import scope.Session;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

public class VelocityToolbox2ViewTest {

    private VelocityToolbox2View toolboxView;
    private HttpServletRequest request;
    private HashMap<String, Object> model;

    @Before
    public void setUp() throws Exception {
        toolboxView = new VelocityToolbox2View();
        toolboxView.setServletContext(new MockServletContext());
        toolboxView.setVelocityEngine(new VelocityEngine());
        request = new MockHttpServletRequest();
        model = new HashMap<String, Object>();
    }

    private Context createViewContext() throws Exception {
        return toolboxView.createVelocityContext(model, request, null);
    }

    private void usingToolbox(String toolboxConfigLocation) {
        toolboxView.setToolboxConfigLocation(toolboxConfigLocation);
    }

    private VelocityToolbox2ViewTest withAttribute(String name, String value) {
        model.put(name, value);
        return this;
    }

    @Test
    public void exposingModelAttributes() throws Exception {
        withAttribute("foo", "bar").withAttribute("key", "value");
        Context context = createViewContext();

        assertThat((String) context.get("foo"), equalTo("bar"));
        assertThat((String) context.get("key"), equalTo("value"));
    }

    @Test
    public void exposingTools() throws Exception {

        usingToolbox("/WEB-INF/toolbox.xml");

        Context context = createViewContext();

        assertThat(context.get("requestScope"), isA((Class) Request.class));
        assertThat(context.get("sessionScope"), isA((Class) Session.class));
        assertThat(context.get("applicationScope"), isA((Class) Application.class));
    }

    @Test
    public void requestScopeToolWillOverwriteAnyScopeTool() throws Exception {
        usingToolbox("/WEB-INF/toolbox.xml");

        Context context = createViewContext();

        assertThat(context.get("foo"), isA((Class) Request.class));
        assertThat(context.get("bar"), isA((Class) Request.class));

    }

    @Test
    public void sessionScopeToolWillOverwriteApplicationScopeTool() throws Exception {
        usingToolbox("/WEB-INF/toolbox.xml");

        Context context = createViewContext();

        assertThat(context.get("other"), isA((Class) Session.class));
    }

    @Test
    public void modelAttributesWillOverwriteAllScopeTools() throws Exception {
        withAttribute("requestScope", "request");
        usingToolbox("/WEB-INF/toolbox.xml");

        assertThat(createViewContext().get("requestScope"), equalTo((Object) "request"));

    }
}