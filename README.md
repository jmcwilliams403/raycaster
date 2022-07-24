# Raycaster

Based on Fredrik Wallgren's Java port of "A first-person engine in 265 lines" using the libGDX framework. 

* http://www.playfuljs.com/a-first-person-engine-in-265-lines/
* http://libgdx.badlogicgames.com/

![Screenshot](https://pbs.twimg.com/media/FYdQNP8XEAEO_mQ.jpg:large)

## Building

To test it out download the repository and build it using gradle.

**Requirements**

* JDK - http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
* Gradle - http://www.gradle.org/

The following instructions assumes you are in the root folder of the project, eg.

    $ git clone https://github.com/jmcwilliams403/raycaster.git
    $ cd raycaster

The first time you build the project is going to take a while since all dependencies are downloaded, will be faster after that.

**Building the desktop launcher**

    $ ./gradlew desktop:run

## License

The code is under GNU General Public License Version 3
