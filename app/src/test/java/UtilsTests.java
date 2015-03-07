
import com.mendhak.gpsvisualizer.common.Utils;
import junit.framework.TestCase;

public class UtilsTests  extends TestCase {

    public UtilsTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void test_GetSpeedDisplay_HighSpeed_KilometersPerHours(){

        String speedDisplay = Utils.GetSpeedDisplay(14, false);

        assertEquals("50.4 km/h",speedDisplay);
    }

    public final void test_GetSpeedDisplay_LowSpeed_MetersPerSecond(){
        String speedDisplay = Utils.GetSpeedDisplay(0.1, false);

        assertEquals("0.1 m/s",speedDisplay);
    }

    public final void test_GetSpeedDisplay_Imperial_MilesPerHours(){

        String speedDisplay = Utils.GetSpeedDisplay(1, true);

        assertEquals("2.237 mi/h",speedDisplay);
    }

    public final void test_GetDistanceDisplay_LargeDistance_Kilometers(){
        String distanceDisplay = Utils.GetDistanceDisplay(12174, false);

        assertEquals("12.174 km", distanceDisplay);
    }

    public final void test_GetDistanceDisplay_SmallDistance_Meters(){
        String distanceDisplay = Utils.GetDistanceDisplay(121, false);

        assertEquals("121 m", distanceDisplay);
    }

    public final void test_GetDistanceDisplay_SmallDistanceImperial_Feet(){
        String distanceDisplay = Utils.GetDistanceDisplay(121, true);

        assertEquals("396.982 ft", distanceDisplay);
    }


    public final void test_GetDistanceDisplay_LargeDistanceImperial_Miles(){
        assertEquals("7.565 mi", Utils.GetDistanceDisplay(12174, true));
        assertEquals("0.501 mi", Utils.GetDistanceDisplay(807, true));
    }

    public final void test_GetTimeDisplay_SmallTimes_Seconds(){
        assertEquals("4 s", Utils.GetTimeDisplay(4000));
    }

    public final void test_GetTimeDisplay_MediumTimes_Minutes(){
        assertEquals("2.2 min", Utils.GetTimeDisplay(132000));
    }

    public final void test_GetTimeDisplay_LargeTimes_Hours(){
        assertEquals("1.9 hrs", Utils.GetTimeDisplay(6840000));
    }

}
