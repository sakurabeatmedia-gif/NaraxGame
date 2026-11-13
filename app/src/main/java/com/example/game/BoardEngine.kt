package com.example.game

import com.example.model.BoosterType
import com.example.model.CandyColor
import com.example.model.CandyTile
import kotlin.random.Random

data class GridPos(val row: Int, val col: Int)

data class TileFallInfo(
    val fromRow: Int,
    val toRow: Int,
    val col: Int,
    val tile: CandyTile
)

data class BoardDropResult(
    val newBoard: List<List<CandyTile>>,
    val fallAnimations: List<TileFallInfo>
)

class BoardEngine(
    private val random: Random = Random.Default
) {
    companion object {
        const val GRID_SIZE = 7
    }

    private var nextTileId = 1L

    fun nextId(): Long = nextTileId++

    fun createPlayableBoard(): List<List<CandyTile>> {
        var attempts = 0
        while (attempts < 100) {
            attempts++
            val grid = MutableList(GRID_SIZE) { row ->
                MutableList(GRID_SIZE) { col ->
                    val forbidden = mutableSetOf<CandyColor>()
                    if (col >= 2) {
                        val c1 = null
                        // Handled below row by row
                    }
                    CandyTile(nextId(), CandyColor.entries.random(random))
                }
            }
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    val forbidden = mutableSetOf<CandyColor>()
                    if (c >= 2 && grid[r][c - 1].color == grid[r][c - 2].color) {
                        forbidden.add(grid[r][c - 1].color)
                    }
                    if (r >= 2 && grid[r - 1][c].color == grid[r - 2][c].color) {
                        forbidden.add(grid[r - 1][c].color)
                    }
                    val allowed = CandyColor.entries.filterNot { it in forbidden }
                    val chosen = allowed.random(random)
                    grid[r][c] = CandyTile(nextId(), chosen)
                }
            }
            if (findMatches(grid).isEmpty() && hasValidMoves(grid)) {
                return grid.map { it.toList() }
            }
        }
        return forcePlayableBoard()
    }

    fun boardFromColors(colors: List<List<CandyColor>>): List<List<CandyTile>> {
        if (colors.size != GRID_SIZE || colors.any { it.size != GRID_SIZE }) {
            return createPlayableBoard()
        }
        return colors.map { row ->
            row.map { color -> CandyTile(nextId(), color) }
        }
    }

    fun swapTiles(board: List<List<CandyTile>>, a: GridPos, b: GridPos): List<List<CandyTile>> {
        val mutable = board.map { it.toMutableList() }.toMutableList()
        val temp = mutable[a.row][a.col]
        mutable[a.row][a.col] = mutable[b.row][b.col]
        mutable[b.row][b.col] = temp
        return mutable.map { it.toList() }
    }

    fun findMatches(board: List<List<CandyTile>>): Set<GridPos> {
        val matched = mutableSetOf<GridPos>()

        // Horizontal matches >= 3
        for (r in 0 until GRID_SIZE) {
            var c = 0
            while (c < GRID_SIZE) {
                val color = board[r][c].color
                var end = c + 1
                while (end < GRID_SIZE && board[r][end].color == color) {
                    end++
                }
                if (end - c >= 3) {
                    for (k in c until end) {
                        matched.add(GridPos(r, k))
                    }
                }
                c = end
            }
        }

        // Vertical matches >= 3
        for (c in 0 until GRID_SIZE) {
            var r = 0
            while (r < GRID_SIZE) {
                val color = board[r][c].color
                var end = r + 1
                while (end < GRID_SIZE && board[end][c].color == color) {
                    end++
                }
                if (end - r >= 3) {
                    for (k in r until end) {
                        matched.add(GridPos(k, c))
                    }
                }
                r = end
            }
        }

        return matched
    }

    fun hasValidMoves(board: List<List<CandyTile>>): Boolean {
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (c + 1 < GRID_SIZE) {
                    val swapped = swapTiles(board, GridPos(r, c), GridPos(r, c + 1))
                    if (findMatches(swapped).isNotEmpty()) return true
                }
                if (r + 1 < GRID_SIZE) {
                    val swapped = swapTiles(board, GridPos(r, c), GridPos(r + 1, c))
                    if (findMatches(swapped).isNotEmpty()) return true
                }
            }
        }
        return false
    }

    fun dropAndRefill(
        board: List<List<CandyTile>>,
        cleared: Set<GridPos>
    ): BoardDropResult {
        val newGrid = MutableList(GRID_SIZE) { MutableList<CandyTile?>(GRID_SIZE) { null } }
        val fallInfos = mutableListOf<TileFallInfo>()

        for (c in 0 until GRID_SIZE) {
            var writeRow = GRID_SIZE - 1
            for (r in (GRID_SIZE - 1) downTo 0) {
                if (GridPos(r, c) !in cleared) {
                    val tile = board[r][c]
                    newGrid[writeRow][c] = tile
                    if (writeRow != r) {
                        fallInfos.add(
                            TileFallInfo(
                                fromRow = r,
                                toRow = writeRow,
                                col = c,
                                tile = tile
                            )
                        )
                    }
                    writeRow--
                }
            }
            val missingCount = writeRow + 1
            for (r in writeRow downTo 0) {
                val spawned = CandyTile(nextId(), CandyColor.entries.random(random))
                newGrid[r][c] = spawned
                // Spawn above the top edge so it falls into place smoothly
                fallInfos.add(
                    TileFallInfo(
                        fromRow = r - missingCount,
                        toRow = r,
                        col = c,
                        tile = spawned
                    )
                )
            }
        }

        val finalized = newGrid.map { row -> row.map { it!! } }
        return BoardDropResult(
            newBoard = finalized,
            fallAnimations = fallInfos
        )
    }

    fun shuffleBoard(board: List<List<CandyTile>>): List<List<CandyTile>> {
        val pool = board.flatten().map { it.color }.toMutableList()
        repeat(60) {
            pool.shuffle(random)
            val candidate = MutableList(GRID_SIZE) { r ->
                MutableList(GRID_SIZE) { c ->
                    CandyTile(nextId(), pool[r * GRID_SIZE + c])
                }
            }
            if (findMatches(candidate).isEmpty() && hasValidMoves(candidate)) {
                return candidate.map { it.toList() }
            }
        }
        return createPlayableBoard()
    }

    fun getBoosterTargets(
        board: List<List<CandyTile>>,
        type: BoosterType,
        pos: GridPos
    ): Set<GridPos> {
        return when (type) {
            BoosterType.HAMMER -> setOf(pos)
            BoosterType.BUCKET -> {
                val targetColor = board[pos.row][pos.col].color
                buildSet {
                    for (r in 0 until GRID_SIZE) {
                        for (c in 0 until GRID_SIZE) {
                            if (board[r][c].color == targetColor) {
                                add(GridPos(r, c))
                            }
                        }
                    }
                }
            }
            BoosterType.TRAIN -> (0 until GRID_SIZE).map { c -> GridPos(pos.row, c) }.toSet()
            BoosterType.BRUSH -> (0 until GRID_SIZE).map { r -> GridPos(r, pos.col) }.toSet()
        }
    }

    private fun forcePlayableBoard(): List<List<CandyTile>> {
        val grid = MutableList(GRID_SIZE) { r ->
            MutableList(GRID_SIZE) { c ->
                val colors = CandyColor.entries
                // Deterministic checkerboard-like pattern with zero 3-in-a-row
                val color = colors[(r * 2 + c) % colors.size]
                CandyTile(nextId(), color)
            }
        }
        // Guarantee a valid swap at (0,0) <-> (0,1)
        val cTarget = CandyColor.RED
        val cOther = CandyColor.BLUE
        grid[0][0] = CandyTile(nextId(), cOther)
        grid[0][1] = CandyTile(nextId(), cTarget)
        grid[1][0] = CandyTile(nextId(), cTarget)
        grid[2][0] = CandyTile(nextId(), cTarget)
        return grid.map { it.toList() }
    }
}
