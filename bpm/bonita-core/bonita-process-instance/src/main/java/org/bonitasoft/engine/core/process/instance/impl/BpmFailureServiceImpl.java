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
package org.bonitasoft.engine.core.process.instance.impl;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.core.process.instance.api.BpmFailureService;
import org.bonitasoft.engine.core.process.instance.model.SBpmFailure;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.springframework.stereotype.Service;

@Service
public class BpmFailureServiceImpl implements BpmFailureService {

    private final PersistenceService persistenceService;

    public BpmFailureServiceImpl(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public void createFlowNodeFailure(SFlowNodeInstance flowNodeInstance, Failure failure)
            throws SPersistenceException {
        persistenceService.insert(SBpmFailure.builder()
                .flowNodeInstanceId(flowNodeInstance.getId())
                .processInstanceId(flowNodeInstance.getParentProcessInstanceId())
                .processDefinitionId(flowNodeInstance.getProcessDefinitionId())
                .scope(failure.scope())
                .context(failure.context())
                .errorMessage(ExceptionUtils.printRootCauseOnly(failure.throwable()))
                .stackTrace(ExceptionUtils.printLightWeightStacktrace(failure.throwable(), 20))
                .build());
    }

    @Override
    public List<SBpmFailure> getFlowNodeFailures(long flowNodeInstanceId, int maxResults) throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, maxResults);
        final Map<String, Object> parameters = Map.ofEntries(Map.entry("flowNodeInstanceId", flowNodeInstanceId));
        return persistenceService.selectList(new SelectListDescriptor<SBpmFailure>("getFlowNodeFailure", parameters,
                SBpmFailure.class, queryOptions));
    }
}
