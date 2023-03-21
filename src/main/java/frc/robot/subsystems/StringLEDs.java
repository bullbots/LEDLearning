package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;


public class StringLEDs extends SubsystemBase {
  private AddressableLED m_led;
  private AddressableLEDBuffer m_ledBuffer;

  private BullLogger m_stringLogger;
  private BullLogger m_intLogger;

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

  private void rainbow() {
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

  public void start() {
    // Set the data
    m_led.setData(m_ledBuffer);
    m_led.start();

    m_stringLogger.logEntry("SetLEDs: Start\n", BullLogger.LogLevel.INFO);
  }

  public void next() {
    // only rainbow is changing
    rainbow();
    // Set the LEDs
    m_led.setData(m_ledBuffer);
  }

  public void stop() {
    m_led.stop();
    m_stringLogger.logEntry("SetLEDs: Stop\n", BullLogger.LogLevel.INFO);
  }
}
