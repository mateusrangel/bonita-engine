/**
 * Copyright (C) 2011 Bonitasoft S.A.
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
package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.service.ServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class ExecutionExceptionCommand extends RuntimeCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final ServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        throw new SCommandExecutionException("fail");
    }

}
