package frc.robot.commands;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class MyWaitCommand extends CommandBase{
    protected MyTimer m_timer = new MyTimer();
    private final double m_duration;
  
    public MyWaitCommand(double seconds) {
        m_duration = seconds;
        SendableRegistry.setName(this, getName() + ": " + seconds + " seconds");
        System.out.print("MyWaitCommand constructor: " + seconds + " seconds\n");
    }
    
    public void initialize() {
        System.out.print("MyWaitCommand initialize\n");
        m_timer.reset();
        m_timer.start();
      }
    
    public void end(boolean interrupted) {
        System.out.print("MyWaitCommand end\n");
        m_timer.stop();
    }
    
      public boolean isFinished() {
        System.out.print("MyWaitCommand isFinished\n");
        return m_timer.hasElapsed(m_duration);
      }
    
      public boolean runsWhenDisabled() {
        return true;
      }
    
      public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
        builder.addDoubleProperty("duration", () -> m_duration, null);
      }
    }
