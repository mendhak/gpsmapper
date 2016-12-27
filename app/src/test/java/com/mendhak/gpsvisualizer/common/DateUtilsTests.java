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

import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class DateUtilsTests {

    @Test
    public void testParseRfc1123DateTime() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DATE, 20);
        cal.set(Calendar.HOUR, 2);
        cal.set(Calendar.MINUTE, 37);
        cal.set(Calendar.SECOND, 14);
        Date expected = cal.getTime();

        assertThat("RFC 1123 Date Time matches",DateUtils.parseRfc1123DateTime("Sat, 20 Jun 2015 02:37:14 GMT"), is(expected) );
        assertThat("Invalid RFC 1123 date is null",  DateUtils.parseRfc1123DateTime("Sat, 20 Jun 2015 02:37:14"), is((Date)null));
        assertThat("Invalid RFC 1123 Date is null",DateUtils.parseRfc1123DateTime("Sat, 20 Jun 2015"), is((Date)null) );
    }

    @Test
    public void testFormatRfc1123DateTime() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DATE, 20);
        cal.set(Calendar.HOUR, 2);
        cal.set(Calendar.MINUTE, 37);
        cal.set(Calendar.SECOND, 14);
        Date date = cal.getTime();

        TimeZone tz = TimeZone.getTimeZone("GMT");
        assertThat("Date to RFC 1123 string",  DateUtils.formatRfc1123DateTime(date, tz), is("Sat, 20 Jun 2015 01:37:14 GMT"));
    }

    @Test
    public void testParseIso8601DateTime() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.YEAR, 1997);
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DATE, 16);
        cal.set(Calendar.HOUR, 18);
        cal.set(Calendar.MINUTE, 20);
        cal.set(Calendar.SECOND, 30);
        Date expected = cal.getTime();

        // dates without times or time zones are not supported
        assertThat("dates without times or time zones are not supported", DateUtils.parseIso8601DateTime("1997"), is((Date)null));
        assertThat("dates without times or time zones are not supported", DateUtils.parseIso8601DateTime("1997-07"), is((Date)null));
        assertThat("dates without times or time zones are not supported", DateUtils.parseIso8601DateTime("1997-07-16"), is((Date)null));

        // dates without seconds are not supported
        assertThat("dates without seconds are not supported", DateUtils.parseIso8601DateTime("1997-07-16T19:20+01:00"), is((Date)null));

        // dates with fractional seconds containing less that 3 digits are not supported
        assertThat("dates with fractional seconds containing less that 3 digits are not supported", DateUtils.parseIso8601DateTime("1997-07-16T19:20:30.45+01:00"), is((Date)null));

        assertThat("Valid ISO 8601 date time is parsed",   DateUtils.parseIso8601DateTime("1997-07-16T19:20:30+01:00") , is(expected));
        assertThat( "Valid ISO 8601 date time is parsed", DateUtils.parseIso8601DateTime("1997-07-16T18:20:30Z"), is(expected));

        cal.set(Calendar.MILLISECOND, 235);
        expected = cal.getTime();

        assertThat("Valid ISO 8601 date time is parsed", DateUtils.parseIso8601DateTime("1997-07-16T19:20:30.235+01:00"), is(expected));
        assertThat("Valid ISO 8601 date time is parsed", DateUtils.parseIso8601DateTime("1997-07-16T18:20:30.235Z"), is(expected));
    }

    @Test
    public void testFormatIso8601DateTime() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        cal.set(Calendar.YEAR, 1997);
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DATE, 16);
        cal.set(Calendar.HOUR, 19);
        cal.set(Calendar.MINUTE, 20);
        cal.set(Calendar.SECOND, 30);
        Date date = cal.getTime();

        TimeZone tz = TimeZone.getTimeZone("GMT");
        assertThat("ISO 8601 string from date",  DateUtils.formatIso8601DateTime(date, tz), is( "1997-07-16T18:20:30+00:00"));
    }
}
