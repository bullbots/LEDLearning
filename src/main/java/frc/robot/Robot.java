// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.*;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.utility.YamlLoader;
import frc.robot.utility.snake.SnakeGame;

public class Robot extends TimedRobot {
  XboxController controller = new XboxController(0);
  // LED Commands

  MatrixLEDs m_LEDSystem;
  private PowerDistribution pdp;

  private Command offlineContinous;
        
  DataLog m_MainLogger;
  
  @Override
  public void robotInit() {
    try {
      m_LEDSystem = new MatrixLEDs(0, 2);

      offlineContinous = new RunMatrixVideoCommand(m_LEDSystem,
              YamlLoader.getVideo("offline"),
              10,
              RunMatrixVideoCommand.RunType.CONTINUOUS
      );

      pdp = new PowerDistribution();

//      SmartDashboard.putData("pdp", pdp);

      // set up a command sequence
      SmartDashboard.putData("Enable LEDs", new EnableLEDs(m_LEDSystem, 0));
      SmartDashboard.putData("Flash LEDs", new Flash(m_LEDSystem, 0.25));
      SmartDashboard.putData("Flash Repeated LEDs", new FlashRepeatCommand(4,0.25, m_LEDSystem));
      SmartDashboard.putData("Off", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("Off")));
      SmartDashboard.putData("Run Row One", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("Row One")));
      SmartDashboard.putData("Run Row Two", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("Row Two")));
      SmartDashboard.putData("Run Col One", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("Col One")));
      SmartDashboard.putData("Run Col Two", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("Col Two")));
      SmartDashboard.putData("Run Eye", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("Eye")));
      SmartDashboard.putData("Traffic Cone Front", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("cone256")));
      SmartDashboard.putData("Purple Cube", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("cube256")));
      SmartDashboard.putData("Pickle", new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("pickle"))); // Pickle doesn't exist so return sad face.

      SmartDashboard.putData("Offline Continuous",
              new RunMatrixVideoCommand(m_LEDSystem,
                      YamlLoader.getVideo("offline"),
                      10,
                      RunMatrixVideoCommand.RunType.CONTINUOUS
                      ));
      SmartDashboard.putData("Offline Once",
              new RunMatrixVideoCommand(m_LEDSystem,
                      YamlLoader.getVideo("offline"),
                      50,
                      RunMatrixVideoCommand.RunType.ONCE
              ));
      // Pickle Video doesn't exist so return sad face.
      SmartDashboard.putData("Pickle Video",
              new RunMatrixVideoCommand(m_LEDSystem,
                      YamlLoader.getVideo("pickle"),
                      50,
                      RunMatrixVideoCommand.RunType.ONCE));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    if (DriverStation.isDisabled()) {
      if (!CommandScheduler.getInstance().isScheduled(offlineContinous)) {
        System.out.println("Info: scheduling Offline Continuous");
        offlineContinous.schedule();
      }
    } else if (CommandScheduler.getInstance().isScheduled(offlineContinous)) {
      System.out.println("Info: scheduling Off");
      // new RunMatrixImageCommand(m_LEDSystem, YamlLoader.getImage("Off")) {
      //   @Override
      //   public boolean isFinished() {
      //     return true;
      //   }
      // }.schedule();
      new RunSnake(m_LEDSystem, new SnakeGame(),
        () -> controller.getRawAxis(0) < -.1, 
        () -> controller.getRawAxis(0) > .1, 
        () -> controller.getRawAxis(1) < -.1,
        () -> controller.getRawAxis(1) > .1
      ).schedule();
    }
  }
}
