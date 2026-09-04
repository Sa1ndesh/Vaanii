package com.vanikanoon.app.util

/**
 * Utility to sanitize AI/legal text for speech synthesis (TTS).
 * Removes markdown symbols, emojis, bullets, asterisks, brackets, and code blocks
 * so Android TTS reads pure, clean, natural sentences without articulating symbols like
 * "asterisk asterisk", "hash", "bullet", "bracket", "emoji", etc.
 */
object SpeechSanitizer {

    fun sanitizeForSpeech(rawText: String): String {
        if (rawText.isBlank()) return ""

        var text = rawText

        // 1. Remove markdown bold/italic/strikethrough: **text**, *text*, __text__, ~~text~~
        text = text.replace(Regex("""[*_~`#>]"""), " ")

        // 2. Remove emojis and non-speech pictographs
        // Unicode ranges for emojis, pictographs, symbols
        text = text.replace(Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+"""), "")
        text = text.replace(Regex("""[\u2600-\u27BF]"""), "")
        text = text.replace(Regex("""[\uFE00-\uFE0F]"""), "")

        // 3. Remove numbered/bulleted list markers like "1. ", "2) ", "- ", "• "
        text = text.replace(Regex("""(?m)^\s*(\d+[\.\)]|[-•–+])\s+"""), "")

        // 4. Remove section symbols or decorative bullets: §, •, —, |, /, \, @, ^, &, =, ~, <, >
        text = text.replace(Regex("""[§•—–|/\\@^&=~<>]"""), " ")

        // 5. Replace brackets and braces: [], {}, () with light commas/spaces so sentences flow
        text = text.replace(Regex("""[\[\]{}()]"""), " ")

        // 6. Clean multiple consecutive punctuation or whitespace
        text = text.replace(Regex("""[:;]{2,}"""), ":")
        text = text.replace(Regex("""\.{2,}"""), ".")
        text = text.replace(Regex("""\s+"""), " ").trim()

        return text
    }
}
