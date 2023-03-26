// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility.snake;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Board {
    private final int width;
    private final int height;

    public Board() {
        this.width = 16;
        this.height = 16;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean contains(Point point) {
        return point.x >= 0 && point.x < this.width && point.y >= 0 && point.y < this.height;
    }

    public Mat getMatrix(Snake snake) {
        Mat matrix = new Mat(height, width, CvType.CV_8UC3);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix.put(i, j, new double[] {0,0,0});
            }
        }
        for (Point bodyPart : snake.getBody()) {
            matrix.put(bodyPart.x, bodyPart.y, new double[] {0,255,0});
        }
        return matrix;
    }
}

