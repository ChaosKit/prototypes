# ChaosKit Architecture

<!--TOC max3-->

## Use cases

ChaosKit will have at least two user-facing applications: a command-line offline renderer, a graphical editor. I imagine the offline renderer will create images of large resolution (think 8192x8192 and bigger) and rendering them real-time seems impractical. The GUI, on the other hand, should support near real-time previews and editing support for its existence to make sense.

Both use cases are essentially still the same application — a fractal flame renderer. Therefore, sharing a large part of their codebase seems a natural choice.

### The Editor

Fractal flames have one significant problem because of their very nature: they're hard to predict. Also, they have lots of parameters to configure their appearance. Because of that, creating a GUI to edit them is very challenging. Of course, putting every parameter in a text field would be easy, but we wan't to make a _user-friendly_ editor, don't we? ;)

Of course, there's some prior art in this area. It's hard to write about fractal flames without mentioning [Apophysis](https://sourceforge.net/projects/apophysis7x/), probably the most feature-complete editor to date. I've also looked at [Chaotica](https://chaoticafractals.com/), which is unique (at least from what I've seen) in that it's a paid application. There's also [flam4](https://sourceforge.net/projects/flam4/), but I haven't tested it.

An interesting thing about all of those editors — they seem to be outdated. There have been no new releases in over two years for both of them.

I don't want to make a competitive analysis of other editors; that's not the point of this document. I just want to highlight that they both have interfaces that are hard to grasp (at least for me) and that's something to improve upon.

#### What I want

From a high level:

* having graphical tools; at least something to manipulate affine transformations
* as close to real-time feedback as possible
* modern, user-friendly interface — probably inspired by [Pixelmator Pro](https://www.pixelmator.com/pro/), if feasible

At this point I should probably start prototyping a design. Will link to it from here.

### Offline renderer

There's nothing much to it, really. It should be a CLI that takes an input and a resolution and outputs a PNG image as fast as possible. No editing, no further configuration, just speed. Utilizing OpenCL or CUDA would be still cool.

## Brainstorming

### The Editor

The ordering is pretty much random.

* close to real-time rendering
* the ability to draw over the rendering output (for graphical tools)
* recoloring without resetting?
  * would be possible if using the palette as a gradient map, **need to investigate**
* voronoi transform groups
  * each transform inside the group has a point
  * if a voronoi transform is picked through the normal weighted picking, the transform closest to the last point is picked from the group (thx for the reddit guy for the idea, **need to investigate and reach back to him**)
  * a voronoi diagram overlay would be nice :D
* a palette editor
  * ship with the original flam3 palette XML
  * add custom ones!
  * pretty much what Photoshop has, with regular and noise palettes
* loading and saving files, duh
  * of course with exporting images
  * embedding the source file in PNGs would be a nice bonus
* the randomizer
  * a list/tree of features to randomize with selectable ranges etc.
  * can't really imagine the GUI for it, sounds really advanced
  * alternative (or complementary feature): a 3x3 grid of nice-looking flames; each click on a would yield similar flames in other ones or something
* animation editor
  * nice timeline, like in non-linear video editing apps
  * interpolate parameters with b-splines
* variation parameters should be hidden somewhere, not exposed, because they're intimidating
* grids and rulers
  * useful for drawing sierpinskis
* import from flam3
* interoperability with electric sheep
  * maybe once all the other features will land ;P
  * import flames from the server and render them
* interoperability with apophysis
  * once we have feature parity ;P


