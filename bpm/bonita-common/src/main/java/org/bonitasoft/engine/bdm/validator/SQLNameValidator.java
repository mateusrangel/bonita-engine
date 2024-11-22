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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Romain Bioteau
 */
public class SQLNameValidator {

    public static enum Grammar {

        SQL, H2, POSTGRES, ORACLE, MYSQL, SQLSERVER;

        @Override
        public String toString() {
            switch (this) {
                case SQL:
                    return "SQL grammar";
                case H2:
                    return "H2";
                case POSTGRES:
                    return "PostgreSQL";
                case ORACLE:
                    return "Oracle";
                case MYSQL:
                    return "MySQL";
                case SQLSERVER:
                    return "Microsoft SQL Server";
                default:
                    return name();
            }
        }
    }

    private final int maxLength;

    private static class KeywordsHolder {

        static Set<String> blockedDbKeywords = new HashSet<>();

        static Map<String, List<Grammar>> discouragedKeywords = new HashMap<>();

        static {
            // initialize keywords
            try (InputStream resourceAsStream = SQLNameValidator.class.getResourceAsStream("/blocked_db_keywords");
                    Scanner scanner = new Scanner(resourceAsStream)) {
                while (scanner.hasNext()) {
                    final String word = scanner.nextLine();
                    blockedDbKeywords.add(word.trim());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Grammar[] grammars = Grammar.values();
            for (Grammar gram : grammars) {
                var fileName = "/" + gram.name().toLowerCase() + "_permissive_keywords";
                try (InputStream resourceAsStream = SQLNameValidator.class.getResourceAsStream(fileName);
                        Scanner scanner = new Scanner(resourceAsStream)) {
                    while (scanner.hasNext()) {
                        final String word = scanner.nextLine();
                        discouragedKeywords.putIfAbsent(word, new ArrayList<Grammar>(grammars.length));
                        discouragedKeywords.get(word).add(gram);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public SQLNameValidator() {
        this(255);
    }

    public SQLNameValidator(final int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isValid(final String name) {
        return name.matches("[a-zA-Z][\\d\\w#@]{0," + maxLength + "}$") && !isSQLKeyword(name);
    }

    public boolean isSQLKeyword(final String name) {
        return KeywordsHolder.blockedDbKeywords.contains(name.toUpperCase());
    }

    /**
     * Check whether this name is a keyword discouraged by SQL or any specific DB vendor.
     *
     * @param name name to check
     * @return the grammars discouraging it (empty when not discouraged)
     */
    public List<Grammar> isKeywordDiscouragedBy(final String name) {
        return Collections.unmodifiableList(
                KeywordsHolder.discouragedKeywords.getOrDefault(name.toUpperCase(), Collections.emptyList()));
    }

}
