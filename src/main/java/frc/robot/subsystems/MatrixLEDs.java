package frc.robot.subsystems;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.numbers.N16;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;

import java.util.ArrayList;
import java.util.List;


public class MatrixLEDs extends SubsystemBase {
  private final AddressableLED led;
  private final AddressableLEDBuffer ledBuffer;
  private final int numRows = 16;
  private final int numCols = 16;

  private BullLogger stringLogger;
  private BullLogger intLogger;

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

  public void setMat(Matrix<N16, N16> mat, int hue) {
    System.out.printf("INFO: mat size: %d x %d%n", mat.getNumRows(), mat.getNumCols());
    for (var i = 0; i < numRows; ++i) {
      for (var j = 0; j < numCols; ++j) {
        var val = (int) mat.get(i, j);

        var curBufIndex = ((i % 2) == 0) ? i * numCols + j : (i + 1) * numCols - 1 - j;

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
    // For every pixel
    for (var i = 0; i < ledBuffer.getLength(); ++i) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (rainbowFirstPixelHue + (i * 180 / ledBuffer.getLength())) % 180;
      // Set the value
      ledBuffer.setHSV(i, hue, 255, 128);
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

  public void start() {
    // Set the data
    led.setData(ledBuffer);
    led.start();
    System.out.print("SetLEDs: Start\n");

    stringLogger.logEntry("SetMatrixLEDs: Start\n", BullLogger.LogLevel.INFO);
  }

  public void next() {
    // only rainbow is changing
    rainbow();
    // Set the LEDs
    led.setData(ledBuffer);
  }


  public void stop() {
    led.stop();
    stringLogger.logEntry("SetMatrixLEDs: Stop\n", BullLogger.LogLevel.INFO);
  }
}
