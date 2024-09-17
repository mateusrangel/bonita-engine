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
package org.bonitasoft.engine.api.impl;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.bonitasoft.engine.api.MaintenanceAPI;
import org.bonitasoft.engine.api.NoSessionRequired;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.classloader.ClassLoaderIdentifier;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.maintenance.MaintenanceDetails;
import org.bonitasoft.engine.platform.NodeNotStartedException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.*;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is in charge of calling APIs while adding behavior:
 * <ul>
 * <li>It sets the classloader to the one from platform or tenant</li>
 * <li>When the method is <b>NOT</b> annotated with {@link NoSessionRequired}, it verifies that the given session is
 * valid, is on the right scope (tenant or platform), and renew it</li>
 * <li>When the method is <b>NOT</b> annotated with {@link CustomTransactions}, it opens a transaction</li>
 * <li>When the method is deprecated, it print a warning</li>
 * <li>When the method or class is <b>NOT</b> annotated with {@link AvailableInMaintenanceMode}, it verifies the
 * maintenance mode
 * is disabled</li>
 * <li>When the method or class is annotated with {@link AvailableInMaintenanceMode} and onlyAvailableInMaintenanceMode
 * is set
 * to true, it verifies the maintenance mode is enabled</li>
 * <li>When the method is <b>NOT</b> annotated with {@link AvailableOnStoppedNode}, it verifies the platform is
 * running</li>
 * </ul>
 */
public class ServerAPIImpl implements ServerAPI {

    private static final Logger logger = LoggerFactory.getLogger(ServerAPIImpl.class);

    private static final String SESSION = "session";
    private static final long serialVersionUID = -161775388604256321L;

    private final APIAccessResolver accessResolver;

    private final boolean cleanSession;

    public ServerAPIImpl(boolean cleanSession) {
        try {
            this.cleanSession = cleanSession;
            accessResolver = getServiceAccessorFactoryInstance().createAPIAccessResolver();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public ServerAPIImpl() {
        this(true);
    }

    /**
     * For Test Mock usage
     */
    public ServerAPIImpl(APIAccessResolver accessResolver) {
        this.cleanSession = true;
        this.accessResolver = accessResolver;
    }

    ServiceAccessorFactory getServiceAccessorFactoryInstance() {
        return ServiceAccessorFactory.getInstance();
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName,
            final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        logger.trace("Starting Server API call {} {}", apiInterfaceName, methodName);

        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        SessionAccessor sessionAccessor = null;
        Session session = null;
        try {
            final Object api = accessResolver.getAPIImplementation(Class.forName(apiInterfaceName));
            try {
                session = (Session) options.get(SESSION);
                sessionAccessor = beforeInvokeMethod(session, api);
                return invokeAPI(api, apiInterfaceName, methodName, classNameParameters, parametersValues, session);
            } catch (final ServerAPIRuntimeException e) {
                throw e.getCause();
            }
        } catch (final BonitaRuntimeException | BonitaException bre) {
            fillGlobalContextForException(session, bre);
            // reset class loader
            Thread.currentThread().setContextClassLoader(baseClassLoader);
            throw createServerWrappedException(bre);
        } catch (final UndeclaredThrowableException ute) {
            // reset class loader
            Thread.currentThread().setContextClassLoader(baseClassLoader);
            throw createServerWrappedException(ute);
        } catch (final Throwable cause) {
            final BonitaRuntimeException throwableToWrap = wrapThrowable(cause);
            fillGlobalContextForException(session, throwableToWrap);
            // reset class loader
            Thread.currentThread().setContextClassLoader(baseClassLoader);
            throw createServerWrappedException(throwableToWrap);
        } finally {
            cleanSessionIfNeeded(sessionAccessor);
            logger.trace("End Server API call {} {}", apiInterfaceName, methodName);
        }
    }

    protected BonitaRuntimeException wrapThrowable(final Throwable cause) {
        return new BonitaRuntimeException(cause);
    }

    private ServerWrappedException createServerWrappedException(final Throwable throwableToWrap) {
        return new ServerWrappedException(throwableToWrap);
    }

    private void fillGlobalContextForException(final Session session, final BonitaContextException be) {
        fillUserNameContextForException(session, be);
    }

    private void fillUserNameContextForException(final Session session, final BonitaContextException be) {
        if (session != null) {
            final String userName = session.getUserName();
            if (userName != null) {
                be.setUserName(userName);
            }
        }
    }

    private void cleanSessionIfNeeded(SessionAccessor sessionAccessor) {
        if (cleanSession) {
            // clean session id
            if (sessionAccessor != null) {
                sessionAccessor.deleteSessionId();
            }
        }
    }

    private SessionAccessor beforeInvokeMethod(final Session session, final Object api)
            throws BonitaHomeNotSetException, BonitaHomeConfigurationException, IOException, SBonitaException,
            ReflectiveOperationException {
        SessionAccessor sessionAccessor = null;

        final ServiceAccessorFactory serviceAccessorFactory = getServiceAccessorFactoryInstance();
        final ServiceAccessor serviceAccessor = serviceAccessorFactory.createServiceAccessor();

        ClassLoader serverClassLoader = null;
        if (session != null) {
            final SessionType sessionType = getSessionType(session);
            sessionAccessor = serviceAccessorFactory.createSessionAccessor();
            switch (sessionType) {
                case PLATFORM:
                    serverClassLoader = beforeInvokeMethodForPlatformSession(sessionAccessor, serviceAccessor,
                            session);
                    break;

                case API:
                    serverClassLoader = beforeInvokeMethodForAPISession(sessionAccessor, serviceAccessor,
                            session);
                    break;

                default:
                    throw new InvalidSessionException("Unknown session type: " + session.getClass().getName());
            }
        } else if (needSession(api)) {
            throw new InvalidSessionException("Session is null!");
        }
        if (serverClassLoader != null) {
            Thread.currentThread().setContextClassLoader(serverClassLoader);
        }
        return sessionAccessor;
    }

    private boolean needSession(Object api) {
        //require a session if "NoSessionRequired" is not present
        Class<?>[] interfaces = api.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if (anInterface.isAnnotationPresent(NoSessionRequired.class)) {
                return false;
            }
        }
        return true;
    }

    private ClassLoader beforeInvokeMethodForAPISession(SessionAccessor sessionAccessor,
            ServiceAccessor serviceAccessor, Session session) throws SBonitaException {
        checkTenantSession(serviceAccessor, session);
        long tenantId = ((APISession) session).getTenantId();
        SessionService sessionService = serviceAccessor.getSessionService();
        sessionService.renewSession(session.getId());
        sessionAccessor.setSessionInfo(session.getId(), tenantId);
        return getTenantClassLoader(serviceAccessor, session);
    }

    private ClassLoader beforeInvokeMethodForPlatformSession(final SessionAccessor sessionAccessor,
            final ServiceAccessor serviceAccessor,
            final Session session) throws SSessionException, SClassLoaderException {
        final PlatformSessionService platformSessionService = serviceAccessor.getPlatformSessionService();
        final PlatformLoginService loginService = serviceAccessor.getPlatformLoginService();

        if (!loginService.isValid(session.getId())) {
            throw new InvalidSessionException("Invalid session");
        }
        platformSessionService.renewSession(session.getId());
        sessionAccessor.setSessionInfo(session.getId(), -1);
        return getPlatformClassLoader(serviceAccessor);
    }

    private SessionType getSessionType(final Session session) {
        SessionType sessionType = null;
        if (session instanceof PlatformSession) {
            sessionType = SessionType.PLATFORM;
        } else if (session instanceof APISession) {
            sessionType = SessionType.API;
        }
        return sessionType;
    }

    private Object invokeAPI(Object api, String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues, final Session session)
            throws Throwable {
        final Class<?>[] parameterTypes = getParameterTypes(classNameParameters);

        final Method method = ClassReflector.getMethod(api.getClass(), methodName, parameterTypes);
        // first, check if a lock is needed before opening any transaction
        // and get the key which defines the functional scope
        Optional<String> lockKey = Optional.ofNullable(method.getAnnotation(WithLock.class)).map(WithLock::key);
        // try and acquire a lock with this scope if necessary
        Supplier<String> failureMessage = () -> MessageFormat.format(
                "Operation ''{0}.{1}'' requires exclusive access. Another operation is already launched with the same ''{2}'' access scope. You may try again after the other operation has finished.",
                apiInterfaceName, methodName, lockKey.orElse(""));
        try (AutoCloseable ignored = withEventualLock(lockKey, session, failureMessage)) {
            // No session required means that there is no transaction
            if (method.isAnnotationPresent(CustomTransactions.class)
                    || Class.forName(apiInterfaceName).isAnnotationPresent(NoSessionRequired.class)) {
                return invokeAPIOutsideTransaction(parametersValues, api, method, apiInterfaceName, session);
            } else {
                return invokeAPIInTransaction(parametersValues, api, method, session, apiInterfaceName);
            }
        }
    }

    /**
     * Eventually get a functional lock auto-closeable resource.
     *
     * @param lockKey the functional key for the lock scope or an empty
     *        optional when no lock is necessary
     * @param session the user session
     * @param failureMessage builds the failure message when lock is already taken
     * @return the auto-closeable resource or a stub ineffective resource when
     *         lockKey is empty
     * @throws UnavailableLockException error with built message when
     *         lock is already taken.
     */
    private AutoCloseable withEventualLock(final Optional<String> lockKey, final Session session,
            Supplier<String> failureMessage) throws Throwable {
        if (lockKey.isPresent()) {
            // try and acquire a lock with this scope
            final long tenantId = (session instanceof APISession) ? ((APISession) session).getTenantId() : 1L;
            LockService lockService = getServiceAccessorFactoryInstance().createServiceAccessor().getLockService();
            BonitaLock lock = lockService.tryLock(1L, lockKey.get(), 1L, TimeUnit.MILLISECONDS, tenantId);
            if (lock == null) {
                // timeout expired, we should not pursue this way
                throw new UnavailableLockException(failureMessage.get());
            }
            return () -> lockService.unlock(lock, tenantId);
        } else {
            // ineffective resource
            return () -> {
            };
        }
    }

    private Object invokeAPIOutsideTransaction(Object[] parametersValues, Object apiImpl, Method method,
            String apiInterfaceName, Session session)
            throws Throwable {
        checkMethodAccessibility(apiImpl, apiInterfaceName, method, session, /* Not in transaction */false);
        return invokeAPI(method, apiImpl, parametersValues);
    }

    protected void checkMethodAccessibility(final Object apiImpl, final String apiInterfaceName, final Method method,
            final Session session,
            boolean isAlreadyInTransaction) {
        final MethodAvailability methodAvailability = getMethodAvailability(apiImpl, method);
        if (methodAvailability.isDeprecated) {
            logger.warn("The API method {}.{} is deprecated. It will be deleted in a future release. " +
                    "Please plan to update your code to use the replacement method instead. Check the Javadoc for more details.",
                    apiInterfaceName, method.getName());
        }
        if (!methodAvailability.isAvailableWhenPlatformIsStopped && !isNodeStarted()) {
            logger.error(
                    "Node not started. Method '{}. {}' cannot be called until node has been started (PlatformAPI.startNode()). Exact class: {}",
                    apiInterfaceName, method.getName(), method.getDeclaringClass().getName());
            throw new NodeNotStartedException();
        }
        // we don't check maintenance mode at platform level and when there is no session
        // when there is no session means that we are trying to log in, in this case it is the LoginApiExt that check if the user is the technical user
        // For tenant level method call:
        if (!(session instanceof APISession)) {
            return;
        }

        if (methodAvailability.isAvailableInMaintenanceMode
                && methodAvailability.isAvailableWhenMaintenanceModeIsDisabled) {
            //method can be called when maintenance is enabled or disabled.
            return;
        }
        boolean isMaintenanceModeEnabled = isMaintenanceModeEnabled(session, isAlreadyInTransaction);
        if (isMaintenanceModeEnabled && !methodAvailability.isAvailableInMaintenanceMode) {
            throw new TenantStatusException(
                    MessageFormat.format("Unable to call API method {0}.{1}, Maintenance mode is enabled.",
                            apiInterfaceName, method.getName()));
        }
        if (!isMaintenanceModeEnabled && !methodAvailability.isAvailableWhenMaintenanceModeIsDisabled) {
            throw new TenantStatusException(MessageFormat.format(
                    "Unable to call API method {0}.{1}, Maintenance mode is disabled and this method can only be called when Maintenance mode is enabled.",
                    apiInterfaceName, method.getName()));
        }
    }

    private static class MethodAvailability {

        boolean isDeprecated;
        boolean isAvailableWhenMaintenanceModeIsDisabled = true;
        boolean isAvailableInMaintenanceMode = true;
        boolean isAvailableWhenPlatformIsStopped;
    }

    private MethodAvailability getMethodAvailability(Object apiInstance, Method method) {
        AvailableInMaintenanceMode availableInMaintenanceMode = Optional
                .ofNullable(method.getAnnotation(AvailableInMaintenanceMode.class))
                .orElseGet(() -> apiInstance.getClass().getAnnotation(AvailableInMaintenanceMode.class));
        AvailableOnStoppedNode availableOnStoppedNode = method.getAnnotation(AvailableOnStoppedNode.class);
        MethodAvailability methodAvailability = new MethodAvailability();
        // Deprecation
        methodAvailability.isDeprecated = method.isAnnotationPresent(Deprecated.class);

        // Maintenance mode
        if (availableInMaintenanceMode == null) {
            methodAvailability.isAvailableInMaintenanceMode = false;
        } else if (availableInMaintenanceMode.onlyAvailableInMaintenanceMode()) {
            methodAvailability.isAvailableWhenMaintenanceModeIsDisabled = false;
        }
        // Platform status
        if (availableOnStoppedNode != null) {
            methodAvailability.isAvailableWhenPlatformIsStopped = true;
        }
        return methodAvailability;
    }

    /**
     * @param session
     *        the session to user
     * @param isAlreadyInTransaction
     *        if the request is made in a transaction
     * @return true if the maintenance mode is enabled, false otherwise
     */
    protected boolean isMaintenanceModeEnabled(final Session session, boolean isAlreadyInTransaction) {
        try {
            MaintenanceAPI maintenanceAPI = accessResolver
                    .getAPIImplementation(MaintenanceAPI.class);
            if (isAlreadyInTransaction) {
                return MaintenanceDetails.State.ENABLED
                        .equals(maintenanceAPI.getMaintenanceDetails().getMaintenanceState());
            } else {
                return selectUserTransactionService(session, getSessionType(session))
                        .executeInTransaction(() -> MaintenanceDetails.State.ENABLED
                                .equals(maintenanceAPI.getMaintenanceDetails().getMaintenanceState()));
            }
        } catch (final Throwable e) {
            throw new BonitaRuntimeException("Cannot determine if the Maintenance mode is enabled", e);
        }
    }

    /**
     * @return true if the node is started, false otherwise.
     */
    private boolean isNodeStarted() {
        try {
            return accessResolver.getAPIImplementation(PlatformAPI.class).isNodeStarted();
        } catch (final Throwable e) {
            return false;
        }
    }

    protected Object invokeAPIInTransaction(final Object[] parametersValues, final Object apiImpl, final Method method,
            final Session session,
            final String apiInterfaceName) throws Throwable {
        if (session == null) {
            throw new BonitaRuntimeException("session is null");
        }
        final UserTransactionService userTransactionService = selectUserTransactionService(session,
                getSessionType(session));

        return userTransactionService.executeInTransaction(() -> {
            try {
                checkMethodAccessibility(apiImpl, apiInterfaceName, method, session,
                        /* Already in a transaction */true);
                return invokeAPI(method, apiImpl, parametersValues);
            } catch (final Throwable cause) {
                throw new ServerAPIRuntimeException(cause);
            }
        });
    }

    UserTransactionService selectUserTransactionService(final Session session, final SessionType sessionType)
            throws BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException,
            ReflectiveOperationException {
        UserTransactionService transactionService;
        final ServiceAccessorFactory serviceAccessorFactory = getServiceAccessorFactoryInstance();
        final ServiceAccessor serviceAccessor = serviceAccessorFactory.createServiceAccessor();
        switch (sessionType) {
            case PLATFORM:
                transactionService = serviceAccessor.getTransactionService();
                break;
            case API:
                transactionService = serviceAccessor.getUserTransactionService();
                break;
            default:
                throw new InvalidSessionException("Unknown session type: " + session.getClass().getName());
        }
        return transactionService;
    }

    protected Object invokeAPI(final Method method, final Object apiImpl, final Object... parametersValues)
            throws Throwable {
        try {
            return method.invoke(apiImpl, parametersValues);
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Class<?>[] getParameterTypes(final List<String> classNameParameters) throws ClassNotFoundException {
        Class<?>[] parameterTypes = null;
        if (classNameParameters != null && !classNameParameters.isEmpty()) {
            parameterTypes = new Class<?>[classNameParameters.size()];
            for (int i = 0; i < parameterTypes.length; i++) {
                final String className = classNameParameters.get(i);
                Class<?> classType;
                if ("int".equals(className)) {
                    classType = int.class;
                } else if ("long".equals(className)) {
                    classType = long.class;
                } else if ("boolean".equals(className)) {
                    classType = boolean.class;
                } else {
                    classType = Class.forName(className);
                }
                parameterTypes[i] = classType;
            }
        }
        return parameterTypes;
    }

    private void checkTenantSession(final ServiceAccessor serviceAccessor, final Session session)
            throws SSchedulerException {
        final SchedulerService schedulerService = serviceAccessor.getSchedulerService();
        if (!schedulerService.isStarted()) {
            logger.debug("The scheduler is not started!");
        }
        final APISession apiSession = (APISession) session;
        final LoginService tenantLoginService = serviceAccessor.getLoginService();
        if (!tenantLoginService.isValid(apiSession.getId())) {
            throw new InvalidSessionException("Invalid session");
        }
    }

    private ClassLoader getTenantClassLoader(final ServiceAccessor serviceAccessor, final Session session)
            throws SClassLoaderException {
        final APISession apiSession = (APISession) session;
        final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
        return classLoaderService.getClassLoader(identifier(ScopeType.TENANT, apiSession.getTenantId()));
    }

    private ClassLoader getPlatformClassLoader(final ServiceAccessor serviceAccessor)
            throws SClassLoaderException {
        ClassLoader classLoader = null;
        PlatformState state = serviceAccessor.getPlatformManager().getState();
        if (state != PlatformState.STARTED) {
            // We do not retrieve the platform classloader when the platform is not yet started
            // It needs to have services to be started to retrieve it
            // Returning null will cause the context classloader to be left untouched
            logger.debug("Tried to retrieve platform classloader on a not started platform, state = {}", state);
            return null;
        }
        final PlatformService platformService = serviceAccessor.getPlatformService();
        // get the platform to put it in cache if needed
        if (!platformService.isPlatformCreated()) {
            try {
                serviceAccessor.getTransactionService().executeInTransaction((Callable<Void>) () -> {
                    platformService.getPlatform();
                    return null;
                });
            } catch (final Exception ignored) {
                // do not throw exceptions: it's just in case the platform was not in cache
            }
        }
        if (platformService.isPlatformCreated()) {
            final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
            classLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        }
        return classLoader;
    }

    protected enum SessionType {
        PLATFORM, API
    }

}
