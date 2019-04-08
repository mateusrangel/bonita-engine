package org.bonitasoft.engine.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class TestsPlugin implements Plugin<Project>{


    public static final String TEST_JVM = "test.jvm"

    @Override
    void apply(Project project) {

        def tests = project.extensions.create("tests", TestsExtension)

        Test integrationTest = project.tasks.create("integrationTest", Test)


        project.afterEvaluate {

            setTestJVM(project, integrationTest)
            setJvmArgs(project, integrationTest)
            if (tests.integrationTestsSuite) {
                integrationTest.include(tests.integrationTestsSuite)
            } else {
                integrationTest.include(tests.integrationTestsPattern)
            }
            integrationTest.systemProperty("bonita.version", project.version)

            if (tests.slowTestsSuite) {
                Test slowTest = project.tasks.create("slowTest", Test)
                slowTest.include(tests.slowTestsSuite)
                setTestJVM(project, slowTest)
                setJvmArgs(project, slowTest)
                slowTest.systemProperty("bonita.version", project.version)
            }


            Test testTask = project.tasks.getByName("test") as Test
            if (testTask) {
                setJvmArgs(project, testTask)
                testTask.include(tests.testPattern)
            }

        }
    }

    private void setJvmArgs(Project project, Test task) {
        def property = project.property('org.gradle.jvmargs')
        if (property) {
            task.jvmArgs property.toString().split(" ")
        }
        def sysProperty = System.getProperty("org.gradle.jvmargs")
        if (sysProperty) {
            task.jvmArgs sysProperty.split(" ")
        }
    }

    private void setTestJVM(Project project, Test task) {
        if (project.hasProperty(TEST_JVM)) {
            def alternateJvm = project.property(TEST_JVM)
            project.logger.info("Parameter '$TEST_JVM' detected. ${project.name} will use alternate JVM '$alternateJvm' to run $task")
            task.executable = alternateJvm
        }
    }

}