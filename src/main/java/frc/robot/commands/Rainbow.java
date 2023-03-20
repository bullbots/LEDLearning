// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.subsystems.MatrixLEDs.LEDMode;

public class Rainbow extends CommandBase {
  private final MatrixLEDs leds;
  public Rainbow(MatrixLEDs leds) {
    addRequirements(leds);
    this.leds = leds;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    leds.setMode(LEDMode.RAINBOW);
    leds.start();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    leds.setMode(LEDMode.OFF);
    leds.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
