package org.jetbrains.plugins.translationUnitGenerator.settings

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class TranslationToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val settings = TranslationUnitSettings.getInstance()

        // Create text fields
        val twigField = JBTextField(settings.twigTemplate).apply { columns = 25 }
        val phpField = JBTextField(settings.phpTemplate).apply { columns = 25 }
        val jsField = JBTextField(settings.jsTemplate).apply { columns = 25 }

        // Translation Unit ID Prefix Field
        val unitIdPrefixField = JBTextField(settings.unitIdPrefix).apply {
            columns = 25
            emptyText.text = "e.g., hello.hi (optional prefix for translation IDs)" // Placeholder
        }

        // Translation file path field (with file picker)
        val pathField = TextFieldWithBrowseButton().apply {
            text = settings.translationPath
            addBrowseFolderListener(
                "Select XLIFF File",
                "Choose an XLIFF (.xlf) translation file",
                project,
                com.intellij.openapi.fileChooser.FileChooserDescriptor(true, false, false, false, false, false)
                    .withFileFilter { it.extension == "xlf" }
            )
        }

        // Update settings when fields change
        twigField.document.addDocumentListener(SimpleDocumentListener { settings.twigTemplate = twigField.text })
        phpField.document.addDocumentListener(SimpleDocumentListener { settings.phpTemplate = phpField.text })
        jsField.document.addDocumentListener(SimpleDocumentListener { settings.jsTemplate = jsField.text })
        pathField.textField.document.addDocumentListener(SimpleDocumentListener { settings.translationPath = pathField.text })
        unitIdPrefixField.document.addDocumentListener(SimpleDocumentListener { settings.unitIdPrefix = unitIdPrefixField.text })

        // Create form layout
        val formPanel: JPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Translation Unit ID Prefix:", unitIdPrefixField, true)
            .addLabeledComponent("Twig Template:", twigField, true)
            .addLabeledComponent("PHP Template:", phpField, true)
            .addLabeledComponent("JS Template:", jsField, true)
            .addLabeledComponent("Translation File Path:", pathField, true)
            .panel

        // Add padding and align UI to the top
        val containerPanel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(10, 15, 10, 10)
            add(formPanel, BorderLayout.NORTH)
        }

        val content = ContentFactory.getInstance().createContent(containerPanel, "Translation Settings", false)
        toolWindow.contentManager.addContent(content)
    }
}


fun interface SimpleDocumentListener : DocumentListener {
    override fun insertUpdate(e: DocumentEvent) = changedUpdate(e)
    override fun removeUpdate(e: DocumentEvent) = changedUpdate(e)
}
