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
package org.bonitasoft.engine.platform;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Test;

public class TenantManagementIT extends CommonBPMServicesTest {

    private final static String STATUS_DEACTIVATED = "DEACTIVATED";

    @After
    public void cleanUpOpenTransaction() {
        TestUtil.closeTransactionIfOpen(getTransactionService());
    }

    @Test
    public void tenantBuilderShouldBuildValidTenant() {
        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();
        final String description = "description";

        final STenant tenant = STenant.builder().name(name).createdBy(createdBy).created(created)
                .status(STATUS_DEACTIVATED).defaultTenant(false)
                .description(description).build();

        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getCreatedBy()).isEqualTo(createdBy);
        assertThat(tenant.getCreated()).isEqualTo(created);
        assertThat(tenant.getDescription()).isEqualTo(description);
    }

}
