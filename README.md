# Raycaster

This is a quick port of "A first-person engine in 265 lines" from JavaScript to Java using the libGDX framework. 

* http://www.playfuljs.com/a-first-person-engine-in-265-lines/
* http://libgdx.badlogicgames.com/

![Screenshot](https://pbs.twimg.com/media/FWx8htEXoAAzElU.jpg:large)

## Porting details

Besides the obvious changes in rendering and input code there is four new types introduced that in the original code are represented either as an `Array` or `Object`.

* Point
* Projection
* Ray
* Step

## Building

To test it out download the repository and build it using gradle.

**Requirements**

* JDK - http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
* Gradle - http://www.gradle.org/

The following instructions assumes you are in the root folder of the project, eg.

    $ git clone https://github.com/jmcwilliams403/raycaster.git
    $ cd raycaster

The first time you build the project is going to take a while since all dependencies are downloaded, will be faster after that.

### Building the desktop version

The easiest way to test the project is on desktop, just run

    $ ./gradlew desktop:run

## License

The code is under GNU General Public License, ported from JavaScript.

The images used are from the article http://www.playfuljs.com/a-first-person-engine-in-265-lines/.
