package net.johanbasson.standup

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import java.io.File
import java.time.LocalDateTime
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


val MONTH_NAMES = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

fun twoChar(v: Int): String {
    return when  {
        v < 10 -> "0$v"
        else -> v.toString()
    }
}

fun getDayFile(storageDir: File, date: LocalDateTime): File {
    val year = date.year
    val month = twoChar(date.monthValue)
    val day = twoChar(date.dayOfMonth)
    val filename = "${year}_${month}_${day}.md"
    val subDir = "$year/$month"

    val subDirFile = File(storageDir, subDir)
    if (!subDirFile.exists()) {
        subDirFile.mkdirs()
    }

    val fileLocation = "$subDir/$filename"
    return File(storageDir, fileLocation)
}

fun createDayFile(file: File): File {
    val now = LocalDateTime.now()
    val dayName = now.dayOfWeek.name.toLowerCase().capitalize()
    val dayNo = now.dayOfMonth
    val monthName = MONTH_NAMES[now.monthValue]
    val year = now.year
    val header = "# $dayName, $dayNo $monthName $year \r\n\r\n- [ ] "
    file.writeText(header)
    return file
}

fun markdownToHtml(content: String): String {
    val options = MutableDataSet()
    val parser: Parser = Parser.builder(options).build()
    val renderer = HtmlRenderer.builder(options).build()
    val document: Node = parser.parse(content)
    return renderer.render(document)
}

fun sendHtmlEmail(host: String, port: Int, userName: String, password: String, subject: String, content: String, toAddress: String) {
    val properties = Properties()
    properties.setProperty("mail.smtp.user", userName)
    properties.setProperty("mail.smtp.password", password)
    properties["mail.smtp.host"] = host
    properties["mail.smtp.port"] = port
    properties["mail.smtp.auth"] = "true"
    properties["mail.smtp.starttls.enable"] = "true"

    val session = Session.getInstance(properties, CredentialsAuthenticator(userName, password))

    val msg: Message = MimeMessage(session)

    msg.setFrom(InternetAddress(userName))
    val toAddresses = arrayOf(InternetAddress(toAddress))
    msg.setRecipients(Message.RecipientType.TO, toAddresses)
    msg.subject = subject
    msg.sentDate = Date()
    msg.setContent(content, "text/html")

    Transport.send(msg)
}

class CredentialsAuthenticator(val username: String, val password: String) : Authenticator() {
    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(username, password)
    }
}