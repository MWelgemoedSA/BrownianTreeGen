# BrownianTreeGen

A generator for brownian trees.

This is a fairly simple java exercise I did mainly for myself.

I did add some optimizations, although I suspect it could be made a lot more efficient with further work.

## Command line arguments

Command line arguments are xSize, ySize, pixelCount, threadCount, all must be integers. Can be followed by image filename and points csv filename");
Alternatively the first command line may be the string 'load', a csv to load and a maximum number of pixels to place before stopping. Optionally threadCount, image filename and a new csv filename."

Example of a Brownian Tree generated with it:

![Brownian Tree](https://raw.githubusercontent.com/MWelgemoedSA/BrownianTreeGen/master/ReadmeImages/BrownianTree.gif)

## Algorithm for generation:

Fundamentally a brownian tree is taking a single point, moving it about randomly (in my case taking a random step to any neighbouring pixel). If the point attempts to move into an existing pixel, it stops and becomes fixed at the position where it attempted the move.

This is fine for small trees, however the amount of random steps before a pixel hits another grows exponentially with a larger canvas, which means to generate a big tree (e.g 1000 x 1000), you need a lot of patience.

However, I am making the assumption that moving a pixel randomly within a circle until it hits the edge of the circle is the same as teleporting to a random point on circle's edge. So, everytime I try to move a pixel first I test the distance to the nearest placed pixel, if that distance is less than a few pixels, I default back to the normal random step logic. However, if it is greater than a few pixels I calculate the largest circle around my current pixel that doesn't contain a placed pixel and teleport to somewhere on its edge.

While this works well, and is very fast for generating brownian trees, it requires me to calculate the nearest neighbour of an arbitrary point quickly and efficiently. A naive nearest neigbour algorithm, which simply compares every point, does work and is still faster than the pure random step algorithm, but I decided to see if I can improve it further.

I decided to store my placed pixels in a [k-d tree](https://en.wikipedia.org/wiki/K-d_tree). Wikipedia's explanation of it is fairly dense and incomprehensible (although it does have some very informative images which I've used further on the readme), but in a nutshell all points are stored in a binary tree. At each level in the binary tree, it takes turns to split the points into a left and right side using either the X or the Y values of the points.

![kd tree](https://raw.githubusercontent.com/MWelgemoedSA/BrownianTreeGen/master/ReadmeImages/Tree_0001.png)

Calculating nearest neighbour of a given point when the placed pixels are stored in a k-d tree is log n, versus n for the naive approach. A k-d tree however is only log n if it's perfectly balanced, which mine isn't because points are constantly inserted without rebalancing. However, rebalancing it is an expensive operation and so far I've found it to be good enough.

![kd tree nearest neighbour search](https://raw.githubusercontent.com/MWelgemoedSA/BrownianTreeGen/master/ReadmeImages/KDTree-animation.gif)

I believe there's still a fair bit of room for optimizations, both in better algorithms and in better java code.