package de.noob.vertretungsplan

import org.json.simple.JSONObject

data class PlanItem(val art: String, val stunde: String, val vertreter: String?, val fach: String, val raum: String, val text: String?) {

    val json: JSONObject
        get() {
            val jsonObject = JSONObject()

            jsonObject["info"] = if (art.lowercase().contains("aufg") && text?.lowercase()?.contains("aufg") == true) {
                    "Aufgaben"
                } else if ((art.lowercase().contains("f.a.") || art.lowercase().contains("fällt aus")) &&
                    (text?.lowercase()?.contains("f.a.") == true || text?.lowercase()?.contains("fällt aus") == true)) {
                    "Fällt aus!"
                } else if (art.lowercase() == text?.lowercase() || text == null) {
                    if (art.lowercase().contains("erw")) {
                        "Findet statt"
                    } else {
                        art
                    }
                } else {
                    "${if (art.lowercase().contains("erw")) { "Findet statt" } else { art }} | ${if (text.lowercase().contains("f.a.")) { "Fällt aus!" } else { text }}"
                }

            jsonObject["lesson"] = stunde
            if (vertreter != null && vertreter.length > 1) {
                jsonObject["substitute"] = vertreter
            }

            jsonObject["subject"] = fach
            jsonObject["room"] = raum


            return jsonObject
        }

}