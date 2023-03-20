package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.*;
import frc.robot.subsystems.MatrixLEDs;

import java.lang.reflect.Field;


public class FlashRepeatCommand extends RepeatCommand {
    private final MatrixLEDs matrixLEDs;

    private int runTimes = 2;
    private int hasRun = 0;
    private boolean prevRun = false;

    private Field m_endedField;

    public FlashRepeatCommand(int runTimes, double flashPeriod, MatrixLEDs matrixLEDs) {
        this(flashPeriod, matrixLEDs);
        this.runTimes = runTimes;
    }

    public FlashRepeatCommand(double flashPeriod, MatrixLEDs matrixLEDs) {
        super(new SequentialCommandGroup(
                new EnableLEDs(matrixLEDs, 0).withTimeout(flashPeriod * 0.5),
                new WaitCommand(flashPeriod * 0.5)
        ));
        this.matrixLEDs = matrixLEDs;
        // each subsystem used by the command must be passed into the
        // addRequirements() method (which takes a vararg of Subsystem)
        addRequirements(this.matrixLEDs);

        try {
            m_endedField = RepeatCommand.class.getDeclaredField("m_ended");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        m_endedField.setAccessible(true);
    }

    @Override
    public void initialize() {
        super.initialize();
        hasRun = 0;
    }

    @Override
    public boolean isFinished() {
        boolean curRun = false;
        try {
            curRun = (boolean) m_endedField.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (curRun && !prevRun) {
            hasRun++;
        }

        if (hasRun >= runTimes) {
            return true;
        }

        prevRun = curRun;
        return false;
    }
}
