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
package org.bonitasoft.engine.tracking;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

/**
 * {@link Record} specific assertions - Generated by CustomAssertionGenerator.
 */
public class RecordAssert extends AbstractAssert<RecordAssert, Record> {

    /**
     * Creates a new <code>{@link RecordAssert}</code> to make assertions on actual Record.
     *
     * @param actual the Record we want to make assertions on.
     */
    public RecordAssert(final Record actual) {
        super(actual, RecordAssert.class);
    }

    /**
     * An entry point for RecordAssert to follow AssertJ standard <code>assertThat()</code>
     * statements.<br>
     * With a static import, one can write directly: <code>assertThat(myRecord)</code> and get
     * specific assertion with code completion.
     *
     * @param actual the Record we want to make assertions on.
     * @return a new <code>{@link RecordAssert}</code>
     */
    public static RecordAssert assertThat(final Record actual) {
        return new RecordAssert(actual);
    }

    /**
     * Verifies that the actual Record's description is equal to the given one.
     *
     * @param description the given description to compare the actual Record's description to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Record's description is not equal to the given one.
     */
    public RecordAssert hasDescription(final String description) {
        // check that actual Record we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        final String assertjErrorMessage = "\nExpecting description of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        final String actualDescription = this.actual.getDescription();
        if (!Objects.areEqual(actualDescription, description)) {
            failWithMessage(assertjErrorMessage, this.actual, description, actualDescription);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Record's duration is equal to the given one.
     *
     * @param duration the given duration to compare the actual Record's duration to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Record's duration is not equal to the given one.
     */
    public RecordAssert hasDuration(final long duration) {
        // check that actual Record we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        final String assertjErrorMessage = "\nExpecting duration of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // check
        final long actualDuration = this.actual.getDuration();
        if (actualDuration != duration) {
            failWithMessage(assertjErrorMessage, this.actual, duration, actualDuration);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Record's name is equal to the given one.
     *
     * @param name the given name to compare the actual Record's name to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Record's name is not equal to the given one.
     */
    public RecordAssert hasName(final TimeTrackerRecords name) {
        // check that actual Record we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        final String assertjErrorMessage = "\nExpecting name of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        final TimeTrackerRecords actualName = this.actual.getName();
        if (!Objects.areEqual(actualName, name)) {
            failWithMessage(assertjErrorMessage, this.actual, name, actualName);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Record's timestamp is equal to the given one.
     *
     * @param timestamp the given timestamp to compare the actual Record's timestamp to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Record's timestamp is not equal to the given one.
     */
    public RecordAssert hasTimestamp(final long timestamp) {
        // check that actual Record we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        final String assertjErrorMessage = "\nExpecting timestamp of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // check
        final long actualTimestamp = this.actual.getTimestamp();
        if (actualTimestamp != timestamp) {
            failWithMessage(assertjErrorMessage, this.actual, timestamp, actualTimestamp);
        }

        // return the current assertion for method chaining
        return this;
    }

}
