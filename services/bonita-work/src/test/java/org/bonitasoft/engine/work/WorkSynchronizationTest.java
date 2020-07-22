/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.work;

import static javax.transaction.Status.STATUS_COMMITTED;
import static javax.transaction.Status.STATUS_ROLLEDBACK;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkSynchronizationTest {

    @Mock
    private WorkExecutorService workExecutorService;
    @Mock
    private SessionAccessor sessionAccessor;
    private final WorkDescriptor workDescriptor1 = WorkDescriptor.create("myWork1");

    @Test
    public void should_submit_work_on_commit() {
        WorkSynchronization workSynchronization = new WorkSynchronization(workExecutorService, sessionAccessor,
                workDescriptor1);

        workSynchronization.afterCompletion(STATUS_COMMITTED);

        verify(workExecutorService).execute(workDescriptor1);
    }

    @Test
    public void should_not_submit_work_on_transaction_not_in_committed_state() {
        WorkSynchronization workSynchronization = new WorkSynchronization(workExecutorService, sessionAccessor,
                workDescriptor1);
        workSynchronization.afterCompletion(STATUS_ROLLEDBACK);

        verify(workExecutorService, never()).execute(workDescriptor1);
    }
}
