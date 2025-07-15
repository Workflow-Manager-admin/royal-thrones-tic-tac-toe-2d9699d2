package com.example.frontendkotlin

import android.animation.ObjectAnimator
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * MainActivity implements a minimal, modern Tic Tac Toe game with king/queen piece visuals.
 * Features:
 * - Interactive 3x3 board, centered on screen
 * - X's show as "♚" (king, in primary color), O's as "♛" (queen, in secondary color)
 * - Two local players
 * - Game status bar (whose turn, winner, draw)
 * - Animated piece placement
 * - Game restart and reset buttons
 * - Winner and draw logic
 * - Modern, minimal, light UI per style guide and description
 */
class MainActivity : AppCompatActivity() {

    // Game model
    private var board = Array(3) { arrayOfNulls<Char>(3) } // null, 'X', 'O'
    private var currentPlayer: Char = 'X' // 'X' = King, 'O' = Queen
    private var isGameOver = false

    // UI elements
    private lateinit var boardGrid: GridLayout
    private lateinit var statusText: TextView
    private lateinit var restartButton: Button
    private lateinit var resetButton: Button
    private val cells: MutableList<Button> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        // Find views
        statusText = findViewById(R.id.statusText)
        boardGrid = findViewById(R.id.boardGrid) as GridLayout
        restartButton = findViewById(R.id.restartButton)
        resetButton = findViewById(R.id.resetButton)

        setupBoard()
        updateStatusBar()

        restartButton.setOnClickListener { restartGame() }
        resetButton.setOnClickListener { resetGame() }
    }

    // Setup 3x3 grid buttons dynamically to ensure equal, minimalistic, responsive squares.
    private fun setupBoard() {
        // Just in case—clear views
        boardGrid.removeAllViews()
        cells.clear()
        val cellSize = resources.displayMetrics.widthPixels.coerceAtMost(resources.displayMetrics.heightPixels) / 3.5

        for (row in 0..2) {
            for (col in 0..2) {
                val btn = Button(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSize.toInt()
                        height = cellSize.toInt()
                        rowSpec = GridLayout.spec(row, 1, 1f)
                        columnSpec = GridLayout.spec(col, 1, 1f)
                        setMargins(6, 6, 6, 6)
                    }
                    gravity = Gravity.CENTER
                    textSize = 42f
                    typeface = Typeface.DEFAULT_BOLD
                    setBackgroundResource(android.R.color.transparent)
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.transparent))
                    setOnClickListener { onCellClick(row, col, this) }
                }
                cells.add(btn)
                boardGrid.addView(btn)
            }
        }
        refreshBoardUI(animated = false)
    }

    /** Handles piece placement and triggers animation, updates model and checks for game over. */
    private fun onCellClick(row: Int, col: Int, btn: Button) {
        if (isGameOver) return
        if (board[row][col] != null) return

        board[row][col] = currentPlayer
        animatePiecePlacement(btn, currentPlayer)
        refreshBoardUI(animated = false)
        if (checkWinner(row, col)) {
            isGameOver = true
            val king = "♚"
            val queen = "♛"
            statusText.text = if (currentPlayer == 'X')
                "$king ${getString(R.string.winner_king)}"
            else
                "$queen ${getString(R.string.winner_queen)}"
        } else if (isBoardFull()) {
            isGameOver = true
            statusText.text = getString(R.string.draw)
        } else {
            switchPlayer()
            updateStatusBar()
        }
    }

    /** Animated piece entry: Fade/scale in. */
    private fun animatePiecePlacement(btn: Button, value: Char) {
        btn.alpha = 0f
        btn.scaleX = 0.85f
        btn.scaleY = 0.85f
        btn.text = pieceSymbol(value)
        btn.setTextColor(
            ContextCompat.getColor(
                this, if (value == 'X') R.color.kingColor else R.color.queenColor
            )
        )
        ObjectAnimator.ofFloat(btn, "alpha", 0f, 1f).apply {
            duration = 280
            start()
        }
        ObjectAnimator.ofFloat(btn, "scaleX", 0.85f, 1f).apply { duration = 250; start() }
        ObjectAnimator.ofFloat(btn, "scaleY", 0.85f, 1f).apply { duration = 250; start() }
    }

    /** Refresh all buttons to match current board state. */
    private fun refreshBoardUI(animated: Boolean) {
        for (i in 0..8) {
            val r = i / 3
            val c = i % 3
            val cellVal = board[r][c]
            val btn = cells[i]
            if (cellVal == null) {
                btn.text = ""
                btn.setTextColor(ContextCompat.getColor(this, R.color.black))
            } else {
                btn.text = pieceSymbol(cellVal)
                btn.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (cellVal == 'X') R.color.kingColor else R.color.queenColor
                    )
                )
            }
            // Subtle highlight animation
            btn.isEnabled = !isGameOver && board[r][c] == null
        }
    }

    /** Returns the thematic Unicode symbol for pieces. */
    private fun pieceSymbol(value: Char): String {
        return if (value == 'X') "♚" else "♛"
    }

    /** Update status bar according to current state. */
    private fun updateStatusBar() {
        // Add chess pieces to status message in code safely
        val king = "♚"
        val queen = "♛"
        statusText.text = if (currentPlayer == 'X')
            "$king ${getString(R.string.turn_king)}"
        else
            "$queen ${getString(R.string.turn_queen)}"
    }

    /** Switch active player. */
    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
    }

    /** Checks if the board is full (draw). */
    private fun isBoardFull(): Boolean {
        for (row in board)
            for (cell in row)
                if (cell == null)
                    return false
        return true
    }

    /** Winner logic, highlights if winner. Returns true if someone won. */
    private fun checkWinner(lastRow: Int, lastCol: Int): Boolean {
        val symbol = board[lastRow][lastCol] ?: return false
        // Check row
        if ((0..2).all { board[lastRow][it] == symbol }) {
            highlightWinningRow(lastRow, 0, lastRow, 2)
            return true
        }
        // Check column
        if ((0..2).all { board[it][lastCol] == symbol }) {
            highlightWinningRow(0, lastCol, 2, lastCol)
            return true
        }
        // Diagonal main
        if (lastRow == lastCol && (0..2).all { board[it][it] == symbol }) {
            highlightWinningRow(0, 0, 2, 2)
            return true
        }
        // Anti-diagonal
        if (lastRow + lastCol == 2 && (0..2).all { board[it][2 - it] == symbol }) {
            highlightWinningRow(0, 2, 2, 0)
            return true
        }
        return false
    }

    /** Highlights winning cells with accent color. */
    private fun highlightWinningRow(r1: Int, c1: Int, r2: Int, c2: Int) {
        val cellsToHighlight = when {
            r1 == r2 -> (0..2).map { i -> 3 * r1 + i }
            c1 == c2 -> (0..2).map { i -> 3 * i + c1 }
            r1 == 0 && c1 == 0 -> (0..2).map { i -> 3 * i + i }
            r1 == 0 && c1 == 2 -> (0..2).map { i -> 3 * i + (2 - i) }
            else -> listOf()
        }
        cellsToHighlight.forEach { idx ->
            cells[idx].setTextColor(ContextCompat.getColor(this, R.color.accent))
        }
    }

    /** Resets the game to initial state for rematch (but preserves total plays). */
    // PUBLIC_INTERFACE
    fun restartGame() {
        board = Array(3) { arrayOfNulls<Char>(3) }
        isGameOver = false
        currentPlayer = 'X'
        refreshBoardUI(animated = false)
        updateStatusBar()
    }

    /** Resets everything, including piece scores (future extension), just restarts for now. */
    // PUBLIC_INTERFACE
    fun resetGame() {
        restartGame()
        // TODO: If implementing scores or stats, reset here.
    }
}
