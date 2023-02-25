// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.EnableLEDs;
import frc.robot.commands.Flash;
import frc.robot.subsystems.SetLEDs;

public class Robot extends TimedRobot {
  // LED Commands
  EnableLEDs m_LEDCommand;
  Flash m_FlashGroup;

  SetLEDs m_LEDSystem;
        
  DataLog m_MainLogger;
  
  @Override
  public void robotInit() {
    try {
      m_LEDSystem = new SetLEDs();
      m_LEDCommand = new EnableLEDs(m_LEDSystem, -1);
      SmartDashboard.putData("LED Color", m_LEDCommand);

      // set up a command sequence
      m_FlashGroup = new Flash(m_LEDSystem, 2.0);
      SmartDashboard.putData("Flash LEDs", m_FlashGroup);
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
