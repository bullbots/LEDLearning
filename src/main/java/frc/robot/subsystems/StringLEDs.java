package frc.robot.subsystems;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utility.BullLogger;
import frc.robot.utility.LEDString;


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
  
  private final LEDString leds;

  private BullLogger m_stringLogger;
  private BullLogger m_intLogger;

  private Timer timer = new Timer();

  // See https://github.com/STMARobotics/frc-7028-2023/blob/main/src/main/java/frc/robot/subsystems/LEDSubsystem.java
  private final AtomicReference<Consumer<LEDString>> bufferConsumer = new AtomicReference<Consumer<LEDString>>(null);
  // This Notifier acts in place of periodic, so updating the buffer will happen on a seperate thread.
  private final Notifier periodicThread;

  public StringLEDs() {
    this(0);
  }

  public StringLEDs(int port) {
    leds = new LEDString(port, 30);

    periodicThread = new Notifier(() ->  {
      Consumer<LEDString> value = bufferConsumer.get();
      if (value != null) {
        // Call the consumer to update the LEDs on this notifier thread
        value.accept(leds);
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
              leds.allOneColor(60);
            }, () -> {
              leds.allOneColor(110);
            })
          );
          break;
        case CONE:
          bufferConsumer.set((leds) -> setAllOnce(leds, 30));
          break;
        case CUBE:
          bufferConsumer.set((leds) -> setAllOnce(leds, 140));
          break;
        case RAINBOW:
          bufferConsumer.set((leds) -> rainbow(leds));
          break;
      }
    }
  }

  private void setAllOnce(LEDString leds, int hue) {
    leds.allOneColor(hue);
    leds.updateLEDs();
    bufferConsumer.set(null);
  }

  private void off(LEDString leds) {
    leds.off();
    leds.updateLEDs();
    bufferConsumer.set(null);
  }

  private void disconnected(LEDString leds) {
    leds.flash(1,
      () -> leds.allOneColor(0)
    );
    leds.updateLEDs();
  }

  private void rainbow(LEDString leds) {
    leds.rainbow();
    leds.updateLEDs();
  }






  public void start() {
    // Set the data
    leds.start();
    periodicThread.startPeriodic(0.02);
    System.out.printf("SetLEDs: Start\nCurrent Mode: %s\n", currentMode);

    m_stringLogger.logEntry("SetLEDs: Start\n", BullLogger.LogLevel.INFO);
  }

  public void stop() {
    leds.stop();
    periodicThread.stop();
    m_stringLogger.logEntry("SetLEDs: Stop\n", BullLogger.LogLevel.INFO);
  }
}
