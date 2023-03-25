// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility;

import java.util.List;

import org.opencv.core.Mat;

public class MatrixVideo {
    public enum RunType {
        ONCE,
        LOOP
    }

    private final List<Mat> frames;
    private final int loopsPerFrame;
    private final RunType runType;
    private int frameNumber = 0;
    private int loopNumber = 0;

    public MatrixVideo(List<Mat> frames, int loopsPerFrame, RunType runType) {
        this.frames = frames;
        this.loopsPerFrame = loopsPerFrame;
        this.runType = runType;
    }

    public void restart() {
        frameNumber = 0;
        loopNumber = 0;
    }

    public Mat update() {
        if (loopNumber++ > loopsPerFrame) {
            frameNumber++;
            loopNumber = 0;
        }
        if (frameNumber == frames.size()) {
            if (runType == RunType.LOOP) {
                frameNumber = 0;
            } else {
                return frames.get(frameNumber-1);
            }
        }
        return frames.get(frameNumber);
    }

    public boolean isFinished() {
        if (runType == RunType.LOOP) {
            return false;
        }
        return frameNumber >= frames.size();
    }
}
