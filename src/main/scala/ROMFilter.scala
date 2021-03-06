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
  // FIXME: https://github.com/mamesupport/catver.ini/blob/master/catver.ini
  val catverStream = getClass.getResourceAsStream("/catver.ini")

  def validCategory(category: String): Boolean =
    categoriesToFilter
      .count { categoryToFilter: String =>
        category.toLowerCase.contains(categoryToFilter.toLowerCase)
      } == 0

  def validPublisher(publisher: String): Boolean =
    publishersToFilter
      .count { publisherToFilter: String =>
        publisher.toLowerCase.contains(publisherToFilter.toLowerCase)
      } == 0

  val categories: Seq[(String, CategoryInfo)] = Source.fromInputStream(catverStream)
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
  val xmlStream = getClass.getResourceAsStream("/listsoftware.0188.xml")
  val softwarelists: Elem = scala.xml.XML.load(xmlStream)
  val software: NodeSeq = softwarelists \ "softwarelist" \ "software"

  val infos: List[ROMInfo] = software
    .iterator
    .flatMap { node =>
      val name = node \@ "name"
      val cloneof = node \@ "cloneof"
      val description = node \ "description"
      val publisher = node \ "publisher"
      val year = node \ "year"
      if (categoryByROM.contains(name) && validPublisher(publisher.text)) {
        val category = categoryByROM(name)
        Some(ROMInfo(name, cloneof, description.text, publisher.text, year.text, category))
      } else {
        None
      }
    }
    .filter(_.isValid)
    .toList

}
