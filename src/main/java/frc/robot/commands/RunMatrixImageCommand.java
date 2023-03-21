package frc.robot.commands;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.numbers.N16;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MatrixLEDs;
import org.opencv.core.Mat;


public class RunMatrixImageCommand extends CommandBase {
    private final MatrixLEDs matrixLEDs;
    private final Mat matrixImage;

    public RunMatrixImageCommand(MatrixLEDs matrixLEDs, Mat matrixImage) {
        this.matrixLEDs = matrixLEDs;
        this.matrixImage = matrixImage;
        addRequirements(this.matrixLEDs);
    }

    @Override
    public void initialize() {
        matrixLEDs.setMat(matrixImage);
        matrixLEDs.start();
    }

    @Override
    public boolean isFinished() {
        return false ;
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        matrixLEDs.stop();
    }
}
