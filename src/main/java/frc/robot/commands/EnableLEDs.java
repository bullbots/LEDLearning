package frc.robot.commands;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.utility.BullLogger;

public class EnableLEDs extends CommandBase {
    MatrixLEDs m_LEDSystem;
    int m_hue;

    BullLogger m_stringLogger;

    public EnableLEDs(MatrixLEDs aLEDSystem, int hue) {
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
        // Fill the buffer with a single color
//        m_LEDSystem.allOneColor(m_hue);
        m_LEDSystem.oneRow(15, m_hue);
//        Mat mat = Mat.eye(16, 16, CV_8U);
//        Matrix<16, 16> mat = new Matrix(new Nat<16>, new Nat<16>);
//        var mat = Matrix.mat(Nat.N16(), Nat.N16()).fill(2.0, 1.0, 0.0, 1.0);
//        var mat = Matrix.eye(Nat.N16());
//        m_LEDSystem.setMat(mat, m_hue);

        // Start the LEDs
        m_LEDSystem.start();

        m_stringLogger.logEntry("EnableLEDs: initialize\n");
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

        m_stringLogger.logEntry("EnableLEDs: end\n");
    }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
