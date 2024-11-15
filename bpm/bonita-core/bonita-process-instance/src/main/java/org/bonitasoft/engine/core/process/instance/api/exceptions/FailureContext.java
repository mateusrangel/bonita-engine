/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import org.bonitasoft.engine.core.process.instance.api.BpmFailureService;

public interface FailureContext {

    static final String UNKNOWN_SCOPE = "UNKNOWN";
    static final String EMPTY_CONTEXT = "";

    /**
     * Describe the scope of the failure with a human-readable failure category.
     *
     * @return The scope of the failure
     */
    default String getFailureScope() {
        return UNKNOWN_SCOPE;
    }

    /**
     * Additional context information about the failure. The returned string is formatted using
     * the following pattern: "context1//context2//context3"
     *
     * @return The context of the failure
     */
    default String getFailureContext() {
        return EMPTY_CONTEXT;
    }

    BpmFailureService.Failure createFailure();

}
