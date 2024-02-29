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
package org.bonitasoft.engine.api.impl.resolver;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;
import org.bonitasoft.engine.bpm.process.Problem;

/**
 * {@link Problem} specific assertions - Generated by CustomAssertionGenerator.
 */
public class ProblemAssert extends AbstractAssert<ProblemAssert, Problem> {

    /**
     * Creates a new <code>{@link ProblemAssert}</code> to make assertions on actual Problem.
     *
     * @param actual the Problem we want to make assertions on.
     */
    public ProblemAssert(Problem actual) {
        super(actual, ProblemAssert.class);
    }

    /**
     * An entry point for ProblemAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myProblem)</code> and get specific assertion with
     * code completion.
     *
     * @param actual the Problem we want to make assertions on.
     * @return a new <code>{@link ProblemAssert}</code>
     */
    public static ProblemAssert assertThat(Problem actual) {
        return new ProblemAssert(actual);
    }

    /**
     * Verifies that the actual Problem's description is equal to the given one.
     *
     * @param description the given description to compare the actual Problem's description to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Problem's description is not equal to the given one.
     */
    public ProblemAssert hasDescription(String description) {
        // check that actual Problem we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected description of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualDescription = actual.getDescription();
        if (!Objects.areEqual(actualDescription, description)) {
            failWithMessage(assertjErrorMessage, actual, description, actualDescription);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Problem's level is equal to the given one.
     *
     * @param level the given level to compare the actual Problem's level to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Problem's level is not equal to the given one.
     */
    public ProblemAssert hasLevel(Problem.Level level) {
        // check that actual Problem we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected level of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        Problem.Level actualLevel = actual.getLevel();
        if (!Objects.areEqual(actualLevel, level)) {
            failWithMessage(assertjErrorMessage, actual, level, actualLevel);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Problem's resource is equal to the given one.
     *
     * @param resource the given resource to compare the actual Problem's resource to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Problem's resource is not equal to the given one.
     */
    public ProblemAssert hasResource(String resource) {
        // check that actual Problem we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected resource of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualResource = actual.getResource();
        if (!Objects.areEqual(actualResource, resource)) {
            failWithMessage(assertjErrorMessage, actual, resource, actualResource);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Problem's resourceId is equal to the given one.
     *
     * @param resourceId the given resourceId to compare the actual Problem's resourceId to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Problem's resourceId is not equal to the given one.
     */
    public ProblemAssert hasResourceId(String resourceId) {
        // check that actual Problem we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected resourceId of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualResourceId = actual.getResourceId();
        if (!Objects.areEqual(actualResourceId, resourceId)) {
            failWithMessage(assertjErrorMessage, actual, resourceId, actualResourceId);
        }

        // return the current assertion for method chaining
        return this;
    }

}
