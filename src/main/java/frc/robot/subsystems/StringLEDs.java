package frc.robot.subsystems;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
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
  private final AtomicReference<Consumer<AddressableLEDBuffer>> bufferConsumer = new AtomicReference<Consumer<AddressableLEDBuffer>>(null);
  // This Notifier acts in place of periodic, so updating the buffer will happen on a seperate thread.
  private final Notifier periodicThread;

  // for rainbow pattern, store what the last hue of the first pixel is
  private int m_rainbowFirstPixelHue;

  public StringLEDs() {
    // This should only be called once because of the port conflict issue
    m_led = new AddressableLED(0); // PWM port 0

    // Reuse buffer
    // Default to a length of 20, start empty output
    // Length is expensive to set, so only set it once, then just update data
    m_ledBuffer = new AddressableLEDBuffer(30);
    m_led.setLength(m_ledBuffer.getLength());

    periodicThread = new Notifier(() ->  {
      Consumer<AddressableLEDBuffer> value = bufferConsumer.get();
      if (value != null) {
        // Call the consumer to update the LEDs on this notifier thread
        value.accept(m_ledBuffer);
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
      }
    }
  }

  private void setLEDs() {
    m_led.setData(m_ledBuffer);
  }

  private void setAllOnce(AddressableLEDBuffer buffer, int hue) {
    allOneColor(buffer, hue);
    setLEDs();
    bufferConsumer.set(null);
  }

  private void off(AddressableLEDBuffer buffer) {
    noColor(buffer);
    setLEDs();
    bufferConsumer.set(null);
  }

  private void disconnected(AddressableLEDBuffer buffer) {
    // TODO: Dino :)
    allOneColor(buffer, 0);
    bufferConsumer.set(null);
  }

  private void rainbowConsumer(AddressableLEDBuffer buffer) {
    rainbow(buffer);
    setLEDs();
  }





  public void allOneColor(AddressableLEDBuffer buffer, int hue) {
    // For every pixel
    for (var i = 0; i < buffer.getLength(); i++) {
      // Set the value
      buffer.setHSV(i, hue, 255, 128);
    }
  }

  public void noColor(AddressableLEDBuffer buffer) {
    // For every pixel
    for (var i = 0; i < buffer.getLength(); i++) {
      // Set the value
      buffer.setHSV(i, 0, 0, 0);
    }
  }

  private void rainbow(AddressableLEDBuffer buffer) {
    // For every pixel
    for (var i = 0; i < buffer.getLength(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / buffer.getLength())) % 180;
      // Set the value
      buffer.setHSV(i, hue, 255, 128);
    }
    // Increase by to make the rainbow "move"
    m_rainbowFirstPixelHue += 3;
    // Check bounds
    m_rainbowFirstPixelHue %= 180;

    // log the color
    m_intLogger.logEntry(m_rainbowFirstPixelHue, BullLogger.LogLevel.DEBUG);

    m_stringLogger.logEntry("Rainbow " + m_rainbowFirstPixelHue, BullLogger.LogLevel.INFO);
  }

  private void alternate(AddressableLEDBuffer buffer, double intervalSeconds, Runnable runnable) {
    alternate(intervalSeconds, runnable, () -> {noColor(buffer);});
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
    m_led.setData(m_ledBuffer);
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
}
