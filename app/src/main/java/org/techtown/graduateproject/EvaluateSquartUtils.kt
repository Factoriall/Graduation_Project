package org.techtown.graduateproject

import android.graphics.Bitmap
import org.techtown.graduateproject.data.BodyPart
import org.techtown.graduateproject.data.Person

import android.util.Log


object EvaluateSquartUtils {
    val horizontalJoints = listOf(
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    val verticalJoints = listOf(
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP)
    )

    fun evaluateSquartPosture(person: Person){
        //if(isStanding(person))
    }

    fun isStanding(person: Person) : Boolean{
        val limit = 50
        horizontalJoints.forEach {
            val pointA = person.keyPoints[it.first.position].coordinate
            val pointB = person.keyPoints[it.second.position].coordinate
            Log.d("updatePose", "pointA, B: " + pointA.x + "," + pointB.x)
            if(Math.abs(pointA.x - pointB.x) >= limit) return false
        }
        return true
    }
}