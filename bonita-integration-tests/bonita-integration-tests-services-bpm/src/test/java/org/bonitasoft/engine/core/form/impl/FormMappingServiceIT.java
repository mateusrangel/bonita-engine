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
 */
package org.bonitasoft.engine.core.form.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.CommonTestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class FormMappingServiceIT extends CommonBPMServicesTest {

    private final SessionService sessionService;
    private final SessionAccessor sessionAccessor;
    private final PageService pageService;
    public FormMappingService formMappingService;

    private final BPMServicesBuilder servicesBuilder;

    private final TransactionService transactionService;
    public static final String PAGE_NAME = "custompage_coucou";
    private SPage page;


    @Before
    public void setup() throws Exception {
        transactionService.begin();
        page = pageService.addPage(
                CommonTestUtil.createTestPageContent(PAGE_NAME, "coucou depuis la page", "C'était juste pour dire coucou"), "mySuperPage.zip",
                54L);
        transactionService.complete();
    }

    @After
    public void tearDown() throws Exception {
        transactionService.begin();
        for (SFormMapping sFormMapping : formMappingService.list(0, 1000)) {
            formMappingService.delete(sFormMapping);
        }
        pageService.deletePage(page.getId());
        transactionService.complete();
    }

    public FormMappingServiceIT() {
        super();
        servicesBuilder = getServicesBuilder();
        transactionService = servicesBuilder.getTransactionService();
        formMappingService = servicesBuilder.getFormMappingService();
        pageService = servicesBuilder.getPageService();
        sessionService = servicesBuilder.getSessionService();
        sessionAccessor = servicesBuilder.getSessionAccessor();
    }

    @Test
    public void createAndListFormMapping() throws Exception {
        transactionService.begin();

        formMappingService.create(15l, "step1", FormMappingType.TASK.getId(), "INTERNAL", PAGE_NAME);
        formMappingService.create(15l, null, FormMappingType.PROCESS_START.getId(), "URL", "http://bit.coin");
        formMappingService.create(15l, null, FormMappingType.PROCESS_OVERVIEW.getId(), "LEGACY", null);
        formMappingService.create(16l, null, FormMappingType.PROCESS_OVERVIEW.getId(), null, null);
        transactionService.complete();

        transactionService.begin();
        List<SFormMapping> list = formMappingService.list(15l, 0, 10);
        List<SFormMapping> listAll = formMappingService.list(0, 10);

        transactionService.complete();
        assertThat(list).extracting("type").containsExactly(FormMappingType.TASK.getId(), FormMappingType.PROCESS_START.getId(),
                FormMappingType.PROCESS_OVERVIEW.getId());
        assertThat(list).extracting("task").containsExactly("step1", null, null);
        assertThat(list).extracting("pageMapping.url").containsExactly(null, "http://bit.coin", null);
        assertThat(list).extracting("pageMapping.urlAdapter").containsExactly(null, null, "LegacyURLAdapter");
        assertThat(list).extracting("pageMapping.pageId").containsExactly(page.getId(), null, null);
        //        assertThat(list).extracting("pageMapping.key").containsExactly();
        assertThat(listAll).extracting("type").containsExactly(FormMappingType.TASK.getId(), FormMappingType.PROCESS_START.getId(),
                FormMappingType.PROCESS_OVERVIEW.getId(), FormMappingType.PROCESS_OVERVIEW.getId());
        assertThat(listAll).extracting("processDefinitionId").containsExactly(15l, 15l, 15l, 16l);
    }

    @Test
    public void create_and_get_FormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", FormMappingType.TASK.getId(), "URL", "http://bit.coin");
        transactionService.complete();

        transactionService.begin();
        SFormMapping sFormMapping = formMappingService.get(taskForm.getId());
        SFormMapping sFormMappingByProperties = formMappingService.get(15l, FormMappingType.TASK.getId(), "step1");
        transactionService.complete();
        assertThat(sFormMapping).isEqualTo(taskForm).isEqualTo(sFormMappingByProperties);
    }

    @Test
    public void create_and_get_FormMapping_with_no_task() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, null, FormMappingType.TASK.getId(), "URL", "http://bit.coin");
        transactionService.complete();

        transactionService.begin();
        SFormMapping sFormMapping = formMappingService.get(taskForm.getId());
        SFormMapping sFormMappingByProperties = formMappingService.get(15l, FormMappingType.TASK.getId());
        transactionService.complete();
        assertThat(sFormMapping).isEqualTo(taskForm);
        assertThat(sFormMappingByProperties).isEqualTo(sFormMapping);
    }

    @Test
    public void delete_FormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", FormMappingType.TASK.getId(), "URL", "http://bit.coin");
        transactionService.complete();

        transactionService.begin();
        formMappingService.delete(formMappingService.get(taskForm.getId()));
        transactionService.complete();

        transactionService.begin();
        try {
            formMappingService.get(taskForm.getId());
            fail("should have thrown a not found Exception");
        } catch (SObjectNotFoundException e) {
            //ok
        }
        transactionService.complete();
    }

    @Test
    public void update_FormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", FormMappingType.TASK.getId(), "URL", "http://bit.coin");
        transactionService.complete();

        transactionService.begin();
        SFormMapping sFormMapping = formMappingService.get(taskForm.getId());
        formMappingService.update(sFormMapping, SFormMapping.TARGET_URL, "newFormName");
        transactionService.complete();

        transactionService.begin();
        SFormMapping updatedInDatabase = formMappingService.get(taskForm.getId());
        transactionService.complete();

        assertThat(sFormMapping).isEqualTo(updatedInDatabase);
        assertThat(updatedInDatabase.getPageMapping().getUrl()).isEqualTo("newFormName");
        assertThat(updatedInDatabase.getTarget()).isEqualTo(FormMappingTarget.URL.name());
        assertThat(updatedInDatabase.getLastUpdateDate()).isGreaterThan(taskForm.getLastUpdateDate());

        SSession john = sessionService.createSession(1, 12, "john", false);
        sessionAccessor.setSessionInfo(john.getId(), 1);

        transactionService.begin();
        SFormMapping reupdated = formMappingService.get(taskForm.getId());
        formMappingService.update(reupdated, SFormMapping.TARGET_INTERNAL, PAGE_NAME);
        transactionService.complete();

        assertThat(reupdated.getPageMapping().getUrl()).isNull();
        assertThat(reupdated.getPageMapping().getPageId()).isEqualTo(page.getId());
        assertThat(reupdated.getTarget()).isEqualTo(FormMappingTarget.INTERNAL.name());
        assertThat(reupdated.getLastUpdateDate()).isGreaterThan(updatedInDatabase.getLastUpdateDate());
        assertThat(reupdated.getLastUpdatedBy()).isEqualTo(12);

    }
}
