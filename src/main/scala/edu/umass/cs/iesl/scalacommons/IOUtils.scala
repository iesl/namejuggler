package edu.umass.cs.iesl.scalacommons

import java.net.URL
import java.io._

object IOUtils {

  // stolen from scalate IOUtil

  def loadText(in: InputStream, encoding: String = "UTF-8"): String = new String(loadBytes(in), encoding)

  def loadTextFile(path: File, encoding: String = "UTF-8") = new String(loadBinaryFile(path), encoding)

  def loadBinaryFile(path: File): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val in = new FileInputStream(path)
    try {
      copy(in, baos)
    } finally {
      in.close
    }

    baos.toByteArray
  }

  def loadBytes(in: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    try {
      copy(in, baos)
    } finally {
      in.close
    }
    baos.toByteArray
  }

  def copy(in: File, out: File): Long = {
    out.getParentFile.mkdirs
    copy(new FileInputStream(in), new FileOutputStream(out))
  }

  def copy(file: File, out: OutputStream): Long = copy(new BufferedInputStream(new FileInputStream(file)), out)

  def copy(in: InputStream, file: File): Long = {
    val out = new FileOutputStream(file)
    try {
      copy(in, out)
    } finally {
      out.close
    }
  }

  def copy(url: URL, file: File): Long = {
    val in = url.openStream
    try {
      copy(in, file)
    } finally {
      in.close
    }
  }

  def copy(in: InputStream, out: OutputStream): Long = {
    var bytesCopied: Long = 0
    val buffer = new Array[Byte](8192)

    var bytes = in.read(buffer)
    while (bytes >= 0) {
      out.write(buffer, 0, bytes)
      bytesCopied += bytes
      bytes = in.read(buffer)
    }

    bytesCopied
  }


  def copy(in: Reader, out: Writer): Long = {
    var charsCopied: Long = 0
    val buffer = new Array[Char](8192)

    var chars = in.read(buffer)
    while (chars >= 0) {
      out.write(buffer, 0, chars)
      charsCopied += chars
      chars = in.read(buffer)
    }

    charsCopied
  }

}
