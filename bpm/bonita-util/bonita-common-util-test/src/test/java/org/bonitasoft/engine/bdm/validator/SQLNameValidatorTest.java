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
package org.bonitasoft.engine.bdm.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bdm.validator.SQLNameValidator.Grammar;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain Bioteau
 */
public class SQLNameValidatorTest {

    private SQLNameValidator sqlNameValidator;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        sqlNameValidator = new SQLNameValidator();
    }

    @Test
    public void shouldIsValid_ReturnsTrue() {
        assertThat(sqlNameValidator.isValid("EMPLOYEE")).isTrue();
        assertThat(sqlNameValidator.isValid("employee")).isTrue();
        assertThat(sqlNameValidator.isValid("employee_#")).isTrue();
        assertThat(sqlNameValidator.isValid("name")).isTrue();
    }

    @Test
    public void shouldSQLKeyword_contains_links_word() {
        assertThat(sqlNameValidator.isSQLKeyword("LINKS"));
    }

    @Test
    public void should_index_beKeywordDiscouragedBy_SQL() {
        assertThat(sqlNameValidator.isKeywordDiscouragedBy("index")).containsExactly(Grammar.SQL);
    }

    @Test
    public void should_offset_beKeywordDiscouragedBy_H2_MySQL_Postgres() {
        // note that SqlServer has OFFSETS, not OFFSET...
        assertThat(sqlNameValidator.isKeywordDiscouragedBy("offset")).containsExactlyInAnyOrder(
                Grammar.H2, Grammar.MYSQL, Grammar.POSTGRES);
    }

    @Test
    public void shouldToString_beDefindeForEachGrammar() {
        for (var gram : Grammar.values()) {
            var toString = gram.toString();
            assertThat(toString).isNotEmpty();
            if (gram != Grammar.H2) {
                assertThat(toString).isNotEqualTo(gram.name());
            }
        }
    }

    @Test
    public void should_isKeywordDiscouragedBy_ReturnsEmpty() {
        assertThat(sqlNameValidator.isKeywordDiscouragedBy("employee")).isEmpty();
    }

    @Test
    public void shouldIsValid_ReturnsFalse() {
        assertThat(sqlNameValidator.isValid("E MPLOYEE")).isFalse();
        assertThat(sqlNameValidator.isValid("@employee")).isFalse();
        assertThat(sqlNameValidator.isValid("5employee")).isFalse();
        assertThat(sqlNameValidator.isValid("émployee")).isFalse();
        assertThat(sqlNameValidator.isValid("employee.name")).isFalse();
        assertThat(sqlNameValidator.isValid("limit")).isFalse();
        assertThat(sqlNameValidator.isValid("order")).isFalse();
        assertThat(sqlNameValidator.isValid("SCOPE")).isFalse();
    }

    @Test
    public void shouldIsValid_ReturnsFalse_if_too_long() {
        sqlNameValidator = new SQLNameValidator(30);
        assertThat(sqlNameValidator.isValid("IMTOOLONGANDVALIDATIONSHOULDFAILED")).isFalse();
        assertThat(sqlNameValidator.isValid("IMNOTTOOLONG")).isTrue();
    }

}
