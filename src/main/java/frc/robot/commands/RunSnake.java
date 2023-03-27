// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.utility.snake.Direction;
import frc.robot.utility.snake.SnakeGame;

public class RunSnake extends CommandBase {
  private final MatrixLEDs leds;
  private final SnakeGame game;
  private final BooleanSupplier up, down, left, right;

  public RunSnake(MatrixLEDs leds, SnakeGame game, BooleanSupplier up, BooleanSupplier down, BooleanSupplier left, BooleanSupplier right) {
    addRequirements(leds);
    this.leds = leds;
    this.game = game;

    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    leds.start();
  }

  int i = 0;

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if (up.getAsBoolean()) {
      game.setDirection(Direction.UP);;
    } else if (down.getAsBoolean()) {
      game.setDirection(Direction.DOWN);
    } else if (left.getAsBoolean()) {
      game.setDirection(Direction.LEFT);
    } else if (right.getAsBoolean()) {
      game.setDirection(Direction.RIGHT);
    }
    leds.setMat(game.getMatrix());
    leds.start();
    
    if (i == 10) {
      game.update();
      i = 0;
    }
    i++;

    if (game.hasLost()) {
      game.reset();
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    leds.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    // return game.hasLost();
    return false;
  }
}
