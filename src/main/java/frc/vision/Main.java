package frc.vision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;

public final class Main {
    static { // sleep needed for early version of this code in 2020. Linux or JVM allowed
             // this program to start before
             // Linux or JVM was fully functional to run correctly the threads spawned by
             // this program
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {throw new RuntimeException(e);
        }
    }
    static { // logger hasn't started yet so use system out and err
        System.out.println("Starting class: " + MethodHandles.lookup().lookupClass().getCanonicalName());
        System.err.println("Starting class: " + MethodHandles.lookup().lookupClass().getCanonicalName());
    }
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    static {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {throw new RuntimeException(e);
        }
    }

    static final String version = "RPi Vision 3/19/2022"; // change this every time  

    // if running without a roboRIO and want to see ShuffleBoard then say true.
    // then a ton of error messages about NT don't print and you can see your own prints.
    // if using roboRIO, then it provides the NT server so say false
    static final boolean startNTserver = false;

    // restart vision if any watchdog tripped; Exit - should restart itself by WPILib server stuff
    static Watchdog watchdog = new Watchdog(8., () -> {
        var watchdog = "Watchdog barked - exiting";
        System.out.println(watchdog);
        System.exit(1);
    });

    private static String checkThrottled() {
        try {
            // execute command to check for Raspberry Pi throttled
            List<String> command = new ArrayList<String>(); // build my command as a list of strings
            command.add("bash");
            command.add("-c");
            command.add("vcgencmd get_throttled");

            ProcessBuilder pb1 = new ProcessBuilder(command);
            Process process1 = pb1.start();
            int errCode1 = process1.waitFor();
            command.clear();

            String checkOutput = output(process1.getInputStream());

            if (errCode1 != 0) {
                var commandError = "get_throttled errors: " + output(process1.getErrorStream());
                System.out.println(commandError);
            }

            checkOutput = checkOutput.substring(0, checkOutput.length() - 1); // a crlf of sorts at the end to remove

            if (checkOutput.equals("throttled=0x0")) {
                checkOutput = "OK";
            } else {
                System.out.println("RPi " + checkOutput);
                // interpret the bits since it's throttled
                String hexThrottleCode = checkOutput.substring(12); // just the throttle code after the "throttled=0x"
                int intThrottleCode = Integer.parseInt(hexThrottleCode, 16); // no 0x at the beginning
                // int intThrottleCode = Integer.decode(hexThrottleCode); works to parse with
                // the 0x first but it's slow
                // valueOf(hexThrottleCode, 16) also works similarly returning an Integer

                // throttled return code
                // 0: under-voltage 0x00000001
                // 1: arm frequency capped 0x00000002
                // 2: currently throttled 0x00000004
                // 16: under-voltage has occurred 0x00010000
                // 17: arm frequency capped has occurred 0x00020000
                // 18: throttling has occurred 0x00040000

                int under_voltage = 0x00000001;
                int arm_frequency_capped = 0x00000002;
                int currently_throttled = 0x00000004;
                int under_voltage_has_occurred = 0x00010000;
                int arm_frequency_capped_has_occurred = 0x00020000;
                int throttling_has_occurred = 0x00040000;
                // most interested in under-voltage so give those errors precedence for operator
                // display
                if ((intThrottleCode & under_voltage_has_occurred) != 0) {
                    System.out.println("under-voltage has occurred;");
                    checkOutput = "under-voltage has occurred";
                }
                if ((intThrottleCode & under_voltage) != 0) {
                    System.out.println("under-voltage;");
                    checkOutput = "under-voltage";
                }
                if ((intThrottleCode & arm_frequency_capped) != 0)
                    System.out.println("arm frequency capped;");
                if ((intThrottleCode & currently_throttled) != 0)
                    System.out.println("currently throttled;");
                if ((intThrottleCode & arm_frequency_capped_has_occurred) != 0)
                    System.out.println("arm frequency capped has occurred;");
                if ((intThrottleCode & throttling_has_occurred) != 0)
                    System.out.println("throttling has occurred;");
            }

            return checkOutput;
        } catch (Exception e) {
            System.out.println("Error in checkThrottled process " + e);
            return "error";
        }
    }
    
    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }

        return sb.toString();
    }

    public static void main(String... args) {
  
        Thread.currentThread().setName("4237Main");
  
        // start NetworkTables
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
        if (startNTserver) 
        {
            System.out.println("Setting up NetworkTables server. Don't request this if using a roboRIO!");
            ntinst.startServer();
        } 
        else 
        {
            System.out.println("Setting up NetworkTables client for team " + 4237);
            ntinst.startClientTeam(4237);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Vision vision = new Vision();

        NetworkTableEntry RPiThrottle;

        synchronized(Vision.tabLock)
        {
        RPiThrottle =
            Vision.cameraTab.add("RPi Throttle", "not throttled")
            .withSize(4, 2)
            .withPosition(25, 14)
            .getEntry();
        
        Shuffleboard.update();
        }

        // Everything should be running by now so set loose the watchdogs to make sure.
        // This assumes main can make to here - there is no watchdog on main but there isn't much looping before here.

        watchdog.enable();

        // loop forever to keep child threads alive, show a heart beat, get camera calibration, and
        // do garbage collection that may be required to free large OpenCV objects
        while(true)
        {
            try 
            {
        
                System.out.println("Program Version " + version + ", current time " + java.time.LocalDateTime.now());
 
                vision.getCalibration(); // get the camera calibration from the Shuffleboard

                RPiThrottle.setString(checkThrottled()); // display on Shuffleboard any throttled message

                System.gc();

                if (startNTserver) 
                {
                    System.out.println("NetworkTables server on RPi. Don't do this if using a roboRIO!");
                }

                Thread.sleep(5000);
            }
            catch (InterruptedException e) 
            {
                throw new RuntimeException(e);
            }
        }
    }
}

// "c:\Program Files\PuTTY\pscp.exe" -v  pi@wpilibpi.local:/home/pi/logs/VisionErrors.txt saveit.txt

/*
There's a couple different ways to do this:

Option 1: Instead of using Uploaded Java jar as the application type,
 select custom and upload your .jar via the File Upload section. This
  will copy the file to the pi without overwriting the runCamera script.
   Then you can change the runCamera script to whatever you want; if you
    remove the ### TYPE: line from runCamera, the webserver will know it's
     a custom script and leave it as custom.

Option 2: Edit /service/camera/run to run a different script than runCamera.
 This will allow you to completely bypass runCamera. The webserver will still
  overwrite runCamera but you can put your own custom script under a different filename.

*/
