package org.jetbrains.plugins.translationUnitGenerator.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.application.ApplicationManager

@Service(Service.Level.APP)
@State(
    name = "TranslationUnitSettings",
    storages = [Storage("translationUnitSettings.xml")]
)
class TranslationUnitSettings : PersistentStateComponent<TranslationUnitSettings> {

    var twigTemplate: String = "{{ '%s'|trans }}"
    var phpTemplate: String = "__('%s')"
    var jsTemplate: String = "t('%s')"
    var translationPath: String = "translations/messages.en.xlf"
    var unitIdPrefix: String = ""

    override fun getState(): TranslationUnitSettings {
        return this
    }

    override fun loadState(state: TranslationUnitSettings) {
        com.intellij.util.xmlb.XmlSerializerUtil.copyBean(state, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TranslationUnitSettings

        if (twigTemplate != other.twigTemplate) return false
        if (phpTemplate != other.phpTemplate) return false
        if (jsTemplate != other.jsTemplate) return false
        if (translationPath != other.translationPath) return false
        if (unitIdPrefix != other.unitIdPrefix) return false // 🔥 Check new field

        return true
    }

    override fun hashCode(): Int {
        var result = twigTemplate.hashCode()
        result = 31 * result + phpTemplate.hashCode()
        result = 31 * result + jsTemplate.hashCode()
        result = 31 * result + translationPath.hashCode()
        result = 31 * result + unitIdPrefix.hashCode() // 🔥 Include new field
        return result
    }

    companion object {
        fun getInstance(): TranslationUnitSettings {
            return ApplicationManager.getApplication().getService(TranslationUnitSettings::class.java)
        }
    }
}

