/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.processor.util;

import java.util.regex.Pattern;

public class NamingKit {

    private final static Pattern VALID_NAME = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");
    private final static Pattern VALID_START = Pattern.compile("[_A-Za-z]");
    private final static Pattern VALID_CHAR = Pattern.compile("[_0-9A-Za-z]");

    /**
     * Graphql 3.x has valid names of [_A-Za-z][_0-9A-Za-z]* and hence Java generated names like Class$Inner wont work
     * so we make a unique name from it
     *
     * @param name the name to ensure
     *
     * @return a valid name
     */
    public static String toGraphqlName(String name) {
        if (VALID_NAME.matcher(name).matches()) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            String sChar = new String(new char[]{c});
            // check start character differently
            if (i == 0) {
                if (!VALID_START.matcher(sChar).matches()) {
                    replace(sb, c);
                } else {
                    sb.append(c);
                }
            } else {
                if (!VALID_CHAR.matcher(sChar).matches()) {
                    replace(sb, c);
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private static void replace(StringBuilder sb, char c) {
        // the most common in Java class land is . and $ so for readability we make them
        // just _
        if (c == '.' || c == '$') {
            sb.append('_');
        } else {
            sb.append("_");
            Integer iChar = (int) c;
            sb.append(iChar.toString());
            sb.append("_");
        }
    }
}
