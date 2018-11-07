package romfilter

import javax.swing.table.{TableModel, TableRowSorter}
import javax.swing._
import javax.swing.event.{DocumentEvent, ListSelectionEvent}

import scala.swing.Table.AutoResizeMode
import scala.swing._
import scala.swing.event._

object ROMFilterUI extends SimpleSwingApplication {

  import javax.swing.UIManager

  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

  private val defaultPublishersToFilter =
    """
      |???
      |bootleg
      |cart2disk
      |coverdisk
      |doujin
      |hack
      |homebrew
      |pirate
      |tape2disk
      |type-in
      |unknown
      |unlicensed
      |unofficial
    """.stripMargin.trim

  private val defaultCategoriesToFilter =
    """
      |Board Game / Chess Machine
      |Casino
      |Computer
      |Electromechanical
      |Game Console / Home Videogame
      |Handheld
      |Mature
      |Medal Game
      |Misc. /
      |MultiGame
      |Printer
      |Puzzle / Cards
      |Puzzle / Sliding
      |Quiz
      |Rhythm
      |Sports / Horse Racing
      |System
      |Tabletop
      |Utilities
      |Whac-A-Mole
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

    val table2dot1 = new Table(0, 0) {
      autoResizeMode = AutoResizeMode.AllColumns
      // see: https://stackoverflow.com/questions/31092309/jtable-doesnt-sort-despite-having-enabled-auto-row-sorter-and-using-comparabl
      override lazy val peer: JTable = new JTable with SuperMixin
    }
    layout(new ScrollPane(table2dot1)) = constraints(2, 1, weightX = 2.0, fill = GridBagPanel.Fill.Both)

    val model = new ROMInfoModel()
    table2dot1.model = model

    val sorter = new TableRowSorter(model)
    table2dot1.peer.setRowSorter(sorter)

    table2dot1.peer.setSelectionModel(new DefaultListSelectionModel() {
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
    })

    // 0, 2
    val filterButton = new Button("Filter")
    layout(filterButton) = constraints(0, 2, gridWidth = 2)
    listenTo(filterButton)

    // 2, 2
    val filterText = new TextField("")
    layout(filterText) = constraints(2, 2, fill = GridBagPanel.Fill.Both)
    listenTo(filterText)

    // Event handling
    reactions += {

      case ActionEvent(`table2dot1`) =>
        println("Hello")

      case ButtonClicked(`filterButton`) =>
        model.infos.clear()

        new ROMFilter(publishersToFilterArea.text.split("\n"), categoriesToFilterArea.text.split("\n"))
          .infos
          .foreach { info =>
            // FIXME: make immutable
            model.infos.append(info)
            model.fireTableDataChanged()
          }

        Dialog.showMessage(contents.head, "Success!")

      case EditDone(`filterText`) =>
        val filter: RowFilter[ROMInfoModel, Integer] = RowFilter.regexFilter(filterText.text, 0, 1, 2, 3, 4)
        sorter.setRowFilter(filter)

    }
  }

  def top: Frame = new MainFrame {
    title = "MAME ROM Filter UI"
    contents = ui
    visible = true
  }

}
