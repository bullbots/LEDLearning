package frc.robot.commands;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.SetLEDs;
import frc.robot.utility.BullLogger;

public class EnableLEDs extends CommandBase {
    SetLEDs m_LEDSystem;
    int m_hue;

    BullLogger m_Logger;

    public EnableLEDs(SetLEDs aLEDSystem, int hue) {
        this.m_LEDSystem = aLEDSystem;
        this.m_hue = hue;
        addRequirements(aLEDSystem);

        try {
          // set up a string logger
          m_Logger = new BullLogger(true, false, BullLogger.LogLevel.DEBUG);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

    }
      
    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        // Fill the buffer with a single color
        m_LEDSystem.allOneColor(m_hue);

        // Start the LEDs
        m_LEDSystem.start();

        m_Logger.putString("EnableLEDs", "EnableLEDs: initialize\n", BullLogger.LogLevel.DEBUG);
    }

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
      if (m_hue == -1) {
        // only for rainbow
        m_LEDSystem.next();
      }
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
        m_LEDSystem.noColor();
        m_LEDSystem.stop();

        m_Logger.putString("EnableLEDs", "EnableLEDs: end\n", BullLogger.LogLevel.DEBUG);
    }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
