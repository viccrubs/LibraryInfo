package libraryinfo.presentation.helpui

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import kotlin.math.max

private class Delta {
    var x: Double = 0.0
    var y: Double = 0.0
}


fun makeDraggable(stage: Stage, byNode: Node) {
    val dragDelta = Delta()
    byNode.setOnMousePressed { mouseEvent ->
        // record a delta distance for the drag and drop operation.
        dragDelta.x = stage.x - mouseEvent.screenX
        dragDelta.y = stage.y - mouseEvent.screenY
        byNode.cursor = Cursor.MOVE
    }
    byNode.setOnMouseReleased { byNode.cursor = Cursor.DEFAULT }
    byNode.setOnMouseDragged {
        stage.x = it.screenX + dragDelta.x
        stage.y = it.screenY + dragDelta.y
    }

    byNode.setOnMouseExited { mouseEvent ->
        if (!mouseEvent.isPrimaryButtonDown) {
            byNode.cursor = Cursor.DEFAULT
        }
    }
}


fun makeResizeable(stage: Stage) {
    val resizeListener = ResizeListener(stage)
    stage.scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener)
    val children = stage.scene.root.childrenUnmodifiable
    for (child in children) {
        addListenerDeeply(child, resizeListener)
    }
}

private fun addListenerDeeply(node: Node, listener: EventHandler<MouseEvent>) {
    node.addEventHandler(MouseEvent.MOUSE_MOVED, listener)
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener)
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener)
    node.addEventHandler(MouseEvent.MOUSE_EXITED, listener)
    node.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener)
    if (node is Parent) {
        val children = node.childrenUnmodifiable
        for (child in children) {
            addListenerDeeply(child, listener)
        }
    }
}

internal class ResizeListener(private val stage: Stage) : EventHandler<MouseEvent> {
    private var cursorEvent = Cursor.DEFAULT
    private val border = 4.0
    private var startX = 0.0
    private var startY = 0.0

    override fun handle(mouseEvent: MouseEvent) {
        val mouseEventType = mouseEvent.eventType
        val scene = stage.scene

        val mouseEventX = mouseEvent.sceneX
        val mouseEventY = mouseEvent.sceneY
        val sceneWidth = scene.width
        val sceneHeight = scene.height

        if (MouseEvent.MOUSE_MOVED == mouseEventType) {
            if (mouseEventX < border && mouseEventY < border) {
                cursorEvent = Cursor.NW_RESIZE
            } else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
                cursorEvent = Cursor.SW_RESIZE
            } else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
                cursorEvent = Cursor.NE_RESIZE
            } else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
                cursorEvent = Cursor.SE_RESIZE
            } else if (mouseEventX < border) {
                cursorEvent = Cursor.W_RESIZE
            } else if (mouseEventX > sceneWidth - border) {
                cursorEvent = Cursor.E_RESIZE
            } else if (mouseEventY < border) {
                cursorEvent = Cursor.N_RESIZE
            } else if (mouseEventY > sceneHeight - border) {
                cursorEvent = Cursor.S_RESIZE
            } else {
                cursorEvent = Cursor.DEFAULT
            }
            scene.cursor = cursorEvent
        } else if (MouseEvent.MOUSE_EXITED == mouseEventType || MouseEvent.MOUSE_EXITED_TARGET == mouseEventType) {
            scene.cursor = Cursor.DEFAULT
        } else if (MouseEvent.MOUSE_PRESSED == mouseEventType) {
            startX = stage.width - mouseEventX
            startY = stage.height - mouseEventY
        } else if (MouseEvent.MOUSE_DRAGGED == mouseEventType) {
            if (Cursor.DEFAULT != cursorEvent) {
                if (Cursor.W_RESIZE != cursorEvent && Cursor.E_RESIZE != cursorEvent) {
                    val minHeight = max(stage.minHeight, border * 2)
                    if (Cursor.NW_RESIZE == cursorEvent || Cursor.N_RESIZE == cursorEvent || Cursor.NE_RESIZE == cursorEvent) {
                        if (stage.height > minHeight || mouseEventY < 0) {
                            stage.height = stage.y - mouseEvent.screenY + stage.height
                            stage.y = mouseEvent.screenY
                        }
                    } else {
                        if (stage.height > minHeight || mouseEventY + startY - stage.height > 0) {
                            stage.height = mouseEventY + startY
                        }
                    }
                }

                if (Cursor.N_RESIZE != cursorEvent && Cursor.S_RESIZE != cursorEvent) {
                    val minWidth = max(stage.minWidth, border * 2)
                    if (Cursor.NW_RESIZE == cursorEvent || Cursor.W_RESIZE == cursorEvent || Cursor.SW_RESIZE == cursorEvent) {
                        if (stage.width > minWidth || mouseEventX < 0) {
                            stage.width = stage.x - mouseEvent.screenX + stage.width
                            stage.x = mouseEvent.screenX
                        }
                    } else {
                        if (stage.width > minWidth || mouseEventX + startX - stage.width > 0) {
                            stage.width = mouseEventX + startX
                        }
                    }
                }
            }

        }
    }
}
