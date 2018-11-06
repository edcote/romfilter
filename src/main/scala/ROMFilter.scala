package romfilter

import scala.io.Source
import scala.xml.{Elem, NodeSeq}

case class CategoryInfo(category: String, name: String)

case class ROMInfo(name: String, cloneof: String, description: String, publisher: String, year: String, category: CategoryInfo) {
  def isClone: Boolean = cloneof != ""

  def isValid: Boolean = !isClone
}

class ROMFilter(publishersToFilter: Seq[String], categoriesToFilter: Seq[String]) {
  // Load and parse category file
  val catverFile: String = getClass.getResource("/catver.ini").getFile

  def validCategory(category: String): Boolean =
    categoriesToFilter
      .count { categoryToFilter: String =>
        category.toLowerCase.contains(categoryToFilter.toLowerCase)
      } == 0


  val categories: Seq[(String, CategoryInfo)] = Source.fromFile(catverFile)
    .getLines
    .filter(_.trim.nonEmpty)
    .filterNot(_.startsWith(";;"))
    .filterNot(_.startsWith("[Category]"))
    .filter(validCategory)
    .map { line =>
      val tokens = line.split("=")
      val name = tokens(0)
      val category = tokens(1)
      val info = CategoryInfo(category, name)
      name -> info
    }
    .toSeq

  val categoryByROM: Map[String, CategoryInfo] = Map(categories: _*)

  // Load and parse MAME XML database
  val xmlFile: String = getClass.getResource("/listsoftware.0188.xml").getFile
  val softwarelists: Elem = scala.xml.XML.loadFile(xmlFile)
  val software: NodeSeq = softwarelists \ "softwarelist" \ "software"

  val infos: List[ROMInfo] = software
    .iterator
    .flatMap { node =>
      val name = node \@ "name"
      val cloneof = node \@ "cloneof"
      val description = node \ "description"
      val publisher = node \ "publisher"
      val year = node \ "year"
      if (categoryByROM.contains(name)) {
        val category = categoryByROM(name)
        Some(ROMInfo(name, cloneof, description.text, publisher.text, year.text, category))
      } else {
        None
      }
    }
    .filter(_.isValid)
    .toList

}

object ROMFilter {
  def main(args: Array[String]): Unit = {
    val publishersToFilter: Seq[String] =
      """
        |<doujin>
        |<homebrew>
        |&lt;unknown&gt;
        |<unknown>
      """.stripMargin.trim.split(System.lineSeparator())

    val categoriesToFilter: Seq[String] =
      """
        |BIOS
        |Casino
        |Mahjong
        |Mature
        |Maze
        |Mini-Games
        |Misc.
        |Misc. Betting
        |Puzzle / Cards
        |Puzzle / Sliding
        |Quiz
        |Rhythm
        |Sports / Bull Fighting
        |Sports / Darts
        |Sports / Fishing
        |Sports / Horse Racing
        |Sports / Horseshoes
        |Surround
        |Tabletop
        |Unplayable
      """.stripMargin.trim.split(System.lineSeparator())

    val romFilter = new ROMFilter(publishersToFilter, categoriesToFilter)
  }
}
