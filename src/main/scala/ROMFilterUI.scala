package romfiler

import scala.swing.GridBagPanel._
import scala.swing._

// See: https://lampsvn.epfl.ch/trac/scala/browser/scala/trunk/src/swing/scala/swing/test

object ROMFilterUI extends SimpleSwingApplication {
  lazy val ui = new GridBagPanel {

    val filterButton = new Button {
      text = "Filter"
    }

    layout(filterButton) = new Constraints {
      fill = Fill.Both
      gridx = 0
      gridy = 0
    }
  }

  def top: Frame = new MainFrame {
    title = "ROM Filter UI"
    contents = ui
  }

}

