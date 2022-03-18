package frc.vision;

public class VisionData 
{
    public static final TargetData targetData = new TargetData(); // others get data from here
}

   // Example of how to access the Vision targeting data that was sourced on RPi
   // Example of how to access the Vision targeting data that was sourced on RPi
   // Example of how to access the Vision targeting data that was sourced on RPi
   // Example of how to access the Vision targeting data that was sourced on RPi
   // Example of how to access the Vision targeting data that was sourced on RPi
   // Example of how to access the Vision targeting data that was sourced on RPi

    // Haven't tried these statements on the roboRIO but this may be it:
    
    // double[] targetData = new double[5];
    // NetworkTableInstance.getDefault()
    // .getEntry("/VisionData/targetInfo")
    // .getDoubleArray(targetData);

    // The data were created from
    // new double[]{
    //     nextTargetData.angleToTurn,
    //     nextTargetData.hubDistance,
    //     nextTargetData.isTargetFound? 1. : 0.,
    //     nextTargetData.isFreshData? 1. : 0.,
    //     (double)nextTargetData.frameNumber}
