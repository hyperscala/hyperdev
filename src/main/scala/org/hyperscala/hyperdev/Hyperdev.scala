package org.hyperscala.hyperdev

import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser

import com.outr.net.http.content.{ContentType, StringContent, FileContent}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import org.hyperscala.realtime.Realtime
import org.hyperscala.web._
import com.outr.net.http.jetty.JettyApplication
import org.powerscala.IO
import org.powerscala.event.Listener
import org.powerscala.property.Property

import scala.util.matching.Regex

object Hyperdev extends BasicWebsite with JettyApplication {
  Realtime

  chooseDirectory()

  val IncludeRegex = """<include\s+file="(.+)"/>""".r

  def chooseDirectory() = {
    val p = Preferences.userNodeForPackage(getClass)
    val startPath = p.get("startPath", ".")
    val fc = new JFileChooser(startPath)
    fc.setDialogTitle("Choose HTML Working Directory")
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    fc.showOpenDialog(null) match {
      case JFileChooser.APPROVE_OPTION => {
        val selectedPath = fc.getSelectedFile.getCanonicalPath
        p.put("startPath", selectedPath)
        configureDirectory(fc.getSelectedFile)
      }
      case _ => System.exit(0)
    }
  }

  def configureDirectory(directory: File) = {
    handlers.on {
      case (request, response) => {
        val path = request.url.path
        val file = new File(directory, path)
        if (file.isFile) {
          val filename = file.getName.toLowerCase
          if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            var contentString = IO.copy(file)
            contentString = IncludeRegex.replaceAllIn(contentString, (m: Regex.Match) => {
              val include = m.group(1)
              val includeFile = new File(directory, include)
              if (includeFile.isFile) {
                IO.copy(includeFile)
              } else {
                s"** Unable to find file in path: ${includeFile.getPath} **"
              }
            })

            val content = StringContent(contentString, ContentType.HTML)
            response.copy(content, HttpResponseStatus.OK)
          } else {
            val content = FileContent(file)
            response.copy(content, HttpResponseStatus.OK)
          }
        } else {
          response
        }
      }
    }
  }
}