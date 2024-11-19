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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.instance.api.BPMFailureService;
import org.bonitasoft.engine.core.process.instance.model.SBPMFailure;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BPMFailureServiceImplTest {

    @Mock
    private PersistenceService persistenceService;

    private BPMFailureServiceImpl service;

    @BeforeEach
    void setup() throws Exception {
        service = new BPMFailureServiceImpl(persistenceService);
        when(persistenceService.insert(any(SBPMFailure.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    void createFlowNodeFailure() throws Exception {
        var now = Instant.now();
        var flowNodeInstance = createFlowNodeInstance();
        var failure = new BPMFailureService.Failure("scope", new Throwable("error message"));

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        verify(persistenceService).insert(bpmFailure);
        assertThat(bpmFailure.getFlowNodeInstanceId()).isEqualTo(flowNodeInstance.getId());
        assertThat(bpmFailure.getProcessDefinitionId()).isEqualTo(flowNodeInstance.getProcessDefinitionId());
        assertThat(bpmFailure.getProcessInstanceId()).isEqualTo(flowNodeInstance.getParentProcessInstanceId());
        assertThat(bpmFailure.getScope()).isEqualTo("scope");
        assertThat(bpmFailure.getErrorMessage()).isEqualTo("Throwable: error message");
        assertThat(bpmFailure.getStackTrace()).isEqualTo(ExceptionUtils.getStackTrace(failure.throwable()));
        assertThat(Instant.ofEpochMilli(bpmFailure.getFailureDate())).isCloseTo(now, within(1000, ChronoUnit.MILLIS));
    }

    private static SFlowNodeInstance createFlowNodeInstance() {
        SFlowNodeInstance flowNodeInstance = Mockito.mock(SFlowNodeInstance.class);
        when(flowNodeInstance.getId()).thenReturn(1L);
        when(flowNodeInstance.getParentProcessInstanceId()).thenReturn(2L);
        when(flowNodeInstance.getProcessDefinitionId()).thenReturn(3L);
        return flowNodeInstance;
    }

    @Test
    void failureWithExpressionContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var failure = new BPMFailureService.Failure("scope",
                new SExpressionEvaluationException(new RuntimeException("error in expression"), "expressionName"));

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("expression::expressionName");
    }

    @Test
    void failureWithMessageContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInMessage = new SBonitaRuntimeException("error in message");
        errorInMessage.setMessageInstanceNameOnContext("messageName");
        var failure = new BPMFailureService.Failure("scope", errorInMessage);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("message::messageName");
    }

    @Test
    void failureWithConnectorContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException("error in connector");
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("get-info::rest-connector::on_enter");
    }

    @Test
    void failureWithConnectorInputContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException("error in connector");
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        errorInConnector.setConnectorInputOnContext("username");
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("get-info::rest-connector::on_enter//input::username");
    }

    @Test
    void failureWithConnectorInputExpressionContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException(new SExpressionEvaluationException(
                new RuntimeException("error in input expression"), "expressionName"));
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        errorInConnector.setConnectorInputOnContext("username");
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("get-info::rest-connector::on_enter//input::username//expression::expressionName");
    }

    @Test
    void failureWithConnectorOutputExpressionContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException(
                new SOperationExecutionException(new SExpressionEvaluationException(
                        new RuntimeException("error in output expression"), "expressionName")));
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("get-info::rest-connector::on_enter//output//expression::expressionName");
    }

    @Test
    void failureWithConnectorValidationContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException(new ConnectorValidationException("error in validation"));
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("get-info::rest-connector::on_enter//input-validation");
    }

    @Test
    void failureWithNamedTransitionContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInTransition = new SBonitaRuntimeException(new SExpressionEvaluationException(
                new RuntimeException("error in consition expression"), "expressionName"));
        errorInTransition.getContext().put(SExceptionContext.TRANSITION_NAME, "transitionName");
        errorInTransition.getContext().put(SExceptionContext.TRANSITION_TARGET_FLOWNODE_NAME, "gateway::gatewayName");
        var failure = new BPMFailureService.Failure("scope", errorInTransition);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("transitionName//to::gateway::gatewayName//expression::expressionName");
    }

    @Test
    void failureWithUnnamedTransitionContext() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInTransition = new SBonitaRuntimeException(new SExpressionEvaluationException(
                new RuntimeException("error in consition expression"), "expressionName"));
        errorInTransition.getContext().put(SExceptionContext.TRANSITION_TARGET_FLOWNODE_NAME, "gateway::gatewayName");
        var failure = new BPMFailureService.Failure("scope", errorInTransition);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("to::gateway::gatewayName//expression::expressionName");
    }

    @Test
    void getFlowNodeFailures() throws Exception {
        service.getFlowNodeFailures(1, 10);

        ArgumentCaptor<SelectListDescriptor<SBPMFailure>> captor = ArgumentCaptor.forClass(SelectListDescriptor.class);
        verify(persistenceService).selectList(captor.capture());

        var descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo("getFlowNodeFailures");
        assertThat(descriptor.getInputParameter("flowNodeInstanceId")).isEqualTo(1L);
        assertThat(descriptor.getReturnType()).isEqualTo(SBPMFailure.class);
        assertThat(descriptor.getStartIndex()).isZero();
        assertThat(descriptor.getPageSize()).isEqualTo(10);
    }
}
