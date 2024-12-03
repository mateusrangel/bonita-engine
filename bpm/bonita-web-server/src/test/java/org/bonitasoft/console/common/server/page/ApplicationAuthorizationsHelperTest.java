/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.livingapps.ApplicationModel;
import org.bonitasoft.livingapps.ApplicationModelFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationAuthorizationsHelperTest {

    @Mock
    ApplicationAPI applicationAPI;

    @Mock
    ApplicationModelFactory applicationFactory;

    @InjectMocks
    ApplicationAuthorizationsHelper applicationAuthorizationsHelper;

    @Mock
    ApplicationModel applicationModel;

    @Mock
    Application application;

    @Test
    public void should_authorize_page_when_appToken_not_null_and_page_authorized_in_application() throws Exception {
        given(applicationFactory.createApplicationModel(any())).willReturn(applicationModel);
        when(applicationModel.authorize(any())).thenReturn(true);
        final boolean isPageAuthorized = applicationAuthorizationsHelper.isAuthorized("appToken");

        assertThat(isPageAuthorized).isTrue();
        verify(applicationModel).authorize(any());
    }

    @Test
    public void should_unAuthorize_page_when_appToken_not_null_and_page_not_authorized_in_application()
            throws Exception {
        given(applicationFactory.createApplicationModel(any(String.class))).willReturn(applicationModel);
        when(applicationModel.authorize(any())).thenReturn(false);
        final boolean isPageAuthorized = applicationAuthorizationsHelper.isAuthorized("appToken");

        assertThat(isPageAuthorized).isFalse();
        verify(applicationModel).authorize(any());
        verify(applicationAPI, never()).searchApplicationPages(any());
    }

    @Test
    public void should_not_authorize_page_when_appToken_not_null_and_page_unauthorized_in_application() {
        final boolean isPageAuthorized = applicationAuthorizationsHelper.isAuthorized("appToken");

        assertThat(isPageAuthorized).isFalse();
    }

    @Test
    public void should_not_authorize_page_when_appToken_is_null() {
        final boolean isPageAuthorized = applicationAuthorizationsHelper.isAuthorized("");

        assertThat(isPageAuthorized).isFalse();
    }

}
