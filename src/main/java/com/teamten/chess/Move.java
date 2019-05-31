/*
 *
 *    Copyright 2016 Lawrence Kesteloot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

// Copyright 2011 Lawrence Kesteloot

package com.teamten.chess;

import java.util.Comparator;

/**
 * Stores a move in a chess game and its inverse.
 */
public class Move {
    private final int mFromIndex;
    private final int mToIndex;
    private final Piece mMovingPiece;
    private final Piece mCapturedPiece;
    private final Piece mPromotedPiece;
    /**
     * Whether the moving side is in check after this move.
     */
    private boolean mMovingInCheck;
    /**
     * Whether the non-moving side is in check after this move.
     */
    private boolean mOtherInCheck;
    /**
     * XXX Remove. Redundant with mOtherInCheck.
     */
    private boolean mCheck;
    /**
     * How many moves since the last capture or pawn move in this game. If this is
     * a capture or pawn move, then the value is 0. If this is the first move of
     * the game (and it's not a pawn move or capture), then the value is 1. The value
     * of -1 means "unknown".
     */
    private int mMovesSinceCapture;

    /**
     * Create a new move.
     *
     * @param capturedPiece the piece captured, or EMPTY if it's a move.
     */
    private Move(int fromIndex, int toIndex, Piece movingPiece, Piece capturedPiece,
            Piece promotedPiece) {

        mFromIndex = fromIndex;
        mToIndex = toIndex;
        mMovingPiece = movingPiece;
        mCapturedPiece = capturedPiece;
        mPromotedPiece = promotedPiece;

        if (capturedPiece != Piece.EMPTY || movingPiece.getPieceType() == PieceType.PAWN) {
            mMovesSinceCapture = 0;
        } else {
            // Unknown, we don't have access to the game history. Will be filled in later.
            mMovesSinceCapture = -1;
        }
    }

    /**
     * Alternative constructor where the moving and captured pieces are
     * determined by the board. Pawns are not promoted.
     */
    public static Move make(Board board, int fromIndex, int toIndex) {
        return makePromotion(board, fromIndex, toIndex, Piece.EMPTY);
    }

    /**
     * Alternative constructor where the moving and captured pieces are
     * determined by the board.
     */
    public static Move makePromotion(Board board, int fromIndex, int toIndex,
            Piece promotedPiece) {

        return new Move(fromIndex, toIndex, board.getPiece(fromIndex), board.getPiece(toIndex),
                promotedPiece);
    }

    /**
     * Parse an long algebraic notation (LAN).
     *
     * @throws IllegalArgumentException if the string is not in LAN.
     */
    public static Move parseLongAlgebraicNotation(Board board, String lan) {
        if (lan.equals("0000")) {
            // Null move. We don't handle this yet.
            throw new IllegalArgumentException("Can't yet handle null move");
        }

        if (lan.length() < 4) {
            throw new IllegalArgumentException("Move too short");
        }

        int fromIndex = Board.fromPosition(lan.substring(0, 2));
        int toIndex = Board.fromPosition(lan.substring(2, 4));

        Piece promotedPiece = Piece.EMPTY;
        if (lan.length() == 5) {
            if (lan.charAt(4) == 'q') {
                if (board.getSide() == Side.WHITE) {
                    promotedPiece = Piece.WHITE_QUEEN;
                } else {
                    promotedPiece = Piece.BLACK_QUEEN;
                }
            } else {
                throw new IllegalArgumentException("Can't handle non-queen promotion");
            }
        }

        return makePromotion(board, fromIndex, toIndex, promotedPiece);
    }

    /**
     * Returns the source index.
     */
    public int getFromIndex() {
        return mFromIndex;
    }

    /**
     * Returns the destination index.
     */
    public int getToIndex() {
        return mToIndex;
    }

    /**
     * Return the piece being moved.
     */
    public Piece getMovingPiece() {
        return mMovingPiece;
    }

    /**
     * Return the piece captured by this move, or EMPTY if not a capturing move.
     */
    public Piece getCapturedPiece() {
        return mCapturedPiece;
    }

    /**
     * Return the piece we're promoting to, or EMPTY if not a promoting move.
     */
    public Piece getPromotedPiece() {
        return mPromotedPiece;
    }

    /**
     * Return whether this move puts the other king in check.
     */
    public boolean isCheck() {
        return mCheck;
    }

    /**
     * Set whether this move puts the other king in check.
     */
    public void setCheck(boolean check) {
        mCheck = check;
    }

    public void setMovingInCheck(boolean movingInCheck) {
        mMovingInCheck = movingInCheck;
    }

    public boolean isMovingInCheck() {
        return mMovingInCheck;
    }

    public void setOtherInCheck(boolean otherInCheck) {
        mOtherInCheck = otherInCheck;
    }

    public boolean isOtherInCheck() {
        return mOtherInCheck;
    }

    public int getMovesSinceCapture() {
        return mMovesSinceCapture;
    }

    public void setMovesSinceCapture(int movesSinceCapture) {
        mMovesSinceCapture = movesSinceCapture;
    }

    /**
     * Applies this move to the board.
     */
    public void applyMove(Board board) {
        Piece capturedPiece = board.movePiece(mFromIndex, mToIndex);
        if (mPromotedPiece != Piece.EMPTY) {
            board.setPiece(mToIndex, mPromotedPiece);
        }

        if (capturedPiece != mCapturedPiece) {
            throw new IllegalStateException("applied move " + this
                    + " doesn't match captured piece ("
                    + mCapturedPiece + " vs " + capturedPiece + ")");
        }
    }

    /**
     * Applies the inverse of this move. The applyMove() method must have
     * already been called.
     */
    public void applyInverseMove(Board board) {
        board.movePiece(mToIndex, mFromIndex);
        board.setPiece(mToIndex, mCapturedPiece);
        if (mPromotedPiece != Piece.EMPTY) {
            board.setPiece(mFromIndex,
                    mPromotedPiece.getSide() == Side.WHITE ? Piece.WHITE_PAWN : Piece.BLACK_PAWN);
        }
    }

    /**
     * Return the Algebraic Notation.
     *
     * See http://en.wikipedia.org/wiki/Algebraic_Chess_Notation
     * 
     * @param useFigurine if true, uses the HTML for a drawing instead of a piece name.
     */
    public String getAlgebraicNotation(boolean useFigurine) {
        StringBuilder builder = new StringBuilder();

        if (mMovingPiece.getPieceType() == PieceType.PAWN) {
            if (mCapturedPiece != Piece.EMPTY) {
                builder.append(Board.getFileLetter(mFromIndex));
            }
        } else {
            if (useFigurine) {
                builder.append(mMovingPiece.getHtmlCharacter());
            } else {
                // Doesn't display well in iTerm, it's wider than a letter:
                /// builder.append(mMovingPiece.getUnicodeCharacter());

                builder.append(mMovingPiece.getAlgebraicCharacter());
            }

            // Here we're supposed to put file and/or rank of moving piece if it's
            // ambiguous, but knowing whether it's ambiguous is a lot of work to do
            // when generating pieces, and it's too late when displaying them. If we
            // really want this we'll need to explicitly calculate this info when a
            // move is going to be displayed.
            builder.append(Board.getPosition(mFromIndex));
        }

        if (mCapturedPiece != Piece.EMPTY) {
            builder.append('x');
        }

        builder.append(Board.getPosition(mToIndex));

        if (mPromotedPiece != Piece.EMPTY) {
            builder.append("=");
            builder.append(Character.toUpperCase(mPromotedPiece.getPieceType().getCharacter()));
        }

        if (mCheck) {
            builder.append("+");
        }

        return builder.toString();
    }

    /**
     * Return the long algebraic notation for the move, e.g., "e2e4" or "b7b8q".
     */
    public String getLongAlgebraicNotation() {
        StringBuilder builder = new StringBuilder();

        builder.append(Board.getPosition(mFromIndex));
        builder.append(Board.getPosition(mToIndex));
        if (mPromotedPiece != Piece.EMPTY) {
            builder.append(Character.toLowerCase(mPromotedPiece.getPieceType().getCharacter()));
        }

        return builder.toString();
    }

    @Override // Object
    public String toString() {
        return getAlgebraicNotation(false);
        /*
        StringBuilder builder = new StringBuilder();

        builder.append(Board.getPosition(mFromIndex));
        builder.append(mCapturedPiece == Piece.EMPTY ? "-" : "x");
        builder.append(Board.getPosition(mToIndex));

        if (mPromotedPiece != Piece.EMPTY) {
            builder.append("=");
            builder.append(Character.toUpperCase(mPromotedPiece.getPieceType().getCharacter()));
        }

        if (mCheck) {
            builder.append("+");
        }

        return builder.toString();
        */
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Move move = (Move) o;

        if (mFromIndex != move.mFromIndex) {
            return false;
        }
        if (mToIndex != move.mToIndex) {
            return false;
        }
        if (mMovingPiece != null ? !mMovingPiece.equals(move.mMovingPiece) : move.mMovingPiece != null) {
            return false;
        }
        if (mCapturedPiece != null ? !mCapturedPiece.equals(move.mCapturedPiece) : move.mCapturedPiece != null) {
            return false;
        }
        return mPromotedPiece != null ? mPromotedPiece.equals(move.mPromotedPiece) : move.mPromotedPiece == null;
    }

    @Override
    public int hashCode() {
        int result = mFromIndex;
        result = 31*result + mToIndex;
        result = 31*result + (mMovingPiece != null ? mMovingPiece.hashCode() : 0);
        result = 31*result + (mCapturedPiece != null ? mCapturedPiece.hashCode() : 0);
        result = 31*result + (mPromotedPiece != null ? mPromotedPiece.hashCode() : 0);
        return result;
    }

    /**
     * A comparator that puts best captures first.
     */
    public static final Comparator<Move> CAPTURE_COMPARATOR = new Comparator<Move>() {
        @Override // Comparator
        public int compare(Move m1, Move m2) {
            Piece p1 = m1.getCapturedPiece();
            Piece p2 = m2.getCapturedPiece();

            int v1 = p1 == Piece.EMPTY ? 0 : p1.getPieceType().getValue();
            int v2 = p2 == Piece.EMPTY ? 0 : p2.getPieceType().getValue();

            // Highest values first.
            if (v2 != v1) {
                return v2 - v1;
            }

            // Checks first.
            if (m1.isOtherInCheck() && !m2.isOtherInCheck()) {
                return -1;
            } else if (!m1.isOtherInCheck() && m2.isOtherInCheck()) {
                return 1;
            }

            // Tie.
            return 0;
        }
    };

    /**
     * Serialize the move to a string. Deserialize later with deserialize(). The
     * string contains only URL-safe characters (a-z, A-Z, digits, and hyphens).
     */
    public String serialize() {
        StringBuilder builder = new StringBuilder();

        builder.append(Board.getPosition(mFromIndex));
        builder.append("-");
        builder.append(Board.getPosition(mToIndex));

        builder.append(mMovingPiece.getCharacter());

        if (mCapturedPiece != Piece.EMPTY) {
            builder.append("x");
            builder.append(mCapturedPiece.getCharacter());
        }

        if (mPromotedPiece != Piece.EMPTY) {
            builder.append("y");
            builder.append(mPromotedPiece.getCharacter());
        }

        if (mCheck) {
            builder.append("z");
        }

        return builder.toString();
    }

    /**
     * Deserialize a move from the string created by serialize().
     */
    public static Move deserialize(String str) {
        int fromIndex = Board.fromPosition(str.substring(0, 2));
        int toIndex = Board.fromPosition(str.substring(3, 5));
        Piece movingPiece;
        Piece capturedPiece;
        Piece promotedPiece;

        // Get moving piece.
        int i = 5;
        movingPiece = Piece.getPieceForCharacter(str.charAt(i));
        i++;

        // Check for captured piece.
        if (i < str.length() && str.charAt(i) == 'x') {
            i++;
            capturedPiece = Piece.getPieceForCharacter(str.charAt(i));
            i++;
        } else {
            capturedPiece = Piece.EMPTY;
        }

        // Check for promoted piece.
        if (i < str.length() && str.charAt(i) == 'y') {
            i++;
            promotedPiece = Piece.getPieceForCharacter(str.charAt(i));
            i++;
        } else {
            promotedPiece = Piece.EMPTY;
        }

        Move move = new Move(fromIndex, toIndex, movingPiece, capturedPiece, promotedPiece);

        // Check for check.
        if (i < str.length() && str.charAt(i) == 'z') {
            move.setCheck(true);
            i++;
        }

        return move;
    }
}
