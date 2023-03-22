// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Timer;

/** Add your docs here. */
public class LEDString {
    private final AddressableLED leds;
    private final AddressableLEDBuffer buffer;
    private final int length;
    
    private final Timer timer = new Timer();

    public LEDString(int port, int length) {
        leds = new AddressableLED(port);
        buffer = new AddressableLEDBuffer(length);
        leds.setLength(buffer.getLength());
        this.length = length;
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

    public void individualPixel(int index, int hue) {
        individualPixel(index, hue, true);
    }
    
    public void individualPixel(int index, int hue, boolean clearOthers) {
        if (clearOthers) {
            for (int i = 0; i < length; i++) {
                if (i == index) {
                    buffer.setHSV(i, hue, 255, 128);
                } else {
                    buffer.setRGB(i, 0, 0, 0);
                }
            }    
        } else {
            buffer.setHSV(index, hue, 255, 128);
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
        // For every pixel
        for (var i = 0; i < buffer.getLength(); i++) {
          // Calculate the hue - hue is easier for rainbows because the color
          // shape is a circle so only one value needs to precess
          final var hue = (rainbowFirstPixelHue + (i * 180 / buffer.getLength())) % 180;
          // Set the value
          buffer.setHSV(i, hue, 255, 128);
        }
        // Increase by to make the rainbow "move"
        rainbowFirstPixelHue += 3;
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
