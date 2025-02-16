package org.jetbrains.plugins.translationUnitGenerator.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.plugins.translationUnitGenerator.settings.TranslationUnitSettings
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class GenerateTranslationUnitAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText ?: return

        // Get settings
        val settings = service<TranslationUnitSettings>()

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val fileType = file?.extension

        // Choose the correct template
        val template = when (fileType) {
            "twig" -> settings.twigTemplate
            "php" -> settings.phpTemplate
            "js" -> settings.jsTemplate
            else -> "{{ '%s'|trans }}" // Default fallback
        }

        // Generate the translation ID
        val filePath = file?.path
        val suggestedId = generateTranslationUnitId(selectedText, project, filePath, settings.unitIdPrefix)

        // 🔥 Now always use the generated ID (no pop-up)
        val translationUnitId = suggestedId

        // Apply template format
        val translatedText = template.format(translationUnitId)

        // Replace selected text in editor
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(
                selectionModel.selectionStart,
                selectionModel.selectionEnd,
                translatedText
            )
        }

        // Save translation unit (now supporting .xlf format)
        saveTranslationUnitToXLF(project, settings.translationPath, translationUnitId, selectedText)
    }

    private fun generateTranslationUnitId(text: String, project: Project, filePath: String?, prefix: String): String {
        val sanitizedText = text.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
        val fileName = filePath?.substringAfterLast("/")?.substringBeforeLast(".") ?: "global"

        // If prefix exists, prepend it
        return if (prefix.isNotEmpty()) "$prefix.$sanitizedText" else "$fileName.$sanitizedText"
    }

    // 🔥 Function to save translation unit in XLIFF (.xlf) format
    private fun saveTranslationUnitToXLF(project: Project, translationPath: String, id: String, text: String) {
        val basePath = project.basePath ?: run {
            Messages.showErrorDialog(project, "Could not determine project base path.", "Error")
            return
        }
        val projectDir = Paths.get(basePath)
        val file = projectDir.resolve(translationPath).toFile()

        // 🔥 Ensure the file is an .xlf file
        if (!file.extension.equals("xlf", ignoreCase = true)) {
            Messages.showErrorDialog(project, "Invalid file type: $translationPath. Please select an XLIFF (.xlf) file.", "Error")
            return
        }

        if (!file.exists()) {
            Messages.showErrorDialog(project, "XLIFF file does not exist: $translationPath", "Error")
            return
        }

        // Load XML document
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document: Document = try {
            documentBuilder.parse(file)
        } catch (e: Exception) {
            Messages.showErrorDialog(project, "Failed to parse XLIFF file: ${e.message}", "Error")
            return
        }

        val body = document.getElementsByTagName("body").item(0)
            ?: run {
                Messages.showErrorDialog(project, "Invalid XLIFF file: No <body> tag found.", "Error")
                return
            }

        // Check if the translation ID already exists
        val existingUnit = (body.childNodes as org.w3c.dom.NodeList).let { nodes ->
            (0 until nodes.length).map { nodes.item(it) }
                .find { it.nodeName == "trans-unit" && it.attributes.getNamedItem("id")?.nodeValue == id }
        }

        if (existingUnit != null) {
            // If ID exists, update the target text
            val targetElement = (existingUnit as Element).getElementsByTagName("target").item(0)
            targetElement.textContent = text
        } else {
            // Otherwise, create a new <trans-unit> and append before </body>
            val transUnit = document.createElement("trans-unit")
            transUnit.setAttribute("id", id)

            val source = document.createElement("source")
            source.textContent = id

            val target = document.createElement("target")
            target.textContent = text

            // Properly format and append elements with new lines
            body.appendChild(document.createTextNode("\n    ")) // Ensure spacing before <trans-unit>
            body.appendChild(transUnit)
            transUnit.appendChild(document.createTextNode("\n        ")) // Indentation before <source>
            transUnit.appendChild(source)
            transUnit.appendChild(document.createTextNode("\n        ")) // Indentation before <target>
            transUnit.appendChild(target)
            transUnit.appendChild(document.createTextNode("\n    ")) // Ensure spacing before closing </trans-unit>
            body.appendChild(document.createTextNode("\n")) // Ensure spacing before </body>

        }

        // Save XML back to file
        val transformer = TransformerFactory.newInstance().newTransformer()
        val source = DOMSource(document)
        val result = StreamResult(file)
        transformer.transform(source, result)

        // Force IntelliJ refresh cache
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)?.refresh(false, false)
        VirtualFileManager.getInstance().syncRefresh()
    }
}
