/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.transition;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.execution.state.AbortedFlowNodeState;
import org.bonitasoft.engine.execution.state.AbortingActivityWithBoundaryState;
import org.bonitasoft.engine.execution.state.AbortingBoundaryEventsOnCompletingActivityState;
import org.bonitasoft.engine.execution.state.AbortingReceiveTaskState;
import org.bonitasoft.engine.execution.state.CancelledFlowNodeState;
import org.bonitasoft.engine.execution.state.CancellingActivityWithBoundaryState;
import org.bonitasoft.engine.execution.state.CancellingReceiveTaskState;
import org.bonitasoft.engine.execution.state.CompletedActivityState;
import org.bonitasoft.engine.execution.state.ExecutingFlowNodeState;
import org.bonitasoft.engine.execution.state.InitializingActivityWithBoundaryEventsState;
import org.bonitasoft.engine.execution.state.WaitingFlowNodeState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ReceiveTaskStates extends FlowNodeStatesAndTransitions {

    public SFlowNodeType getFlowNodeType() {
        return SFlowNodeType.RECEIVE_TASK;
    }

    public ReceiveTaskStates(InitializingActivityWithBoundaryEventsState initializingActivityWithBoundary,
            AbortingBoundaryEventsOnCompletingActivityState abortingBoundaryEventsOnCompletingActivityState,
            CompletedActivityState completed,
            AbortingActivityWithBoundaryState abortingActivityWithBoundary,
            AbortedFlowNodeState aborted,
            @Qualifier("cancellingActivityWithBoundaryState") CancellingActivityWithBoundaryState cancellingActivityWithBoundary,
            ExecutingFlowNodeState executing,
            AbortingReceiveTaskState abortingReceiveTask,
            @Qualifier("cancellingReceiveTaskState") CancellingReceiveTaskState cancellingReceiveTask,
            CancelledFlowNodeState cancelled,
            WaitingFlowNodeState waiting) {

        defineNormalTransitionForFlowNode(initializingActivityWithBoundary, waiting, executing,
                abortingBoundaryEventsOnCompletingActivityState, completed);
        defineAbortTransitionForFlowNode(abortingActivityWithBoundary, abortingReceiveTask, aborted);
        defineCancelTransitionForFlowNode(cancellingActivityWithBoundary, cancellingReceiveTask, cancelled);
    }
}
