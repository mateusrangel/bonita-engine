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
package org.bonitasoft.livingapps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.ApplicationLink;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.impl.ApplicationImpl;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.livingapps.exception.CreationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationModelFactoryTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    ApplicationAPI applicationApi;

    @InjectMocks
    ApplicationModelFactory factory;

    @Test(expected = CreationException.class)
    public void should_throw_create_error_exception_when_search_fail() throws Exception {
        given(applicationApi.getIApplicationByToken(any(String.class))).willThrow(ApplicationNotFoundException.class);

        factory.createApplicationModel("foo");
    }

    @Test(expected = CreationException.class)
    public void should_throw_create_error_exception_when_only_application_link_is_found() throws Exception {
        given(applicationApi.getIApplicationByToken(any(String.class))).willReturn(mock(ApplicationLink.class));

        factory.createApplicationModel("foo");
    }

    @Test
    public void should_return_application_found() throws Exception {
        final ApplicationImpl application = new ApplicationImpl("foobar", "1.0", "bazqux");
        application.setId(3);
        given(applicationApi.getIApplicationByToken(any(String.class))).willReturn(application);
        given(applicationApi.getApplicationHomePage(3)).willReturn(new ApplicationPageImpl(1, 1, "home"));

        final ApplicationModel model = factory.createApplicationModel("foo");

        assertThat(model.getApplicationHomePage()).isEqualTo("home/");
    }

}
