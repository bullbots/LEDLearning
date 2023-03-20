package frc.robot.subsystems;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;


public class StringLEDs extends SubsystemBase {
  public enum LEDMode {
    OFF,
    DISCONNECTED,
    DISABLED,
    CONE,
    CUBE,
    RAINBOW
  }

  private LEDMode currentMode = LEDMode.OFF;
  
  private AddressableLED m_led;
  private AddressableLEDBuffer m_ledBuffer;

  private BullLogger m_stringLogger;
  private BullLogger m_intLogger;

  private Timer timer = new Timer();

  // See https://github.com/STMARobotics/frc-7028-2023/blob/main/src/main/java/frc/robot/subsystems/LEDSubsystem.java
  private final LEDWrapperMethods ledMethods = new LEDWrapperMethods();
  private final AtomicReference<Consumer<LEDWrapperMethods>> bufferConsumer = new AtomicReference<Consumer<LEDWrapperMethods>>(null);
  // This Notifier acts in place of periodic, so updating the buffer will happen on a seperate thread.
  private final Notifier periodicThread;

  // for rainbow pattern, store what the last hue of the first pixel is
  private int m_rainbowFirstPixelHue;

  public StringLEDs() {
    this(0);
  }

  public StringLEDs(int port) {
    // This should only be called once because of the port conflict issue
    m_led = new AddressableLED(port); // PWM port 0

    // Reuse buffer
    // Default to a length of 20, start empty output
    // Length is expensive to set, so only set it once, then just update data
    m_ledBuffer = new AddressableLEDBuffer(30);
    m_led.setLength(m_ledBuffer.getLength());
    m_led.setData(m_ledBuffer);

    periodicThread = new Notifier(() ->  {
      Consumer<LEDWrapperMethods> value = bufferConsumer.get();
      if (value != null) {
        // Call the consumer to update the LEDs on this notifier thread
        value.accept(ledMethods);
      }
    });
    periodicThread.setName("LED String");
  
    try {
      // set up a string logger
      m_stringLogger = new BullLogger("stringSetLEDs", true, false);

      m_stringLogger.setLogType(BullLogger.LogType.STRING);
      m_stringLogger.setLogLevel(BullLogger.LogLevel.DEBUG);

      // set up an int logger
      m_intLogger = new BullLogger("intSetLEDs", true, true);

      m_intLogger.setLogType(BullLogger.LogType.INT);

      m_intLogger.setLogLevel(BullLogger.LogLevel.DEBUG);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void setMode(LEDMode mode) {
    if (mode != currentMode) {
      currentMode = mode;
      timer.stop();
      timer.reset();
      switch (currentMode) {
        case OFF:
          bufferConsumer.set((leds) -> off(leds));
          break;
        case DISCONNECTED:
          bufferConsumer.set((leds) -> disconnected(leds));
          break;
        case DISABLED:
          bufferConsumer.set(
            (leds) -> leds.alternate(2, () -> {
              leds.allOneColor(60); // Green?
            }, () -> {
              leds.allOneColor(110); // Blue?
            })
          );
          break;
        case CONE:
          // Not sure how hsv works tbh, everything I find online is in the range [0,360), not [0,180).
          // This is supposed to be yellow, I don't know if it will be.
          bufferConsumer.set((leds) -> setAllOnce(leds, 30));
          break;
        case CUBE:
          bufferConsumer.set((leds) -> setAllOnce(leds, 140)); // Purple?
          break;
        case RAINBOW:
          bufferConsumer.set((leds) -> rainbowConsumer(leds));
          break;
      }
    }
  }

  private void setAllOnce(LEDWrapperMethods leds, int hue) {
    leds.allOneColor(hue);
    m_led.setData(m_ledBuffer);
    bufferConsumer.set(null);
  }

  private void off(LEDWrapperMethods leds) {
    leds.noColor();
    m_led.setData(m_ledBuffer);
    bufferConsumer.set(null);
  }

  private void disconnected(LEDWrapperMethods leds) {
    m_led.setData(m_ledBuffer);
    bufferConsumer.set(null);
  }

  private void rainbowConsumer(LEDWrapperMethods leds) {
    leds.rainbow();
    m_led.setData(m_ledBuffer);
  }






  public void start() {
    // Set the data
    m_led.start();
    periodicThread.startPeriodic(0.02);
    System.out.printf("SetLEDs: Start\nCurrent Mode: %s\n", currentMode);

    m_stringLogger.logEntry("SetLEDs: Start\n", BullLogger.LogLevel.INFO);
  }

  public void stop() {
    m_led.stop();
    periodicThread.stop();
    m_stringLogger.logEntry("SetLEDs: Stop\n", BullLogger.LogLevel.INFO);
  }

  @SuppressWarnings("unused")
  private class LEDWrapperMethods {
    public void allOneColor(int hue) {
      // For every pixel
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Set the value
        m_ledBuffer.setHSV(i, hue, 255, 128);
      }
    }
  
    public void noColor() {
      // For every pixel
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Set the value
        m_ledBuffer.setHSV(i, 0, 0, 0);
      }
    }
  
    public void rainbow() {
      // For every pixel
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Calculate the hue - hue is easier for rainbows because the color
        // shape is a circle so only one value needs to precess
        final var hue = (m_rainbowFirstPixelHue + (i * 180 / m_ledBuffer.getLength())) % 180;
        // Set the value
        m_ledBuffer.setHSV(i, hue, 255, 128);
      }
      // Increase by to make the rainbow "move"
      m_rainbowFirstPixelHue += 3;
      // Check bounds
      m_rainbowFirstPixelHue %= 180;
  
      // log the color
      m_intLogger.logEntry(m_rainbowFirstPixelHue, BullLogger.LogLevel.DEBUG);
  
      m_stringLogger.logEntry("Rainbow " + m_rainbowFirstPixelHue, BullLogger.LogLevel.INFO);
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
