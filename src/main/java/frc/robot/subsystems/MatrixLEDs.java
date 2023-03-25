package frc.robot.subsystems;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;
import frc.robot.utility.LEDMatrix;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;


public class MatrixLEDs extends SubsystemBase {
  public enum LEDMode {
    OFF,
    DISCONNECTED,
    DISABLED,
    CONE,
    CUBE,
    RAINBOW,
    RAVE
  }

  private LEDMode currentMode = LEDMode.OFF;

  private final LEDMatrix leds;
  private static final int numRows = 16;
  private static final int numCols = 16;

  private BullLogger stringLogger;
  private BullLogger intLogger;

  // See https://github.com/STMARobotics/frc-7028-2023/blob/main/src/main/java/frc/robot/subsystems/LEDSubsystem.java
  private final AtomicReference<Consumer<LEDMatrix>> methodConsumer = new AtomicReference<Consumer<LEDMatrix>>(null);
  // This Notifier acts in place of periodic, so updating the buffer will happen on a seperate thread.
  private final Notifier periodicThread;

  public MatrixLEDs() {
    this(9); // Default PWM port 9
  }

  public MatrixLEDs(int port) {
    leds = new LEDMatrix(port, numRows, numCols);

    periodicThread = new Notifier(() ->  {
      Consumer<LEDMatrix> value = methodConsumer.get();
      if (value != null) {
        // Call the consumer to update the LEDs on this notifier thread
        value.accept(leds);
      }
    });
    periodicThread.setName("LED Matrix");

    try {
      // set up a string logger
      stringLogger = new BullLogger("stringSetLEDs", true, false);

      stringLogger.setLogType(BullLogger.LogType.STRING);
      stringLogger.setLogLevel(BullLogger.LogLevel.DEBUG);

      // set up an int logger
      intLogger = new BullLogger("intSetLEDs", true, true);

      intLogger.setLogType(BullLogger.LogType.INT);

      intLogger.setLogLevel(BullLogger.LogLevel.DEBUG);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Sets the LED matrix to the given hue.
   * @param matrix the matrix controlling what pixels are changed
   */
  public void setCustomMatrix(Mat matrix) {
    currentMode = null;
    methodConsumer.set((leds) -> matrix(leds, matrix));
  }

  public void setMode(LEDMode mode) {
    if (mode != currentMode) {
      currentMode = mode;
      switch (currentMode) {
        case OFF:
          methodConsumer.set((leds) -> off(leds));
          break;
        case DISCONNECTED:
          methodConsumer.set((leds) -> disconnected(leds));
          break;
        case DISABLED:
          methodConsumer.set(
            (leds) -> leds.alternate(2, () -> {
              setAll(leds, 60);
            }, () -> {
              setAll(leds, 110);
            })
          );
          break;
        case CONE:
          // Not sure how hsv works tbh, everything I find online is in the range [0,360), not [0,180).
          // This is supposed to be yellow, I don't know if it will be.
          methodConsumer.set((leds) -> setAllOnce(leds, 30));
          break;
        case CUBE:
          methodConsumer.set((leds) -> setAllOnce(leds, 140)); // Purple?
          break;
        case RAINBOW:
          methodConsumer.set((leds) -> rainbow(leds));
          break;
        case RAVE:
          methodConsumer.set((leds) -> rainbowParty(leds));
      }
    }
  }

  /**
   * Sets all the LEDs to the given color then clears the consumer
   */
  private void setAllOnce(LEDMatrix leds, int hue) {
    leds.allOneColor(hue);
    leds.updateLEDs();
    methodConsumer.set(null);
  }

  /**
   * Sets all the LEDs to the given color
   */
  private void setAll(LEDMatrix leds, int hue) {
    leds.allOneColor(hue);
    leds.updateLEDs();
  }

  /**
   * Turns off all the LEDs then clears the consumer
   */
  private void off(LEDMatrix leds) {
    leds.off();
    leds.updateLEDs();
    methodConsumer.set(null);
  }

  /**
   * Shows the animation for when the driverstation is disconnected
   */
  private void disconnected(LEDMatrix leds) {
    // TODO: Dino :)
    leds.flash(1,
      () -> leds.allOneColor(0)
    );
    leds.updateLEDs();
  }

  /**
   * Plays a rainbow animation on the LEDs
   */
  private void rainbow(LEDMatrix leds) {
    leds.rainbow();
    leds.updateLEDs();
    // We don't reset the bufferConsumer because the rainbow should keep updating.
  }

  /**
   * Plays a weird rainbow animation on the LEDs
   */
  private void rainbowParty(LEDMatrix leds) {
    leds.rainbowParty();
    leds.updateLEDs();
    // We don't reset the bufferConsumer because the rainbow should keep updating.
  }

  /**
   * Sets certain LEDs to the given color, determined by the matrix
   */
  private void matrix(LEDMatrix leds, Mat mat) {
    leds.setMatrix(mat);
    leds.updateLEDs();
    methodConsumer.set(null);
  }

  /**
   * Starts updating the LEDs
   */
  public void start() {
    // Set the data
    leds.updateLEDs();
    leds.start();
    periodicThread.startPeriodic(0.02);
    System.out.printf("SetLEDs: Start\nCurrent Mode: %s\n", currentMode);

    stringLogger.logEntry("SetMatrixLEDs: Start\n", BullLogger.LogLevel.INFO);
  }

  /**
   * Stops updating the LEDs
   */
  public void stop() {
    leds.stop();
    periodicThread.stop();
    stringLogger.logEntry("SetMatrixLEDs: Stop\n", BullLogger.LogLevel.INFO);
  }

  /**
   * Generates a matrix with a row of 1.0, determined by the row num, and the rest 0.0
   */
  public static Mat oneRow(int rowNum) {
    // Create a 16x16 Mat with type CV_8UC3 and set it to black (default)
    Mat mat = new Mat(16, 16, CvType.CV_8UC3, new Scalar(0, 0, 0));
    
    // Create a Scalar object with the white color value (255, 255, 255)
    Scalar whiteColor = new Scalar(255, 255, 255);

    // Create a submat for the desired row
    Mat rowMat = mat.row(rowNum);

    // Set the rowMat to white using the setTo() method
    rowMat.setTo(whiteColor);

    return mat;
  }

  public static Mat oneCol(int rowNum) {
    // Create a 16x16 Mat with type CV_8UC3 and set it to black (default)
    Mat mat = new Mat(16, 16, CvType.CV_8UC3, new Scalar(0, 0, 0));

    // Create a Scalar object with the white color value (255, 255, 255)
    Scalar whiteColor = new Scalar(255, 255, 255);

    // Create a submat for the desired column
    Mat colMat = mat.col(rowNum);

    // Set the rowMat to white using the setTo() method
    colMat.setTo(whiteColor);

    return mat;
  }

  public static Mat eye() {
    Mat eyeMatrix = Mat.eye(numRows, numCols, CvType.CV_8UC1);
    Core.multiply(eyeMatrix, new Scalar(255), eyeMatrix);
    Mat eyeMatrixBGR = new Mat();
    Core.merge(Arrays.asList(eyeMatrix, eyeMatrix, eyeMatrix), eyeMatrixBGR);
    return eyeMatrixBGR;
  }
}
