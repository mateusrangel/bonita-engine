/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.permissions

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.doReturn

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
public class ProcessSupervisorPermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def PermissionRule rule = new ProcessSupervisorPermissionRule()
    @Mock
    def ProcessAPI processAPI
    @Mock
    def IdentityAPI identityAPI
    @Mock
    def User user
    def long currentUserId = 16l

    @Before
    public void before() {
        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    public void should_check_verify_post_is_true_when_process_owner() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "process_id":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(true).when(processAPI).isUserProcessSupervisor(154l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_post_is_false_when_not_process_owner() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "process_id":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(false).when(processAPI).isUserProcessSupervisor(154l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_check_verify_get_is_true_when_process_owner() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn([
            "process_id": "154",
            "other"     : "sample"
        ]).when(apiCallContext).getFilters()
        doReturn(true).when(processAPI).isUserProcessSupervisor(154l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_get_is_false_when_not_process_owner() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn([
            "process_id": "154",
            "other"     : "sample"
        ]).when(apiCallContext).getFilters()
        doReturn(false).when(processAPI).isUserProcessSupervisor(154l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_check_verify_delete_return_false_when_not_process_owner() {
        doReturn(true).when(apiCallContext).isDELETE()
        doReturn(["154", "1", "2", "3"]).when(apiCallContext).getCompoundResourceId()
        doReturn(false).when(processAPI).isUserProcessSupervisor(154l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_check_verify_delete_return_true_when_process_owner() {
        doReturn(true).when(apiCallContext).isDELETE()
        doReturn(["154", "1", "2", "3"]).when(apiCallContext).getCompoundResourceId()
        doReturn(true).when(processAPI).isUserProcessSupervisor(154l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }
}
