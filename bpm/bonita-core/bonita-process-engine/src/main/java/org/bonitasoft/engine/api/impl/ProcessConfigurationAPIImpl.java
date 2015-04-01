/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.FormMappingNotFoundException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageURL;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.form.SearchFormMappings;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Baptiste Mesta
 */
public class ProcessConfigurationAPIImpl implements ProcessConfigurationAPI {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public SearchResult<FormMapping> searchFormMappings(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        FormMappingService formMappingService = tenantAccessor.getFormMappingService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchFormMappings searchFormMappings = new SearchFormMappings(formMappingService, searchEntitiesDescriptor.getSearchFormMappingDescriptor(),
                searchOptions);
        try {
            searchFormMappings.execute();
            return searchFormMappings.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public PageURL resolvePageURL(String key) throws NotFoundException {
        PageMappingService pageMappingService = getTenantAccessor().getPageMappingService();
        try {
            return ModelConvertor.toPageURL(pageMappingService.resolvePageURL(pageMappingService.get(key)));
        } catch (SObjectNotFoundException e) {
            throw new NotFoundException(e);
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    FormMapping getFormMapping(long processDefinitionId, FormMappingType type, String taskName) throws FormMappingNotFoundException {
        final FormMappingService formMappingService = getTenantAccessor().getFormMappingService();
        try {
            SFormMapping sFormMapping;
            if (taskName == null) {
                sFormMapping = formMappingService.get(processDefinitionId, type.getId());
            } else {
                sFormMapping = formMappingService.get(processDefinitionId, type.getId(), taskName);
            }
            return ModelConvertor.toFormMapping(sFormMapping);
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (SObjectNotFoundException e) {
            throw new FormMappingNotFoundException("Form mapping not found for " + type.name() + " on process " + processDefinitionId);
        }
    }

    @Override
    public void updateFormMapping(final long formMappingId, final String form, FormMappingTarget target) throws FormMappingNotFoundException, UpdateException {
        final FormMappingService formMappingService = getTenantAccessor().getFormMappingService();
        try {
            getTenantAccessor().getPageMappingService();
            SFormMapping sFormMapping = formMappingService.get(formMappingId);
            formMappingService.update(sFormMapping, target.name(), form);
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (SObjectNotFoundException e) {
            throw new FormMappingNotFoundException("Unable to find the form mapping with id " + formMappingId);
        } catch (SObjectModificationException e) {
            throw new UpdateException("Unable to update the form mapping " + formMappingId, e);
        }

    }
}
