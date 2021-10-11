package org.techtown.graduateproject

import org.techtown.graduateproject.data.BodyPart
import org.techtown.graduateproject.data.Person

import kotlin.math.abs
import kotlin.math.atan2


object EvaluateSquartUtils {
    private val horizontalJoints = listOf(
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    private val verticalJoints = listOf(
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP)
    )

    private val leftLegJoints = listOf(
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE)
    )
    private val rightLegJoints = listOf(
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    private val sideLimit = 50

    fun evaluateSquartPosture(person: Person, isSquartMode: Boolean): String{
        if(!isSquartMode || !isSide(person)) return isSideStanding(person)
        else{
            val angleLeftFloor = getAngleBetweenFloor(person, leftLegJoints)
            val angleRightFloor = getAngleBetweenFloor(person, rightLegJoints)
            if(angleLeftFloor < 50 || angleRightFloor < 50) return "badSit"

            val angleLeft = getAngleOfLegs(person ,leftLegJoints)
            val angleRight = getAngleOfLegs(person, rightLegJoints)

            if(angleLeft < 110 && angleRight < 110) return "sit"
            else if(angleLeft > 150 && angleRight > 150) return "stand"
            return "mid"
        }
    }

    private fun getAngleBetweenFloor(person: Person, legJoints: List<Pair<BodyPart, BodyPart>>): Double{
        val knee = person.keyPoints[legJoints[1].first.position].coordinate
        val ankle = person.keyPoints[legJoints[1].second.position].coordinate

        val line1 = Pair(
            (knee.y - ankle.y).toDouble(),
            (knee.x - ankle.x).toDouble())
        return angleBetween2Lines(line1, Pair(0.toDouble(), 1.toDouble()) )
    }

    private fun getAngleOfLegs(person: Person, legJoints: List<Pair<BodyPart, BodyPart>>): Double {
        val hip = person.keyPoints[legJoints[0].first.position].coordinate
        val knee = person.keyPoints[legJoints[0].second.position].coordinate
        val ankle = person.keyPoints[legJoints[1].second.position].coordinate

        val line1 = Pair(
            (hip.y - knee.y).toDouble(),
            (hip.x - knee.x).toDouble())
        val line2 = Pair(
            (ankle.y - knee.y).toDouble(),
            (ankle.x - knee.x).toDouble())
        return angleBetween2Lines(line1, line2)
    }

    fun isSideStanding(person: Person) : String{
        val limit = 50
        horizontalJoints.forEach {
            val pointA = person.keyPoints[it.first.position].coordinate
            val pointB = person.keyPoints[it.second.position].coordinate
            if(abs(pointA.x - pointB.x) >= limit) return "not"
        }

        var idx = 0
        horizontalJoints.forEach {
            val pointA = person.keyPoints[it.first.position].coordinate
            val pointB = person.keyPoints[it.second.position].coordinate
            val lmt = if(idx < 2) 100 else 50
            if(abs(pointA.y - pointB.y) < lmt) return "not"
            idx++
        }

        if(!isSide(person)) return "straight"

        return "side"
    }

    private fun isSide(person: Person): Boolean{
        verticalJoints.forEach {
            val pointA = person.keyPoints[it.first.position].coordinate
            val pointB = person.keyPoints[it.second.position].coordinate
            if(abs(pointA.x - pointB.x) >= sideLimit) return false
        }
        return true
    }

    private fun angleBetween2Lines(line1: Pair<Double, Double>, line2: Pair<Double, Double>): Double {
        val angle1: Double = atan2(
            line1.first,
            line1.second
        )
        val angle2: Double = atan2(
            line2.first,
            line2.second
        )
        val angle = abs(angle1 - angle2) * 180 / Math.PI

        return if(angle >= 180) 360 - angle
        else angle
    }
}