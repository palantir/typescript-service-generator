/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator.utils;

public final class PathUtils {

    private PathUtils() {
        // no
    }

    public static String trimSlashes(String input) {
        if (input.startsWith("/")) {
            input = input.substring(1);
        }
        if (input.endsWith("/")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }
}
