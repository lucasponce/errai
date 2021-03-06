package org.jboss.errai.security;

import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.security.server.SecurityRoleInterceptor;
import org.jboss.errai.security.shared.*;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.shared.PageRequest;
import org.junit.Before;
import org.junit.Test;

import javax.interceptor.InvocationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author edewit@redhat.com
 */
public class SecurityRoleInterceptorTest {
  private AuthenticationService authenticationService;
  private SecurityRoleInterceptor interceptor;

  @Before
  public void setUp() throws Exception {
    authenticationService = mock(AuthenticationService.class);
    interceptor = new SecurityRoleInterceptor(authenticationService);
  }

  @Test
  public void shouldVerifyUserInRole() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getTarget()).thenReturn(new Service());
    when(context.getMethod()).thenReturn(getAnnotatedServiceMethod());
    when(authenticationService.getRoles()).thenReturn(Arrays.asList(new Role("admin"), new Role("user")));
    interceptor.aroundInvoke(context);

    // then
    verify(context).proceed();
  }

  @Test(expected = SecurityException.class)
  public void shouldThrowExceptionWhenUserNotInRole() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    invokeTest(context, new Service());

    // then
    fail("security exception should have been thrown");
  }

  @Test(expected = SecurityException.class)
  public void shouldFindMethodWhenNoInterface() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    invokeTest(context, this);

    // then
    fail("security exception should have been thrown");
  }

  private void invokeTest(InvocationContext context, Object service) throws Exception {
    when(context.getTarget()).thenReturn(service);
    when(context.getMethod()).thenReturn(getAnnotatedServiceMethod());
    when(authenticationService.getRoles()).thenReturn(new ArrayList<Role>());
    interceptor.aroundInvoke(context);
  }

  @Test(expected = SecurityException.class)
  public void shouldFindMethodWhenOnInterface() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getTarget()).thenReturn(new Service());
    when(context.getMethod()).thenReturn(Service.class.getMethod("annotatedServiceMethod"));
    when(authenticationService.getRoles()).thenReturn(new ArrayList<Role>());
    interceptor.aroundInvoke(context);

    // then
    fail("security exception should have been thrown");
  }

  @Test
  public void shouldVerifyUserInRoleClientSide() throws Exception {
    //given
    RemoteCallContext context = mock(RemoteCallContext.class);
    RemoteServiceProxyFactory.addRemoteProxy(AuthenticationService.class, new ProxyProvider() {
      @Override
      public Object getProxy() {
        return new MockAuthenticationService(Arrays.asList(new Role("user")));
      }
    });

    final Boolean[] redirectToLoginPage = {Boolean.FALSE};
    interceptor = new SecurityRoleInterceptor(authenticationService) {
      @Override
      protected void navigateToPage(Class<? extends UniquePageRole> roleClass) {
        redirectToLoginPage[0] = Boolean.TRUE;
      }
    };

    //when
    when(context.getAnnotations()).thenReturn(getAnnotatedServiceMethod().getAnnotations());
    interceptor.aroundInvoke(context);

    //then
    assertTrue(redirectToLoginPage[0]);
  }

  private Method getAnnotatedServiceMethod() throws NoSuchMethodException {
    return ServiceInterface.class.getMethod("annotatedServiceMethod");
  }

  public static interface  ServiceInterface {
    @RequireRoles("admin")
    void annotatedServiceMethod();
  }
  public static class Service implements ServiceInterface {
    @Override
    public void annotatedServiceMethod() {
    }
  }

  @SuppressWarnings("unchecked")
  public static class MockAuthenticationService extends AbstractRpcProxy implements AuthenticationService {
    List<Role> roleList;

    public MockAuthenticationService() {
    }

    public MockAuthenticationService(List<Role> roleList) {
      this.roleList = roleList;
    }

    @Override
    public User login(String username, String password) {
      return null;
    }

    @Override
    public boolean isLoggedIn() {
      remoteCallback.callback(Boolean.FALSE);
      return false;
    }

    @Override
    public void logout() {
    }

    @Override
    public User getUser() {
      return null;
    }

    @Override
    public List<Role> getRoles() {
      remoteCallback.callback(roleList);
      return roleList;
    }

    @Override
    public boolean hasPermission(PageRequest pageRequest) {
      return false;
    }
  }
}
