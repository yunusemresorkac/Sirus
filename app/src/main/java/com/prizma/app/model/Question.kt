package com.prizma.app.model

data class Question(
    val questionId : String = "",
    val question: String = "",
    val correctAnswer: String = "",
    val wrongAnswers: List<String> = emptyList(),
    val status : Int = 0
) {
    fun shuffleAnswers(): List<String> {
        val answers = mutableListOf<String>()
        answers.add(correctAnswer)
        answers.addAll(wrongAnswers)
        answers.shuffle()
        return answers
    }

    fun getCorrectAnswerIndex(): Int {
        return shuffleAnswers().indexOf(correctAnswer)
    }
    fun retrieveCorrectAnswer(): String {
        return correctAnswer
    }
}