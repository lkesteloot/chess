// Copyright 2015 Lawrence Kesteloot

package com.teamten.chess;

import java.util.ArrayList;
import java.util.List;

import com.teamten.chess.Board;
import com.teamten.chess.ComputerPlayer;
import com.teamten.chess.Piece;
import com.teamten.chess.Move;
import com.teamten.chess.Game;
import com.teamten.chess.Side;
import com.teamten.chess.server.ChessServer;

/**
 * Plays chess to test the code.
 */
public class Chess {
    private static final boolean CHECK_TEST = false;
    private static final boolean PROMOTION_TEST = false;
    private static final boolean PLAY_GAME = false;
    private static final boolean PLAY_PUZZLE_GAME = true;
    private static final boolean CHESS_SERVER = false;

    public static void main(String[] args) {
        final Board board = new Board();
        final Game game = new Game(board);

        if (PLAY_GAME) {
            ComputerPlayer whitePlayer = new ComputerPlayer(board, game, Side.WHITE);
            ComputerPlayer blackPlayer = new ComputerPlayer(board, game, Side.BLACK);

            if (args.length == 6) {
                // Ew.
                board.initializeWithFen(args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5]);
            } else {
                board.initializeTraditionalChess();
            }
            board.print(System.out, "", null);

            int side = Side.WHITE;
            while (true) {
                System.out.println(board);
                ComputerPlayer player = side == Side.WHITE ? whitePlayer : blackPlayer;
                ComputerPlayer.EvaluatedMove evaluatedMove = makePlayerMove(player);
                game.writePgn("game.pgn", Side.IN_PROGRESS, 0, "computer1", "computer2");
                Move move = evaluatedMove.getMove();
                if (move == null) {
                    break;
                }
                if (game.isDrawFrom50MoveRule()) {
                    System.out.println("Game is drawn from 50-move-rule");
                    break;
                }
                board.print(System.out, side == Side.BLACK ? "    " : "", move);
                side = Side.getOtherSide(side);
            }
        }

        if (PLAY_PUZZLE_GAME) {
            playPuzzleGame();
        }

        if (CHECK_TEST) {
            board.initializeEmpty();
            int index = Board.getIndex(4, 6);
            Piece piece = Piece.WHITE_KING;
            board.setPiece(index, piece);
            board.setPiece(Board.getIndex(6, 7), Piece.BLACK_QUEEN);
            board.setPiece(Board.getIndex(3, 5), Piece.WHITE_KNIGHT);
            board.print(System.out, "", null);
            List<Move> moveList = new ArrayList<Move>();
            piece.addMoves(board, index, false, moveList);
            board.removeIllegalMoves(moveList);
            for (Move move : moveList) {
                game.addMove(move);
                board.print(System.out, "    ", move);

                game.undoMove();
            }
        }

        if (PROMOTION_TEST) {
            board.initializeEmpty();
            board.setPiece(Board.getIndex(4, 6), Piece.WHITE_PAWN);
            board.setPiece(Board.getIndex(1, 1), Piece.WHITE_KING);
            board.setPiece(Board.getIndex(1, 8), Piece.BLACK_KING);
            board.print(System.out, "", null);
            ComputerPlayer computerPlayer = new ComputerPlayer(board, game, Side.WHITE);
            makePlayerMove(computerPlayer);
            board.print(System.out, "", null);
        }

        if (CHESS_SERVER) {
            ChessServer chessServer = new ChessServer();
            chessServer.start();
        }
    }

    private static void playPuzzleGame() {
        // Starting side.
        int side = Side.WHITE;

        // http://www.futilitycloset.com/2011/08/20/two-chess-problems-solution/
        Board board = Board.parse(
                "........" +
                ".pN....." +
                ".Pb....." +
                "n.q.n..." +
                "........" +
                "PpkpP..Q" +
                ".N.R...." +
                "..K.....");

        // http://www.futilitycloset.com/2011/09/10/black-and-white-14/
        board = Board.parse(
                ".......Q" +
                ".......b" +
                ".....p.p" +
                "........" +
                ".....NN." +
                "....KP.." +
                ".......R" +
                "......k.");

        // http://www.futilitycloset.com/2011/11/07/black-and-white-18/
        board = Board.parse(
                "........" +
                "........" +
                "........" +
                "........" +
                ".......p" +
                "....PQpp" +
                "R.....q." +
                "KR....Bk");

        // http://www.futilitycloset.com/2011/12/18/black-and-white-22/
        board = Board.parse(
                "........" +
                "........" +
                "........" +
                "..N....." +
                "...k.K.." +
                "........" +
                "..Q....." +
                "........");

        // http://www.futilitycloset.com/2011/12/24/black-and-white-23/
        board = Board.parse(
                ".......R" +
                "........" +
                "........" +
                "........" +
                "......pq" +
                ".......k" +
                "....Np.r" +
                ".....KbQ");

        // http://www.futilitycloset.com/2011/12/31/black-and-white-24/
        board = Board.parse(
                "...R...K" +
                "........" +
                ".....Q.." +
                "........" +
                ".....N.." +
                ".B......" +
                "..PB...." +
                ".qbk....");

        // http://www.futilitycloset.com/2012/02/10/black-and-white-30/
        board = Board.parse(
                "........" +
                "........" +
                "........" +
                "...p...." +
                "...R...." +
                "K.p....." +
                ".pN....R" +
                ".kb....Q");

        // http://www.futilitycloset.com/2012/03/02/black-and-white-33/
        board = Board.parse(
                "kr.....R" +
                "rp......" +
                "......K." +
                "........" +
                "....Q..." +
                "........" +
                "........" +
                "R.......");

        // http://www.futilitycloset.com/2012/03/23/black-and-white-36/
        board = Board.parse(
                "..k..r.r" +
                "...R.Q.." +
                "..P....." +
                "...Pb..." +
                "........" +
                ".......P" +
                "......P." +
                ".......K");

        // http://www.futilitycloset.com/2013/05/03/black-and-white-94/
        board = Board.parse(
                "......Bk" +
                "......bP" +
                "......b." +
                "........" +
                "...B.K.." +
                "........" +
                "........" +
                "......Q.");

        // http://www.chessmaniac.com/ELORating/ELO_Chess_Rating.shtml, diagram 1
        board = Board.parse(
                "r.b...k." +
                "......p." +
                "P.n.pr.p" +
                "q.p....." +
                ".b.P...." +
                "..N..N.." +
                "PP.QBPPP" +
                "R...K..R");
        side = Side.BLACK;

        // http://www.chessmaniac.com/ELORating/ELO_Chess_Rating.shtml, diagram 3
        board = Board.parse(
                "........" +
                "...r..p." +
                "ppPBp.p." +
                ".k......" +
                ".n..K..." +
                "......R." +
                ".P...P.." +
                "........");
        side = Side.BLACK;

        board = Board.parse(
                "........" +
                "....kb.p" +
                "..p.P.pP" +
                ".pP...P." +
                ".P...K.." +
                ".B......" +
                "........" +
                "........");
        side = Side.BLACK;

        // http://i.imgur.com/kSajzIn.jpg

        board = Board.parse(
                "........" +
                "....p..." +
                ".pP....." +
                "p..P...R" +
                "...k.K.." +
                "........" +
                ".....P.." +
                "....r...");
        side = Side.WHITE;

        // https://plus.google.com/u/0/+Chessendgames/posts/7aLW4hER7JC
        board = Board.parse(
                "k....n.." +
                "..Q....." +
                "..K....." +
                "...B...." +
                ".q......" +
                "........" +
                "........" +
                "........");
        side = Side.WHITE;

        board = Board.parse(
                "...RN..." +
                "r....p.p" +
                "......p." +
                ".....k.." +
                "p...R..." +
                "....P.K." +
                ".....P.P" +
                "r.......");
        side = Side.WHITE;

        /*
        board = Board.parse(
                "........" +
                "........" +
                "........" +
                "........" +
                "........" +
                "........" +
                "........" +
                "........");
        side = Side.WHITE;
        */

        // Upper case = white.

        Game game = new Game(board);
        ComputerPlayer whitePlayer = new ComputerPlayer(board, game, Side.WHITE);
        ComputerPlayer blackPlayer = new ComputerPlayer(board, game, Side.BLACK);

        board.print(System.out, "", null);

        while (true) {
            ComputerPlayer player = side == Side.WHITE ? whitePlayer : blackPlayer;
            ComputerPlayer.EvaluatedMove evaluatedMove = makePlayerMove(player);
            Move move = evaluatedMove.getMove();
            if (move == null) {
                break;
            }
            if (game.isDrawFrom50MoveRule()) {
                System.out.println("Game is drawn from 50-move-rule");
                break;
            }
            board.print(System.out, side == Side.BLACK ? "    " : "", move);
            side = Side.getOtherSide(side);
        }
    }

    private static ComputerPlayer.EvaluatedMove makePlayerMove(ComputerPlayer computerPlayer) {
        ComputerPlayer.Result result = computerPlayer.makeMove(20000);
        ComputerPlayer.EvaluatedMove evaluatedMove = result.mEvaluatedMove;
        int side = computerPlayer.getSide();

        Move move = evaluatedMove.getMove();
        if (move == null) {
            System.out.println(Side.toString(side) + " cannot move, end of game");
        } else {
            System.out.printf("%s makes move %s with score %f (%d ms, %,d moves considered)%n",
                    Side.toString(side), move, evaluatedMove.getScore(),
                    result.mElapsedTime, result.mMovesConsidered);

            System.out.print("Principal variation:");
            for (ComputerPlayer.EvaluatedMove e = evaluatedMove; e != null && e.getMove() != null; e = e.getNextMove()) {
                int backgroundColor = 243;
                int foregroundColor;
                if (side == Side.WHITE) {
                    foregroundColor = 255;
                } else {
                    foregroundColor = 232;
                }
                System.out.printf(" %c[48;5;%dm%c[38;5;%dm %s %c[0m",
                        27, backgroundColor, 27, foregroundColor, e.getMove(), 27);
                side = Side.getOtherSide(side);
            }
            System.out.println();
        }

        return evaluatedMove;
    }
}
