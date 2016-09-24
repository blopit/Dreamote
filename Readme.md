Dreamote
==============

OpenCV draw buttons and sliders sends inputs to arduino 
--------------
Inspiration
--------------

We love it when thing we create come to life so we decided to make a drawable remote control

What it does
--------------

You draw the controls and you can interact with them in the real world and it will output to serial port to arduino and turn on lights and things

How we built it
--------------

We used OpenCV image processing Java library to detect specific contour shapes for controls, and detect skin colour blobs for user interaction. We then checked if a specific vertex of the bloc is interacting with a draw element. If we send an output to serial port with is then decoded by the Arduino board and it performs some task like dimming or turning off/on an led.
Challenges we ran into

OpenCV can be really hard to work with since Java support is new and thus there is extremely little documentation and stackoverflow questions It was hard to consistently and effectively detect hands so we opted to only perfectly detects Shrenil's hand colour
Accomplishments that we're proud of

This is the first time we were able to finish a project of this scale and amazingness in such a sort amount of time. 