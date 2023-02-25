package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.subsystems.SetLEDs;

public class Flash extends SequentialCommandGroup {
    public Flash(SetLEDs aLEDSystem, double waitTime) {
        super(
            new EnableLEDs(aLEDSystem, 0).withTimeout(waitTime),
            new WaitCommand(waitTime), 
            new EnableLEDs(aLEDSystem, 0).withTimeout(waitTime)
    );
    }
    
}
