package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.subsystems.MatrixLEDs;

public class Flash extends SequentialCommandGroup {
    public Flash(MatrixLEDs aLEDSystem, double waitTime) {
        super(

            new EnableLEDs(aLEDSystem, 0).withTimeout(waitTime),
            new WaitCommand(waitTime), 
            new EnableLEDs(aLEDSystem, 0).withTimeout(waitTime)
    );
    }
    
}
