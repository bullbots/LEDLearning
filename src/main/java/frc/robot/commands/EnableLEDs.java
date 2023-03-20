package frc.robot.commands;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.SetLEDs;
import frc.robot.subsystems.SetLEDs.LEDMode;
import frc.robot.utility.BullLogger;

public class EnableLEDs extends CommandBase {
    SetLEDs m_LEDSystem;
    int m_hue;

    BullLogger m_stringLogger;

    public EnableLEDs(SetLEDs aLEDSystem, int hue) {
        this.m_LEDSystem = aLEDSystem;
        this.m_hue = hue;
        addRequirements(aLEDSystem);

        try {
          // set up a string logger
          m_stringLogger = new BullLogger("stringEnableLEDs", true, false);

          m_stringLogger.setLogType(BullLogger.LogType.STRING);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

    }
      
    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        // Set the mode to disabled.
        m_LEDSystem.setMode(LEDMode.DISABLED);

        // Start the LEDs
        m_LEDSystem.start();

        m_stringLogger.logEntry("EnableLEDs: initialize\n");
    }

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {}

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
        m_LEDSystem.setMode(LEDMode.OFF);
        m_LEDSystem.stop();

        m_stringLogger.logEntry("EnableLEDs: end\n");
    }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
