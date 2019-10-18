package org.bonitasoft.engine.platform;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.impl.SPlatformPropertiesImpl;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.tenant.TenantManager;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PlatformManagerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private NodeConfiguration nodeConfiguration;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PlatformService platformService;
    @Mock
    private TenantManager tenantManager;
    public PlatformManager platformManager;
    @Mock
    public PlatformLifecycleService platformLifecycleService1;
    @Mock
    public PlatformLifecycleService platformLifecycleService2;

    @Before
    public void before() throws Exception {
        platformManager = spy(new PlatformManager(nodeConfiguration, transactionService, platformService, asList(platformLifecycleService1, platformLifecycleService2)));
        when(transactionService.executeInTransaction(any())).thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        doReturn(tenantManager).when(platformManager).getTenantManager(any());
        doReturn(new SPlatform("1.3.0", "1.2.0", "1.1.0", "someUser", 123455)).when(platformService).getPlatform();
        doReturn(new SPlatformPropertiesImpl("1.3.0")).when(platformService).getSPlatformProperties();
    }

    @Test
    public void should_start_scheduler_when_starting_node() throws Exception {

        platformManager.start();

        verify(platformLifecycleService1).start();
        verify(platformLifecycleService2).start();
    }

    @Test
    public void should_start_platform_only_once() throws Exception {
        platformManager.start();

        platformManager.start();

        verify(platformLifecycleService1).start();
        verify(platformLifecycleService2).start();
    }

    @Test
    public void should_stop_services_when_stopping_platform() throws Exception {
        platformManager.start();

        platformManager.stop();

        verify(platformLifecycleService1).stop();
        verify(platformLifecycleService2).stop();
    }

    @Test
    public void should_activate_tenant_using_tenantManager() throws Exception {
        doReturn(deactivated(new STenant())).when(platformService).getTenant(123L);

        platformManager.activateTenant(123L);

        verify(tenantManager).activate();
    }

    @Test
    public void should_throw_exception_when_activating_already_activated_Tenant() throws Exception {
        doReturn(activated(new STenant())).when(platformService).getTenant(123L);

        assertThatThrownBy(() -> platformManager.activateTenant(123L))
                .isInstanceOf(STenantActivationException.class);
    }
    @Test
    public void should_throw_exception_when_deactivating_already_deactivated_Tenant() throws Exception {
        doReturn(deactivated(new STenant())).when(platformService).getTenant(123L);

        assertThatThrownBy(() -> platformManager.deactivateTenant(123L))
                .isInstanceOf(STenantDeactivationException.class);
    }

    @Test
    public void should_throw_not_found_when_deactivating_unexisting_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(123L);

        assertThatThrownBy(() -> platformManager.deactivateTenant(123L))
                .isInstanceOf(STenantNotFoundException.class);
    }
    @Test
    public void should_throw_not_found_when_activating_unexisting_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(123L);

        assertThatThrownBy(() -> platformManager.activateTenant(123L))
                .isInstanceOf(STenantNotFoundException.class);
    }

    @Test
    public void should_deactivate_tenant_using_tenantManager() throws Exception {
        doReturn(activated(new STenant())).when(platformService).getTenant(123L);

        platformManager.deactivateTenant(123L);

        verify(tenantManager).deactivate();
    }

    private STenant deactivated(STenant tenant) {
        tenant.setStatus(STenant.DEACTIVATED);
        return tenant;
    }
    private STenant activated(STenant tenant) {
        tenant.setStatus(STenant.ACTIVATED);
        return tenant;
    }

}