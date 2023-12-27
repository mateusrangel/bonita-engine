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
package org.bonitasoft.web.rest.server.api.bpm.flownode.archive;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedActivityVariable;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.restlet.resource.Get;

public class ArchivedActivityVariableResource extends CommonResource {

    public static final String ACTIVITYDATA_ACTIVITY_ID = "activityId";
    public static final String VARIABLE_NAME = "variableName";

    private final ProcessAPI processAPI;

    public ArchivedActivityVariableResource(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    @Get("json")
    public ArchivedActivityVariable getArchivedActivityVariable() {
        final String name = getAttribute(VARIABLE_NAME);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Attribute '" + VARIABLE_NAME + "' is mandatory in order to get the archived case variable");
        }
        try {
            return ArchivedActivityVariable
                    .create(processAPI.getArchivedActivityDataInstance(name, getActivityIdParameter()));
        } catch (ArchivedDataNotFoundException e) {
            throw new APINotFoundException(e);
        }
    }

    private long getActivityIdParameter() {
        final String activityId = getAttribute(ACTIVITYDATA_ACTIVITY_ID);
        if (activityId == null) {
            throw new IllegalArgumentException(
                    "Attribute '" + ACTIVITYDATA_ACTIVITY_ID
                            + "' is mandatory in order to get the archived activity variable");
        }
        return Long.parseLong(activityId);
    }

}
