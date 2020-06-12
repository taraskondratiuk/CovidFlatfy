package main.scala.com.example.utility

trait FileDownloader {
  def downloadFile(fileUrl: String, outputPath: String): Unit = {
    val src = scala.io.Source.fromURL(fileUrl)
    val out = new java.io.FileWriter(outputPath)
    out.write(src.mkString)
    out.close()
  }
}
