package connect_four.domain.models

import com.beatsnake.connect_four.data.SocketMessage
import com.beatsnake.connect_four.data.SocketMessage.*
import com.beatsnake.connect_four.domain.models.*
import kotlinx.serialization.Serializable

@Serializable
data class Board(val columns: List<Column>) {

    companion object {

        fun generateEmptyBoard(): Board {
            val columns = mutableListOf<Column>()
            repeat(7) {
                val slots = mutableListOf<Slot>()
                repeat(6) { slot ->
                    slots.add(Slot(index = slot))
                }
                columns.add(Column(slots = slots))
            }
            return Board(columns = columns)
        }

    }

    fun update(updatedSlot: Int, turn: Turn): Board = Board(
        columns = columns.mapIndexed { index, column ->
            if (index == updatedSlot) {
                val emptySlot = column.slots.lastOrNull { it.availability == Availability.Available }
                if (emptySlot == null) {
                    throw ColumnAlreadyFilledException
                } else {
                    column.copy(slots = column.slots.map { if (it == emptySlot) it.claim(turn) else it })
                }
            } else
                column
        }
    )

    fun updateGameStatus(turn: Turn): SocketMessage {
        val width = columns.size
        val height = columns.first().slots.size

        return when {
            isGameWon(width, height) -> GameOver(this, GameOverStatus.Won, turn)
            drawCheck() -> GameOver(this, GameOverStatus.Draw, turn)
            else -> PlayerTurn(this, turn)
        }
    }

    private fun isGameWon(width: Int, height: Int): Boolean = horizontalWinCheck(width, height) ||
            verticalWinCheck(height, width) ||
            ascendingDiagonallyWinCheck(width, height) ||
            descendingDiagonallyWinCheck(width, height)

    private fun drawCheck(): Boolean {
        if (
            columns.all { column ->
                column.slots.all { slot ->
                    slot.availability != Availability.Available
                }
            }
        ) {
            return true
        }
        return false
    }

    private fun descendingDiagonallyWinCheck(width: Int, height: Int): Boolean {
        for (columnIndex in 3 until width) {
            for (rowIndex in 3 until height) {
                val gameWon = listOf(
                    columns[columnIndex].slots[rowIndex],
                    columns[columnIndex - 1].slots[rowIndex - 1],
                    columns[columnIndex - 2].slots[rowIndex - 2],
                    columns[columnIndex - 3].slots[rowIndex - 3],
                ).checkWin()

                if (gameWon) return true
            }
        }
        return false
    }

    private fun ascendingDiagonallyWinCheck(width: Int, height: Int): Boolean {
        for (columnIndex in 3 until width) {
            for (rowIndex in 0 until height - 3) {
                val gameWon = listOf(
                    columns[columnIndex].slots[rowIndex],
                    columns[columnIndex - 1].slots[rowIndex + 1],
                    columns[columnIndex - 2].slots[rowIndex + 2],
                    columns[columnIndex - 3].slots[rowIndex + 3],
                ).checkWin()

                if (gameWon) return true
            }
        }
        return false
    }

    private fun verticalWinCheck(height: Int, width: Int): Boolean {
        for (rowIndex in 0 until height - 3) {
            for (columnIndex in 0 until width) {
                val gameWon = listOf(
                    columns[columnIndex].slots[rowIndex],
                    columns[columnIndex].slots[rowIndex + 1],
                    columns[columnIndex].slots[rowIndex + 2],
                    columns[columnIndex].slots[rowIndex + 3],
                ).checkWin()

                if (gameWon) return true
            }
        }
        return false
    }

    private fun horizontalWinCheck(width: Int, height: Int): Boolean {
        for (columnIndex in 0 until width - 3) {
            for (rowIndex in 0 until height) {
                val gameWon = listOf(
                    columns[columnIndex].slots[rowIndex],
                    columns[columnIndex + 1].slots[rowIndex],
                    columns[columnIndex + 2].slots[rowIndex],
                    columns[columnIndex + 3].slots[rowIndex],
                ).checkWin()

                if (gameWon) return true
            }
        }
        return false
    }

}

private fun List<Slot>.checkWin(): Boolean = groupBy {
    it.availability
}.any {
    it.key != Availability.Available && it.value.size == 4
}

