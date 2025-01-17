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
package org.bonitasoft.engine.execution.work.failurewrapping;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.commons.exceptions.ExceptionContext;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.execution.work.WrappingBonitaWork;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * This work manages the transaction in its handleFailure method.
 * Don't use this work with {@link org.bonitasoft.engine.execution.work.TxBonitaWork}
 *
 * @author Celine Souchet
 */
public abstract class TxInHandleFailureWrappingWork extends WrappingBonitaWork {

    protected TxInHandleFailureWrappingWork(final BonitaWork work) {
        super(work);
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        return getWrappedWork().work(context);
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        // Enrich the exception before log it.
        if (e instanceof ExceptionContext exceptionContext) {
            final ServiceAccessor serviceAccessor = getServiceAccessor(context);
            final UserTransactionService transactionService = serviceAccessor.getUserTransactionService();
            transactionService.executeInTransaction((Callable<Void>) () -> {
                setExceptionContext(exceptionContext, context);
                return null;
            });
        }
        getWrappedWork().handleFailure(e, context);
    }

    protected abstract void setExceptionContext(final ExceptionContext exceptionContext,
            final Map<String, Object> context) throws SBonitaException;
}
