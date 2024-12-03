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

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.livingapps.ApplicationModel;
import org.bonitasoft.livingapps.ApplicationModelFactory;

public class ApplicationAuthorizationsHelper {

    private final APISession apiSession;
    private final ApplicationModelFactory applicationFactory;

    public ApplicationAuthorizationsHelper(final APISession apiSession,
            final ApplicationModelFactory applicationModelFactory) {
        this.apiSession = apiSession;
        this.applicationFactory = applicationModelFactory;
    }

    public boolean isAuthorized(final String applicationToken) {
        try {
            final ApplicationModel application = applicationFactory.createApplicationModel(applicationToken);
            return application.authorize(apiSession);
        } catch (final Exception e) {
            return false;
        }
    }

}
