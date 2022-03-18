package frc.constants;

//TODO: Add constants
public final class Constant 
{

    // VISION   Constants
    public static final double VERTICAL_CAMERA_ANGLE_OF_VIEW = 35.0;

    public static final int targetCameraWidth = 160;
    public static final int targetCameraHeight = 120;

    public static final int intakeCameraWidth = 320;
    public static final int intakeCameraHeight = 240;

    // enter (x, y) coordinates x ascending order, must add at least 2 data points
    public static final double[][] pixelsToUnitsTable =
    {
        // pixels can vary from about 0 to targetCameraWidth (above)
        {0., 0.},
        {targetCameraWidth , targetCameraWidth}
    };

}