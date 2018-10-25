package romfilter

import scala.io.Source

case class ROMInfo(name: String, cloneof: String, description: String, publisher: String, year: String, category: String) {
  def isClone: Boolean = cloneof != ""

  def valid: Boolean = {
    !isClone
  }
}

class ROMFilter(invalidPublishers: Seq[String], invalidCategories: Seq[String]) {
  // Load and parse category file
  private def validCategory(category: String): Boolean =
    invalidPublishers.count { invalid =>
      !category.contains(invalid)
    } > 0

  private val catverFile = getClass.getResource("/catver.ini").getFile

  private val category: Map[String, String] = Source.fromFile(catverFile)
    .getLines
    .filter(_.trim.nonEmpty)
    .filterNot(_.startsWith(";;"))
    .filterNot(_.startsWith("[Category]"))
    .map { x =>
      val y = x.split("=")
      y(0) -> y(1)
    }
    .toMap

  // Load and parse MAME XML database
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
      ROMInfo(name, cloneof, description.text, publisher.text, year.text, category.getOrElse(name, "Unknown"))
    }
    .filter {
      case rom if rom.valid && validCategory(category.getOrElse(rom.name, "")) => true
      case _ => false
    }

}

object ROMFilter {
  def main(args: Array[String]): Unit = {
    val publishers =
      """
        |&lt;unknown&gt;
        |<unknown>
        |<homebrew>
        |<doujin>
      """.stripMargin.trim.split(System.lineSeparator())

    val categories =
      """
        |Ball & Paddle
        |Casino
        |Puzzle / Sliding
        |Misc.
        |Misc. Betting
        |Sports / Darts
        |Sports / Horse Racing
        |Sports / Horseshoes
        |Tabletop
        |Unplayable
        |Mahjong
        |Rhythm
        |Puzzle / Cards
        |Maze
        |Quiz
        |BIOS
        |Mature
      """.stripMargin.trim.split(System.lineSeparator())

    val romFilter = new ROMFilter(publishers, categories)
  }
}
