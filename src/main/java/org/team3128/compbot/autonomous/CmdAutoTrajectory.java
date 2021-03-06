package org.team3128.compbot.autonomous; 

import org.team3128.common.control.trajectory.Trajectory;
import org.team3128.common.control.trajectory.TrajectoryGenerator;
import org.team3128.common.control.trajectory.constraint.TrajectoryConstraint;
import org.team3128.common.utility.math.Pose2D;
import org.team3128.common.utility.units.Length;
import org.team3128.compbot.subsystems.FalconDrive;
import org.team3128.common.drive.DriveSignal;
import org.team3128.compbot.subsystems.Constants;
import org.team3128.common.utility.Log;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj.Timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CmdAutoTrajectory implements Command {

    private Set<Subsystem> requirements;

    public ArrayList<Pose2D> waypoints = new ArrayList<Pose2D>();
    public Trajectory trajectory;

    private double speed, acceleration, timeExpected, timeInitial, timeCurrent, timeoutMs;
    private FalconDrive drive;

    public CmdAutoTrajectory(FalconDrive drive, double speed, double acceleration, double timeoutMs,
            Pose2D... inputWaypoints) {

        this.requirements = new HashSet<Subsystem>();

        this.speed = speed;
        this.acceleration = acceleration;

        this.drive = drive;
        this.requirements.add(drive);

        this.timeoutMs = timeoutMs;

        for (Pose2D ele : inputWaypoints) {
            waypoints.add(ele);
        }
    }

    @Override
    public Set<Subsystem> getRequirements() {
        return requirements;
    }

    @Override
    public void initialize() {
        trajectory = TrajectoryGenerator.generateTrajectory(waypoints, new ArrayList<TrajectoryConstraint>(), 0, 0,
                speed * Constants.MechanismConstants.inchesToMeters, acceleration, false);
        drive.setAutoTrajectory(trajectory, false);
        drive.startTrajectory();
        timeExpected = trajectory.getTotalTimeSeconds();
        timeInitial = Timer.getFPGATimestamp();
    }

    @Override
    public synchronized boolean isFinished() {
        timeCurrent = Timer.getFPGATimestamp();
        if ((timeCurrent - timeInitial) >= Math.min(timeExpected, timeoutMs)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void end(boolean interrupted) {
        drive.setWheelVelocity(new DriveSignal(0, 0));
    }
}