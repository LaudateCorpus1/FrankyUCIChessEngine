/*
 * MIT License
 *
 * Copyright (c) 2018 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fko.FrankyEngine.Franky;

import java.util.ArrayList;
import java.util.List;

/**
 * This enumeration class represents all squares on a chess board.
 * It uses a numbering for a x88 board so that a1=0 and a2=16
 * It has several convenience methods for calculation in relation
 * to other squares.
 * <p>
 * As enumeration is type safe and also very fast this is preferred over
 * static final int.
 */
public enum Square {

  // @formatter:off
  /*
   0   1   2   3   4   5   6   7 */
  a8, b8, c8, d8, e8, f8, g8, h8,  // 0  0-7
  a7, b7, c7, d7, e7, f7, g7, h7,  // 1  8-15
  a6, b6, c6, d6, e6, f6, g6, h6,  // 2  16-23
  a5, b5, c5, d5, e5, f5, g5, h5,  // 3  24-31
  a4, b4, c4, d4, e4, f4, g4, h4,  // 4  32-39
  a3, b3, c3, d3, e3, f3, g3, h3,  // 5  40-47
  a2, b2, c2, d2, e2, f2, g2, h2,  // 6  48-55
  a1, b1, c1, d1, e1, f1, g1, h1,  // 7  56-63
  NOSQUARE;
  // @formatter:on

  private static final int     WHITE = 0;
  private static final int     BLACK = 1;
  private static final boolean UP    = true;
  private static final boolean DOWN  = false;

  // pre-filled list with all squares
  public static final Square[] values;

  /**
   * pre-filled list with all valid squares
   */
  public static final List<Square> validSquares;

  // Move deltas north, south, east, west and combinations
  public static final int N  = -8;
  public static final int E  = 1;
  public static final int S  = 8;
  public static final int W  = -1;
  public static final int NE = N + E;
  public static final int SE = S + E;
  public static final int SW = S + W;
  public static final int NW = N + W;

  public static final int[] pawnDirections       = {N, NW, NE};
  public static final int[] pawnAttackDirections = {NW, NE};
  public static final int[] knightDirections     = {N + N + E, N + E + E, S + E + E, S + S + E,
                                                    S + S + W, S + W + W, N + W + W, N + N + W};
  public static final int[] bishopDirections     = {NE, SE, SW, NW};
  public static final int[] rookDirections       = {N, E, S, W};
  public static final int[] queenDirections      = {N, NE, E, SE, S, SW, W, NW};
  public static final int[] kingDirections       = {N, NE, E, SE, S, SW, W, NW};

  /*
   * pre computed mapping from position of bit in bitboard to square
   * To get position use <code>Long.numberOfLeadingZeros</code>
   */
  private static Square[] trailingZerosMap = new Square[65];

  // precomputed values
  private boolean validSquare;
  private long    bitBoard;

  // precomputed neighbours
  private Square north;
  private Square northEast;
  private Square east;
  private Square southEast;
  private Square south;
  private Square southWest;
  private Square west;
  private Square northWest;

  // precomputed file and rank
  private File file;
  private Rank rank;

  private boolean[] isPawnBaseRow = new boolean[2];

  // precomputed diagonals
  private long upDiag;
  private long downDiag;

  /*
     Due to the weired way Java initializes Enums you can't access static values
     from the constructor as the constructor is executed first. This forces this
     construct which has the consequence of not being able to use final fields!
     Therefore we need to make them private (with Getter) to protect them from
     external change.
      */
  // 1st static block
  static {
    values = Square.values();
    validSquares = new ArrayList<>(64);

    // pre-compute fields
    for (Square s : values) {

      // Exclude NOSQUARE
      if (s.ordinal() == 64) {
        s.validSquare = false;
        s.bitBoard = 0L;
        s.file = File.NOFILE;
        s.rank = Rank.NORANK;
        continue;
      }

      // mark as valid square
      s.validSquare = true;

      // add to valid squares list
      validSquares.add(s);

      // set bit for bitboard
      s.bitBoard = 1L << s.ordinal();

      // mapping between enum ordinal and bbIndex
      trailingZerosMap[Long.numberOfTrailingZeros(s.bitBoard)] = s;

      // pre-compute file and rank
      s.file = File.values[s.ordinal() & 7];
      s.rank = Rank.values[7 - (s.ordinal() >>> 3)];

      // pre-compute all neighbours
      // middle squares have all neighbours
      if (s.file.ordinal() > 0
        && s.file.ordinal() < 7
        && s.rank.ordinal() > 0
        && s.rank.ordinal() < 7
      ) {
        s.north = Square.values()[s.ordinal() + N];
        s.northEast = Square.values()[s.ordinal() + NE];
        s.east = Square.values()[s.ordinal() + E];
        s.southEast = Square.values()[s.ordinal() + SE];
        s.south = Square.values()[s.ordinal() + S];
        s.southWest = Square.values()[s.ordinal() + SW];
        s.west = Square.values()[s.ordinal() + W];
        s.northWest = Square.values()[s.ordinal() + NW];
      }
      // upper left corner
      else if (s.file == File.a && s.rank == Rank.r8) {
        s.north = NOSQUARE;
        s.northEast = NOSQUARE;
        s.east = Square.values()[s.ordinal() + E];
        s.southEast = Square.values()[s.ordinal() + SE];
        s.south = Square.values()[s.ordinal() + S];
        s.southWest = NOSQUARE;
        s.west = NOSQUARE;
        s.northWest = NOSQUARE;
      }
      // upper right corner
      else if (s.file == File.h && s.rank == Rank.r8) {
        s.north = NOSQUARE;
        s.northEast = NOSQUARE;
        s.east = NOSQUARE;
        s.southEast = NOSQUARE;
        s.south = Square.values()[s.ordinal() + S];
        s.southWest = Square.values()[s.ordinal() + SW];
        s.west = Square.values()[s.ordinal() + W];
        s.northWest = NOSQUARE;
      }
      // lower left corner
      else if (s.file == File.a && s.rank == Rank.r1) {
        s.north = Square.values()[s.ordinal() + N];
        s.northEast = Square.values()[s.ordinal() + NE];
        s.east = Square.values()[s.ordinal() + E];
        s.southEast = NOSQUARE;
        s.south = NOSQUARE;
        s.southWest = NOSQUARE;
        s.west = NOSQUARE;
        s.northWest = NOSQUARE;
      }
      // lower right corner
      else if (s.file == File.h && s.rank == Rank.r1) {
        s.north = Square.values()[s.ordinal() + N];
        s.northEast = NOSQUARE;
        s.east = NOSQUARE;
        s.southEast = NOSQUARE;
        s.south = NOSQUARE;
        s.southWest = NOSQUARE;
        s.west = Square.values()[s.ordinal() + W];
        s.northWest = Square.values()[s.ordinal() + NW];
      }
      // else upper rank
      else if (s.rank == Rank.r8) {
        s.north = NOSQUARE;
        s.northEast = NOSQUARE;
        s.east = Square.values()[s.ordinal() + E];
        s.southEast = Square.values()[s.ordinal() + SE];
        s.south = Square.values()[s.ordinal() + S];
        s.southWest = Square.values()[s.ordinal() + SW];
        s.west = Square.values()[s.ordinal() + W];
        s.northWest = NOSQUARE;
      }
      // else file h
      else if (s.file == File.h) {
        s.north = Square.values()[s.ordinal() + N];
        s.northEast = NOSQUARE;
        s.east = NOSQUARE;
        s.southEast = NOSQUARE;
        s.south = Square.values()[s.ordinal() + S];
        s.southWest = Square.values()[s.ordinal() + SW];
        s.west = Square.values()[s.ordinal() + W];
        s.northWest = Square.values()[s.ordinal() + NW];
      }
      // else lower rank
      else if (s.rank == Rank.r1) {
        s.north = Square.values()[s.ordinal() + N];
        s.northEast = Square.values()[s.ordinal() + NE];
        s.east = Square.values()[s.ordinal() + E];
        s.southEast = NOSQUARE;
        s.south = NOSQUARE;
        s.southWest = NOSQUARE;
        s.west = Square.values()[s.ordinal() + W];
        s.northWest = Square.values()[s.ordinal() + NW];
      }
      // else file a
      else if (s.file == File.a) {
        s.north = Square.values()[s.ordinal() + N];
        s.northEast = Square.values()[s.ordinal() + NE];
        s.east = Square.values()[s.ordinal() + E];
        s.southEast = Square.values()[s.ordinal() + SE];
        s.south = Square.values()[s.ordinal() + S];
        s.southWest = NOSQUARE;
        s.west = NOSQUARE;
        s.northWest = NOSQUARE;
      }

      // pre-compute base row squares
      s.isPawnBaseRow[WHITE] = (s.rank == Rank.r2);
      s.isPawnBaseRow[BLACK] = (s.rank == Rank.r7);
    }

    // When we have to find a bit in a zero-bitmap the number of trailingZeros
    // is 64 and would lead to an out of array range. Therefore this array has
    // 65 elements and the last is NOSQUARE
    trailingZerosMap[64] = NOSQUARE;

  }

  // upward diagonal bitboards @formatter:off
  public static final long a1UpDiag   = computeDiag(a1, UP);
  public static final long a8UpDiag   = computeDiag(a8, UP);
  public static final long a7UpDiag   = computeDiag(a7, UP);
  public static final long a6UpDiag   = computeDiag(a6, UP);
  public static final long a5UpDiag   = computeDiag(a5, UP);
  public static final long a4UpDiag   = computeDiag(a4, UP);
  public static final long a3UpDiag   = computeDiag(a3, UP);
  public static final long a2UpDiag   = computeDiag(a2, UP);
  public static final long b1UpDiag   = computeDiag(b1, UP);
  public static final long c1UpDiag   = computeDiag(c1, UP);
  public static final long d1UpDiag   = computeDiag(d1, UP);
  public static final long e1UpDiag   = computeDiag(e1, UP);
  public static final long f1UpDiag   = computeDiag(f1, UP);
  public static final long g1UpDiag   = computeDiag(g1, UP);
  public static final long h1UpDiag   = computeDiag(h1, UP);
  // downward diagonal bitboards
  public static final long a1DownDiag = computeDiag(a1, DOWN);
  public static final long a2DownDiag = computeDiag(a2, DOWN);
  public static final long a3DownDiag = computeDiag(a3, DOWN);
  public static final long a4DownDiag = computeDiag(a4, DOWN);
  public static final long a5DownDiag = computeDiag(a5, DOWN);
  public static final long a6DownDiag = computeDiag(a6, DOWN);
  public static final long a7DownDiag = computeDiag(a7, DOWN);
  public static final long a8DownDiag = computeDiag(a8, DOWN);
  public static final long b8DownDiag = computeDiag(b8, DOWN);
  public static final long c8DownDiag = computeDiag(c8, DOWN);
  public static final long d8DownDiag = computeDiag(d8, DOWN);
  public static final long e8DownDiag = computeDiag(e8, DOWN);
  public static final long f8DownDiag = computeDiag(f8, DOWN);
  public static final long g8DownDiag = computeDiag(g8, DOWN);
  public static final long h8DownDiag = computeDiag(h8, DOWN);
  // computing the diagonals
  private static long computeDiag(Square square, boolean up) {
    long bitboard = 0L;
    do {
      bitboard |= square.bitBoard;
      if (up) square = square.getNorthEast();
      else square = square.getSouthEast();
    } while (square != NOSQUARE);
    return bitboard;
  }
  // @formatter:on

  /**
   * Default enum constructor
   */
  Square() {
  }

  /*
    Due to the weired way Java initializes Enums you can't access static values
    from the constructor as the constructor is executed first. This forces this
    construct which has the consequence of not being able to use final fields!
    Therefore we need to make them private (with Getter) to protect them from
    external change.
     */
  // 2nd static block
  static {
    // pre-compute fields
    for (Square s : validSquares) {
      // pre-compute diagonals for squares
      if ((s.bitBoard & a8UpDiag) != 0) s.upDiag = a8UpDiag;
      else if ((s.bitBoard & a7UpDiag) != 0) s.upDiag = a7UpDiag;
      else if ((s.bitBoard & a6UpDiag) != 0) s.upDiag = a6UpDiag;
      else if ((s.bitBoard & a5UpDiag) != 0) s.upDiag = a5UpDiag;
      else if ((s.bitBoard & a4UpDiag) != 0) s.upDiag = a4UpDiag;
      else if ((s.bitBoard & a3UpDiag) != 0) s.upDiag = a3UpDiag;
      else if ((s.bitBoard & a2UpDiag) != 0) s.upDiag = a2UpDiag;
      else if ((s.bitBoard & a1UpDiag) != 0) s.upDiag = a1UpDiag;
      else if ((s.bitBoard & b1UpDiag) != 0) s.upDiag = b1UpDiag;
      else if ((s.bitBoard & c1UpDiag) != 0) s.upDiag = c1UpDiag;
      else if ((s.bitBoard & d1UpDiag) != 0) s.upDiag = d1UpDiag;
      else if ((s.bitBoard & e1UpDiag) != 0) s.upDiag = e1UpDiag;
      else if ((s.bitBoard & f1UpDiag) != 0) s.upDiag = f1UpDiag;
      else if ((s.bitBoard & g1UpDiag) != 0) s.upDiag = g1UpDiag;
      else if ((s.bitBoard & h1UpDiag) != 0) s.upDiag = h1UpDiag;
      else s.upDiag = 0;

      if ((s.bitBoard & a1DownDiag) != 0) s.downDiag = a1DownDiag;
      else if ((s.bitBoard & a2DownDiag) != 0) s.downDiag = a2DownDiag;
      else if ((s.bitBoard & a3DownDiag) != 0) s.downDiag = a3DownDiag;
      else if ((s.bitBoard & a4DownDiag) != 0) s.downDiag = a4DownDiag;
      else if ((s.bitBoard & a5DownDiag) != 0) s.downDiag = a5DownDiag;
      else if ((s.bitBoard & a6DownDiag) != 0) s.downDiag = a6DownDiag;
      else if ((s.bitBoard & a7DownDiag) != 0) s.downDiag = a7DownDiag;
      else if ((s.bitBoard & a8DownDiag) != 0) s.downDiag = a8DownDiag;
      else if ((s.bitBoard & b8DownDiag) != 0) s.downDiag = b8DownDiag;
      else if ((s.bitBoard & c8DownDiag) != 0) s.downDiag = c8DownDiag;
      else if ((s.bitBoard & d8DownDiag) != 0) s.downDiag = d8DownDiag;
      else if ((s.bitBoard & e8DownDiag) != 0) s.downDiag = e8DownDiag;
      else if ((s.bitBoard & f8DownDiag) != 0) s.downDiag = f8DownDiag;
      else if ((s.bitBoard & g8DownDiag) != 0) s.downDiag = g8DownDiag;
      else if ((s.bitBoard & h8DownDiag) != 0) s.downDiag = h8DownDiag;
      else s.downDiag = 0;
    }
  }

  /**
   * @param x88index
   * @return the Square for the given index of a 0x88 board or NOSQUARE if not a valid index
   */
  public static Square getSquare(int x88index) {
    return Square.values[x88index];
  }
  /**
   * Returns the Square for the given file and rank
   * @param f
   * @param r
   * @return the Square for the given file and rank
   */
  public static Square getSquare(int f, int r) {
    if (f < 1 || f > 8 || r < 1 || r > 8) return Square.NOSQUARE;
    // index starts with 0 while file and rank start with 1 - decrease
    return Square.values[(7 - (r - 1)) * 8 + (f - 1)];
  }

  /**
   * Finds the first set bit in a bitboard and returns the according Square.
   * Can be used to loop through all set squares in a bitboard in conjunction
   * with removeFirstSquare()
   *
   * @param bitboard
   * @return the first Square of the given Bitboard from a8-h8-h1
   */
  public static Square getFirstSquare(long bitboard) {
    return trailingZerosMap[Long.numberOfTrailingZeros(bitboard)];
  }

  /**
   * Finds the first set bit in a bitboard and removes it.
   * Can be used to loop through all set squares in a bitboard in conjunction
   * with getFirstSquare()
   *
   * @param bitboard
   * @return the bitboard without the removed square
   */
  public static long removeFirstSquare(long bitboard) {
    final long bit = trailingZerosMap[Long.numberOfTrailingZeros(bitboard)].bitboard();
    assert (bitboard & bit) != 0 : "Bit to be removed not set.";
    return bitboard ^ bit;
  }

  /**
   * Returns the square from the given notation and checks if
   * it is a valid square.
   *
   * @param s
   * @return The Square of the notation or NOSQAURE if the notation was invalid
   */
  public static Square fromUCINotation(final String s) {
    final Square square;
    try {
      square = Square.valueOf(s);
    } catch (IllegalArgumentException e) {
      return NOSQUARE;
    }
    return square.validSquare ? square : NOSQUARE;
  }

  /**
   * @return true if Square is a valid chess square
   */
  public boolean isValidSquare() {
    return validSquare;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getNorth() {
    return north;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getNorthEast() {
    return northEast;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getEast() {
    return east;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getSouthEast() {
    return southEast;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getSouth() {
    return south;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getSouthWest() {
    return southWest;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getWest() {
    return west;
  }

  /**
   * Returns the square north of this square. as seen from the white side.
   *
   * @return square north
   */
  public Square getNorthWest() {
    return northWest;
  }

  /**
   * @return Square.File for this Square
   */
  public File getFile() {
    return file;
  }

  /**
   * @return Square.Rank for this Square
   */
  public Rank getRank() {
    return rank;
  }

  /**
   * Returns a list of all valid squares in the correct order. [0]=a1, [63]=h8
   */
  public static List<Square> getValueList() {
    return validSquares;
  }

  /**
   * pre-computed on which upwards diagonal this square is
   */
  public long getUpDiag() {
    return upDiag;
  }

  /**
   * pre-computed on which downwards diagonal this square is
   */
  public long getDownDiag() {
    return downDiag;
  }

  /**
   * pre-computed bitBoard representation
   */
  public long bitboard() {
    return bitBoard;
  }

  /**
   * @return true if this square is on the 2nd rank
   */
  public boolean isWhitePawnBaseRow() {
    return this.isPawnBaseRow[WHITE];
  }

  /**
   * @return true if this square is on the 7th rank
   */
  public boolean isBlackPawnBaseRow() {
    return this.isPawnBaseRow[BLACK];
  }

  /**
   * @return true if this square is on the 2nd (white) or 7th (black) rank
   */
  public boolean isPawnBaseRow(Color c) {
    return this.isPawnBaseRow[c.ordinal()];
  }

  /**
   * This enum represents all files of a chess board. If used in a loop via values() omit NOFILE.
   */
  public enum File {a, b, c, d, e, f, g, h, NOFILE;

    // pre-filled list with all squares
    public static final File[] values;
    public final        long   bitBoard;

    static {
      values = File.values();
    }

    File() {
      final long a = 0b0000000100000001000000010000000100000001000000010000000100000001L;
      if (ordinal() < 8) bitBoard = a << ordinal();
      else bitBoard = 0;
    }

    /**
     * returns the file index number from 1..8
     *
     * @return
     */
    public int get() {
      return this.ordinal() + 1;
    }

    /**
     * returns the enum File for a given file number
     *
     * @param file
     * @return
     */
    public static File get(int file) {
      return File.values[file - 1];
    }

    @Override
    public String toString() {
      if (this == NOFILE) {
        return "-";
      }
      return this.name();
    }}

  /**
   * This enum represents all ranks of a chess board If used in a loop via values() omit NORANK.
   */
  public enum Rank {r1, r2, r3, r4, r5, r6, r7, r8, NORANK;

    // pre-filled list with all squares
    public static final Rank[] values;
    public final        long   bitBoard;

    static {
      values = Rank.values();
    }

    Rank() {
      final long mask = 0b11111111L;
      if (ordinal() < 8) this.bitBoard = mask << (8 * (7 - ordinal()));
      else bitBoard = 0;
    }

    /**
     * returns the rank index number from 1..8
     *
     * @return
     */
    public int get() {
      return this.ordinal() + 1;
    }

    /**
     * returns the enum Rank for a given rank number
     *
     * @param rank
     * @return
     */
    public static Rank get(int rank) {
      return Rank.values[rank - 1];
    }

    @Override
    public String toString() {
      if (this == NORANK) {
        return "-";
      }
      return "" + (this.ordinal() + 1);
    }}

}
