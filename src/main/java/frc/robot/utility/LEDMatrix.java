// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility;

import org.opencv.core.Mat;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Timer;

public class LEDMatrix {
    private final AddressableLED leds;
    private final AddressableLEDBuffer buffer;
    private final int numRows, numCols;

    private final Timer timer = new Timer();

    public LEDMatrix(int port, int numRows, int numCols) {
        leds = new AddressableLED(port);
        buffer = new AddressableLEDBuffer(numRows * numCols);
        leds.setLength(buffer.getLength());
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public void start() {
        timer.reset();
        timer.start();
        leds.start();
    }

    public void stop() {
        timer.stop();
        leds.stop();
    }

    public void updateLEDs() {
        leds.setData(buffer);
    }

    /**
     * Returns the index of an (x, y) coordinate, accounting for the serpentine wiring.
     * @param x positive to the left
     * @param y positive downwards
     * @return the index of the led as it's wired.
     */
    public int oneDimensionalIndex(int x, int y) {
        return ((x % 2) == 0) ? x * numCols + y : (x + 1) * numCols - 1 - y;
    }

    public void setMatrix(Mat mat) {
        setMatrix(mat, true);
    }

    public void setMatrix(Mat mat, boolean clearOthers) {
        for (var i = 0; i < numRows; ++i) {
            for (var j = 0; j < numCols; ++j) {
                double[] element = mat.get(i, j);
                var curBufIndex = oneDimensionalIndex(i, j);

                if (clearOthers && element[0] == 0 && element[1] == 0 && element[2] == 0) {
                    buffer.setRGB(curBufIndex, 0, 0, 0);
                } else {
                    buffer.setRGB(curBufIndex, (int) element[2], (int) element[1], (int) element[0]);
                }
            }
        }
    }

    public void oneCol(int col, int hue) {
        oneCol(col, hue, true);
    }

    public void oneCol(int col, int hue, boolean clearOthers) {
        if (col >= numCols) {
            return;
        }
        if (clearOthers) {
            for (var j = 0; j < numCols; ++j) {
                var idxStart = j * numRows;
                var idxStop = idxStart + numRows;
            
                if (col != j) {
                    for (var i = idxStart; i < idxStop; ++i) {
                        buffer.setRGB(i, 0, 0, 0);
                    }
                    continue;
                }
            
                // Set the value
                for (var i = idxStart; i < idxStop; ++i) {
                    buffer.setHSV(i, hue, 255, 128);
                }
            }    
        } else {
            int startIndex = oneDimensionalIndex(col, 0);
            for (int j = startIndex; j < startIndex+numRows; j++) {
                buffer.setHSV(j, hue, 255, 128);
            }
        }
    }

    public void oneRow(int row, int hue) {
        oneRow(row, hue, true);
    }
    
    public void oneRow(int row, int hue, boolean clearOthers) {
        if (row >= numRows) {
            return;
        }
        if (clearOthers) {
            for (var i = 0; i < numRows; ++i) {
                var idxStart = i * numCols;
                var idxStop = idxStart + numCols;
            
                if (row != i) {
                    for (var j = idxStart; j < idxStop; ++j) {
                        buffer.setRGB(j, 0, 0, 0);
                    }
                    continue;
                }
            
                // Set the value
                for (var j = idxStart; j < idxStop; ++j) {
                    buffer.setHSV(j, hue, 255, 128);
                }
            }
        } else {
            int startIndex = oneDimensionalIndex(row, 0);
            for (int i = startIndex; i < startIndex+numCols; i++) {
                buffer.setHSV(i, hue, 255, 128);
            }
        }
    }
    
    public void allOneColor(int hue) {
        // For every pixel
        for (var i = 0; i < buffer.getLength(); i++) {
            // Set the value
            buffer.setHSV(i, hue, 255, 128);
        }
    }
    
    public void off() {
        // For every pixel
        for (var i = 0; i < buffer.getLength(); ++i) {
            // Set the value
            buffer.setRGB(i, 0, 0, 0);
        }
    }
    
    private int rainbowFirstPixelHue = 0;
    public void rainbow() {
        // Diagonal rainbow
        for (int i = 0; i < numRows; i++) {
            // int hue = (rainbowFirstPixelHue + (i * 180 / buffer.getLength())) % 180;
            final int rowStartHue = (rainbowFirstPixelHue + (i * 180 / (2 * numRows))) % 180;
            for (int j = 0; j < numCols; j++) {
                final int hue = (rowStartHue + (j * 180 / (2 * numCols))) % 180;
                buffer.setHSV(oneDimensionalIndex(i, j), hue, 255, 128);
            }
        }
        // Increase by to make the rainbow "move"
        rainbowFirstPixelHue++;
        // Check bounds
        rainbowFirstPixelHue %= 180;
    }

    public void rainbowParty() {
        for (int i = 0; i < numRows; i++) {
            // int hue = (rainbowFirstPixelHue + (i * 180 / buffer.getLength())) % 180;
            final int rowStartHue = (rainbowFirstPixelHue + (i * 180 / (2 * numRows))) % 180;
            for (int j = 0; j < numCols; j++) {
                final int hue = (rowStartHue + (j * 180 / (2 * numCols))) % 180;
                buffer.setHSV(i * numCols + j, hue, 255, 128);
            }
        }
        // Increase by to make the rainbow "move"
        rainbowFirstPixelHue++;
        // Check bounds
        rainbowFirstPixelHue %= 180;
    }

    public void flash(double intervalSeconds, Runnable runnable) {
        alternate(intervalSeconds, runnable, () -> {off();});
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
