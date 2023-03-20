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
    RAINBOW,
    CUSTOM
  }

  private LEDMode currentMode = LEDMode.OFF;

  private final AddressableLED led;
  private final AddressableLEDBuffer ledBuffer;
  private final int numRows = 16;
  private final int numCols = 16;

  private BullLogger stringLogger;
  private BullLogger intLogger;

  // Used for alternate()
  private Timer timer = new Timer();

  // See https://github.com/STMARobotics/frc-7028-2023/blob/main/src/main/java/frc/robot/subsystems/LEDSubsystem.java
  private final AtomicReference<Consumer<AddressableLEDBuffer>> bufferConsumer = new AtomicReference<Consumer<AddressableLEDBuffer>>(null);
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
      Consumer<AddressableLEDBuffer> value = bufferConsumer.get();
      if (value != null) {
        // Call the consumer to update the LEDs on this notifier thread
        value.accept(ledBuffer);
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

  public void setCustomMatrix(Matrix<N16, N16> matrix, int hue) {
    currentMode = LEDMode.CUSTOM;
    timer.stop();
    timer.reset();
    bufferConsumer.set((buffer) -> matrixConsumer(buffer, matrix, hue));
  }

  public void setMode(LEDMode mode) {
    if (mode != currentMode) {
      currentMode = mode;
      timer.stop();
      timer.reset();
      switch (currentMode) {
        case OFF:
          bufferConsumer.set((buffer) -> off(buffer));
        case DISCONNECTED:
          bufferConsumer.set((buffer) -> disconnected(buffer));
        case DISABLED:
          bufferConsumer.set(
            (buffer) -> alternate(2, () -> {
              setAllOnce(buffer, 60); // Green?
            }, () -> {
              setAllOnce(buffer, 110); // Blue?
            })
          );
        case CONE:
          // Not sure how hsv works tbh, everything I find online is in the range [0,360), not [0,180).
          // This is supposed to be yellow, I don't know if it will be.
          bufferConsumer.set((buffer) -> setAllOnce(buffer, 30));
        case CUBE:
          bufferConsumer.set((buffer) -> setAllOnce(buffer, 140)); // Purple?
        case RAINBOW:
          bufferConsumer.set((buffer) -> rainbowConsumer(buffer));
        case CUSTOM:
          return;
      }
    }
  }

  private void setAllOnce(AddressableLEDBuffer buffer, int hue) {
    allOneColor(buffer, hue);
    led.setData(buffer);
    bufferConsumer.set(null);
  }

  private void off(AddressableLEDBuffer buffer) {
    noColor(buffer);
    led.setData(buffer);
    bufferConsumer.set(null);
  }

  private void disconnected(AddressableLEDBuffer buffer) {
    // TODO: Dino :)
    allOneColor(buffer, 0);
    bufferConsumer.set(null);
  }

  private void rainbowConsumer(AddressableLEDBuffer buffer) {
    rainbow(buffer);
    led.setData(buffer);
    // We don't reset the bufferConsumer because the rainbow should keep updating.
  }

  private void matrixConsumer(AddressableLEDBuffer buffer, Matrix<N16, N16> mat, int hue) {
    setMat(buffer, mat, hue);
    led.setData(buffer);
    bufferConsumer.set(null);
  }




  private void setMat(AddressableLEDBuffer buffer, Matrix<N16, N16> mat, int hue) {
    System.out.printf("INFO: mat size: %d x %d%n", mat.getNumRows(), mat.getNumCols());
    for (var i = 0; i < numRows; ++i) {
      for (var j = 0; j < numCols; ++j) {
        var val = (int) mat.get(i, j);

        var curBufIndex = ((i % 2) == 0) ? i * numCols + j : (i + 1) * numCols - 1 - j;

        // Set the value
//        System.out.printf("INFO: row: %d, col: %d, val: %d%n", i, j, val);

        if (val == 1) {
          buffer.setHSV(curBufIndex, hue, 255, 128);
        }
        else {
          buffer.setHSV(curBufIndex, 0, 0, 0);
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

  private void allOneColor(AddressableLEDBuffer buffer, int hue) {
    // For every pixel
    for (var i = 0; i < buffer.getLength(); i++) {
      // Set the value
      buffer.setHSV(i, hue, 255, 128);
    }
  }

  private void noColor(AddressableLEDBuffer buffer) {
    // For every pixel
    for (var i = 0; i < buffer.getLength(); ++i) {
      // Set the value
      buffer.setHSV(i, 0, 0, 0);
    }
  }

  private void rainbow(AddressableLEDBuffer buffer) {
    // Diagonal rainbow
    for (int i = 0; i < numRows; i++) {
      // int hue = (rainbowFirstPixelHue + (i * 180 / buffer.getLength())) % 180;
      final int rowStartHue = (rainbowFirstPixelHue + (i * 180 / numRows)) % 180;
      for (int j = 0; j < numCols; j++) {
        final int hue = (rowStartHue + (j * 180 / numCols)) % 180;
        buffer.setHSV(i*numCols+j, hue, 255, 128);
      }
    }
    // Increase by to make the rainbow "move"
    rainbowFirstPixelHue += 3;
    // Check bounds
    rainbowFirstPixelHue %= 180;

    // log the color
    intLogger.logEntry(rainbowFirstPixelHue, BullLogger.LogLevel.DEBUG);

    stringLogger.logEntry("Rainbow " + rainbowFirstPixelHue, BullLogger.LogLevel.INFO);
  }

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

  private void alternate(double intervalSeconds, Runnable a, Runnable b) {
    timer.start(); // Make sure the timer is running
    long currentTime = System.currentTimeMillis();
    if ((currentTime % (int) (intervalSeconds * 2000)) < (int) (intervalSeconds * 1000)) {
      a.run();
    } else {
      b.run();
    }
  }

  public void start() {
    // Set the data
    led.setData(ledBuffer);
    led.start();
    periodicThread.startPeriodic(0.02);
    System.out.print("SetLEDs: Start\n");

    stringLogger.logEntry("SetMatrixLEDs: Start\n", BullLogger.LogLevel.INFO);
  }

  public void stop() {
    led.stop();
    periodicThread.stop();
    stringLogger.logEntry("SetMatrixLEDs: Stop\n", BullLogger.LogLevel.INFO);
  }
}
