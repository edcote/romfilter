package romfilter

import scala.io.Source

case class ROMInfo(name: String, cloneof: String, description: String, publisher: String, year: String)(implicit val invalidPublishers: Set[String]) {
  def isClone: Boolean = cloneof != ""

  def valid: Boolean = {
    !isClone
  }
}

class ROMFilter {
  implicit private val invalidPublishers =
    """
      |&lt;unknown&gt;
      |<unknown>
      |<homebrew>
      |<doujin>
    """.stripMargin.trim.split("\n").toSet

  private val catverFile = getClass.getResource("/catver.ini").getFile
  // FIXME: bug in expression
  private val categories = Source.fromFile(catverFile)
    .getLines
    .filter(_.trim.nonEmpty)
    .filterNot(_.startsWith(Seq(";;", "[")))
    .map(_.split("=", 2))
    .map { s =>
      (s(0), s(1).trim)
    }
    .toMap

  private val xmlFile = getClass.getResource("/listsoftware.0188.xml").getFile
  private val softwarelists = scala.xml.XML.loadFile(xmlFile)
  private val software = softwarelists \ "softwarelist" \ "software"

  private val db = software
    .map { node =>
      val name = node \@ "name"
      val cloneof = node \@ "cloneof"
      val description = node \ "description"
      val publisher = node \ "publisher"
      val year = node \ "year"
      ROMInfo(name, cloneof, description.text, publisher.text, year.text)
    }
    .filter {
      case rom if rom.valid => true
      case _ => false
    }

  // FIXME: write to file
  db.foreach(println)
}

object ROMFilter {
  def main(args: Array[String]): Unit = {
    val romFilter = new ROMFilter()
  }
}
