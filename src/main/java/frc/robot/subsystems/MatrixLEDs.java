package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.Arrays;

public class MatrixLEDs extends SubsystemBase {
  private final AddressableLED led;
  private final AddressableLEDBuffer ledBuffer;
  private static final int numRows = 16;
  private static final int numCols = 16;
  private static final double globalScale = 0.25;

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

  public void setMat(Mat mat) {
    Mat scaledMat = new Mat();
    Core.multiply(mat, new Scalar(globalScale, globalScale, globalScale), scaledMat);

    for (var i = 0; i < numCols; ++i) {
      for (var j = 0; j < numRows; ++j) {
        double[] element = scaledMat.get(i, j);

        var curBufIndex = ((j % 2) == 0) ? j * numCols + i : (j + 1) * numCols - 1 - i;

        // Set the value
//        System.out.printf("INFO: row: %d, col: %d, val: %d%n", i, j, val);

        ledBuffer.setRGB(curBufIndex, (int) element[2], (int) element[1], (int) element[0]);
      }
    }
  }

  public static Mat off() {
    return Mat.zeros(numRows, numRows, CvType.CV_8UC3);
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
