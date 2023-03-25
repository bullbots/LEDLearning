package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MatrixLEDs;
import frc.robot.utility.MatrixVideo;
import frc.robot.utility.MatrixVideo.RunType;

import org.opencv.core.Mat;

import java.util.List;


public class RunMatrixVideoCommand extends CommandBase {
    private final MatrixLEDs matrixLEDs;

    private final MatrixVideo video;
    private Mat lastFrame;

    public RunMatrixVideoCommand(MatrixLEDs matrixLEDs, List<Mat> matrixImages, int robotCyclesPerFrame, RunType runType) {
        this.matrixLEDs = matrixLEDs;
        this.video = new MatrixVideo(matrixImages, robotCyclesPerFrame, runType);
        addRequirements(this.matrixLEDs);
    }

    @Override
    public void initialize() {
        video.restart();
        matrixLEDs.start();
    }

    @Override
    public void execute() {
        Mat frame = video.update();
        if (!frame.equals(lastFrame)) {
            matrixLEDs.setCustomMatrix(frame);
        }
        lastFrame = frame;
    }

    @Override
    public boolean isFinished() {
        return video.isFinished();
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        matrixLEDs.stop();
    }
}
