// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility.snake;

import org.opencv.core.Mat;

import java.util.List;

// Thanks ChatGPT

import java.util.Random;

public class SnakeGame {
    private final Board board;
    private final Snake snake;
    private Point fruit;

    public SnakeGame() {
        this.board = new Board();
        this.snake = new Snake(new Point(1, 1));
        this.eatFruit();
    }

    public void eatFruit() {
        this.snake.grow();
    
        // Generate a new random location for the fruit
        Random rand = new Random();
        Point newFruit;
        do {
            newFruit = new Point(rand.nextInt(this.board.getWidth()), rand.nextInt(this.board.getHeight()));
        } while (this.snake.getBody().contains(newFruit)); // Make sure the fruit is not on the snake's body
    
        this.fruit = newFruit;
    }
    

    public Mat getMatrix() {
        Mat mat = board.getMatrix(snake);
        mat.put(fruit.x, fruit.y, new double[] {0,0,255});
        return mat;
    }

    public void update() {
        this.snake.move();

        if (this.snake.getHead().equals(this.fruit)) {
            this.snake.grow();
            this.eatFruit();
        }
    }

    public boolean hasLost() {
        Point head = this.snake.getHead();

        // Check if the snake's head is out of bounds
        if (!this.board.contains(head)) {
            return true;
        }

        // Check if the snake's head collided with its body
        List<Point> body = this.snake.getBody();
        if (body.size() > 1) {
            for (int i = 1; i < body.size(); i++) {
                if (head.equals(body.get(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setDirection(Direction direction) {
        this.snake.setDirection(direction);
    }

    public Point getFruit() {
        return this.fruit;
    }

    public Snake getSnake() {
        return this.snake;
    }
}


