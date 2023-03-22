package frc.robot.subsystems;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.numbers.N16;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;
import frc.robot.utility.LEDMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


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

  private final LEDMatrix<N16, N16> leds;
  private final int numRows = 16;
  private final int numCols = 16;

  private BullLogger stringLogger;
  private BullLogger intLogger;

  // See https://github.com/STMARobotics/frc-7028-2023/blob/main/src/main/java/frc/robot/subsystems/LEDSubsystem.java
  private final AtomicReference<Consumer<LEDMatrix<N16, N16>>> methodConsumer = new AtomicReference<Consumer<LEDMatrix<N16, N16>>>(null);
  // This Notifier acts in place of periodic, so updating the buffer will happen on a seperate thread.
  private final Notifier periodicThread;

  public MatrixLEDs() {
    this(9); // Default PWM port 9
  }

  public MatrixLEDs(int port) {
    leds = new LEDMatrix<N16, N16>(port, numRows, numCols);

    periodicThread = new Notifier(() ->  {
      Consumer<LEDMatrix<N16, N16>> value = methodConsumer.get();
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
   * @param hue the color to change to
   */
  public void setCustomMatrix(Matrix<N16, N16> matrix, int hue) {
    currentMode = null;
    methodConsumer.set((leds) -> matrix(leds, matrix, hue));
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
  private void setAllOnce(LEDMatrix<N16, N16> leds, int hue) {
    leds.allOneColor(hue);
    leds.updateLEDs();
    methodConsumer.set(null);
  }

  /**
   * Sets all the LEDs to the given color
   */
  private void setAll(LEDMatrix<N16, N16> leds, int hue) {
    leds.allOneColor(hue);
    leds.updateLEDs();
  }

  /**
   * Turns off all the LEDs then clears the consumer
   */
  private void off(LEDMatrix<N16, N16> leds) {
    leds.off();
    leds.updateLEDs();
    methodConsumer.set(null);
  }

  /**
   * Shows the animation for when the driverstation is disconnected
   */
  private void disconnected(LEDMatrix<N16, N16> leds) {
    // TODO: Dino :)
    leds.flash(1,
      () -> leds.allOneColor(0)
    );
    leds.updateLEDs();
  }

  /**
   * Plays a rainbow animation on the LEDs
   */
  private void rainbow(LEDMatrix<N16, N16> leds) {
    leds.rainbow();
    leds.updateLEDs();
    // We don't reset the bufferConsumer because the rainbow should keep updating.
  }

  /**
   * Plays a weird rainbow animation on the LEDs
   */
  private void rainbowParty(LEDMatrix<N16, N16> leds) {
    leds.rainbowParty();
    leds.updateLEDs();
    // We don't reset the bufferConsumer because the rainbow should keep updating.
  }

  /**
   * Sets certain LEDs to the given color, determined by the matrix
   */
  private void matrix(LEDMatrix<N16, N16> leds, Matrix<N16, N16> mat, int hue) {
    leds.setMatrix(mat, hue);
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
  public static Matrix<N16, N16> oneRow(int rowNum) {
    List<Double> repeatingList = new ArrayList<>(16);

    for (int i = 0; i < 16 * rowNum; i++) {
      repeatingList.add(0.0);
    }

    for (int i = 0; i < 16; i++) {
      repeatingList.add(1.0);
    }

    for (int i = 0; i < 16 * (16 - rowNum) - 16; i++) {
      repeatingList.add(0.0);
    }

    double[] doubleArray = repeatingList.stream().mapToDouble(Double::doubleValue).toArray();
    return Matrix.mat(Nat.N16(), Nat.N16()).fill(doubleArray);
  }
}
