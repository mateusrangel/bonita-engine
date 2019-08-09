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
package org.bonitasoft.engine.api.impl.projectdeployer.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileOperations.asInputStream;
import static org.bonitasoft.engine.api.impl.projectdeployer.validator.ArtifactValidatorFactory.artifactValidator;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.engine.api.impl.projectdeployer.ApplicationArchive;
import org.bonitasoft.engine.api.impl.projectdeployer.descriptor.DeploymentDescriptor;
import org.bonitasoft.engine.api.impl.projectdeployer.descriptor.DeploymentDescriptorReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactValidatorTest {

    private ArtifactValidator validator;
    private ApplicationArchive applicationArchive;

    @Before
    public void setup() throws Exception {
        validator = artifactValidator();
        applicationArchive = new ApplicationArchive();
    }

    @After
    public void after() throws Exception {
        if (applicationArchive != null) {
            applicationArchive.close();
        }
    }

    @Test
    public void should_compute_validation_status_of_an_application_archive() throws Exception {
        String deployDescriptorJson = "{" +
                " 'applications':[ { 'file' : 'apps/MyApp.xml' } ]" +
                " ,'bdmAccessControl': { 'file' : 'bdm/bdm_access_control.xml' }" +
                " ,'businessDataModel': { 'file' : 'bdm/bdm.zip' }" +
                " ,'layouts':[ { 'file' : 'layouts/layout.zip' } ]" +
                " ,'organization': { 'file' : 'organization/ACME.xml' }" +
                " ,'pages':[ { 'file' : 'pages/page-ExampleVacationManagement.zip' } ]" +
                " ,'processes':[ { 'file' : 'processes/New-Vacation-Request--1.0.bar' } ]" +
                " ,'profiles':[ { 'file' : 'organization/Profile_Data.xml' } ]" +
                " ,'restAPIExtensions':[ { 'file' :'restAPIExtensions/VacationRequestRestAPI-1.0.0-SNAPSHOT.zip' } ]" +
                " ,'themes':[ { 'file' : 'themes/custom-theme.zip' } ]" +
                "}";
        applicationArchive.setDeploymentDescriptor(deployDescriptorFromSingleQuoteJson(deployDescriptorJson));

        applicationArchive.addFile("apps/MyApp.xml", asInputStream("invalid application"));
        applicationArchive.addFile("bdm/bdm.zip", asInputStream("invalid business data model"));
        applicationArchive.addFile("bdm/bdm_access_control.xml", asInputStream("invalid access control"));
        applicationArchive.addFile("layouts/layout.zip", asInputStream("invalid layout"));
        applicationArchive.addFile("organization/ACME.xml", asInputStream("invalid organization"));
        applicationArchive.addFile("organization/Profile_Data.xml", asInputStream("invalid profiles"));
        applicationArchive.addFile("pages/page-ExampleVacationManagement.zip", asInputStream("invalid page"));
        applicationArchive.addFile("processes/New-Vacation-Request--1.0.bar", asInputStream("invalid process"));
        applicationArchive.addFile("restAPIExtensions/VacationRequestRestAPI-1.0.0-SNAPSHOT.zip",
                asInputStream("invalid rest api extension"));
        applicationArchive.addFile("themes/custom-theme.zip", asInputStream("invalid theme"));

        ValidationStatus validationStatus = validator.computeValidationStatus(applicationArchive);
        assertThat(validationStatus.isOK()).isFalse();
        assertThat(validationStatus.getChildren()).extracting("severity").containsOnly(3); // only errors
        assertThat(validationStatus.getChildren()).extracting("message").containsOnly(
                "The artifact 'ACME.xml' is not an artifact of type 'Organization'.",
                "The artifact 'Profile_Data.xml' is not an artifact of type 'Profile'.",
                "The artifact 'MyApp.xml' is not an artifact of type 'Application'.",
                "The artifact 'page-ExampleVacationManagement.zip' is not an artifact of type 'Page'.",
                "The artifact 'VacationRequestRestAPI-1.0.0-SNAPSHOT.zip' is not an artifact of type 'Rest API Extension'.",
                "The artifact 'layout.zip' is not an artifact of type 'Layout'.",
                "The artifact 'custom-theme.zip' is not an artifact of type 'Theme'.",
                "The artifact 'New-Vacation-Request--1.0.bar' is not an artifact of type 'Process'.",
                "The artifact 'bdm.zip' is not an artifact of type 'Business Data Model'.",
                "The artifact 'bdm_access_control.xml' is not an artifact of type 'BDM Access Control'.");
    }

    @Test
    public void should_compute_validation_status_of_an_application_archive_where_resources_do_not_exist()
            throws Exception {
        String deployDescriptorJson = "{" +
                " 'applications':[ { 'file' : 'apps/MyApp.xml' } ]" +
                " ,'bdmAccessControl': { 'file' : 'bdm/bdm_access_control.xml' }" +
                " ,'businessDataModel': { 'file' : 'bdm/bdm.zip' }" +
                " ,'layouts':[ { 'file' : 'layouts/layout.zip' } ]" +
                " ,'organization': { 'file' : 'organization/ACME.xml' }" +
                " ,'pages':[ { 'file' : 'pages/page-ExampleVacationManagement.zip' } ]" +
                " ,'processes':[ { 'file' : 'processes/New-Vacation-Request--1.0.bar' } ]" +
                " ,'profiles':[ { 'file' : 'organization/Profile_Data.xml' } ]" +
                " ,'restAPIExtensions':[ { 'file' :'restAPIExtensions/VacationRequestRestAPI-1.0.0-SNAPSHOT.zip' } ]" +
                " ,'themes':[ { 'file' : 'themes/custom-theme.zip' } ]" +
                "}";
        applicationArchive.setDeploymentDescriptor(deployDescriptorFromSingleQuoteJson(deployDescriptorJson));

        ValidationStatus validationStatus = validator.computeValidationStatus(applicationArchive);
        assertThat(validationStatus.isOK()).isFalse();
        assertThat(validationStatus.getChildren()).extracting("severity").containsOnly(ValidationStatus.ERROR);
        assertThat(validationStatus.getChildren()).extracting("message").containsOnly(
                "The file 'organization/ACME.xml' for artifact of type 'Organization' does not exist.",
                "The file 'organization/Profile_Data.xml' for artifact of type 'Profile' does not exist.",
                "The file 'apps/MyApp.xml' for artifact of type 'Application' does not exist.",
                "The file 'pages/page-ExampleVacationManagement.zip' for artifact of type 'Page' does not exist.",
                "The file 'restAPIExtensions/VacationRequestRestAPI-1.0.0-SNAPSHOT.zip' for artifact of type 'Rest API Extension' does not exist.",
                "The file 'layouts/layout.zip' for artifact of type 'Layout' does not exist.",
                "The file 'themes/custom-theme.zip' for artifact of type 'Theme' does not exist.",
                "The file 'processes/New-Vacation-Request--1.0.bar' for artifact of type 'Process' does not exist.",
                "The file 'bdm/bdm.zip' for artifact of type 'Business Data Model' does not exist.",
                "The file 'bdm/bdm_access_control.xml' for artifact of type 'BDM Access Control' does not exist.");
    }

    private static DeploymentDescriptor deployDescriptorFromSingleQuoteJson(String json) throws IOException {
        return new DeploymentDescriptorReader().fromJson(json.replaceAll("'", "\""));
    }

    @Test
    public void should_compute_organization_validation_status() {
        File organizationFile = getFile("/Organization_Data.xml");

        assertThat(validator.computeOrganizationValidationStatus(organizationFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computeBdmValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(organizationFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(organizationFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_profile_validation_status() {
        File profileFile = getFile("/Profile_Data.xml");

        assertThat(validator.computeProfileValidationStatus(profileFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computeBdmValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(profileFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(profileFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_application_validation_status() {
        File applicationFile = getFile("/application.xml");

        assertThat(validator.computeApplicationValidationStatus(applicationFile).isOK()).isTrue();

        assertThat(validator.computeBdmValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(applicationFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(applicationFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_page_validation_status() {
        File pageFile = getFile("/page.zip");

        assertThat(validator.computePageValidationStatus(pageFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeBdmValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(pageFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(pageFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_rest_api_extension_validation_status() {
        File restApiExtensionFile = getFile("/RestAPI-1.0.0.zip");

        assertThat(validator.computeRestApiExtensionValidationStatus(restApiExtensionFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computeBdmValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(restApiExtensionFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(restApiExtensionFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_process_validation_status() {
        File processFile = getFile("/CreateAndUpdateData--1.0.bar");

        assertThat(validator.computeProcessValidationStatus(processFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computeBdmValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(processFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(processFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_bdm_validation_status() {
        File bdmFile = getFile("/bdm.zip");

        assertThat(validator.computeBdmValidationStatus(bdmFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(bdmFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(bdmFile).isOK()).isFalse();

    }

    @Test
    public void should_compute_bdm_access_control_validation_status() {
        File bdmAccessControlFile = getFile("/bdm_access_control.xml");

        assertThat(validator.computeBdmAccessControlValidationStatus(bdmAccessControlFile).isOK()).isTrue();

        assertThat(validator.computeBdmValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computeApplicationValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(bdmAccessControlFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(bdmAccessControlFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_layout_validation_status() {
        File layoutFile = getFile("/layout.zip");

        assertThat(validator.computeLayoutValidationStatus(layoutFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computeBdmValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(layoutFile).isOK()).isFalse();
        assertThat(validator.computeThemeValidationStatus(layoutFile).isOK()).isFalse();
    }

    @Test
    public void should_compute_theme_validation_status() {
        File themeFile = getFile("/custom-theme.zip");

        assertThat(validator.computeThemeValidationStatus(themeFile).isOK()).isTrue();

        assertThat(validator.computeApplicationValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computeBdmValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computeBdmAccessControlValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computeLayoutValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computeOrganizationValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computePageValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computeProcessValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computeProfileValidationStatus(themeFile).isOK()).isFalse();
        assertThat(validator.computeRestApiExtensionValidationStatus(themeFile).isOK()).isFalse();
    }

    private File getFile(String name) {
        return new File(this.getClass().getResource(name).getFile());
    }

}
