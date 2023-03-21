// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.*;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.utility.ImagesYamlLoader;
import frc.robot.utility.VideosYamlLoader;

public class Robot extends TimedRobot {
  // LED Commands
  private final ImagesYamlLoader imagesLoader = new ImagesYamlLoader();
  private final VideosYamlLoader videoLoader = new VideosYamlLoader();

  MatrixLEDs m_LEDSystem;
        
  DataLog m_MainLogger;
  
  @Override
  public void robotInit() {
    try {
      m_LEDSystem = new MatrixLEDs();

      // set up a command sequence
      SmartDashboard.putData("Enable LEDs", new EnableLEDs(m_LEDSystem, 0));
      SmartDashboard.putData("Flash LEDs", new Flash(m_LEDSystem, 0.25));
      SmartDashboard.putData("Flash Repeated LEDs", new FlashRepeatCommand(4,0.25, m_LEDSystem));
      SmartDashboard.putData("Run Row One", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Row One")));
      SmartDashboard.putData("Run Row Two", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Row Two")));
      SmartDashboard.putData("Run Col One", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Col One")));
      SmartDashboard.putData("Run Col Two", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Col Two")));
      SmartDashboard.putData("Run Eye", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("Eye")));
      SmartDashboard.putData("Traffic Cone", new RunMatrixImageCommand(m_LEDSystem, imagesLoader.get("traffic-cone-512")));

      SmartDashboard.putData("Offline Continuous",
              new RunMatrixVideoCommand(m_LEDSystem,
                      videoLoader.get("Offline"),
                      10,
                      RunMatrixVideoCommand.RunType.CONTINUOUS
                      ));
      SmartDashboard.putData("Offline Once",
              new RunMatrixVideoCommand(m_LEDSystem,
                      videoLoader.get("Offline"),
                      50,
                      RunMatrixVideoCommand.RunType.ONCE
              ));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }
}
