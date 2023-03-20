// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.EnableLEDs;
import frc.robot.commands.Flash;
import frc.robot.commands.FlashRepeatCommand;
import frc.robot.commands.LEDDefaultCommand;
import frc.robot.commands.RunMatrixImageCommand;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.utility.ImagesYamlLoader;

public class Robot extends TimedRobot {
  // LED Commands
  private final ImagesYamlLoader imagesLoader = new ImagesYamlLoader();

  MatrixLEDs m_LEDSystem;
        
  DataLog m_MainLogger;
  
  @Override
  public void robotInit() {
    try {
      m_LEDSystem = new MatrixLEDs();

      m_LEDSystem.setDefaultCommand(new LEDDefaultCommand(m_LEDSystem));

      // set up a command sequence
      SmartDashboard.putData("Enable LEDs", new EnableLEDs(m_LEDSystem, 0));
      SmartDashboard.putData("Flash LEDs", new Flash(m_LEDSystem, 0.25));
      SmartDashboard.putData("Flash Repeated LEDs", new FlashRepeatCommand(4,0.25, m_LEDSystem));
      SmartDashboard.putData("Run Row One", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Row One")));
      SmartDashboard.putData("Run Row Tw0", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Row Two")));
      SmartDashboard.putData("Run Eye", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Eye")));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }
}
