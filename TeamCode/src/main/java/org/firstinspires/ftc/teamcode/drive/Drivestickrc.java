package org.firstinspires.ftc.teamcode.drive;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * This file contains an example of an iterative (Non-Linear) "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all iterative OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */
/*
    @Autonomous = this is for Autonomous mode
    @TeleOp = this is for User Controlled mode

    name = the name that will display on the Driver Hub
    group = allows you to group OpModes
 */
@TeleOp(name="DriverControl;)", group="yo")
@Disabled
@Config
//@Disabled  This way it will run on the robot
public class Drivestickrc extends OpMode {
    // Declare OpMode members.
    private final ElapsedTime runtime = new ElapsedTime();  //timer

    /*
    Declare motors to type DcMotorEx

    Documentation:
   x    
     */

    //Touch Sensors
    //private DigitalChannel intakeSensor;

    //Motors
    public Rev2mDistanceSensor sideLeftDistanceSensor;
    public Rev2mDistanceSensor sideRightDistanceSensor;
    public DcMotorEx wheelFL;
    public DcMotorEx wheelFR;
    public DcMotorEx wheelBL;
    public DcMotorEx wheelBR;
    public DcMotorEx Viper;
    public Servo Pixelflip;
    public Servo drone;
    public DcMotorEx Intake;



    //Servos



    private double speedMod;
    private final boolean rumbleLevel = true;
    private double rotation = 0;
    final double TRIGGER_THRESHOLD = 0.75;
    private int[] armLevelPosition = {0, 1200, 1800, 2400};
    private int[] flipposPosition = {0, 925};
    private boolean clawOpen = false;
    private int armLevel = 0;
    private double previousRunTime;
    private double inputDelayInSeconds = .5;


    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {


        telemetry.addData("Status", "Initialization Started");


        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).

        //Sets the motors hardwareMap in the driver hub
        wheelFL = hardwareMap.get(DcMotorEx.class, "wheelFL");
        wheelFR = hardwareMap.get(DcMotorEx.class, "wheelFR");
        wheelBL = hardwareMap.get(DcMotorEx.class, "wheelBL");
        wheelBR = hardwareMap.get(DcMotorEx.class, "wheelBR");
        Viper = hardwareMap.get(DcMotorEx.class, "viper");
        Intake = hardwareMap.get(DcMotorEx.class, "intake");
        // Sets the servos hardwareMap in the driver hub
        Pixelflip = hardwareMap.get(Servo.class, "Pixelflip");
        drone = hardwareMap.get(Servo.class, "Dronelauncher");



        // Sets the mode of the wheels to run with or without encoders
        wheelFL.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        wheelFR.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        wheelBL.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        wheelBR.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);


        Viper.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        Viper.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        Viper.setTargetPosition(260);
        Viper.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        Viper.setTargetPositionTolerance(50);

        // sets the direction of the wheels
        wheelFL.setDirection(DcMotorSimple.Direction.FORWARD);
        wheelFR.setDirection(DcMotorSimple.Direction.REVERSE);
        wheelBL.setDirection(DcMotorSimple.Direction.FORWARD);
        wheelBR.setDirection(DcMotorSimple.Direction.REVERSE);


        // Tell the driver that initialization is complete.
        telemetry.addData("Status", "Initialization Complete");


    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {


    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {

        runtime.reset();
        previousRunTime = getRuntime();

    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
//this will run the methods repeadtly
        precisionControl();
        drivingControl();
        Viperlift();
        pixelflip();
        dronelauncher();
        intake();


        telemetry.addData("Left Trigger Position", gamepad1.left_trigger);


        //Arm Slide Data
        telemetry.addData("velocity", Viper.getVelocity());
        telemetry.addData("slidePosition", Viper.getCurrentPosition());
        telemetry.addData("is at target", !Viper.isBusy());
        //Arm Slide Data
        telemetry.addData("Target Slide Position", armLevelPosition[armLevel]);
        telemetry.addData("Slide Position", Viper.getCurrentPosition());
        telemetry.addData("Velocity", Viper.getVelocity());
        telemetry.addData("is at target", !Viper.isBusy());
        telemetry.addData("Tolerance: ", Viper.getTargetPositionTolerance());

        // Show the elapsed game time and power for each wheel.
        telemetry.addData("Status", "Run Time: " + runtime.toString());
        //telemetry.addData("Motors", "wheelFL (%.2f), front right (%.2f), back left (%.2f),  right (%.2f)", wheelFL, wheelFR, wheelBL, wheelBR);

//        telemetry.addData("range", String.format("%.3f cm", sideDistanceSensor.getDistance(DistanceUnit.CM)));
//        telemetry.addData("range edited", sideDistanceSensor.getDistance(DistanceUnit.CM));

        telemetry.update();
    }


    public void precisionControl() {
        if (gamepad1.left_bumper) {
            speedMod = .25;

        } else if (gamepad1.right_bumper) {


        } else if (gamepad1.right_trigger > 0) {
            speedMod = 0.5;

        } else {
            speedMod = 1;
        }
    }

    public void drivingControl() {

        //gets controller input
//        double r = Math.hypot(gamepad1.left_stick_x, gamepad1.left_stick_y);
//
//        //make calculations based upon the input
//        double robotAngle = Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x) - Math.PI / 4;
//        double rightX = -gamepad1.right_stick_x;
//        rotation += 1 * rightX;
//        final double v1 = r * Math.cos(robotAngle) - rightX;
//        final double v2 = r * Math.sin(robotAngle) + rightX;
//        final double v3 = r * Math.sin(robotAngle) - rightX;
//        final double v4 = r * Math.cos(robotAngle) + rightX;

        //change the power for each wheel
        double accel = (gamepad1.right_trigger - gamepad1.left_trigger);
        double turnSpeed = gamepad1.left_stick_x;
        double strafeSpeed = gamepad1.right_stick_x;

        final double FL = (accel + turnSpeed - strafeSpeed);
        final double FR = (accel - turnSpeed - strafeSpeed);
        final double BL = (accel + turnSpeed + strafeSpeed);
        final double BR = (accel - turnSpeed + strafeSpeed);

//        func double to1(double = input) {
//            if (input > 1){
//                return 1;
//            }else if (input < -1) {
//                return -1;
//            }
//
//        }

        wheelFL.setPower(FL * speedMod);
        wheelFR.setPower(FR * speedMod);
        wheelBL.setPower(BL * speedMod);
        wheelBR.setPower(BR * speedMod);
    }
    public void Viperlift() {

        if ((gamepad1.dpad_up || gamepad2.dpad_up) && (armLevel < armLevelPosition.length - 1) && (getRuntime() - previousRunTime >= inputDelayInSeconds)) {

            previousRunTime = getRuntime();
            armLevel++;
        }
        if ((gamepad1.dpad_down || gamepad2.dpad_down) && (armLevel > 0) && (getRuntime() - previousRunTime >= inputDelayInSeconds) ) {

            previousRunTime = getRuntime();
            armLevel--;


        }

        //sets to driving level
        if (gamepad1.y || gamepad2.y)  {
            armLevel = 1;
        }

        if ((gamepad2.share)) {
            armLevel = 0;
        }


        if (armLevel == 1) {
            Viper.setVelocity(2000);
            //if statement to set speed only going down
        }

        if (getRuntime() - previousRunTime >= inputDelayInSeconds + .25 && rumbleLevel) {

        }
        Viper.setTargetPosition(armLevelPosition[armLevel]);
        Viper.setTargetPositionTolerance(20);

    }

    public void pixelflip() {
        if (gamepad2.left_trigger > 0) { //tune this value where u need it
            Pixelflip.setPosition(0);
        }
        if (gamepad2.right_trigger > 0) {
            Pixelflip.setPosition(1); //tune this value where u need it
        }


    }


        public void dronelauncher () {


            if (gamepad2.touchpad) {
                // move to 180 degrees when pressing the touchpad button on ps4..
                drone.setPosition(1);

            }
        }
        public void intake () {
            if (gamepad2.left_bumper) { // turns the intake to spin untill the pixel is in the bucket
                Intake.setPower(1);
            }
            else {
                Intake.setPower(0);
            }

        }
    }












/*
 * Code to run ONCE after the driver hits STOP
 *
 */

/*
 * Code to run ONCE after the driver hits STOP
 */


//@Override