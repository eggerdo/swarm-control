<!-- Uses markdown syntax for neat display at github -->

# SwarmControl

## Introduction
In our attempt to control a swarm of robots we came up with a first draft of an app that will let you connect to a wide range of different robots, including but not limited to toy robots, cleaning robots, flying robots and our self developed arduino robot aka "Dotty". The app provides remote control for the different robots and provides the user with their sensor information. In a second stage the app will make use of the available sensors of each individual robot and control the robots either individually or as a swarm, depending on the task.

Another goal of the app will be to connect the robots to the Internet and interface them with the cloud. This will serve several purposes:
* The robots can be connected with each other even if they are physically far apart and communicate among them without the need of direct links
* The robots can store their sensor information in the cloud avoiding the need of storage capacity on the robot itself and facilitating the sharing of data between robots
* Computation-intensive tasks such as image processing or navigation can be offloaded to the cloud.

The swarm behaviours available in the app will cover subjects like playing (eg. play tag, hide and seek, etc), moving (eg. marching, following, dancing etc), chatting (eg. count each other) and charting (eg. searching, rescuing, guarding etc).

The app can be found on the Android market as the [Swarm Control app](https://play.google.com/store/apps/details?id=org.dobots.swarmcontrol). In the first draft we added the following two robots:

## IRobot Roomba
The iRobot Roomba 521 is equipped with a Bluetooth module ([RooTooth](https://www.sparkfun.com/products/684?) from Sparkfun) in order to connect it to the smartphone.

### Control
With this wireless connection we can control the Roomba remotely:
* Start autonomous behaviour actions provided by the Roomba such as Clean, Spot and Dock
* Activate the brushes and vacuum cleaner
* Move the Roomba manually forward and backward  wasell as rotate the Roomba on the spot using arrow buttons
* Drive the Roomba around using the smartphone's accelerometer

### Sensors
We can also query all of the Roomba's sensors and display them on the smartphone. This means we can see if the Roomba detects a cliff, bumps in an obchastacle or detects a wall. We can also monitor the power consumption, the remaining battery charge and the rging state.

For now this information is only displayed on the smartphone and not used further. But in a later stage, this sensory information will allow us to develop behaviour for a swarm of Roombas. One possible scenario could be a team of two or more robots which coordinate the cleaning of a floor and use the same charging station but in such a way that there will never be one robot waiting for another to finish.

## Mindstorm NXT
The Mindstorm NXT 2.0 comes equipped with a Bluetooth module which made it perfect for a fast incorporation. We fixed the design of the robot to a differential robot for now and connected the left wheel motor to output port A and the right wheel motor to output port B.

### Control
As with the Roomba, the NXT can be controlled remotely:
* Move the NXT forward and backward as well as rotate it on the spot using arrow buttons
* Drive the NXT around using the smartphone's accelerometer

### Sensors
In contrary to the Roomba we can choose the type of sensors we want to add to the NXT from a wide variety. Moreover, we are not fixed to a certain design of the robot but want to be able to add robots with different sets of sensors. To reflect this we can define in the app for each input port what type of sensor is attached and enable only the sensors we want to observe.

Besides the sensors we can also monitor the encoder of the motors which provide us with a tacho count and gives us a sense of direction and distance that the robot has travelled.

## Upcoming
Since the app is just starting to take shape and is under heavy development there are a lot of things that need to be done still.
* Several Robots are waiting to be included in the app: Surveyor, Meccano Spykee, SpyGear Trakr, FinchRobot, and many more
* Multiple robots should be able to be paired into a swarm and then controlled simultaneously
* Swarm Behaviours need to be implemented, starting with simple flock movements and dancing patterns
* And not forgetting, the robots want to be hooked up to the Internet and the cloud

If you are interested you should definitely check back on our website http://www.dobots.nl where we will keep track of the development. But also the app is completely open source; you can find the code at https://github.com/eggerdo/swarm-control/. Feel free to contribute and add your own robots to the list or let us know which ones you want to see added!

