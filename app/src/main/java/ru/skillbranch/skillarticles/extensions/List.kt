package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {

    return bounds.map { (leftBound, rightBound) ->
        this.filter {
            it.first in leftBound..rightBound && it.second in leftBound..rightBound
        }
    }
}
