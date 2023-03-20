package frc.robot.subsystems;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.numbers.N16;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;

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
    RAINBOW
  }

  private LEDMode currentMode = LEDMode.OFF;

  private final AddressableLED led;
  private final AddressableLEDBuffer ledBuffer;
  private final int numRows = 16;
  private final int numCols = 16;

  private BullLogger stringLogger;
  private BullLogger intLogger;

  // Used for alternate()
  private final Timer timer = new Timer();

  // See https://github.com/STMARobotics/frc-7028-2023/blob/main/src/main/java/frc/robot/subsystems/LEDSubsystem.java
  private final LEDWrapperMethods ledMethods = new LEDWrapperMethods();
  private final AtomicReference<Consumer<LEDWrapperMethods>> methodConsumer = new AtomicReference<Consumer<LEDWrapperMethods>>(null);
  // This Notifier acts in place of periodic, so updating the buffer will happen on a seperate thread.
  private final Notifier periodicThread;

  // for rainbow pattern, store what the last hue of the first pixel is
  private int rainbowFirstPixelHue;

  public MatrixLEDs() {
    this(9); // Default PWM port 9
  }

  public MatrixLEDs(int port) {
    // This should only be called once because of the port conflict issue
    led = new AddressableLED(port); // PWM port 9

    // Reuse buffer
    // Default to a length of 20, start empty output
    // Length is expensive to set, so only set it once, then just update data
    ledBuffer = new AddressableLEDBuffer(numRows * numCols);
    led.setLength(ledBuffer.getLength());

    periodicThread = new Notifier(() ->  {
      Consumer<LEDWrapperMethods> value = methodConsumer.get();
      if (value != null) {
        // Call the consumer to update the LEDs on this notifier thread
        value.accept(ledMethods);
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
    timer.stop();
    timer.reset();
    methodConsumer.set((leds) -> matrix(leds, matrix, hue));
  }

  public void setMode(LEDMode mode) {
    if (mode != currentMode) {
      currentMode = mode;
      timer.stop();
      timer.reset();
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
      }
    }
  }

  /**
   * Sets all the LEDs to the given color then clears the consumer
   */
  private void setAllOnce(LEDWrapperMethods leds, int hue) {
    leds.allOneColor(hue);
    led.setData(ledBuffer);
    methodConsumer.set(null);
  }

  /**
   * Sets all the LEDs to the given color
   */
  private void setAll(LEDWrapperMethods leds, int hue) {
    leds.allOneColor(hue);
    led.setData(ledBuffer);
  }

  /**
   * Turns off all the LEDs then clears the consumer
   */
  private void off(LEDWrapperMethods leds) {
    leds.noColor();
    led.setData(ledBuffer);
    methodConsumer.set(null);
  }

  /**
   * Shows the animation for when the driverstation is disconnected
   */
  private void disconnected(LEDWrapperMethods leds) {
    // TODO: Dino :)
    leds.flash(1,
      () -> leds.allOneColor(0)
    );
    led.setData(ledBuffer);
    // If we want to animate the dino, get rid of this line.
    methodConsumer.set(null);
  }

  /**
   * Plays a rainbow animation on the LEDs
   */
  private void rainbow(LEDWrapperMethods leds) {
    leds.rainbow();
    led.setData(ledBuffer);
    // We don't reset the bufferConsumer because the rainbow should keep updating.
  }

  /**
   * Sets certain LEDs to the given color, determined by the matrix
   */
  private void matrix(LEDWrapperMethods leds, Matrix<N16, N16> mat, int hue) {
    leds.setMat(mat, hue);
    led.setData(ledBuffer);
    methodConsumer.set(null);
  }

  /**
   * Starts updating the LEDs
   */
  public void start() {
    // Set the data
    led.setData(ledBuffer);
    led.start();
    periodicThread.startPeriodic(0.02);
    System.out.printf("SetLEDs: Start\nCurrent Mode: %s\n", currentMode);

    stringLogger.logEntry("SetMatrixLEDs: Start\n", BullLogger.LogLevel.INFO);
  }

  /**
   * Stops updating the LEDs
   */
  public void stop() {
    led.stop();
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

  /**
   * Returns the index of an (x, y) coordinate, accounting for the serpentine wiring.
   * @param x positive to the left
   * @param y positive downwards
   */
  public int oneDimensionalIndex(int x, int y) {
    return ((x % 2) == 0) ? x * numCols + y : (x + 1) * numCols - 1 - y;
  }

  @SuppressWarnings("unused")
  private class LEDWrapperMethods {
    public void setMat(Matrix<N16, N16> mat, int hue) {
      System.out.printf("INFO: mat size: %d x %d%n", mat.getNumRows(), mat.getNumCols());
      for (var i = 0; i < numRows; ++i) {
        for (var j = 0; j < numCols; ++j) {
          var val = (int) mat.get(i, j);
  
          var curBufIndex = oneDimensionalIndex(i, j);
  
          // Set the value
  //        System.out.printf("INFO: row: %d, col: %d, val: %d%n", i, j, val);
  
          if (val == 1) {
            ledBuffer.setHSV(curBufIndex, hue, 255, 128);
          }
          else {
            ledBuffer.setHSV(curBufIndex, 0, 0, 0);
          }
        }
      }
    }
  
    public void oneRow(int row, int hue) {
      if (row >= numRows) {
        intLogger.logEntry(String.format("SetMatrixLEDs row out of bounds: %d >= %d", row, numRows), BullLogger.LogLevel.WARNING);
      }
  
      for (var i = 0; i < numRows; ++i) {
        var idxStart = i * numCols;
        var idxStop = idxStart + numCols;
  
        if (row != i) {
          for (var j = idxStart; j < idxStop; ++j) {
            ledBuffer.setRGB(j, 0, 0, 0);
          }
          continue;
        }
  
        // Set the value
        for (var j = idxStart; j < idxStop; ++j) {
          ledBuffer.setHSV(j, hue, 255, 128);
        }
      }
    }
  
    public void allOneColor(int hue) {
      // For every pixel
      for (var i = 0; i < ledBuffer.getLength(); i++) {
        // Set the value
        ledBuffer.setHSV(i, hue, 255, 128);
      }
    }
  
    public void noColor() {
      // For every pixel
      for (var i = 0; i < ledBuffer.getLength(); ++i) {
        // Set the value
        ledBuffer.setHSV(i, 0, 0, 0);
      }
    }
  
    private void rainbow() {
      // Diagonal rainbow
      for (int i = 0; i < numRows; i++) {
        // int hue = (rainbowFirstPixelHue + (i * 180 / ledBuffer.getLength())) % 180;
        final int rowStartHue = (rainbowFirstPixelHue + (i * 180 / (2 * numRows))) % 180;
        for (int j = 0; j < numCols; j++) {
          final int hue = (rowStartHue + (j * 180 / (2 * numCols))) % 180;
          ledBuffer.setHSV(oneDimensionalIndex(i, j), hue, 255, 128);
        }
      }
      // Increase by to make the rainbow "move"
      rainbowFirstPixelHue++;
      // Check bounds
      rainbowFirstPixelHue %= 180;
  
      // log the color
      intLogger.logEntry(rainbowFirstPixelHue, BullLogger.LogLevel.DEBUG);
  
      stringLogger.logEntry("Rainbow " + rainbowFirstPixelHue, BullLogger.LogLevel.INFO);
    }

    public void flash(double intervalSeconds, Runnable runnable) {
      alternate(intervalSeconds, runnable, () -> {noColor();});
    }
  
    public void alternate(double intervalSeconds, Runnable a, Runnable b) {
      timer.start(); // Make sure the timer is running
      long currentTime = System.currentTimeMillis();
      if ((currentTime % (int) (intervalSeconds * 2000)) < (int) (intervalSeconds * 1000)) {
        a.run();
      } else {
        b.run();
      }
    }
  }
}
