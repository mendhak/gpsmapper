/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPS Visualizer.
 *
 * GPS Visualizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPS Visualizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPS Visualizer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.mendhak.gpsvisualizer.common;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class UtilsTests {



    @Test
    public final void test_GetSpeedDisplay_HighSpeed_KilometersPerHours(){
        String speedDisplay = Utils.GetSpeedDisplay(14, false);
        assertThat("High speed is in km/h", speedDisplay, is("50.4 km/h"));
    }

    @Test
    public final void test_GetSpeedDisplay_LowSpeed_MetersPerSecond(){
        String speedDisplay = Utils.GetSpeedDisplay(0.1, false);
        assertThat("Low speed in m/s", speedDisplay, is("0.1 m/s"));
    }

    @Test
    public final void test_GetSpeedDisplay_Imperial_MilesPerHours(){
        String speedDisplay = Utils.GetSpeedDisplay(1, true);
        assertThat("Imperial in miles/h", speedDisplay, is("2.237 mi/h"));
    }

    @Test
    public final void test_GetDistanceDisplay_LargeDistance_Kilometers(){
        String distanceDisplay = Utils.GetDistanceDisplay(12174, false);

        assertThat("Large distance in km", distanceDisplay, is("12.174 km"));
    }

    @Test
    public final void test_GetDistanceDisplay_SmallDistance_Meters(){
        String distanceDisplay = Utils.GetDistanceDisplay(121, false);

        assertThat("Small distances in meters", distanceDisplay, is("121 m"));
    }

    @Test
    public final void test_GetDistanceDisplay_SmallDistanceImperial_Feet(){
        String distanceDisplay = Utils.GetDistanceDisplay(121, true);

        assertThat("Small distance imperial in feet", distanceDisplay, is("396.982 ft"));
    }


    @Test
    public final void test_GetDistanceDisplay_LargeDistanceImperial_Miles(){
        assertThat("Large imperial distances in miles", Utils.GetDistanceDisplay(12174, true), is("7.565 mi") );
        assertThat("Large imperial distances in miles",  Utils.GetDistanceDisplay(807, true), is("0.501 mi"));
    }

    @Test
    public final void test_GetTimeDisplay_SmallTimes_Seconds(){
        assertThat("Small times in seconds", Utils.GetTimeDisplay(4000), is("4 s"));
    }

    @Test
    public final void test_GetTimeDisplay_MediumTimes_Minutes(){
        assertThat("Medium times in minutes",  Utils.GetTimeDisplay(132000), is("2.2 min") );
    }

    @Test
    public final void test_GetTimeDisplay_LargeTimes_Hours(){
        assertThat("Large times in hours", Utils.GetTimeDisplay(6840000), is("1.9 hrs"));
    }

}
