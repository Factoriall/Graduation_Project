package org.techtown.graduateproject

import android.graphics.Bitmap
import android.graphics.PointF
import org.techtown.graduateproject.data.BodyPart
import org.techtown.graduateproject.data.Person

import android.util.Log


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

    fun evaluateSquartPosture(person: Person, isSquartMode: Boolean): String{
        if(!isSquartMode) return isSideStanding(person)
        else{
            val leftHip = person.keyPoints[leftLegJoints[0].first.position].coordinate
            val leftKnee = person.keyPoints[leftLegJoints[0].second.position].coordinate
            val leftAnkle = person.keyPoints[leftLegJoints[1].second.position].coordinate

            val line1 = Pair(
                (leftHip.y - leftKnee.y).toDouble(),
                (leftHip.x - leftKnee.x).toDouble())
            val line2 = Pair(
                (leftAnkle.y - leftKnee.y).toDouble(),
                (leftAnkle.x - leftKnee.x).toDouble())
            val angle1 = angleBetween2Lines(line1, line2)

            val rightHip = person.keyPoints[rightLegJoints[0].first.position].coordinate
            val rightKnee = person.keyPoints[rightLegJoints[0].second.position].coordinate
            val rightAnkle = person.keyPoints[rightLegJoints[1].second.position].coordinate

            val line11 = Pair(
                (rightHip.y - rightKnee.y).toDouble(),
                (rightHip.x - rightKnee.x).toDouble())
            val line22 = Pair(
                (rightAnkle.y - rightKnee.y).toDouble(),
                (rightAnkle.x - rightKnee.x).toDouble())
            val angle2 = angleBetween2Lines(line11, line22)
            return "left: $angle1\nright: $angle2"
        }
    }

    fun isSideStanding(person: Person) : String{
        val limit = 50
        horizontalJoints.forEach {
            val pointA = person.keyPoints[it.first.position].coordinate
            val pointB = person.keyPoints[it.second.position].coordinate
            if(Math.abs(pointA.x - pointB.x) >= limit) return "not"
        }

        verticalJoints.forEach {
            val pointA = person.keyPoints[it.first.position].coordinate
            val pointB = person.keyPoints[it.second.position].coordinate
            if(Math.abs(pointA.x - pointB.x) >= limit) return "straight"
        }

        return "side"
    }

    fun angleBetween2Lines(line1: Pair<Double, Double>, line2: Pair<Double, Double>): Double {
        val angle1: Double = Math.atan2(
            line1.first,
            line1.second
        )
        val angle2: Double = Math.atan2(
            line2.first,
            line2.second
        )
        return angle1 - angle2
    }
}