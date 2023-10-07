import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

//utils class to help with computing the cheapest seam
class Utils {

  // returns a new cheapest seam info for the pixelgraph, either a vertical or
  // horizontal seam
  SeamInfo computeCheapestSeam(BorderPixel borderPixel, String direction, int width, int height) {

    // arraylist to store one row of seams
    ArrayList<SeamInfo> seams = new ArrayList<>();
    // current pixel, starting at top left
    APixel currPixel = borderPixel.topLeft;

    // the bound is either the hight or width of the pixelgraph, based on the
    // direction of seam we are taking out
    int bound;
    if (direction.equals("vertical")) {
      bound = width;
    } else if (direction.equals("horizontal")) {
      bound = height;
    } else {
      throw new IllegalArgumentException();
    }

    // for each pixel in the first row/column, create a new seam info with a null
    // camefrom
    for (int index = 0; index < bound; index += 1) {
      // add the seam to seams
      seams.add(new SeamInfo(currPixel, direction));
      if (direction.equals("vertical")) {
        currPixel = currPixel.right;
      } else {
        currPixel = currPixel.down;
      }
    }

    // left pixel is the pixel below topleft
    APixel prevPixel;
    if (direction.equals("vertical")) {
      prevPixel = borderPixel.topLeft.down;
    } else {
      prevPixel = borderPixel.topLeft.right;
    }

    // beginning of row or column is meant to
    // keep a marker pointing to the beginning of a row or column
    // again depends on the direction of the seam
    APixel beginningOfRowOrColumn = prevPixel;

    // for each row in the pixel image, not including the first row
    int otherDimensionBound;
    if (direction.equals("vertical")) {
      otherDimensionBound = height;
    } else {
      otherDimensionBound = width;
    }

    // for each row/column beyond the first, mutate the seams arraylist
    // to contain a new arraylist of cheapest seams, based on the current row/column
    for (int index = 1; index < otherDimensionBound; index += 1) {

      this.calculateSeamsForRowOrColumn(seams, prevPixel, direction, bound);

      if (direction.equals("vertical")) {
        beginningOfRowOrColumn = beginningOfRowOrColumn.down;
      } else {
        beginningOfRowOrColumn = beginningOfRowOrColumn.right;
      }
      prevPixel = beginningOfRowOrColumn;
    }

    // find the min of the seam info arraylist
    SeamInfo currMin = seams.get(0);
    // for each seam in the arraylist, compare the seam to the currMin to see
    // if the totalweight is lower
    for (SeamInfo seam : seams) {
      if (currMin.compareSeamInfo(seam) > 0) {
        currMin = seam;
      }
    }

    return currMin;
  }

  // mutates the seams arraylist to update each index to a new cheapest seam
  void calculateSeamsForRowOrColumn(ArrayList<SeamInfo> seams, APixel prevPixel,
      String direction, int bound) {

    SeamInfo prevCalculated = null;

    // for each column/row in the current column/row(this method is called inside a
    // for loop that iterates through
    // columns/rows), get accumulate a previously calculated cheapest seam (the new
    // cheapest seam for the prior index),
    for (int index = 0; index < bound; index += 1) {

      prevCalculated = this.calculateNewMin(prevCalculated, prevPixel, seams, index, direction,
          bound);
      // move the left pixel to the right
      if (direction.equals("vertical")) {
        prevPixel = prevPixel.right;
      } else {
        prevPixel = prevPixel.down;

      }
    }
    // after the for loop, the last seaminfo in seams hasn't been updated, so update
    // it
    seams.set(bound - 1, prevCalculated);
  }

  // returns a new minimum seam info based on the given pixel and seams
  SeamInfo calculateNewMin(SeamInfo previouslyCalculated, APixel leftPixel,
      ArrayList<SeamInfo> seams, int colIndex, String direction, int bound) {

    // temporary arraylist to store the possible seams that this new seam will stem
    // from
    ArrayList<SeamInfo> possibleNewSeams = new ArrayList<>();

    // if this isn't the furthest left/top pixel of this row/column
    if (colIndex > 0) {
      possibleNewSeams.add(seams.get(colIndex - 1));
    }
    // if this isn't the furthest right/bottom pixel of this row/colum
    if (colIndex < bound - 1) {
      possibleNewSeams.add(seams.get(colIndex + 1));
    }
    possibleNewSeams.add(seams.get(colIndex));

    SeamInfo currMin = possibleNewSeams.get(0);

    // for each possible seam (after the first), compare to the minimum
    for (int index = 1; index < possibleNewSeams.size(); index += 1) {
      if (currMin.compareSeamInfo(possibleNewSeams.get(index)) > 0) {
        currMin = possibleNewSeams.get(index);
      }
    }

    // if this isn't the first call, update the seams arraylist
    if (colIndex > 0) {
      seams.set(colIndex - 1, previouslyCalculated);
    }
    // return a new seam info containing the given pixel, direction, and min seam
    return new SeamInfo(leftPixel, currMin, direction);

  }
}

// represents an APixel
abstract class APixel {
  Color color;
  APixel left;
  APixel right;
  APixel up;
  APixel down;
  // if this pixel was highlighted for removal or not
  boolean highLighted;

  // constructor which takes in fields for left...
  APixel(Color color, APixel left, APixel right, APixel up, APixel down) {
    this.left = left;
    this.right = right;
    this.up = up;
    this.down = down;
    this.color = color;

    if (this.left == null || this.right == null || this.up == null || this.down == null) {
      throw new IllegalArgumentException();
    }

    // updates the given APixels to point at this pixel
    this.left.update(this, "right");
    this.right.update(this, "left");
    this.up.update(this, "down");
    this.down.update(this, "up");

    // check connections ensures that the pixels around this pixel have
    // proper connections, and by extension, this pixel has proper connections.
    // we can't check directly if this pixel has proper connections because
    // as the graph is being built, the right of this pixel is just a border pixel,
    // until the
    // right pixel is actually instantiated
    if (this.left.checkConnections() || this.right.checkConnections() || this.up.checkConnections()
        || this.down.checkConnections()) {

      throw new IllegalArgumentException("Invalid formation of pixels");
    }
    this.highLighted = false;

  }

  // default constructor for a Apixel, makes the Apixel point to itself
  APixel(Color color) {
    this.left = this;
    this.right = this;
    this.up = this;
    this.down = this;
    this.color = color;
    this.highLighted = false;
  }

  // highlights this pixel
  void highlight() {
    this.highLighted = true;
  }

  // sets the color of the pixel at the given position in the given image to a
  // color
  // corresponding with this pixel
  void setColor(ComputedPixelImage image, int colorState, Posn position) {

    Color color;

    // if this pixel is highlighted, return red
    // also turn off the highlighting, because this pixel will
    // be deleted next frame
    if (this.highLighted) {
      this.highLighted = false;
      color = Color.RED;
    }

    // if the colorState is 0, just return the color of the pixel
    else if (colorState == 0) {
      color = this.color;
    }

    // if the state is 1, return the energy color of the pixel
    else if (colorState == 1) {
      // sqrt(32) is the max energy a
      // pixel can have
      double energyColor = (this.computeEnergy() / Math.sqrt(32));
      color = new Color((int) (energyColor * 255), (int) (energyColor * 255),
          (int) (energyColor * 255));
    } else {
      color = this.color;
    }

    image.setColorAt(position.x, position.y, color);

  }

  // removes the current pixel from the seam,
  // then removes the rest of the seam
  abstract void remove(SeamInfo seam, String direction, APixel prev);

  // inserts the current pixel back into the pixel graph
  // then inserts the rest of the seam
  abstract void insert(SeamInfo seam, String direction);

  // returns true if this pixel has invalid connections
  boolean checkConnections() {
    return (this.left.up != this.up.left || this.right.up != this.up.right
        || this.left.down != this.down.left || this.right.down != this.down.right);

  }

  // updates a pointer with the given APixel
  abstract void update(APixel other, String direction);

  // returns the brightness of this pixel 0-1
  double computeBrightness() {
    return (this.color.getRed() + this.color.getBlue() + this.color.getGreen()) / (3 * 255.0);
  }

  // returns the energy of this pixel, 0 - root(32)
  double computeEnergy() {
    double horizontalEnergy = (this.left.up.computeBrightness()
        + (2 * this.left.computeBrightness()) + this.left.down.computeBrightness())
        - (this.right.up.computeBrightness() + (2 * this.right.computeBrightness())
            + this.right.down.computeBrightness());
    double verticalEnergy = (this.up.left.computeBrightness() + (2 * this.up.computeBrightness())
        + this.up.right.computeBrightness())
        - (this.down.left.computeBrightness() + (2 * this.down.computeBrightness())
            + this.down.right.computeBrightness());
    return Math.sqrt((horizontalEnergy * horizontalEnergy) + (verticalEnergy * verticalEnergy));
  }

  // updates the topLeft of the APixel
  abstract void updateTopLeft(APixel maybeTopLeft, APixel updateTo);
}

// represents the borderpixel of a pixelimage, used to close off loose ends
class BorderPixel extends APixel {

  // points to the top left pixel of a pixelimage
  APixel topLeft;

  BorderPixel() {
    // has to be black
    super(Color.BLACK);
    topLeft = null;
  }

  // sets the topleft
  void setTopLeft(APixel topLeft) {
    this.topLeft = topLeft;
  }

  // does nothing, because this pixel always gotta point to itself
  void update(APixel other, String direction) {
    return;
  }

  // removes the rest of the seam
  void remove(SeamInfo seam, String direction, APixel prev) {
    if (seam != null) {
      seam.remove(this);
    }
  }

  // inserts the rest of the seam
  void insert(SeamInfo seam, String direction) {
    if (seam != null) {
      seam.insert();
    }
  }

  // updates the topleft field ONLY IF THE GIVEN MAYBETOPLEFT IS THE ACTUAL
  // TOPLEFT YO
  void updateTopLeft(APixel maybeTopLeft, APixel updateTo) {
    if (this.topLeft == maybeTopLeft) {
      if (updateTo != this) {
        this.topLeft = updateTo;
      }
    }
  }
}

// represents a PIXEL!!!
class Pixel extends APixel {

  // yuh
  Pixel(Color color, APixel left, APixel right, APixel up, APixel down) {

    super(color, left, right, up, down);
  }

  // has no top left so how can it be updated ??????!?!?!??!!?
  void updateTopLeft(APixel maybeTopLeft, APixel updateTo) {
    return;

  }

  // updates the pointers based on the direction
  void update(APixel other, String direction) {
    if (direction.equals("left")) {
      this.left = other;
    } else if (direction.equals("right")) {
      this.right = other;
    } else if (direction.equals("up")) {
      this.up = other;
    } else if (direction.equals("down")) {
      this.down = other;
    } else {
      throw new IllegalArgumentException();
    }
  }

  // removes this pixel and then the rest of the seam
  void remove(SeamInfo seam, String direction, APixel prev) {

    // if the seam is vertical
    if (direction.equals("vertical")) {

      // update this.right and this.left to point at eachother
      this.right.update(this.left, "left");
      this.left.update(this.right, "right");

      // if the previous removed APixel is this.right.down, update this.right
      // and this.down to point at eachother
      if (prev == this.right.down) {
        this.right.update(this.down, "down");
        this.down.update(this.right, "up");
      }
      // else if prev is this.left.down, update this.left and this.down to point at
      // eachother
      else if (prev == this.left.down) {
        this.left.update(this.down, "down");
        this.down.update(this.left, "up");
      }
      this.left.updateTopLeft(this, this.right);

    }

    // if this is a horizontal seam
    else if (direction.equals("horizontal")) {

      // update this.down and this.up to point at eachother
      this.up.update(this.down, "down");
      this.down.update(this.up, "up");

      // if previously removed APixel is this.up.right, update
      // this.up and this.right to point at eachother (side by side)
      if (prev == this.up.right) {
        this.up.update(this.right, "right");
        this.right.update(this.up, "left");
      }
      // update this.down and this.right to point at eachother (side by side)
      else if (prev == this.down.right) {
        this.down.update(this.right, "right");
        this.right.update(this.down, "left");
      }

      this.left.updateTopLeft(this, this.down);

    } else {
      throw new IllegalArgumentException();
    }
    // removes the seamster
    if (seam != null) {
      seam.remove(this);
    }

  }

  // inserts this pixel back into da pixelgraph
  void insert(SeamInfo seam, String direction) {

    // update this's connections to point back at this
    this.left.update(this, "right");
    this.right.update(this, "left");
    this.down.update(this, "up");
    this.up.update(this, "down");

    // restore the topleft APixel if needed
    if (direction.equals("vertical")) {
      this.left.updateTopLeft(this.right, this);
    } else if (direction.equals("horizontal")) {
      this.left.updateTopLeft(this.down, this);
    }

    // inserts the rest of the seam
    if (seam != null) {
      seam.insert();
    }

  }

}

// represents a pixel graph
class PixelGraph {
  // pixel image corresponding to this pixelgraph
  ComputedPixelImage pixelImage;
  // width in pixels
  int width;
  // height in pixels
  int height;
  // borderpixel
  BorderPixel borderPixel;
  // the history of seams removed
  ArrayList<SeamInfo> removalHistory;

  PixelGraph(FromFileImage image) {
    this.borderPixel = new BorderPixel();
    this.width = (int) image.getWidth();
    this.height = (int) image.getHeight();
    this.pixelImage = new ComputedPixelImage(this.width, this.height);

    if (this.width == 0 || this.height == 0) {
      throw new IllegalArgumentException("Image is too smol");
    }

    // starts creating the graph of pixels,
    APixel abovePixel = borderPixel;
    // iterates through each row, then column of the given fileImage to
    // create a pixel object for every pixel in the image
    for (int y = 0; y < this.height; y++) {
      APixel leftPixel = borderPixel;
      APixel leftOfRow = borderPixel;
      // for each column
      for (int x = 0; x < this.width; x++) {
        // update the left pixel to be a new pixel with the color at x,y
        // basically works like this:
        // p -> p -> p -> p
        leftPixel = new Pixel(image.getColorAt(x, y), leftPixel, borderPixel, abovePixel,
            borderPixel);
        // if this is the very first pixel, set it as top left
        if (x == 0 && y == 0) {
          this.borderPixel.setTopLeft(leftPixel);
        }
        // if this is the first pixel of the row, leave behind a marker that can be used
        // to access the beginning of the next
        // row
        if (x == 0) {
          leftOfRow = leftPixel;
        }
        // iterate the pixel above left pixel to the right
        abovePixel = abovePixel.right;
      }
      // at the end of the first iteration of the row loop, change the above
      // pixel from the border pixel to the topleft
      if (y == 0) {
        abovePixel = this.borderPixel.topLeft;
      }
      // otherwise, the above pixel is just the beginning of the previous row\
      else {
        abovePixel = leftOfRow;
      }
    }
    this.removalHistory = new ArrayList<>();
  }

  // highlights the cheapest seam red
  void highlightSeamRed(int directionInt) {
    String directionString;
    if (directionInt == 0) {
      directionInt = (int) (Math.random() * 2 + 1);
    }
    if (directionInt == 1) {
      directionString = "vertical";
    } else if (directionInt == 2) {
      directionString = "horizontal";
    } else {
      throw new IllegalArgumentException();
    }

    if (this.width > 0 && this.height > 0) {
      // adds the cheapest seam to history
      this.removalHistory
          .add(new Utils().computeCheapestSeam(this.borderPixel, directionString, this.width,
              this.height));
      // creates a copy
      SeamInfo tempCheapest = this.removalHistory.get(this.removalHistory.size() - 1);
      tempCheapest.hightlightPixel();
    }

  }

  // removes the cheapest seam
  void removeCheapestSeam() {
    if (this.width > 0 && this.height > 0) {
      SeamInfo toBeRemoved = this.removalHistory.get(this.removalHistory.size() - 1);
      // subtracts one from the width cuz this seam is getting rid of one pixel from
      // every row
      if (toBeRemoved.direction.equals("vertical")) {
        this.width -= 1;

      } else {
        this.height -= 1;
      }
      toBeRemoved.remove(this.borderPixel);
    }
  }

  // redraws the pixel image given the state
  void redrawPixelImage(int colorState) {

    if (this.width > 0 && this.height > 0) {
      APixel leftOfRow = this.borderPixel.topLeft;
      APixel leftPixel = this.borderPixel.topLeft;

      this.pixelImage = new ComputedPixelImage(this.width, this.height);
      // goes through each row of the pixel graph
      for (int rowIndex = 0; rowIndex < this.height; rowIndex += 1) {

        // goes through each pixel of the row, and sets the pixelImage pixel at this
        // position
        // to the color of that pixel, given a colorstate
        for (int colIndex = 0; colIndex < this.width; colIndex += 1) {
          leftPixel.setColor(this.pixelImage, colorState, new Posn(colIndex, rowIndex));
          leftPixel = leftPixel.right;
        }
        leftOfRow = leftOfRow.down;
        leftPixel = leftOfRow;
      }
    }
    // if the w/h is 0, draw a blank pixel image
    else {
      this.pixelImage = new ComputedPixelImage(1, 1);
    }
  }

  // adds the cheapest seam back into the pixelgraph
  void reInsertCheapestSeam() {

    if (this.removalHistory.size() > 0) {
      // gets the most recently removed seam(the last one in the history)
      SeamInfo toBeInserted = this.removalHistory.remove(this.removalHistory.size() - 1);
      if (toBeInserted.direction.equals("vertical")) {
        this.width += 1;
      } else {
        this.height += 1;
      }
      toBeInserted.insert();

    }
  }

}

// represents a Seam Info
class SeamInfo {

  APixel pixel;
  SeamInfo cameFrom;
  double totalWeight;
  String direction;

  // used for the first Seam of every column, has no prev seam so its null
  SeamInfo(APixel pixel, String direction) {
    if (pixel == null) {
      throw new IllegalArgumentException();
    }
    this.pixel = pixel;
    this.cameFrom = null;
    this.totalWeight = this.pixel.computeEnergy();
    if (!(direction.equals("vertical") || direction.equals("horizontal"))) {
      throw new IllegalArgumentException();
    }
    this.direction = direction;
  }

  // used for every subsequent Seam, adds the pixel's totalweight to
  // the totalweight of the cameFrom seam
  SeamInfo(APixel pixel, SeamInfo cameFrom, String direction) {
    if (pixel == null || cameFrom == null) {
      throw new IllegalArgumentException();
    }
    this.pixel = pixel;
    this.cameFrom = cameFrom;
    this.totalWeight = cameFrom.totalWeight + this.pixel.computeEnergy();

    if (!(direction.equals("vertical") || direction.equals("horizontal"))) {
      throw new IllegalArgumentException();
    }
    if (!direction.equals(cameFrom.direction)) {
      throw new IllegalArgumentException();
    }
    this.direction = direction;
  }

  // removes this seam from the graph
  void remove(APixel prev) {
    this.pixel.remove(this.cameFrom, this.direction, prev);
  }

  // inserts this seam into the graph
  void insert() {
    this.pixel.insert(this.cameFrom, this.direction);
  }

  // highlights this seam in the graph
  void hightlightPixel() {
    this.pixel.highlight();
    if (this.cameFrom != null) {
      this.cameFrom.hightlightPixel();
    }
  }

  // returns the difference between this totalweight and
  // that of other
  double compareSeamInfo(SeamInfo other) {
    return this.totalWeight - other.totalWeight;
  }

}

// represents the WorldClass ImageCondenser,
// which shows da stuff
class ImageCondenser extends World {

  PixelGraph pixelGraph;
  // when cycle is true, remove a seam
  // when cycle is false, highlight a seam
  boolean cycle;
  // color state of the image, 0 for regular color, 1 for energy
  int colorState;
  // true is remove
  // false is insert
  boolean remove;
  // 0 is random, 1 is vertical, 2 is horizontal
  int removalDirection;
  // t is level paused, false is level go
  boolean paused;

  ImageCondenser(FromFileImage img) {
    this.pixelGraph = new PixelGraph(img);
    this.cycle = false;
    this.colorState = 0;
    this.remove = true;
    this.removalDirection = 0;
    this.paused = false;
  }

  // makes the scene of this world with teh pixelimage of the pixelGraph
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(pixelGraph.width, pixelGraph.height);
    scene.placeImageXY(pixelGraph.pixelImage, (pixelGraph.width / 2), (pixelGraph.height / 2));
    // scene.placeImageXY(pixelGraph.pixelImage, 2000, 1000);
    return scene;
  }

  // on tick method
  public void onTick() {

    if (!this.paused) {
      // if the mode is remove, or the cycle is still true(meaning a seam has been
      // highlighted but not yet removed)
      if (this.remove || this.cycle) {
        // if cycle is true, remove the highlighted seam
        if (cycle) {
          this.pixelGraph.removeCheapestSeam();
          cycle = false;
        }
        // otherwise, highlight the cheapest seam
        else {
          this.pixelGraph.highlightSeamRed(this.removalDirection);
          cycle = true;
        }
      }

      // otherwise, insert da seams
      else if (!this.remove) {
        this.pixelGraph.reInsertCheapestSeam();

      }

      // redraw the pixelimage every frame given this colorstate
      this.pixelGraph.redrawPixelImage(this.colorState);

    }
  }

  // on keys, do this stuff
  public void onKeyEvent(String key) {

    // if key is r, switch modes
    if (key.equals("r")) {
      this.remove = !this.remove;
    }

    // if key is c, change color state
    if (key.equals("c")) {
      if (this.colorState == 1) {
        this.colorState = 0;
      } else {
        this.colorState += 1;
      }
    }

    // if v, change the removal direciton
    if (key.equals("v")) {
      if (this.removalDirection == 2) {
        this.removalDirection = 0;
      } else {
        this.removalDirection += 1;
      }
    }

    // if space, pause
    if (key.equals(" ")) {
      this.paused = !this.paused;
    }

  }
}

class Examples {

  // do stuff with the balloon image
  void testBigBang(Tester t) {
    ImageCondenser world = new ImageCondenser(new FromFileImage("balloons.png"));

    world.bigBang(1800, 1000, .00001);
  }

  Utils u = new Utils();
  ImageCondenser threeByThreeWorld;
  PixelGraph threeByThree;
  APixel topLeft;
  APixel topMiddle;
  APixel topRight;
  APixel middleLeft;
  APixel middleMiddle;
  APixel middleRight;
  APixel bottomLeft;
  APixel bottomMiddle;
  APixel bottomRight;

  void init() {
    // image condenser from 3X3.png,
    // looks like:
    // red, white, pink
    // green, grey, yellow
    // blue, cyan, magenta
    threeByThreeWorld = new ImageCondenser(new FromFileImage("3X3.png"));
    threeByThree = new PixelGraph(new FromFileImage("3X3.png"));
    topLeft = threeByThree.borderPixel.topLeft;
    topMiddle = topLeft.right;
    topRight = topMiddle.right;
    middleLeft = topLeft.down;
    middleMiddle = middleLeft.right;
    middleRight = middleMiddle.right;
    bottomLeft = middleLeft.down;
    bottomMiddle = bottomLeft.right;
    bottomRight = bottomMiddle.right;
  }

  // testing the method highlight in APixel
  void testHighlight(Tester t) {
    init();

    // Pixel
    t.checkExpect(topLeft.highLighted, false);
    topLeft.highlight();
    t.checkExpect(topLeft.highLighted, true);

    // BorderPixel
    t.checkExpect(threeByThree.borderPixel.highLighted, false);
    threeByThree.borderPixel.highlight();
    t.checkExpect(threeByThree.borderPixel.highLighted, true);

  }

  void testImageCondenser(Tester t) {
    init();

    // testing the onKeyEvent
    t.checkExpect(threeByThreeWorld.remove, true);
    threeByThreeWorld.onKeyEvent("r");
    t.checkExpect(threeByThreeWorld.remove, false);
    threeByThreeWorld.onKeyEvent("r");
    t.checkExpect(threeByThreeWorld.remove, true);

    t.checkExpect(threeByThreeWorld.removalDirection, 0);
    threeByThreeWorld.onKeyEvent("v");
    t.checkExpect(threeByThreeWorld.removalDirection, 1);
    threeByThreeWorld.onKeyEvent("v");
    t.checkExpect(threeByThreeWorld.removalDirection, 2);
    threeByThreeWorld.onKeyEvent("v");
    t.checkExpect(threeByThreeWorld.removalDirection, 0);

    t.checkExpect(threeByThreeWorld.colorState, 0);
    threeByThreeWorld.onKeyEvent("c");
    t.checkExpect(threeByThreeWorld.colorState, 1);
    threeByThreeWorld.onKeyEvent("c");
    t.checkExpect(threeByThreeWorld.colorState, 0);

    t.checkExpect(threeByThreeWorld.paused, false);
    threeByThreeWorld.onKeyEvent(" ");
    t.checkExpect(threeByThreeWorld.paused, true);
    threeByThreeWorld.onKeyEvent(" ");
    t.checkExpect(threeByThreeWorld.paused, false);

    // test makeScene
    WorldScene scene = new WorldScene(3, 3);
    scene.placeImageXY(threeByThree.pixelImage, 3, 3);
    t.checkExpect(threeByThreeWorld.makeScene(), scene);

  }

  void testComputeBrightness(Tester t) {

    t.checkInexact(topLeft.computeBrightness(), .3333, .001);
    t.checkInexact(topMiddle.computeBrightness(), 1.0, .001);
    t.checkInexact(topRight.computeBrightness(), .7908, .001);
    t.checkInexact(middleLeft.computeBrightness(), .3333, .001);
    t.checkInexact(middleMiddle.computeBrightness(), .5019, .001);
    t.checkInexact(middleRight.computeBrightness(), .6666, .001);
    t.checkInexact(bottomLeft.computeBrightness(), .3333, .001);
    t.checkInexact(bottomMiddle.computeBrightness(), .6666, .001);
    t.checkInexact(bottomRight.computeBrightness(), .6666, .001);

    t.checkInexact(threeByThree.borderPixel.computeBrightness(), 0.0, .001);

  }

  void testComputeEnergy(Tester t) {
    init();

    t.checkInexact(topLeft.computeEnergy(), 2.7614, .001);
    t.checkInexact(topMiddle.computeEnergy(), 2.3609, .001);
    t.checkInexact(topRight.computeEnergy(), 3.1029, .001);
    t.checkInexact(middleLeft.computeEnergy(), 2.6913, .001);
    t.checkInexact(middleMiddle.computeEnergy(), 1.6582, .001);
    t.checkInexact(middleRight.computeEnergy(), 2.7332, .001);
    t.checkInexact(bottomLeft.computeEnergy(), 2.1757, .001);
    t.checkInexact(bottomMiddle.computeEnergy(), 2.2395, .001);
    t.checkInexact(bottomRight.computeEnergy(), 2.5954, .001);
    t.checkInexact(threeByThree.borderPixel.computeEnergy(), 0.0, .001);

  }

  // // testing getColor of APixel
  void testGetColor(Tester t) {
    init();
    // testing getColor(0), which returns the normal color of a pixel
    ComputedPixelImage img = new ComputedPixelImage(3, 3);

    topLeft.setColor(img, 0, new Posn(0, 0));
    t.checkExpect(img.getColorAt(0, 0), Color.RED);

    topMiddle.setColor(img, 0, new Posn(1, 0));
    t.checkExpect(img.getColorAt(1, 0), Color.WHITE);

    topRight.setColor(img, 0, new Posn(2, 0));
    t.checkExpect(img.getColorAt(2, 0), Color.PINK);

    middleLeft.setColor(img, 0, new Posn(0, 1));
    t.checkExpect(img.getColorAt(0, 1), Color.GREEN);

    middleMiddle.setColor(img, 0, new Posn(1, 1));
    t.checkExpect(img.getColorAt(1, 1), Color.GRAY);

    middleRight.setColor(img, 0, new Posn(2, 1));
    t.checkExpect(img.getColorAt(2, 1), Color.YELLOW);

    bottomLeft.setColor(img, 0, new Posn(0, 2));
    t.checkExpect(img.getColorAt(0, 2), Color.BLUE);

    bottomMiddle.setColor(img, 0, new Posn(1, 2));
    t.checkExpect(img.getColorAt(1, 2), Color.CYAN);

    bottomRight.setColor(img, 0, new Posn(2, 2));
    t.checkExpect(img.getColorAt(2, 2), Color.MAGENTA);

    threeByThree.borderPixel.setColor(img, 0, new Posn(0, 0));
    t.checkExpect(img.getColorAt(0, 0), Color.BLACK);

    // testing getColor(1), which returns the energy color of a pixel

    topLeft.setColor(img, 1, new Posn(0, 0));
    t.checkExpect(img.getColorAt(0, 0), new Color(124, 124, 124));

    topMiddle.setColor(img, 1, new Posn(1, 0));
    t.checkExpect(img.getColorAt(1, 0), new Color(106, 106, 106));

    topRight.setColor(img, 1, new Posn(2, 0));
    t.checkExpect(img.getColorAt(2, 0), new Color(139, 139, 139));

    middleLeft.setColor(img, 1, new Posn(0, 1));
    t.checkExpect(img.getColorAt(0, 1), new Color(121, 121, 121));

    middleMiddle.setColor(img, 1, new Posn(1, 1));
    t.checkExpect(img.getColorAt(1, 1), new Color(74, 74, 74));

    middleRight.setColor(img, 1, new Posn(2, 1));
    t.checkExpect(img.getColorAt(2, 1), new Color(123, 123, 123));

    bottomLeft.setColor(img, 1, new Posn(0, 2));
    t.checkExpect(img.getColorAt(0, 2), new Color(98, 98, 98));

    bottomMiddle.setColor(img, 1, new Posn(1, 2));
    t.checkExpect(img.getColorAt(1, 2), new Color(100, 100, 100));

    bottomRight.setColor(img, 1, new Posn(2, 2));
    t.checkExpect(img.getColorAt(2, 2), new Color(117, 117, 117));

    threeByThree.borderPixel.setColor(img, 1, new Posn(0, 0));
    t.checkExpect(img.getColorAt(0, 0), Color.BLACK);

    // testing get color when pixel is highlighted
    topMiddle.highlight();
    topMiddle.setColor(img, 1, new Posn(1, 0));
    t.checkExpect(img.getColorAt(1, 0), Color.RED);
    // calling getColor once resets highlighted to false
    topMiddle.setColor(img, 0, new Posn(1, 0));
    t.checkExpect(img.getColorAt(1, 0), Color.WHITE);
  }

  void testUpdate(Tester t) {
    init();
    t.checkExpect(topLeft.right, topMiddle);
    // updating topLeft.right to topRight
    topLeft.update(topRight, "right");
    t.checkExpect(topLeft.right, topRight);

    // updating topLeft.up to be bottomLeft
    topLeft.update(bottomLeft, "up");
    t.checkExpect(topLeft.up, bottomLeft);

    topLeft.update(middleMiddle, "left");
    t.checkExpect(topLeft.left, middleMiddle);

    topLeft.update(bottomRight, "down");
    t.checkExpect(topLeft.down, bottomRight);

    // testing the invalid argument exception for passing in an invalid direction
    // string
    t.checkException(new IllegalArgumentException(), topLeft, "update", bottomLeft, "meow");
  }

  // testing the update topLeft method of APixel, which updates the topLeft field
  // of BorderPixel when
  // one of the given pixels IS the topleft, and does nothing in the case of Pixel
  void testUpdateTopLeft(Tester t) {
    init();

    // showing what middleMiddle's current pointers are
    t.checkExpect(middleMiddle.up, topMiddle);
    t.checkExpect(middleMiddle.left, middleLeft);
    t.checkExpect(middleMiddle.right, middleRight);
    t.checkExpect(middleMiddle.down, bottomMiddle);
    middleMiddle.updateTopLeft(topLeft, bottomLeft);
    // since middleMiddle is a pixel, it has no topLeft field
    t.checkExpect(middleMiddle.up, topMiddle);
    t.checkExpect(middleMiddle.left, middleLeft);
    t.checkExpect(middleMiddle.right, middleRight);
    t.checkExpect(middleMiddle.down, bottomMiddle);

    // testing on borderPixel, which DOES have a topLeft field
    t.checkExpect(threeByThree.borderPixel.topLeft, topLeft);
    // proper update, where the first object passed is the topLeft, and the next is
    // topLeft's right
    threeByThree.borderPixel.updateTopLeft(topLeft, topMiddle);
    t.checkExpect(threeByThree.borderPixel.topLeft, topMiddle);
    // improper update, where the first object passed is NOT the current topLeft
    threeByThree.borderPixel.updateTopLeft(middleMiddle, bottomRight);
    // borderPixel's topLeft is still topMiddle
    t.checkExpect(threeByThree.borderPixel.topLeft, topMiddle);
  }

  // testing checkConnections, which returns false if the connections are good
  void testCheckConnections(Tester t) {
    init();
    t.checkExpect(topLeft.checkConnections(), false);
    t.checkExpect(topMiddle.checkConnections(), false);
    t.checkExpect(topRight.checkConnections(), false);
    t.checkExpect(middleLeft.checkConnections(), false);
    t.checkExpect(middleMiddle.checkConnections(), false);
    t.checkExpect(middleRight.checkConnections(), false);
    t.checkExpect(bottomLeft.checkConnections(), false);
    t.checkExpect(bottomMiddle.checkConnections(), false);
    t.checkExpect(bottomRight.checkConnections(), false);

    // checking the errors thrown when you try to construct a pixel with
    // improper connections
    t.checkConstructorException(new IllegalArgumentException("Invalid formation of pixels"),
        "Pixel", Color.BLACK, topLeft, bottomRight, threeByThree.borderPixel, middleMiddle);

  }

  // testing the constructor of PixelGraph
  void testPixelGraphConstructor(Tester t) {
    init();

    // checking that the threeByThree pixelGraph has width and height 3
    t.checkExpect(threeByThree.height, 3);
    t.checkExpect(threeByThree.width, 3);

    // checking the arrangment of pixels in threeByThree (i can't just create
    // replicas of each pixel and test
    // if they are the same as threeByThree's pixels, because a Pixel can only be
    // the same as another Pixel if it
    // points to the same spot in memory, intensional equality)
    // i'm checking the colors, because the image has 9 unique colors for 9
    // different pixels
    // looks like:
    // red, white, pink
    // green, gray, yellow
    // blue, cyan, magenta

    // row 1
    // checking the topLeft of threeByThree's borderPixel, which should be red
    t.checkExpect(topLeft.color, Color.RED);
    // checking the top-middle pixel, which should be white
    t.checkExpect(topMiddle.color, Color.WHITE);
    // checking the top-right pixel, which should be pink
    t.checkExpect(topRight.color, Color.PINK);

    // row 2
    // checking the middle-left pixel, which should be green
    t.checkExpect(middleLeft.color, Color.GREEN);
    // checking the middle-middle pixel, which should be gray
    t.checkExpect(middleMiddle.color, Color.GRAY);
    // checking the middle-right pixel, which should be yellow
    t.checkExpect(middleRight.color, Color.YELLOW);

    // row 3
    // checking the bottom-left pixel, which should be blue
    t.checkExpect(bottomLeft.color, Color.BLUE);
    // checking the bottom-middle pixel, which should be cyan
    t.checkExpect(bottomMiddle.color, Color.CYAN);
    // checking the bottom-right pixel, which should be magenta
    t.checkExpect(bottomRight.color, Color.MAGENTA);

    // now, we need to make sure that the pointers work in every direction
    // checking that topLeft.down.up equals topleft
    t.checkExpect(topLeft.down.up, topLeft);
    // checking that topLeft.right.left equals topleft
    t.checkExpect(topLeft.right.left, topLeft);
    // checking that topLeft.left.right equals borderPixel (topLeft.left ==
    // borderPixel, and borderPixel only points to itself)
    t.checkExpect(topLeft.left.right, threeByThree.borderPixel);
    // checking that topLeft.up.down equals borderPixel (topLeft.up ==
    // borderPixel, and borderPixel only points to itself)
    t.checkExpect(topLeft.up.down, threeByThree.borderPixel);

    // testing that topMiddle.left.right is topMiddle
    t.checkExpect(topMiddle.left.right, topMiddle);
    // testing that topMiddle.right.left is topMiddle
    t.checkExpect(topMiddle.right.left, topMiddle);
    // testing that topMiddle.down.up is topMiddle
    t.checkExpect(topMiddle.down.up, topMiddle);
    // checking that topMiddle.up.down equals borderPixel (topMiddle.up ==
    // borderPixel, and borderPixel only points to itself)
    t.checkExpect(topMiddle.up.down, threeByThree.borderPixel);

    // testing that topLeft.down.right.up is topMiddle
    t.checkExpect(topLeft.down.right.up, topMiddle);
    // testing that topMiddle.down.left.up is topLeft
    t.checkExpect(topMiddle.down.left.up, topLeft);

    // testing that topRight.right.left is borderPixel (topRight.right ==
    // borderPixel,
    // and borderPixel only points to itself)
    t.checkExpect(topRight.right.left, threeByThree.borderPixel);

    // testing that middleMiddle.up.down is middleMiddle
    t.checkExpect(middleMiddle.up.down, middleMiddle);
    t.checkExpect(middleMiddle.left.right, middleMiddle);
    t.checkExpect(middleMiddle.right.left, middleMiddle);
    t.checkExpect(middleMiddle.down.up, middleMiddle);
    // testing in a circle
    t.checkExpect(middleMiddle.up.left.down.right, middleMiddle);
    t.checkExpect(middleMiddle.up.right.down.left, middleMiddle);
    t.checkExpect(middleMiddle.right.down.left.up, middleMiddle);
    t.checkExpect(middleMiddle.right.up.left.down, middleMiddle);
    t.checkExpect(middleMiddle.down.right.up.left, middleMiddle);
    t.checkExpect(middleMiddle.down.left.up.right, middleMiddle);
    t.checkExpect(middleMiddle.left.down.right.up, middleMiddle);
    t.checkExpect(middleMiddle.left.up.right.down, middleMiddle);

  }

  // testing the compute cheapest seam method of pixelGraph
  void testComputeCheapestSeam(Tester t) {
    init();

    // based on some very not fun math, the cheapest VERTICAL seam of threeBythree
    // is:
    // blue, gray, white, with a totalweight of ~ 6.195
    // starts with white pixel
    SeamInfo cheapestVerticalSeam = new SeamInfo(topMiddle,
        "vertical");
    // then the gray pixel
    cheapestVerticalSeam = new SeamInfo(middleMiddle, cheapestVerticalSeam,
        "vertical");
    // then the blue pixel
    cheapestVerticalSeam = new SeamInfo(bottomLeft, cheapestVerticalSeam,
        "vertical");

    t.checkExpect(u.computeCheapestSeam(threeByThree.borderPixel,
        "vertical", 3, 3), cheapestVerticalSeam);

    // now, lets compute the cheapest horizontal seam, which is, from right to left:
    // magenta, gray, blue
    SeamInfo cheapestHorizontalSeam = new SeamInfo(bottomLeft,
        "horizontal");
    cheapestHorizontalSeam = new SeamInfo(middleMiddle, cheapestHorizontalSeam,
        "horizontal");
    cheapestHorizontalSeam = new SeamInfo(bottomRight, cheapestHorizontalSeam,
        "horizontal");
    t.checkExpect(u.computeCheapestSeam(threeByThree.borderPixel,
        "horizontal", 3, 3), cheapestHorizontalSeam);

    // lets assume that highlightCheapestSeam and removeCheapestSeam works as
    // intended.
    // highlight the cheapest vertical seam, and add it to the history list, and
    // remove it
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();

    // now, the next new cheapest vertical seam should be this, from bottom to top
    // magenta, yellow, pink
    cheapestVerticalSeam = new SeamInfo(topRight, "vertical");
    cheapestVerticalSeam = new SeamInfo(middleRight, cheapestVerticalSeam,
        "vertical");
    cheapestVerticalSeam = new SeamInfo(bottomRight, cheapestVerticalSeam,
        "vertical");

    t.checkExpect(u.computeCheapestSeam(threeByThree.borderPixel,
        "vertical", 2, 3), cheapestVerticalSeam);

    // testing an invalid string on computeCheapestSeam
    t.checkException(new IllegalArgumentException(), u,
        "computeCheapestSeam", threeByThree.borderPixel, "meow", 3, 3);

  }

  // void calculateSeamsForRow(ArrayList<SeamInfo> seams, APixel prevPixel, String
  // direction, int bound)

  // testing calculating the seams for a row
  void testCalculateSeamsForRow(Tester t) {
    init();
    // this method is only called starting at the second row/column of pixels,
    // because the first row/column has to be created
    // with seaminfo's with null cameFroms
    // creating that first row of seamInfos
    ArrayList<SeamInfo> seams = new ArrayList<>();
    seams.add(new SeamInfo(topLeft, "vertical"));
    seams.add(new SeamInfo(topMiddle, "vertical"));
    seams.add(new SeamInfo(topRight, "vertical"));

    // Creating a copy of that arraylist, which then has the seams supposedly
    // created by calculateSeamsFroRow, to test against
    ArrayList<SeamInfo> copyOfSeams = new ArrayList<>();
    copyOfSeams.add(new SeamInfo(topLeft, "vertical"));
    copyOfSeams.add(new SeamInfo(topMiddle, "vertical"));
    copyOfSeams.add(new SeamInfo(topRight, "vertical"));
    // for the first iteration of csfr, the seamInfo with the white pixel is the
    // cheapest, so all subsequent seams will
    // contain it
    copyOfSeams.set(0, new SeamInfo(middleLeft, copyOfSeams.get(1),
        "vertical"));
    copyOfSeams.set(2, new SeamInfo(middleRight, copyOfSeams.get(1),
        "vertical"));
    copyOfSeams.set(1, new SeamInfo(middleMiddle, copyOfSeams.get(1),
        "vertical"));

    // in this case, prevPixel is the leftMost pixel of the second row, and the
    // bound is 3, the width of the pixelGraph
    u.calculateSeamsForRowOrColumn(seams, middleLeft, "vertical", 3);

    // compare seams to copy
    t.checkExpect(seams, copyOfSeams);

    // now, we will test that horizontal seams work
    seams = new ArrayList<>();
    seams.add(new SeamInfo(topLeft, "horizontal"));
    seams.add(new SeamInfo(middleLeft, "horizontal"));
    seams.add(new SeamInfo(bottomLeft, "horizontal"));

    copyOfSeams = new ArrayList<>();
    copyOfSeams.add(new SeamInfo(topLeft, "horizontal"));
    copyOfSeams.add(new SeamInfo(middleLeft, "horizontal"));
    copyOfSeams.add(new SeamInfo(bottomLeft, "horizontal"));
    // for the first iteration of csfr,
    // the top seam takes the green seamInfo, and the middle and bottom take the
    // blue seaminfo
    copyOfSeams.set(0, new SeamInfo(topMiddle, copyOfSeams.get(1),
        "horizontal"));
    copyOfSeams.set(1, new SeamInfo(middleMiddle, copyOfSeams.get(2),
        "horizontal"));
    copyOfSeams.set(2, new SeamInfo(bottomMiddle, copyOfSeams.get(2),
        "horizontal"));

    u.calculateSeamsForRowOrColumn(seams, topMiddle, "horizontal", 3);
    t.checkExpect(seams, copyOfSeams);

  }

  // test calculateMinSeam
  // SeamInfo calculateNewMin(SeamInfo previouslyCalculated, APixel leftPixel,
  // ArrayList<SeamInfo> seams, int colIndex,
  // String direction, int bound), which returns a new minimum seam and sets index
  // - 1 of seams to the previous min seam
  void testCalculateNewMinSeam(Tester t) {
    init();
    ArrayList<SeamInfo> seams = new ArrayList<>();
    seams.add(new SeamInfo(topLeft, "vertical"));
    seams.add(new SeamInfo(topMiddle, "vertical"));
    seams.add(new SeamInfo(topRight, "vertical"));

    // calculate the min seam for index 0, which should be a seam containing the
    // white pixel then the green pixel
    SeamInfo newCheapestSeam = u.calculateNewMin(null, middleLeft,
        seams, 0, "vertical", 3);
    SeamInfo newCheapestSeamCopy = new SeamInfo(middleLeft, seams.get(1),
        "vertical");
    t.checkExpect(newCheapestSeam, newCheapestSeamCopy);

    // next iteration
    SeamInfo newCheapestSeamIter2 = u.calculateNewMin(newCheapestSeam, middleMiddle,
        seams, 1,
        "vertical", 3);
    SeamInfo newCheapestSeamCopyIter2 = new SeamInfo(middleMiddle,
        new SeamInfo(topMiddle, "vertical"), "vertical");
    t.checkExpect(newCheapestSeamIter2, newCheapestSeamCopyIter2);
    // seams(0) should now be newCheapestSeam
    t.checkExpect(seams.get(0), newCheapestSeam);

    // next iteration
    SeamInfo newCheapestSeamIter3 = u.calculateNewMin(newCheapestSeamCopyIter2,
        middleRight, seams,
        2, "vertical", 3);
    SeamInfo newCheapestSeamCopyIter3 = new SeamInfo(middleRight,
        new SeamInfo(topMiddle, "vertical"), "vertical");
    t.checkExpect(newCheapestSeamIter3, newCheapestSeamCopyIter3);
    t.checkExpect(seams.get(1), newCheapestSeamIter2);

    // TESTING ON HORIZONTAL SEAMS
    seams = new ArrayList<>();
    seams.add(new SeamInfo(topLeft, "horizontal"));
    seams.add(new SeamInfo(middleLeft, "horizontal"));
    seams.add(new SeamInfo(bottomLeft, "horizontal"));

    // iteration 1
    newCheapestSeam = u.calculateNewMin(null, topMiddle,
        seams, 0, "horizontal", 3);
    newCheapestSeamCopy = new SeamInfo(topMiddle, new SeamInfo(middleLeft,
        "horizontal"),
        "horizontal");
    t.checkExpect(newCheapestSeam, newCheapestSeamCopy);

    // iteration 2
    newCheapestSeamIter2 = u.calculateNewMin(newCheapestSeam, middleMiddle,
        seams, 1, "horizontal",
        3);
    newCheapestSeamCopyIter2 = new SeamInfo(middleMiddle,
        new SeamInfo(bottomLeft, "horizontal"),
        "horizontal");
    t.checkExpect(newCheapestSeamIter2, newCheapestSeamCopyIter2);
    t.checkExpect(seams.get(0), newCheapestSeam);

    // iteration 3
    newCheapestSeamIter3 = u.calculateNewMin(newCheapestSeamIter2,
        bottomMiddle, seams, 2,
        "horizontal", 3);
    newCheapestSeamCopyIter3 = new SeamInfo(bottomMiddle,
        new SeamInfo(bottomLeft, "horizontal"),
        "horizontal");
    t.checkExpect(newCheapestSeamIter3, newCheapestSeamCopyIter3);
    t.checkExpect(seams.get(1), newCheapestSeamIter2);
  }

  // testing the highlightCheapestSeam method of PixelGraph, which highlights the
  // cheapest seam and
  // adds it to the history arraylist
  void testHighlightCheapestSeam(Tester t) {
    init();
    // checking that the cheapest seam is not already highlighted
    SeamInfo cheapestSeam = u.computeCheapestSeam(threeByThree.borderPixel,
        "vertical", 3, 3);
    while (cheapestSeam != null) {
      t.checkExpect(cheapestSeam.pixel.highLighted, false);
      cheapestSeam = cheapestSeam.cameFrom;
    }

    // resetting the cheapest seam
    cheapestSeam = u.computeCheapestSeam(threeByThree.borderPixel,
        "vertical", 3, 3);
    // checking that the history arraylist is empty
    t.checkExpect(threeByThree.removalHistory, new ArrayList<>());

    // 1 means vertical seam, 0 means random(can't be tested lol), and 2 means
    // horizontal
    threeByThree.highlightSeamRed(1);
    ArrayList<SeamInfo> removalHistory = new ArrayList<>();
    removalHistory.add(cheapestSeam);
    t.checkExpect(threeByThree.removalHistory, removalHistory);
    while (cheapestSeam != null) {
      t.checkExpect(cheapestSeam.pixel.highLighted, true);
      cheapestSeam = cheapestSeam.cameFrom;
    }

    init();

    // trying it on horizontal seam
    cheapestSeam = u.computeCheapestSeam(threeByThree.borderPixel,
        "horizontal", 3, 3);
    threeByThree.highlightSeamRed(2);
    removalHistory = new ArrayList<>();
    removalHistory.add(cheapestSeam);
    t.checkExpect(threeByThree.removalHistory, removalHistory);
    while (cheapestSeam != null) {
      t.checkExpect(cheapestSeam.pixel.highLighted, true);
      cheapestSeam = cheapestSeam.cameFrom;
    }
  }

  // testing the removeCheapestSeam method of PixelGraph, which takes the last
  // element of the removalHistory and removes it from the graph
  void testRemoveCheapestSeam(Tester t) {
    init();
    // the cheapest vertical seam of threebythree is topmiddle, middlemiddle, and
    // bottomleft.
    // testing the connections for these pixels
    t.checkExpect(topLeft.right, topMiddle);
    t.checkExpect(topRight.left, topMiddle);
    t.checkExpect(middleMiddle.up, topMiddle);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    // must call highlight cheapest then remove
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    t.checkExpect(topLeft.right, topRight);
    t.checkExpect(topRight.left, topLeft);
    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(middleMiddle.up, topMiddle);

    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleRight);
    t.checkExpect(middleRight.left, middleLeft);
    t.checkExpect(bottomMiddle.up, middleLeft);

    t.checkExpect(middleLeft.down, bottomMiddle);
    t.checkExpect(bottomMiddle.left, threeByThree.borderPixel);

    init();

    // testing on a horizontal seam
    // cheapest is bottomLeft, middleMiddle, bottomRight

    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleRight.down, bottomRight);
    t.checkExpect(bottomMiddle.right, bottomRight);

    threeByThree.highlightSeamRed(2);
    threeByThree.removeCheapestSeam();

    t.checkExpect(middleLeft.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.left, middleLeft);

    t.checkExpect(topMiddle.down, bottomMiddle);
    t.checkExpect(middleLeft.right, bottomMiddle);
    t.checkExpect(middleRight.left, bottomMiddle);
    t.checkExpect(bottomMiddle.up, topMiddle);
    t.checkExpect(middleRight.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.right, middleRight);

    // testing that calling remove after the graph is empty will not
    // throw an error or erase the topLeft pointer
    init();
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    t.checkExpect(threeByThree.width, 0);
    t.checkExpect(threeByThree.borderPixel.topLeft, topLeft);

  }

  // testing redrawPixelImage
  void testRedrawPixelImage(Tester t) {
    init();
    ComputedPixelImage copy = new ComputedPixelImage(3, 3);
    copy.setColorAt(0, 0, Color.RED);
    copy.setColorAt(1, 0, Color.WHITE);
    copy.setColorAt(2, 0, Color.PINK);
    copy.setColorAt(0, 1, Color.GREEN);
    copy.setColorAt(1, 1, Color.GRAY);
    copy.setColorAt(2, 1, Color.YELLOW);
    copy.setColorAt(0, 2, Color.BLUE);
    copy.setColorAt(1, 2, Color.CYAN);
    copy.setColorAt(2, 2, Color.MAGENTA);

    // colorstate 0, normal pixel color
    threeByThree.redrawPixelImage(0);
    t.checkExpect(copy, threeByThree.pixelImage);

    // testing colorstate 1, pixel energy color
    copy.setColorAt(0, 0, new Color(124, 124, 124));
    copy.setColorAt(1, 0, new Color(106, 106, 106));
    copy.setColorAt(2, 0, new Color(139, 139, 139));
    copy.setColorAt(0, 1, new Color(121, 121, 121));
    copy.setColorAt(1, 1, new Color(74, 74, 74));
    copy.setColorAt(2, 1, new Color(123, 123, 123));
    copy.setColorAt(0, 2, new Color(98, 98, 98));
    copy.setColorAt(1, 2, new Color(100, 100, 100));
    copy.setColorAt(2, 2, new Color(117, 117, 117));

    threeByThree.redrawPixelImage(1);
    t.checkExpect(copy, threeByThree.pixelImage);

    // testing after highlighting the cheapest vertical seam
    threeByThree.highlightSeamRed(1);
    threeByThree.redrawPixelImage(1);
    copy.setColorAt(1, 0, Color.RED);
    copy.setColorAt(1, 1, Color.RED);
    copy.setColorAt(0, 2, Color.RED);
    t.checkExpect(copy, threeByThree.pixelImage);

    // removing the cheapest vertical seam
    threeByThree.removeCheapestSeam();

    // copy width is now 2, not 3
    copy = new ComputedPixelImage(2, 3);
    copy.setColorAt(0, 0, Color.RED);
    copy.setColorAt(1, 0, Color.PINK);
    copy.setColorAt(0, 1, Color.GREEN);
    copy.setColorAt(1, 1, Color.YELLOW);
    copy.setColorAt(0, 2, Color.CYAN);
    copy.setColorAt(1, 2, Color.MAGENTA);
    threeByThree.redrawPixelImage(0);
    t.checkExpect(copy, threeByThree.pixelImage);

    // checking a situation where the graph is width/height 0
    // assume remove works
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    t.checkExpect(threeByThree.width, 0);
    threeByThree.redrawPixelImage(0);
    t.checkExpect(threeByThree.pixelImage, new ComputedPixelImage(1, 1));
  }

  // testing the re-insert cheapest seam method of PixelGraph
  void testReinsertCheapestSeam(Tester t) {
    init();
    // removing the cheapest horizontal seam, and checking that the connections
    // exclude the pixels in the seam
    threeByThree.highlightSeamRed(1);
    threeByThree.removeCheapestSeam();
    t.checkExpect(topLeft.right, topRight);
    t.checkExpect(topRight.left, topLeft);
    t.checkExpect(middleMiddle.up, topMiddle);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleRight);
    t.checkExpect(middleRight.left, middleLeft);
    t.checkExpect(bottomMiddle.up, middleLeft);

    t.checkExpect(middleLeft.down, bottomMiddle);
    t.checkExpect(bottomMiddle.left, threeByThree.borderPixel);

    // removing the next cheapest HORIZONTAL seam:
    // removes middleRight, bottomMiddle
    // pointers still point at middleRight
    t.checkExpect(topRight.down, middleRight);
    t.checkExpect(bottomRight.up, middleRight);
    t.checkExpect(middleLeft.right, middleRight);
    // pointers still point at bottomMiddle
    t.checkExpect(bottomRight.left, bottomMiddle);
    t.checkExpect(middleLeft.down, bottomMiddle);

    threeByThree.highlightSeamRed(2);
    threeByThree.removeCheapestSeam();

    t.checkExpect(topRight.down, bottomRight);
    t.checkExpect(bottomRight.up, topRight);
    t.checkExpect(middleLeft.right, bottomRight);

    t.checkExpect(bottomRight.left, middleLeft);
    t.checkExpect(middleLeft.down, threeByThree.borderPixel);

    // reinserting the horizontal seam
    threeByThree.reInsertCheapestSeam();

    // pointers should now point at middleRight
    t.checkExpect(topRight.down, middleRight);
    t.checkExpect(bottomRight.up, middleRight);
    t.checkExpect(middleLeft.right, middleRight);
    // pointers should now point at bottomMiddle
    t.checkExpect(bottomRight.left, bottomMiddle);
    t.checkExpect(middleLeft.down, bottomMiddle);

    // inserting the vertical seam back
    threeByThree.reInsertCheapestSeam();
    // the connections should now be restored
    t.checkExpect(topLeft.right, topMiddle);
    t.checkExpect(topRight.left, topMiddle);
    t.checkExpect(middleMiddle.up, topMiddle);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

  }

  // testing the remove method in Pixel, which takes in a SeamInfo next, APixel
  // prevRemoved, and String direction
  void testPixelRemoveandInsert(Tester t) {
    init();

    // testing remove on topLeft, with a null next and a null prevRemoved, and
    // string "vertical"
    t.checkExpect(topMiddle.left, topLeft);
    t.checkExpect(middleLeft.up, topLeft);

    topLeft.remove(null, "vertical", middleMiddle);
    t.checkExpect(topMiddle.left, threeByThree.borderPixel);
    t.checkExpect(middleLeft.up, topMiddle);

    // borderPixel's topLeft should now be topMiddle
    t.checkExpect(topMiddle, threeByThree.borderPixel.topLeft);

    // now testing insert
    topLeft.insert(null, "vertical");
    t.checkExpect(topMiddle.left, topLeft);
    t.checkExpect(middleLeft.up, topLeft);
    t.checkExpect(topLeft, threeByThree.borderPixel.topLeft);

    init();
    // testing remove on bottomLeft, given the rest of the cheapest vertical seam
    SeamInfo cheapestVertical = u.computeCheapestSeam(threeByThree.borderPixel,
        "vertical", 3, 3);
    cheapestVertical = cheapestVertical.cameFrom;

    // testing the connections for these pixels
    t.checkExpect(topLeft.right, topMiddle);
    t.checkExpect(topRight.left, topMiddle);
    t.checkExpect(middleMiddle.up, topMiddle);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    bottomLeft.remove(cheapestVertical, "vertical", null);

    t.checkExpect(topLeft.right, topRight);
    t.checkExpect(topRight.left, topLeft);
    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(middleMiddle.up, topMiddle);

    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleRight);
    t.checkExpect(middleRight.left, middleLeft);
    t.checkExpect(bottomMiddle.up, middleLeft);

    t.checkExpect(middleLeft.down, bottomMiddle);
    t.checkExpect(bottomMiddle.left, threeByThree.borderPixel);

    init();

    // testing on a horizontal seam, with bottomRight as the root
    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleRight.down, bottomRight);
    t.checkExpect(bottomMiddle.right, bottomRight);

    SeamInfo cheapestHorizontal = u.computeCheapestSeam(threeByThree.borderPixel,
        "horizontal", 3, 3);
    cheapestHorizontal = cheapestHorizontal.cameFrom;

    bottomRight.remove(cheapestHorizontal, "horizontal", null);

    t.checkExpect(middleLeft.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.left, middleLeft);

    t.checkExpect(topMiddle.down, bottomMiddle);
    t.checkExpect(middleLeft.right, bottomMiddle);
    t.checkExpect(middleRight.left, bottomMiddle);
    t.checkExpect(bottomMiddle.up, topMiddle);
    t.checkExpect(middleRight.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.right, middleRight);

    // reinserting the pixel
    bottomRight.insert(cheapestHorizontal, "horizontal");
    // testing on a horizontal seam, with bottomRight as the root
    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleRight.down, bottomRight);
    t.checkExpect(bottomMiddle.right, bottomRight);

    init();

    // testing insert/remove in BorderPixel:
    cheapestVertical = u.computeCheapestSeam(threeByThree.borderPixel,
        "vertical", 3, 3);
    threeByThree.borderPixel.remove(cheapestVertical, "vertical", null);

    t.checkExpect(topLeft.right, topRight);
    t.checkExpect(topRight.left, topLeft);
    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(middleMiddle.up, topMiddle);

    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleRight);
    t.checkExpect(middleRight.left, middleLeft);
    t.checkExpect(bottomMiddle.up, middleLeft);

    t.checkExpect(middleLeft.down, bottomMiddle);
    t.checkExpect(bottomMiddle.left, threeByThree.borderPixel);

    // testing insert
    threeByThree.borderPixel.insert(cheapestVertical, "vertical");
    // testing the connections for these pixels
    t.checkExpect(topLeft.right, topMiddle);
    t.checkExpect(topRight.left, topMiddle);
    t.checkExpect(middleMiddle.up, topMiddle);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    cheapestHorizontal = u.computeCheapestSeam(threeByThree.borderPixel,
        "horizontal", 3, 3);
    threeByThree.borderPixel.remove(cheapestHorizontal, "horizontal", null);

    t.checkExpect(middleLeft.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.left, middleLeft);

    t.checkExpect(topMiddle.down, bottomMiddle);
    t.checkExpect(middleLeft.right, bottomMiddle);
    t.checkExpect(middleRight.left, bottomMiddle);
    t.checkExpect(bottomMiddle.up, topMiddle);
    t.checkExpect(middleRight.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.right, middleRight);

    // reinserting the pixel
    threeByThree.borderPixel.insert(cheapestHorizontal, "horizontal");
    // testing on a horizontal seam, with bottomRight as the root
    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleRight.down, bottomRight);
    t.checkExpect(bottomMiddle.right, bottomRight);

    // testing SEAMINFO remove and insert
    cheapestVertical = u.computeCheapestSeam(threeByThree.borderPixel,
        "vertical", 3, 3);
    cheapestVertical.remove(null);

    t.checkExpect(topLeft.right, topRight);
    t.checkExpect(topRight.left, topLeft);
    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(middleMiddle.up, topMiddle);

    // OTHER PIXELS REMOVED SHOULD STILL POINT TO THEIR ORIGINAL NEIGHBORS
    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleRight);
    t.checkExpect(middleRight.left, middleLeft);
    t.checkExpect(bottomMiddle.up, middleLeft);

    t.checkExpect(middleLeft.down, bottomMiddle);
    t.checkExpect(bottomMiddle.left, threeByThree.borderPixel);

    // testing insert
    cheapestVertical.insert();
    // testing the connections for these pixels
    t.checkExpect(topLeft.right, topMiddle);
    t.checkExpect(topRight.left, topMiddle);
    t.checkExpect(middleMiddle.up, topMiddle);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    cheapestHorizontal = u.computeCheapestSeam(threeByThree.borderPixel,
        "horizontal", 3, 3);
    cheapestHorizontal.remove(null);
    t.checkExpect(middleLeft.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.left, middleLeft);

    t.checkExpect(topMiddle.down, bottomMiddle);
    t.checkExpect(middleLeft.right, bottomMiddle);
    t.checkExpect(middleRight.left, bottomMiddle);
    t.checkExpect(bottomMiddle.up, topMiddle);
    t.checkExpect(middleRight.down, threeByThree.borderPixel);
    t.checkExpect(bottomMiddle.right, middleRight);

    // reinserting the pixel
    cheapestHorizontal.insert();
    // testing on a horizontal seam, with bottomRight as the root
    t.checkExpect(middleLeft.down, bottomLeft);
    t.checkExpect(bottomMiddle.left, bottomLeft);

    t.checkExpect(topMiddle.down, middleMiddle);
    t.checkExpect(middleLeft.right, middleMiddle);
    t.checkExpect(middleRight.left, middleMiddle);
    t.checkExpect(bottomMiddle.up, middleMiddle);

    t.checkExpect(middleRight.down, bottomRight);
    t.checkExpect(bottomMiddle.right, bottomRight);
  }

  // testing the seaminfo constructor
  void testSeamInfoConstructor(Tester t) {
    // null APixel
    t.checkConstructorException(new IllegalArgumentException(), "SeamInfo", null, "vertical");
    // inconsistent directions
    t.checkConstructorException(new IllegalArgumentException(), "SeamInfo", topLeft,
        new SeamInfo(bottomLeft, "horizontal"), "vertical");
  }

}