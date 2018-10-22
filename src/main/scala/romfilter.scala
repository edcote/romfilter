package romfilter

case class ROMInfo(name: String, cloneof: String, descrption: String, publisher: String, year: String)

class ROMFilter {
  private val illegalPublishers =
    """
      |&lt;unknown&gt;
      |<unknown>
      |<homebrew>
      |<doujin>
    """.stripMargin.trim.split("\n").toSet
  private val xmlFile = getClass.getResource("/listsoftware.0188.xml").getFile
  private val softwarelists = scala.xml.XML.loadFile(xmlFile)
  private val software = softwarelists \ "softwarelist" \ "software"

  def illegalFilter(info: ROMInfo): Boolean = {
    illegalPublishers.contains(info.publisher) ||
      info.cloneof != ""

  }

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
      case info if illegalFilter(info) => false
      case _ => true
    }

  // FIXME: write to file
  db.foreach(println)
}

object ROMFilter {
  def main(args: Array[String]): Unit = {
    val romFilter = new ROMFilter()
  }
}
