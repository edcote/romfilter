package romfilter

import javax.swing.JTable
import javax.swing.table.TableRowSorter

import scala.swing.Table.AutoResizeMode
import scala.swing._
import scala.swing.event._

object ROMFilterUI extends SimpleSwingApplication {

  import javax.swing.UIManager

  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

  private val defaultPublishersToFilter =
    """
      |<doujin>
      |<homebrew>
      |&lt;unknown&gt;
      |<unknown>
    """.stripMargin.trim

  private val defaultCategoriesToFilter =
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
    """.stripMargin.trim

  lazy val ui = new GridBagPanel {

    def constraints(x: Int, y: Int,
                    gridWidth: Int = 1, gridHeight: Int = 1,
                    weightX: Double = 0.0, weightY: Double = 0.0,
                    fill: GridBagPanel.Fill.Value = GridBagPanel.Fill.None): Constraints = {
      val c = new Constraints
      c.gridx = x
      c.gridy = y
      c.gridwidth = gridWidth
      c.gridheight = gridHeight
      c.weightx = weightX
      c.weighty = weightY
      c.fill = fill
      c
    }

    // 0, 0
    layout(new Label("Publishers to filter")) = constraints(0, 0)

    // 1, 0
    layout(new Label("Categories to filter")) = constraints(1, 0)

    // 2, 0
    layout(new Label("Results")) = constraints(2, 0)

    // 0, 1
    val publishersToFilterArea = new TextArea(defaultPublishersToFilter, 30, 20)
    layout(new ScrollPane(publishersToFilterArea)) = constraints(0, 1, fill = GridBagPanel.Fill.Both)

    // 1, 1
    val categoriesToFilterArea = new TextArea(defaultCategoriesToFilter, 30, 20)
    layout(new ScrollPane(categoriesToFilterArea)) = constraints(1, 1, fill = GridBagPanel.Fill.Both)

    // 2, 1
    val model = new ROMInfoModel()
    val rowSorter = new TableRowSorter(model)
    //    for (col <- names.indices) {
    //      rowSorter.setSortable(col, true)
    //    }


    val table2dot1 = new Table(0, 0) {
      autoResizeMode = AutoResizeMode.AllColumns
      //      peer.setRowSorter(rowSorter)
      // see: https://stackoverflow.com/questions/31092309/jtable-doesnt-sort-despite-having-enabled-auto-row-sorter-and-using-comparabl
      override lazy val peer: JTable = new JTable with SuperMixin
    }
    layout(new ScrollPane(table2dot1)) = constraints(2, 1, weightX = 2.0, fill = GridBagPanel.Fill.Both)

    table2dot1.peer.setAutoCreateRowSorter(true)
    table2dot1.model = model

    // 0, 2
    val filterButton = new Button("Filter")
    layout(filterButton) = constraints(0, 2, gridWidth = 2)
    listenTo(filterButton)

    // 2, 2
    val cancelButton = new Button("Cancel")
    layout(cancelButton) = constraints(2, 2)
    listenTo(cancelButton)

    // Event handling
    reactions += {
      case ButtonClicked(`filterButton`) =>
        new ROMFilter(publishersToFilterArea.text.split("\n"), categoriesToFilterArea.text.split("\n"))
          .infos
          .foreach { info =>
            // FIXME: make immutable
            model.infos.append(info)
            model.fireTableDataChanged()
          }
        Dialog.showMessage(contents.head, "Success!")
      case ButtonClicked(`cancelButton`) =>
        sys.exit(0)
    }
  }

  def top: Frame = new MainFrame {
    title = "ROM Filter UI"
    contents = ui
    visible = true
  }

}

