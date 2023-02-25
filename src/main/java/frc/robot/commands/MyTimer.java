package frc.robot.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;

public class MyTimer {
  public static double getFPGATimestamp() {
    System.out.print("MyTimer getFPGATimestamp\n");
    return RobotController.getFPGATime() / 1000000.0;
  }

  public static double getMatchTime() {
    System.out.print("MyTimer getMatchTime\n");
    return DriverStation.getMatchTime();
  }

  public static void delay(final double seconds) {
    System.out.print("MyTimer delay\n");
    try {
      Thread.sleep((long) (seconds * 1e3));
    } catch (final InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  private double m_startTime;
  private double m_accumulatedTime;
  private boolean m_running;

  /** Timer constructor. */
  public MyTimer() {
    System.out.print("MyTimer constructor\n");
    reset();
  }

  private double getMsClock() {
    double time = RobotController.getFPGATime() / 1000.0;
    System.out.print("MyTimer getMsClock " + time + "\n");

    return time;
  }

  public double get() {
    System.out.print("MyTimer get\n");

    if (m_running) {
      return m_accumulatedTime + (getMsClock() - m_startTime) / 1000.0;
    } else {
      return m_accumulatedTime;
    }
  }

  public void reset() {
    System.out.print("MyTimer reset\n");
    m_accumulatedTime = 0;
    m_startTime = getMsClock();
  }

  public void start() {
    System.out.print("MyTimer start\n");
    if (!m_running) {
      m_startTime = getMsClock();
      m_running = true;
    }
  }

  public void stop() {
    System.out.print("MyTimer stop\n");
    m_accumulatedTime = get();
    m_running = false;
  }

  public boolean hasElapsed(double seconds) {
    System.out.print("MyTimer hasElapsed\n");
    return get() >= seconds;
  }

  public boolean advanceIfElapsed(double seconds) {
    System.out.print("MyTimer advanceIfElapsed\n");
    if (get() >= seconds) {
      // Advance the start time by the period.
      // Don't set it to the current time... we want to avoid drift.
      m_startTime += seconds * 1000;
      return true;
    } else {
      return false;
    }
  }
}
