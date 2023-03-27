// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility.snake;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private Direction direction;
    private final List<Point> body;
    private int length;

    public Snake(Point startPoint) {
        this.direction = Direction.RIGHT;
        this.body = new ArrayList<>();
        this.body.add(startPoint);
        this.length = 1;
    }

    public void setDirection(Direction direction) {
        if (this.direction.getX() + direction.getX() == 0 &&
            this.direction.getY() + direction.getY() == 0) {
            return;
        }
        this.direction = direction;
    }

    public Point getHead() {
        return this.body.get(0);
    }

    public List<Point> getBody() {
        return this.body;
    }

    public int getLength() {
        return this.length;
    }

    public void move() {
        Point head = this.getHead();
        Point newHead = new Point(head.x + this.direction.getX(), head.y + this.direction.getY());
        this.body.add(0, newHead);
        if (this.body.size() > this.length) {
            this.body.remove(this.body.size() - 1);
        }
    }

    public void grow() {
        this.length++;
    }

    public boolean hasCollidedWithBody() {
        Point head = this.getHead();
        for (int i = 1; i < this.body.size(); i++) {
            if (head.equals(this.body.get(i))) {
                return true;
            }
        }
        return false;
    }
}
