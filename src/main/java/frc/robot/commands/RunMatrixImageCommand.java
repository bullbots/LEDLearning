package frc.robot.commands;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.numbers.N16;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.subsystems.MatrixLEDs.LEDMode;


public class RunMatrixImageCommand extends CommandBase {
    private final MatrixLEDs matrixLEDs;
    private final Matrix<N16, N16> matrixImage;

    public RunMatrixImageCommand(MatrixLEDs matrixLEDs, Matrix<N16, N16> matrixImage) {
        this.matrixLEDs = matrixLEDs;
        this.matrixImage = matrixImage;
        addRequirements(this.matrixLEDs);
    }

    @Override
    public void initialize() {
        matrixLEDs.setCustomMatrix(matrixImage, 0);
        matrixLEDs.start();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        matrixLEDs.stop();
        matrixLEDs.setMode(LEDMode.OFF);
    }
}
