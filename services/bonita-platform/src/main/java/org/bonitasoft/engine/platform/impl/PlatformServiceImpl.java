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
package org.bonitasoft.engine.platform.impl;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.*;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
@Slf4j
public class PlatformServiceImpl implements PlatformService {

    private static final String QUERY_GET_DEFAULT_TENANT = "getDefaultTenant";

    private final PersistenceService platformPersistenceService;
    private final SPlatformProperties sPlatformProperties;
    private final Recorder recorder;
    private final PlatformRetriever platformRetriever;

    private static Long defaultTenantId = null;

    public PlatformServiceImpl(final PersistenceService platformPersistenceService, PlatformRetriever platformRetriever,
            final Recorder recorder,
            final SPlatformProperties sPlatformProperties) {
        this.platformPersistenceService = platformPersistenceService;
        this.sPlatformProperties = sPlatformProperties;
        this.recorder = recorder;
        this.platformRetriever = platformRetriever;
    }

    @Override
    public SPlatform getPlatform() throws SPlatformNotFoundException {
        try {
            return platformRetriever.getPlatform();
        } catch (SPlatformNotFoundException e) {
            throw e;
        } catch (final Exception e) {
            throw new SPlatformNotFoundException("Unable to check if a platform already exists : " + e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public STenant getTenant(final long id) throws STenantNotFoundException {
        STenant tenant;
        try {
            tenant = platformPersistenceService.selectById(new SelectByIdDescriptor<>(STenant.class, id));
            if (tenant == null) {
                throw new STenantNotFoundException("No tenant found with id: " + id);
            }
        } catch (final Exception e) {
            throw new STenantNotFoundException("Unable to get the tenant : " + e.getMessage(), e);
        }
        return tenant;
    }

    // FIXME: Not necessary anymore, as platform is always created by ScriptExecutor at startup
    @Override
    public boolean isPlatformCreated() {
        try {
            getPlatform();
            return true;
        } catch (final SPlatformNotFoundException e) {
            return false;
        }
    }

    @Override
    public STenant getDefaultTenant() throws STenantNotFoundException {
        try {
            STenant tenant = platformPersistenceService
                    .selectOne(new SelectOneDescriptor<>(QUERY_GET_DEFAULT_TENANT, null, STenant.class));
            if (tenant == null) {
                throw new STenantNotFoundException("No default tenant found");
            }
            return tenant;
        } catch (final SBonitaReadException e) {
            throw new STenantNotFoundException("Unable to check if a default tenant already exists: " + e.getMessage(),
                    e);
        }
    }

    @Override
    public long getDefaultTenantId() throws STenantNotFoundException {
        if (defaultTenantId == null) {
            try {
                defaultTenantId = platformPersistenceService
                        .selectOne(new SelectOneDescriptor<>("getDefaultTenantId", null, STenant.class));
            } catch (final SBonitaReadException e) {
                throw new STenantNotFoundException("Unable to retrieve default tenant id: " + e.getMessage(), e);
            }
        }
        return defaultTenantId;
    }

    @Override
    public void activateTenant(final long tenantId) throws STenantNotFoundException, STenantActivationException {
        final STenant tenant = getDefaultTenant();
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(STenant.STATUS, STenant.ACTIVATED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new STenantActivationException("Problem while activating tenant: " + tenant, e);
        }
    }

    @Override
    public void deactivateTenant(final long tenantId) throws STenantNotFoundException, STenantDeactivationException {
        final STenant tenant = getDefaultTenant();
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(STenant.STATUS, STenant.DEACTIVATED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new STenantDeactivationException("Problem while deactivating tenant: " + tenant, e);
        }
    }

    @Override
    public void pauseTenant(long tenantId) throws STenantUpdateException, STenantNotFoundException {
        final STenant tenant = getDefaultTenant();
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(STenant.STATUS, STenant.PAUSED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new STenantUpdateException("Unable to update tenant status in database.", e);
        }
    }

    @Override
    public SPlatformProperties getSPlatformProperties() {
        return sPlatformProperties;
    }

    @Override
    public void updatePlatform(final EntityUpdateDescriptor descriptor) throws SPlatformUpdateException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(getPlatform(), descriptor), PLATFORM);
        } catch (final SRecorderException | SPlatformNotFoundException e) {
            throw new SPlatformUpdateException("Problem while updating platform: ", e);
        }
    }

}
