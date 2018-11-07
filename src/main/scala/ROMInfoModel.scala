package romfilter

import javax.swing.table.AbstractTableModel

import scala.collection.mutable

class ROMInfoModel extends AbstractTableModel {
  val headers = Array("Name", "Description", "Category", "Year", "Publisher")

  val infos: mutable.ArrayBuffer[ROMInfo] = new mutable.ArrayBuffer()

  override def isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

  override def getRowCount: Int = infos.size

  override def getColumnName(column: Int): String = headers(column)

  override def getColumnCount: Int = headers.length

  override def getValueAt(rowIndex: Int, columnIndex: Int): String = {
    val info = infos(rowIndex)

    columnIndex match {
      case 0 => info.name
      case 1 => info.description
      case 2 => info.category.category
      case 3 => info.year
      case 4 => info.publisher
      case _ => throw new NoSuchElementException("Invalid column")
    }
  }

  override def getColumnClass(columnIndex: Int): Class[_] = {
    getValueAt(0, columnIndex).getClass
  }

}