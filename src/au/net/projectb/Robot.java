package au.net.projectb;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.GenericHID.Hand;

/**
 * The Java Virtual Machine on the roboRIO is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	VictorSP leftDriveBack;
	VictorSP leftDriveFront;
	
	VictorSP rightDriveFront;
	VictorSP rightDriveBack;
	
	Spark leftIntake;
	Spark rightIntake;
	
	TalonSRX armMotor;
	double armSetpoint;
	
	Joystick stick;	
	boolean stickReversed;
	XboxController xbox;
	
	UsbCamera driveCamera;
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialisation code.
	 */
	@Override
	public void robotInit() {
		leftDriveBack = new VictorSP(0); // PWM Port, madke sure this is set correctly.
		leftDriveFront = new VictorSP(1);
		
		rightDriveFront = new VictorSP(2);
		rightDriveBack = new VictorSP(3);
		
		leftIntake = new Spark(5);
		rightIntake = new Spark(6);
		
		armMotor = new TalonSRX(10);
		armMotor.setNeutralMode(NeutralMode.Brake);
		armMotor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 0);
		armMotor.configPeakCurrentLimit(30, 0);
		armMotor.configPeakCurrentDuration(250, 0);
		armMotor.configContinuousCurrentLimit(20, 0);
		armMotor.configClosedloopRamp(0.25, 0);
		armMotor.configOpenloopRamp(0.375, 0);
		armMotor.enableCurrentLimit(true);
		
		armMotor.configPeakOutputForward(1.0, 0);
		armMotor.configPeakOutputReverse(-1.0, 0);
		
		armMotor.config_kP(0, 0.0, 0);
		
		armSetpoint = armMotor.getSelectedSensorPosition(0);
		
		stick = new Joystick(0);
		stickReversed = false;
		xbox = new XboxController(1); // USB port, set in driverstation.
		
		driveCamera = CameraServer.getInstance().startAutomaticCapture(0);
	}

	/**
	 * Runs once at the start of auto.
	 */
	@Override
	public void autonomousInit() {
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		if (edu.wpi.first.wpilibj.DriverStation.getInstance().getMatchTime() > 12.3) {
			drive(0.3, 0.0);
		} else {
			drive(0.0, 0.0);
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		
		if (stick.getRawButtonPressed(2)) {
			stickReversed = !stickReversed;
		}
		
		// double means a floating point (decimal) number with a precision of "hella"
		double power = -stick.getY(Hand.kLeft); // negated, because microsoft is weird and up is negative
		double steering = stick.getX(Hand.kRight); 
		if (stickReversed) {
			power = -power;
		}
		drive(power, steering);
		
//		if (xbox.getBumper(Hand.kRight)) {
//			leftIntake.set(xbox.getTriggerAxis(Hand.kLeft));	
//			rightIntake.set(-0.9);
//		} else if (xbox.getBumper(Hand.kLeft)) {
//			leftIntake.set(-1.0);
//			rightIntake.set(1.0);
//		} else {
//			leftIntake.set(0.0);
//			rightIntake.set(0.0);
//		}
		
		if (xbox.getBumper(Hand.kLeft)) {  // shoot out
			leftIntake.set(-1.0);
		} else { // intake
			leftIntake.set(xbox.getTriggerAxis(Hand.kLeft));
		}
		
		if(xbox.getBumper(Hand.kRight)) { //shooty i think
			rightIntake.set(1.0);
		} else { //intake
			rightIntake.set(-xbox.getTriggerAxis(Hand.kRight));
		}
//		armSetpoint += xbox.getY(Hand.kLeft) * 60;
//		System.out.println(armSetpoint);
		
		armMotor.set(ControlMode.PercentOutput, xbox.getY(Hand.kLeft) * 0.8);
		System.out.println(xbox.getY(Hand.kLeft));
	}
	
	/**
	 * 
	 * @param power power of the motor controllers from -1 to +1
	 * @param steering scale -1 left, to +1 right
	 */
	public void drive(double power, double steering) {
		leftDriveBack.set(power + steering);
		leftDriveFront.set(power + steering);
		
		rightDriveFront.set(-(power - steering));
		rightDriveBack.set(-(power - steering));
	}
}
