/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of gpsvisualizer.
 *
 * gpsvisualizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * gpsvisualizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gpsvisualizer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.mendhak.gpsvisualizer.common;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

//From: https://gist.github.com/mraccola/702330625fad8eebe7d3

public final class DateUtils {

    // RFC 1123 constants
    private static final String RFC_1123_DATE_TIME = "EEE, dd MMM yyyy HH:mm:ss z";

    // ISO 8601 constants
    private static final String ISO_8601_PATTERN_1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String ISO_8601_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String[] SUPPORTED_ISO_8601_PATTERNS = new String[]{ISO_8601_PATTERN_1,
            ISO_8601_PATTERN_2};
    private static final int TICK_MARK_COUNT = 2;
    private static final int COLON_PREFIX_COUNT = "+00".length();
    private static final int COLON_INDEX = 22;

    /**
     * Parses a date from the specified RFC 1123-compliant string.
     *
     * @param string the string to parse
     * @return the {@link Date} resulting from the parsing, or null if the string could not be
     * parsed
     */
    public static Date parseRfc1123DateTime(String string) {
        try {
            return new SimpleDateFormat(RFC_1123_DATE_TIME, Locale.US).parse(string);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Formats the specified date to an RFC 1123-compliant string.
     *
     * @param date     the date to format
     * @param timeZone the {@link TimeZone} to use when formatting the date
     * @return the formatted string
     */
    public static String formatRfc1123DateTime(Date date, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(RFC_1123_DATE_TIME, Locale.US);
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        }
        return dateFormat.format(date);
    }

    /**
     * Parses a date from the specified ISO 8601-compliant string.
     *
     * @param string the string to parse
     * @return the {@link Date} resulting from the parsing, or null if the string could not be
     * parsed
     */
    public static Date parseIso8601DateTime(String string) {
        if (string == null) {
            return null;
        }
        String s = string.replace("Z", "+00:00");
        for (String pattern : SUPPORTED_ISO_8601_PATTERNS) {
            String str = s;
            int colonPosition = pattern.lastIndexOf('Z') - TICK_MARK_COUNT + COLON_PREFIX_COUNT;
            if (str.length() > colonPosition) {
                str = str.substring(0, colonPosition) + str.substring(colonPosition + 1);
            }
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(str);
            } catch (final ParseException e) {
                // try the next one
            }
        }
        return null;
    }

    /**
     * Formats the specified date to an ISO 8601-compliant string.
     *
     * @param date     the date to format
     * @param timeZone the {@link TimeZone} to use when formatting the date
     * @return the formatted string
     */
    public static String formatIso8601DateTime(Date date, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_PATTERN_1, Locale.US);
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        }
        String formatted = dateFormat.format(date);
        if (formatted != null && formatted.length() > COLON_INDEX) {
            formatted = formatted.substring(0, 22) + ":" + formatted.substring(22);
        }
        return formatted;
    }

    private DateUtils() {
        // hide constructor
    }
}
