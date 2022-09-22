package de.noob.vertretungsplan

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.jsoup.Jsoup
import spark.Request
import spark.Response
import spark.Spark
import spark.Spark.get
import spark.Spark.port
import java.security.KeyManagementException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


var lastRefresh = -1L
var cache = JSONArray()

var lastInfoRefresh = -1L
var cacheInfo = ""

fun main() {

    System.clearProperty("javax.net.ssl.trustStore")

    port(8080)
    get("/vertretungsplan-data") { req: Request, resp: Response ->

        resp.type("application/json")
        resp.header("Access-Control-Allow-Origin", "*")

        if (lastRefresh < System.currentTimeMillis() - 60000) {
            cache = JSONArray().also {
                it.add(getData("http://www.pgb-info.de/joomla/images/sampledata/untis/Web/f1/subst_001.htm"))
                it.add(getData("http://www.pgb-info.de/joomla/images/sampledata/untis/Web/f2/subst_001.htm"))
            }
            lastRefresh = System.currentTimeMillis()
        }

        cache.toJSONString()

    }

    get("/vertretungsplan-info") { req: Request, resp: Response ->

        resp.header("Access-Control-Allow-Origin", "*")

        if (lastInfoRefresh < System.currentTimeMillis() - 60000) {
            cacheInfo = getInfo("http://www.pgb-info.de/joomla/images/sampledata/untis/Web/ticker.htm")
            lastInfoRefresh = System.currentTimeMillis()
        }

        cacheInfo

    }

}

fun getData(url: String): JSONObject {
    val result = JSONObject()
    val doc = Jsoup.connect(url).ignoreHttpErrors(true).validateTLSCertificates(false).get()
    result["state"] = doc.body().ownText()
    result["date"] = addZeros(doc.getElementsByClass("mon_title").first()!!.ownText().split("(Seite")[0])

    val dataArray = JSONArray()
    var currentClassObject = JSONObject()
    var currentClassObjectData = JSONArray()
    doc.getElementsByClass("mon_list").first()!!.children().first()!!.children().also { it.removeAt(0) }.forEach {
        if (it.children().first()!!.className() == "list inline_header") {
            if (currentClassObject.isNotEmpty()) dataArray.add(currentClassObject.also { obj -> obj["data"] = currentClassObjectData })
            currentClassObject = JSONObject()
            currentClassObjectData = JSONArray()
            currentClassObject["class"] = it.children().first()!!.ownText().split(" ")[0]
        } else {
            currentClassObjectData.add(
                PlanItem(
                    it.child(0).ownText(),
                    it.child(1).ownText(),
                    if (it.child(2).ownText() == "&nbsp;" || it.child(2).ownText() == "---") null else it.child(2).ownText(),
                    if (it.child(4).ownText() == "&nbsp;" || it.child(4).ownText().length < 2 || it.child(4).ownText() == "---") it.child(3).ownText() else it.child(4).ownText(),
                    if (it.child(6).ownText() == "&nbsp;" || it.child(6).ownText().length < 2 || it.child(6).ownText() == "---") it.child(5).ownText() else it.child(6).ownText(),
                    if (it.child(7).ownText() == "&nbsp;" || it.child(7).ownText().length < 2 || it.child(7).ownText() == "---") null else (it.child(7).ownText()
                )
            ).json)
        }
    }
    if (currentClassObject.isNotEmpty()) dataArray.add(currentClassObject.also { obj -> obj["data"] = currentClassObjectData })
    currentClassObject = JSONObject()
    currentClassObjectData = JSONArray()
    result["data"] = dataArray

    return result
}

fun getInfo(url: String): String {
    return Jsoup.connect(url).ignoreHttpErrors(true).validateTLSCertificates(false).get().getElementsByClass("html-marquee").first().ownText()
}

fun addZeros(dateString: String): String {
    val numbers = dateString.split(".")
    var toReturn = ""
    numbers.forEach {
        if (it.length <= 1) {
            toReturn += "0$it."
        } else {
            toReturn += "$it."
        }
    }
    toReturn = toReturn.substring(0, toReturn.length - 1)
    return toReturn
}