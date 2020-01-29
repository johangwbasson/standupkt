package net.johanbasson.standup

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class Main : CliktCommand() {
    override fun run() = Unit
}

class Open : CliktCommand() {
    private val location by option("-l", "--location", help = "Storage directory").required()
    private val editor by option("-e", "--editor", help="Editor to use")

    override fun run() {
        val now = LocalDateTime.now()
        val file = getDayFile(File(location.orEmpty()), now)
        if (!file.exists()) {
            createDayFile(file)
        }
        ProcessBuilder(editor, file.absolutePath).start()
    }
}

class Report : CliktCommand() {
    private val location by option("-l", "--location", help = "Storage directory").required()
    private val hostname by option("-s", "--server", help = "SMTP Server hostname").required()
    private val port by option("-p", "--port", help = "SMTP Server port").int().required()
    private val username by option("-u", "--username", help = "SMTP Server username").required()
    private val password by option("-w", "--password", help = "SMTP Server password").required()
//    private val from by option("-f", "--from", help="From Address")
    private val to by option("-t", "--to", help = "To Address").required()

    override fun run() {
        val today = LocalDateTime.now()

        val dayName = today.dayOfWeek.name.toLowerCase().capitalize()
        val year = today.year
        val dayNo = today.dayOfMonth
        val monthName = MONTH_NAMES[today.monthValue]

        val todayFile = getDayFile(File(location.orEmpty()), today)
        val todayHtml = markdownToHtml(todayFile.readText())
        var yesterdayHtml = ""

        var yesterday = today.minus(1, ChronoUnit.DAYS)
        if (DayOfWeek.MONDAY == today.dayOfWeek) {
            yesterday = today.minus(3, ChronoUnit.DAYS)
        }

        val yesterdayFile = getDayFile(File(location.orEmpty()), yesterday)
        if (yesterdayFile.exists()) {
            yesterdayHtml = markdownToHtml(yesterdayFile.readText())
        }

        val html = """<html>
        <head>
            <style>
            </style>
        </head>
        <body>
            $yesterdayHtml 
            <hl/>
            $todayHtml
        </body>
        <html>"""

        val subject = "${dayName}, $dayNo $monthName $year Standup Report"
        sendHtmlEmail(hostname, port, username, password, subject, html, to)
    }
}

fun main(args: Array<String>) = Main().subcommands(Open(), Report()).main(args)