package com.example.utility

class FileDownloader {
  def downloadFile(fileUrl: String, outputPath: String): Unit = {
    val src = scala.io.Source.fromURL(fileUrl)
    val out = new java.io.FileWriter(outputPath)
    out.write(src.mkString)
    out.close()
  }
}

object FileDownloader {
  def apply(): FileDownloader = new FileDownloader()
}